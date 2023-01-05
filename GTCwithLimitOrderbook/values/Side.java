package values;

public class Side {
	
	public static Side BUY = new Side( "Buy", 1 );
	public static Side SELL = new Side( "Sell", -1 );
	
	private String _description;
	private int _sign;

	private Side() {
		// This prevents instantiation without arguments.
	}
	
	private Side( String description, int sign ) {
		_description = description;
		_sign = sign;
	}
	
	@Override
	public String toString() {
		return String.format( 
			"%s( %s, %d )",
			this.getClass().getSimpleName(),
			_description, 
			_sign 
		);
	}

	public int getSign() {
		return _sign;
	}
	
	
}
