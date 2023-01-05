package values;

public class Price {
	
	private long _value;

	public Price( long value ) {
		if( value <= 0 )
			throw new IllegalArgumentException(
				"Price has to be greater than zero"
			);
		_value = value;
	}
	
	public long getValue() {
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
	
	
}
