package bill.zkaifleet.model ;

import java.util.HashMap ;
import java.util.Map ;
import java.util.function.Function ;

import com.fasterxml.jackson.annotation.JsonInclude ;

@JsonInclude ( JsonInclude.Include.NON_NULL )
public interface Predicate {

	public String name ( ) ;

	public default String space ( ) {
		String s = getClass ( ).getSimpleName ( ).toLowerCase ( ) ;
		if ( s.endsWith ( "predicate" ) ) {
			s = s.substring ( 0, s.length ( ) - "predicate".length ( ) ) ;
		}
		return s ;
	}

	public String ontology ( ) ;

	public final Function <Predicate, String> namer = new Function <> ( ) {

		private Map <String, String> name = new HashMap <> ( ) ;

		@Override
		public String apply ( Predicate t ) {
			if ( ! name.containsKey ( t.name ( ) ) ) {
				name.put ( t.name ( ), t.ontology ( ) + ":" + t.space ( ) + ":" + t.name ( ) ) ;
			}
			return name.get ( t.name ( ) ) ;
		}
	} ;

	default String fqName ( ) {
		return namer.apply ( this ) ;
	}
}
