package exchangeMessage;

import client.ClientId;
import client.ClientOrderId;
import exchange.ExchangeOrderId;

public class OrderAccepted {
	
	private ExchangeOrderId _exchangeOrderId;
	private ClientOrderId _clientOrderId;
	private ClientId _clientId;
	
	public OrderAccepted(
		ClientId clientId,
		ClientOrderId clientOrderId,
		ExchangeOrderId exchangeOrderId
	) {
		_clientId = clientId;
		_clientOrderId = clientOrderId;
		_exchangeOrderId = exchangeOrderId;
	}
	
	public ClientOrderId getClientOrderId() {
		return _clientOrderId;
	}

	public ExchangeOrderId getExchangeOrderId() {
		return _exchangeOrderId;
	}
	
	public ClientId getClientId() {
		return _clientId;
	}

	@Override
	public String toString() {
		return String.format(
			"%s( %s, %s )",
			this.getClass().getSimpleName(),
			_clientOrderId,
			_exchangeOrderId
		);
	}

}
