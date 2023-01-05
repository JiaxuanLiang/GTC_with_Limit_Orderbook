package exchangeMessage;

import client.ClientId;
import exchange.ExchangeOrderId;

public class CancelRejected {
	
	private ExchangeOrderId _exchangeOrderId;
	private ClientId _clientId;

	public CancelRejected( ClientId clientId, ExchangeOrderId exchangeOrderId ) {
		_clientId = clientId;
		_exchangeOrderId = exchangeOrderId;
	}
	
	@Override
	public String toString() {
		return String.format(
			"%s( %s, %s )",
			this.getClass().getSimpleName(),
			_clientId,
			_exchangeOrderId
		);
	}
	
	public ExchangeOrderId getExchangeOrderId() { return _exchangeOrderId; }

	public ClientId getClientId() {
		return _clientId;
	}

}
