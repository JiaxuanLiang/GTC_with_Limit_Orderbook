package exchangeMessage;

import client.ClientId;
import exchange.ExchangeOrderId;
import values.Price;
import values.Quantity;
import values.Side;

public class Fill {
	
	private ExchangeOrderId _exchangeOrderId;
	private Price _price;
	private Side _side;
	private Quantity _quantity;
	private ClientId _clientId;
	
	public Fill( 
		ClientId clientId,
		ExchangeOrderId exchangeOrderId, 
		Side side,
		Quantity quantity,
		Price price
	) {
		
		_clientId = clientId;
		_exchangeOrderId = exchangeOrderId;
		_price = price;
		_side = side;
		_quantity = quantity;
	}
	
	@Override
	public String toString() {
		return String.format(
			"%s( %s, %s, %s, %s, %s )",
			this.getClass().getSimpleName(),
			_clientId,
			_exchangeOrderId,
			_side,
			_quantity,
			_price
		);
	}

	public ExchangeOrderId getExchangeOrderId() {
		return _exchangeOrderId;
	}

	public Price getPrice() {
		return _price;
	}

	public Side getSide() {
		return _side;
	}

	public Quantity getQuantity() {
		return _quantity;
	}

	public ClientId getClientId() {
		return _clientId;
	}

}
