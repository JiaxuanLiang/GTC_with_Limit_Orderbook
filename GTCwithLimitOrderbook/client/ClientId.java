package client;

public class ClientId {

	private String _stringId;

	public ClientId(String string) {
		_stringId = string;
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
		if( ! ( o instanceof ClientId ) )
			return false;
		if( o == this )
			return true;
		return _stringId.equals( ((ClientId) o )._stringId );
	}

}
