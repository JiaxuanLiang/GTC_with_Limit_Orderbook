package exchange;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import client.Client;
import client.ClientId;
import clientMessage.Cancel;
import clientMessage.GTCOrder;
import exchangeMessage.CancelRejected;
import exchangeMessage.Cancelled;
import exchangeMessage.Fill;
import exchangeMessage.OrderAccepted;
import values.Price;
import values.Side;

public class Exchange {
	
	/** Map used to quickly find order that clients reference by their exchange order ids. */
	private HashMap<ExchangeOrderId, RestingOrder> _ordersMap;

	/** Map from price to price level */
	private TreeMap<Price, PriceLevel> _bidBook;
	private TreeMap<Price, PriceLevel> _offerBook;

	/** Map from a client id to a client. */
	private HashMap<ClientId, Client> _clientsMap;
	
	public static final Exchange BonniesExchange = new Exchange();
	
	
	/** private constructor */
	private Exchange() {
		_ordersMap = new HashMap<ExchangeOrderId,RestingOrder>();
		_bidBook = new TreeMap<Price,PriceLevel>(
				
				new Comparator<Price>() {
				@Override
				public int compare(Price p1, Price p2) {
					return -Long.compare( p1.getValue(), p2.getValue() ); // in descending order, start from the highest and earliest 
				}
				
			}
		);
		_offerBook = new TreeMap<Price,PriceLevel>(
			
				new Comparator<Price>() {
				@Override
				public int compare(Price p1, Price p2) {
					return Long.compare( p1.getValue(), p2.getValue() ); // in ascending order, start from the lowest and earliest 
				}
				
			}
		);
		_clientsMap = new HashMap<ClientId,Client>();
	}
	
	public HashMap<ExchangeOrderId,RestingOrder> getRestingOrders() {
		return _ordersMap;
	}

	public TreeMap<Price, PriceLevel> getBidBook() {
		return _bidBook;
	}

	public TreeMap<Price, PriceLevel> getOfferBook() {
		return _offerBook;
	}

	@Override
	public String toString() {
		return String.format(
			"%s( ordersMap = %s, bidBook = %s, offerBook = %s )",
			this.getClass().getSimpleName(),
			_ordersMap,
			_bidBook,
			_offerBook
		);
	}

	
	/** Add client to map of clients that are allowed
	 *  to trade with this exchange.
	 *  
	 * @param client Client to add to map.
	 */
	public void registerClient( Client client ) {
		_clientsMap.put( client.getClientId(), client );
	}
	
	/** Retrieve a client for a given client id.
	 * 
	 * We will use this as a proxy for a comms connection, although
	 * in real life, what would be retrieved is something that would
	 * do the communication required.
	 * 
	 * @param clientId Client id of the client we want to retrieve.
	 * @return Client associated with above client id
	 */
	private Client getClientForClientId(ClientId clientId) {
		Client client = this._clientsMap.get( clientId );
		if( client == null )
			throw new IllegalStateException(
				String.format(
					"No client found for %s",
					clientId
				)
			);
		return client;
	}
	
	
	/** Cancel an order specified by the client.
	 * 
	 *  Will send a Cancelled message or CancelRejected
	 *  message back to client.
	 *  
	 * @param cancel The cancel message received from the client.
	 */
	public void processCancelMessage(Cancel cancel) {
		
		// Find the resting order to which this cancel
		// makes reference.
		RestingOrder restingOrder = _ordersMap.get( cancel.getExchangeOrderId() );
		
		// If we coudn't find the order, we will reject
		// the cancel request. This can happen if the
		// order was already executed (or, in real life,
		// expired).
		if( restingOrder == null ) {
			// No such order. Reject the cancel.
			Client client = this.getClientForClientId( cancel.getClientId() );
			CancelRejected cancelRejected = new CancelRejected(
				cancel.getClientId(),
				cancel.getExchangeOrderId()
			);
			client.processCancelRejected( cancelRejected );
			return;
		}
		
		// Resting order was found. Proceed with cancel.
		// Zero out the quantity to prevent the order in
		// the price level order queue from matching with
		// sweeping orders.
		restingOrder.setToZeroQty();
		
		// Tell the client the cancel was successful.
		Cancelled cancelled = new Cancelled(
			cancel.getExchangeOrderId(),
			cancel.getClientId()
		);
		
		Client client = this.getClientForClientId( cancel.getClientId() );
		client.processCancelled( cancelled ); // update client history
		
		// Remove this cancelled order from the orders map.
		_ordersMap.remove( cancel.getExchangeOrderId() );
	}
	
	
	/** process GTC Order */
	public void processGTCOrder(GTCOrder gtcOrder) {
		
		// Tell client order was accepted. In the real
		// world, we'd check this order for specifications,
		// including limits set by the exchange or the
		// client, duplicate client order ids, etc.
		this.sendOrderAcceptedMessage( gtcOrder );
		
		// Sweep price levels.
		try{ 
			this.sweepPriceLevels( gtcOrder );
		} catch (Exception e) {
		      System.out.println(e.getMessage());
		} finally {
			// Are we done with this order?
			if( gtcOrder.getQuantity().getValue() == 0 )
				// Yes, we're done with this order so just
				// exit this method.
				return;
			
			// No, we're not done with this order.
			// Something is left to go into the book.
			// Currently, the variable 'book' is a 
			// reference to the book we've been
			// sweeping. Now we want the other book.
			this.placeRestingOrder( gtcOrder );
		}
		
	}
	
	/** Tell client order was accepted.
	 * 
	 * The specification of GTC order will be used to create the
	 * order accepted message.
	 * 
	 * @param gtcOrder Good till cancelled message sent by client
	 *                 to exchange.
	 */
	private void sendOrderAcceptedMessage(GTCOrder gtcOrder) {
		Client client = this.getClientForClientId( gtcOrder.getClientId() );
		
		gtcOrder.setExchangeOrderId( 
			new ExchangeOrderId(
				RandomNumberGenerator.NextLong().toString()
			)
		);
		OrderAccepted orderAccepted = new OrderAccepted(
			gtcOrder.getClientId(),
			gtcOrder.getClientOrderId(),
			gtcOrder.getExchangeOrderId()
		);
		
		client.processOrderAccepted( orderAccepted );
	}

	/** Sweep book pricelevels 
	 * 1) find the right book to sweep.
	 * 2) It should discard price levels that become
	 *    empty after the sweep.
	 * 3) It should discard orders that are depleted
	 *    to zero quantity or are already zero
	 *    quantity because they had been cancelled.
	 * 4) It should obtain fills by calling a resting
	 *    order's matchWith method. Two fills will
	 *    be returned for each matched resting order,
	 *    one for the resting order's client and one
	 *    for the sweeping order's client.
	 * 5) It should obtain the counter parties to the 
	 *    fills by calling the getClientForClientId 
	 *    method.
	 * 6) It should tell each of the counter parties to
	 *    process the fills by calling their
	 *    processFill methods.
	 *    
	 * @param gtcOrder
	 */
	public void sweepPriceLevels( GTCOrder gtcOrder ) {

		// Find the book to sweep	
		TreeMap<Price,PriceLevel> book = gtcOrder.getSide() == Side.BUY ? _offerBook : _bidBook;
		
		Iterator<Price> p_iter = book.keySet().iterator();
		
		while(p_iter.hasNext()) {
			
			Price p = p_iter.next();
			// Does the gtcOrder has a positive quantity? Yes, continue. 
			// No, throw an exception and exist
			if ( gtcOrder.getQuantity().getValue()==0 ) {
				throw new IllegalArgumentException(
						"Can't sweep 0 quantity gtc order."
					);
			}
			
			// Get the price level
			PriceLevel priceLevel = book.get( p );
			
			// - Does the price level exist?
			// - No, discard price levels and continue
			if( priceLevel.getOrders() == null ) {
				book.remove( p );
			}	
			
			// - Yes, sweep the price levels
			// Discard orders that are already zero quantity because they had been cancelled.
			boolean buy_condition = (gtcOrder.getSide() == Side.BUY) && (gtcOrder.getPrice().getValue() >= p.getValue());
			boolean sell_condition = (gtcOrder.getSide() == Side.SELL) && (gtcOrder.getPrice().getValue() <= p.getValue());
			
			if (buy_condition || sell_condition) {
				
				LinkedList<RestingOrder> orders = priceLevel.getOrders();
				Iterator<RestingOrder> iter = orders.iterator();
				while(iter.hasNext()) {
					RestingOrder order = iter.next();
					if ( order.getQuantity().getValue() == 0 ) {
						iter.remove();
					} else {
						// Obtain fills by calling a resting's order matchWith method
						Fill[] fills= order.matchWith( gtcOrder ); 
						
						if ( fills[0] != null ) {
							Fill restingOrderFill = fills[0];
							Fill sweepingFill = fills[1];
							
						    // Obtain the counter parties to the fills by calling the getClientForClientId method	
							Client restingOrderClient = getClientForClientId( restingOrderFill.getClientId() );
							Client sweepingClient = getClientForClientId( sweepingFill.getClientId() );
							
						    // Tell each of the counter parties to process the fills by calling their processFill methods
							restingOrderClient.processFill( restingOrderFill );
							sweepingClient.processFill( sweepingFill );
						}
						
						// Discard orders that are depleted to zero quantity
						if ( order.getQuantity().getValue() == 0 ) {
							iter.remove();
							}	
						}
					}
				
				// - Does the price level exist?
				// - No, discard price levels and continue
				if( priceLevel.getOrders().isEmpty() ) {
					book.remove(p);
					}	
				}
			p_iter = book.keySet().iterator();
			}
	}
	
	public void placeRestingOrder( GTCOrder gtcOrder ) {
		
		Client client = this.getClientForClientId( gtcOrder.getClientId() );
		
		// Find book.
		TreeMap<Price,PriceLevel> book = gtcOrder.getSide() == Side.BUY ? _bidBook : _offerBook;
		// Try to get the price level.
		PriceLevel priceLevel = book.get( gtcOrder.getPrice() );
		// Does the price level not exist?
		if( priceLevel == null ) {
			// Yes, the price level does not exist so make a
			// new one.
			priceLevel = new PriceLevel( gtcOrder.getPrice() );
			book.put( gtcOrder.getPrice(), priceLevel );
		}
		// Create the new resting order.
				RestingOrder restingOrder = new RestingOrder(
					gtcOrder.getClientId(),
					gtcOrder.getExchangeOrderId(),
					gtcOrder.getSide(),
					gtcOrder.getQuantity(),
					gtcOrder.getPrice()
				);
		// Add the new resting order to the price level.
		priceLevel.getOrders().addLast( restingOrder );
		// Add the new resting order to the global map
		// of all orders. (Note that the exchange order
		// id was randomly generated when we instantiated
		// the resting order but is predictable because
		// we are using a seeded random number generator.
		_ordersMap.put( restingOrder.getExchangeOrderId(), restingOrder );
		// Tell the client that they have a resting order
		// in the book.
		
		
		client.processRestingOrder( restingOrder );
	}

}
