package exchange;

public class ExchangeOrderId {

	private String _stringId;

	public ExchangeOrderId( String stringId ) {
		_stringId = stringId;
	}
	
	@Override
	public String toString() {
		return String.format(
			"%s( %s )",
			this.getClass().getSimpleName(),
			_stringId
		);
	}
	
	@Override
	public int hashCode() {
		return _stringId.hashCode(); 
	}
	
	@Override
	public boolean equals( Object o ) {
		if( ! ( o instanceof ExchangeOrderId ) )
			return false;
		if( o == this )
			return true;
		return ((ExchangeOrderId) o )._stringId.equals( _stringId );
	}
}
