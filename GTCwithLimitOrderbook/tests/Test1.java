package tests;


import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import client.Client;
import client.ClientId;
import client.ClientOrderId;
import clientMessage.Cancel;
import clientMessage.GTCOrder;
import exchange.Exchange;
import exchange.ExchangeOrderId;
import exchange.PriceLevel;
import exchange.RandomNumberGenerator;
import exchange.RestingOrder;
import values.Price;
import values.Quantity;
import values.Side;

public class Test1 {
	
	@Test
	public void createGTCOrder() {
		
		// Create an exchange.
		Exchange exchange = Exchange.BonniesExchange;
		Assertions.assertEquals( 
			"Exchange( ordersMap = {}, bidBook = {}, offerBook = {} )", 
			exchange.toString() 
		);
		
		// Create a new client, Bonnie.
		ClientId clientId = new ClientId( "Bonnie" );
		Client client = new Client( clientId );
		Assertions.assertEquals( "Client( ClientId( Bonnie ), [] )", client.toString() );
		
		// Create a new buy order for 100 shares with a price of $128
		Side side = Side.BUY;
		Assertions.assertEquals( "Side( Buy, 1 )", side.toString() );
		Quantity quantity = new Quantity( 100 );
		Assertions.assertEquals( "Quantity( 100 )", quantity.toString() );
		Price price = new Price( 1280000 ); // $128
		// Give this order a new client order id.
		ClientOrderId clientOrderId = new ClientOrderId( "MyBigHunch" );
		Assertions.assertEquals( "ClientOrderId( MyBigHunch )", clientOrderId.toString() );
		GTCOrder order = new GTCOrder(
			clientId,
			clientOrderId,
			side,
			quantity,
			price
		);
		Assertions.assertEquals(
			"GTCOrder( ClientId( Bonnie ), ClientOrderId( MyBigHunch ), Side( Buy, 1 ), Quantity( 100 ), Price( 1280000 ), exchangeOrderId = null )",
			order.toString()
		);
		
		// We seed this random number generator to
		// make sure that we get a reproducible stream
		// of random numbers. The only place it's used
		// in our code is to produce random ids for
		// our exchange order id objects.
		RandomNumberGenerator.Seed( 1000L );
		
		// We register the client with the exchange.
		exchange.registerClient( client );
		
		// We send our order to the exchange.
		exchange.processGTCOrder( order );
		
		// Make sure that a price level is created and
		// the new order becomes a resting order at that
		// price level.
		Assertions.assertEquals(
			"Exchange( ordersMap = "
			+ "{ExchangeOrderId( 487 )=RestingOrder( ClientId( Bonnie ), ExchangeOrderId( 487 ), Side( Buy, 1 ), Quantity( 100 ) )}, "
			+ "bidBook = {Price( 1280000 )=PriceLevel( Price( 1280000 ), [RestingOrder( ClientId( Bonnie ), ExchangeOrderId( 487 ), Side( Buy, 1 ), Quantity( 100 ) )] )}, "
			+ "offerBook = {} )",
			exchange.toString()
		);
		
		// Our second order of 200 shares.
		// We need another client order id.
		clientOrderId = new ClientOrderId( "MySecondBigHunch" );
		quantity = new Quantity( 200 );
		order = new GTCOrder(
			clientId,
			clientOrderId,
			side,
			quantity,
			price
		);
		exchange.processGTCOrder( order );

		// Check the state of the exchange.
		Assertions.assertEquals(
			"Exchange( ordersMap = {ExchangeOrderId( 487 )=RestingOrder( ClientId( Bonnie ), ExchangeOrderId( 487 ), Side( Buy, 1 ), Quantity( 100 ) ), "
			+ "ExchangeOrderId( 935 )=RestingOrder( ClientId( Bonnie ), ExchangeOrderId( 935 ), Side( Buy, 1 ), Quantity( 200 ) )}, "
			+ "bidBook = {Price( 1280000 )=PriceLevel( Price( 1280000 ), [RestingOrder( ClientId( Bonnie ), ExchangeOrderId( 487 ), Side( Buy, 1 ), Quantity( 100 ) ), "
			+ "RestingOrder( ClientId( Bonnie ), ExchangeOrderId( 935 ), Side( Buy, 1 ), Quantity( 200 ) )] )}, "
			+ "offerBook = {} )",
			exchange.toString()
		);
		
		// Check the state of the client.
		// Make sure that the client that placed the order
		// has the following events in their history:
		// 1) An order accepted message for the first order.
		// 2) A resting order acknowledgement for the first order.
		// 3) An order accepted message for the second order.
		// 4) A resting order acknowledgement for the second order.

		Assertions.assertEquals(
			client.toString(),
			"Client( ClientId( Bonnie ), "
			+ "[OrderAccepted( ClientOrderId( MyBigHunch ), ExchangeOrderId( 487 ) ), RestingOrder( ClientId( Bonnie ), ExchangeOrderId( 487 ), Side( Buy, 1 ), Quantity( 100 ) ), "
			+ "OrderAccepted( ClientOrderId( MySecondBigHunch ), ExchangeOrderId( 935 ) ), RestingOrder( ClientId( Bonnie ), ExchangeOrderId( 935 ), Side( Buy, 1 ), Quantity( 200 ) )] )"
		);
		
		// Make sure that the orders map - the one that will be used
		// for cancellations - has 2 resting orders.
		Assertions.assertEquals( exchange.getRestingOrders().size(), 2 );
		// Cancel one of the orders, order id "487". For that,
		// we need its id.
		PriceLevel priceLevel = exchange.getBidBook().get( price );
		ExchangeOrderId exchangeOrderId = priceLevel.getOrder( 0 ).getExchangeOrderId();
		Cancel cancel = new Cancel(
			clientId,
			exchangeOrderId
		);
		exchange.processCancelMessage(cancel);
		
		// Now make sure there is only one order left.
		// The reference to the cancelled one has been removed
		// from the orders map, but the order itself continues
		// to sit in the order book. Check for the new size of
		// the order map. It should be 1, not, as in the
		// previous check, 2.
		Assertions.assertEquals( exchange.getRestingOrders().size(), 1 );
		
		// Now, get a reference to the one that sits in the order book.
		// It should still be there, but with a quantity of 0 to prevent
		// it from matching with any sweeping order.
		RestingOrder restingOrder = exchange.getBidBook().get( new Price( 1280000 ) ).getOrder( 0 );
		Assertions.assertTrue(
			restingOrder.getExchangeOrderId().toString().equals( "ExchangeOrderId( 487 )" )
			&&
			restingOrder.getQuantity().getValue() == 0
		);
		
		// Now, make sure that the remaining order - the one that wasn't 
		// cancelled - still has its original quantity of 200.
		restingOrder = exchange.getBidBook().get( new Price( 1280000 ) ).getOrder( 1 );
		Assertions.assertEquals( restingOrder.getQuantity().getValue(), 200 );
		
		// If we add one more buy order and then check for
		// a properly performed sell sweep, we will find out three
		// things. First, we will find out whether the sweep
		// ignores the 0 quantity order. Second, we will find out 
		// if the sweep traverses all three price levels. Finally, 
		// we will find out if the sweep performs the correct 
		// accounting in sending fills to both counter parties.
		
		// We start by adding one more order for Bonnie, this
		// time at $127. This should create a new price level.
		
		exchange.processGTCOrder(
			new GTCOrder(
				clientId,
				new ClientOrderId( "BonniesThirdOrder" ),
				side,
				new Quantity( 100 ),
				new Price( 1270000 )
			)
		);
		
		// Check the state of the exchange.
		Set<Price> prices = exchange.getBidBook().keySet();
		Assertions.assertEquals(
				prices.toString(), "[Price( 1280000 ), Price( 1270000 )]"
		);
		
		exchange.processGTCOrder(
				new GTCOrder(
					clientId,
					new ClientOrderId( "BonniesFourthOrder" ),
					side,
					new Quantity( 300 ),
					new Price( 1250000 )
				)
			);
		
		// In the bid book, we now have 0 at 128, 200 at 
		// $128, 100 at $127, and 300 at $125. We will 
		// now sweep with a sell order of 400 at $126. 
		// In the bid book, that should leave only 300 at
		// $125. In the offer book, it should create a new
		// $126 price level with quantity 100.
		
		ClientId clientId2 = new ClientId( "Domino" );
		Client client2 = new Client( clientId2 );
		exchange.registerClient( client2 );
		exchange.processGTCOrder(
			new GTCOrder(
				clientId2,
				new ClientOrderId( "DominosFirstOrder" ),
				Side.SELL,
				new Quantity( 400 ),
				new Price( 1260000 )
			)
		);
		
		
		// Check the bid book.
		Assertions.assertEquals(
			1,
			exchange.getBidBook().keySet().size()
		);
		Assertions.assertEquals(
			exchange.getBidBook().get( new Price( 1250000 ) ).getOrder(0).getQuantity().getValue(),
			300
		);
		
		// Check the offer book
		Assertions.assertEquals(
			exchange.getOfferBook().keySet().size(),
			1
		);
		Assertions.assertEquals(
			exchange.getOfferBook().get( new Price( 1260000 ) ).getOrder(0).getQuantity().getValue(),
			100
		);
		
		// Make sure the global orders map has only 2
		// orders in it.
		Assertions.assertEquals(
			exchange.getRestingOrders().size(),
			2
		);
		
		// Check state of client Bonnie.
		String BonniesState = client.toString();
		Assertions.assertEquals(
			"Client( ClientId( Bonnie ), [OrderAccepted( ClientOrderId( MyBigHunch ), ExchangeOrderId( 487 ) ), RestingOrder( ClientId( Bonnie ), ExchangeOrderId( 487 ), Side( Buy, 1 ), Quantity( 100 ) ), OrderAccepted( ClientOrderId( MySecondBigHunch ), ExchangeOrderId( 935 ) ), RestingOrder( ClientId( Bonnie ), ExchangeOrderId( 935 ), Side( Buy, 1 ), Quantity( 200 ) ), Cancelled( ClientId( Bonnie ), ExchangeOrderId( 487 ) ), OrderAccepted( ClientOrderId( BonniesThirdOrder ), ExchangeOrderId( 676 ) ), RestingOrder( ClientId( Bonnie ), ExchangeOrderId( 676 ), Side( Buy, 1 ), Quantity( 100 ) ), OrderAccepted( ClientOrderId( BonniesFourthOrder ), ExchangeOrderId( 124 ) ), RestingOrder( ClientId( Bonnie ), ExchangeOrderId( 124 ), Side( Buy, 1 ), Quantity( 300 ) ), Fill( ClientId( Bonnie ), ExchangeOrderId( 935 ), Side( Buy, 1 ), Quantity( 200 ), Price( 1280000 ) ), Fill( ClientId( Bonnie ), ExchangeOrderId( 676 ), Side( Buy, 1 ), Quantity( 100 ), Price( 1270000 ) )] )",
			BonniesState
		);
		
		// Check state of client Domino.
		String DominosState = client2.toString();
		Assertions.assertEquals(
			"Client( ClientId( Domino ), [OrderAccepted( ClientOrderId( DominosFirstOrder ), ExchangeOrderId( 792 ) ), Fill( ClientId( Domino ), ExchangeOrderId( 792 ), Side( Sell, -1 ), Quantity( 200 ), Price( 1280000 ) ), Fill( ClientId( Domino ), ExchangeOrderId( 792 ), Side( Sell, -1 ), Quantity( 100 ), Price( 1270000 ) ), RestingOrder( ClientId( Domino ), ExchangeOrderId( 792 ), Side( Sell, -1 ), Quantity( 100 ) )] )",
			DominosState
		);
						
	}

}
