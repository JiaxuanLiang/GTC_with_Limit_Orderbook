package values;

public class Quantity {
	
	private int _value;
	
	public Quantity( int value ) { 
		if( value < 0 )
			throw new IllegalArgumentException(
				"Quantity can't be less than zero"
			);
		_value = value;
	}
	
	public int getValue() {
		return _value;
	}
	
	@Override
	public String toString() {
		return String.format( 
			"%s( %d )", 
			this.getClass().getSimpleName(),
			_value
		);
	}
	
	public void reduceBy( Quantity quantity ) throws IllegalArgumentException {
		if( quantity.getValue() > this.getValue() )
			throw new IllegalArgumentException(
				"Can't reduce by more than available quantity"
			);
		_value -= quantity.getValue();
	}


}
