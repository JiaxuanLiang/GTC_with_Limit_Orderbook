package exchange;

import java.util.LinkedList;
import values.Price;

/** This class is just a container for Price 
 *  and a list of resting orders.
 *
 */

public class PriceLevel {
	
	private Price _price;
	private LinkedList<RestingOrder> _orders;
	
	public PriceLevel(Price price) {
		_price = price;
		_orders = new LinkedList<RestingOrder>();
	}
	
	public RestingOrder getOrder( int i ) {
		return _orders.get( i );
	}

	public Price getPrice() {
		return _price;
	}
	
	public LinkedList<RestingOrder> getOrders() {
		return _orders;
	}
	
	@Override
	public String toString() {
		return String.format(
			"%s( %s, %s )",
			this.getClass().getSimpleName(),
			_price.toString(),
			_orders.toString()
		);
	}

	
}
