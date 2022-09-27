package client;

import java.util.LinkedList;

import exchange.RestingOrder;
import exchangeMessage.CancelRejected;
import exchangeMessage.Cancelled;
import exchangeMessage.Fill;
import exchangeMessage.OrderAccepted;

public class Client {
	
	private LinkedList<String> _history;
	
	private ClientId _clientId;
	
	public Client( ClientId clientId ) {
		_clientId = clientId;
		_history = new LinkedList<String>();
	}
	
	public String toString() {
		return String.format(
			"Client( %s, %s )",
			_clientId.toString(),
			_history.toString()
		);
	}

	public ClientId getClientId() {
		return _clientId;
	}

	public void processCancelled(Cancelled cancelled) {
		_history.addLast( cancelled.toString() );
	}

	public void processCancelRejected(CancelRejected cancelRejected) {
		_history.addLast( cancelRejected.toString() );
	}

	public void processFill(Fill fill) {
		_history.addLast( fill.toString() );
	}

	public void processOrderAccepted(OrderAccepted orderAccepted) {
		_history.addLast( orderAccepted.toString() );
		
	}

	public void processRestingOrder(RestingOrder restingOrder) {
		_history.addLast( restingOrder.toString() );
	}

}
