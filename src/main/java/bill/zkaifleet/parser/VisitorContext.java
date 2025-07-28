package bill.zkaifleet.parser;

import java.util.HashMap ;
import java.util.Map ;
import java.util.Set ;

public class VisitorContext {

	private VisitorContext parentContext = null ;
	private Map <String, Object> contextData = new HashMap <>( ) ;

	public VisitorContext ( ) {
	}

	public VisitorContext ( VisitorContext parentContext ) {
		this.parentContext = parentContext ;
	}

	@SuppressWarnings ( "unchecked" )
	public <T> T get ( String key, Class <T> type ) {
		if ( contextData.containsKey ( key ) ) {
			return ( T ) contextData.get ( key ) ;
		} else if ( parentContext != null ) {
			return parentContext.get ( key, type ) ;
		}
		return null ;
	}

	public void put ( String key, Object value ) {
		contextData.put ( key, value ) ;
	}

	public Set <String> keys ( ) {
		return contextData.keySet ( ) ;
	}

	public VisitorContext getParent ( ) {
		return parentContext ;
	}
}
