package clientMessage;

import client.ClientId;
import exchange.ExchangeOrderId;

public class Cancel{

	private ClientId _clientId;
	private ExchangeOrderId _exchangeOrderId;

	public Cancel( ClientId clientId, ExchangeOrderId exchangeOrderId ) {
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

	public ExchangeOrderId getExchangeOrderId() {
		return _exchangeOrderId;
	}

	public ClientId getClientId() { return _clientId; }

}
