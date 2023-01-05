package clientMessage;

import client.ClientId;
import client.ClientOrderId;
import exchange.ExchangeOrderId;
import values.Price;
import values.Quantity;
import values.Side;

public class GTCOrder {

	private ClientOrderId _clientOrderId;
	private Side _side;
	private Quantity _quantity;
	private Price _price;
	private ExchangeOrderId _exchangeOrderId;
	private ClientId _clientId;

	public GTCOrder( 
		ClientId      clientId,
		ClientOrderId clientOrderId,
		Side          side,
		Quantity      quantity,
		Price         price
	) {
		_clientId = clientId;
		_clientOrderId = clientOrderId;
		_side = side;
		_quantity = quantity;
		_price = price;
		_exchangeOrderId = null; // The exchange will give this to us
	}
	
	public void setExchangeOrderId( ExchangeOrderId exchangeorderId ) {
		_exchangeOrderId = exchangeorderId;
	}
	
	@Override
	public String toString() {
		return String.format(
			"%s( %s, %s, %s, %s, %s, %s )",
			this.getClass().getSimpleName(),
			_clientId,
			_clientOrderId,
			_side,
			_quantity,
			_price,
			_exchangeOrderId == null ? "exchangeOrderId = null" : _exchangeOrderId.toString()
		);
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
	
	public ExchangeOrderId getExchangeOrderId() {
		return _exchangeOrderId;
	}
	
	public ClientOrderId getClientOrderId() {
		return _clientOrderId;
	}

	public ClientId getClientId() {
		return _clientId;
	}

}
