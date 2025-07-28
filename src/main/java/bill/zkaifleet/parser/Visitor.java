package bill.zkaifleet.parser;

import java.util.Set ;

public class Visitor {
	private VisitorContext root ;
	private VisitorContext current ;
	private int counter = 0 ;

	public Visitor ( ) {
		root = new VisitorContext ( ) ;
		current = root ;
	}

	public <T> T get ( String s, Class <T> type ) {
		return current.get ( s, type ) ;
	}

	public void set ( String s, Object t ) {
		current.put ( s, t ) ;
	}

	public void childContext ( String s ) {
		VisitorContext child = current.get ( s, VisitorContext.class ) ;
		if ( child == null ) {
			child = new VisitorContext ( current ) ;
			current.put ( s, child ) ;
		}
		current = child ;
	}

	public Set <String> keys ( ) {
		return current.keys ( ) ;
	}

	public void pop ( ) {
		current = current.getParent ( ) ;
	}

	public int increment ( ) {
		return counter++ ;
	}
}
