package exchange;

import client.ClientId;
import clientMessage.GTCOrder;
import exchangeMessage.Fill;
import values.Price;
import values.Quantity;
import values.Side;

public class RestingOrder{
	
	private ClientId _clientId;
	private ExchangeOrderId _exchangeOrderId;
	private Side _side;
	private Quantity _quantity;
	private Price _price;

	public RestingOrder(
		ClientId clientId,
		ExchangeOrderId exchangeOrderId,
		Side side,
		Quantity quantity,
		Price price
	) {
		_clientId = clientId;
		_exchangeOrderId = exchangeOrderId;
		_side = side;
		_quantity = quantity;
		_price = price;
	}
	
	public ExchangeOrderId getExchangeOrderId() {
		return _exchangeOrderId;
	}

	public ClientId getClientId() {
		return this._clientId;
	}
	
	public Side getSide() {
		return _side;
	}

	public Quantity getQuantity() {
		return _quantity;
	}

	public Price getPrice() {
		return _price;
	}

	public void setToZeroQty() {
		_quantity = new Quantity( 0 );
	}

	public Fill[] matchWith( GTCOrder gtcOrder ) {
		int matchQuantity = Math.min( 
			gtcOrder.getQuantity().getValue(),
			this.getQuantity().getValue()
		);
		
		// Is the matched quantity zero?
		if( matchQuantity == 0 )
			// The matched quantity is zero so return
			// an empty array of fills.
			
			return new Fill[ 0 ];
		
		// The matched quantity is not zero so there
		// will be two fills, one for the sweeping
		// order and one for the resting order.
		Fill[] fills = new Fill[ 2 ];
		
		// First, create a fill for resting order.
		ClientId clientId = this.getClientId(); // Resting client id
		ExchangeOrderId exchangeOrderId = this.getExchangeOrderId();
		Side side = this.getSide(); // Resting order side
		Quantity quantity = new Quantity( matchQuantity );
		Price price = this.getPrice(); // Resting order limit price
		fills[ 0 ] = new Fill(
			clientId,
			exchangeOrderId, 
			side,
			quantity,
			price
		);
		
		// Now, create a fill for the sweeping order.
		clientId = gtcOrder.getClientId();
		exchangeOrderId = gtcOrder.getExchangeOrderId();
		side = gtcOrder.getSide();
		// Quantity is same for both parties as set above.
		// Price is the price for the resting order
		fills[ 1 ] = new Fill(
			clientId,
			exchangeOrderId,
			side,
			quantity,
			price
		);
		
		// Reduce quantity of this resting order by
		// quantity of the fill.
		_quantity.reduceBy( quantity );
		
		// Reduce quantity of sweeping order by quantity
		// of the fill.
		gtcOrder.getQuantity().reduceBy( quantity );
		
		// Return fills.
		return fills;
	}

	@Override
	public String toString() {
		return String.format(
			"%s( %s, %s, %s, %s )",
			this.getClass().getSimpleName(),
			_clientId,
			_exchangeOrderId,
			_side,
			_quantity,
			_price
		);
	}

	
}
