package client;

public class ClientOrderId {
	
	private String _stringId;

	public ClientOrderId( String stringId ) {
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

}
