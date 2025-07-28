package bill.zkaifleet.model;

import java.util.List ;

public enum BasePredicate implements Predicate {

	root ( new PredicateQualifier ( false, false, null, List.of ( Ontology.class ), null, null, null ) ),
	id ( new PredicateQualifier ( true, false, "ids", null, null, String.class, ( Object id, Ject ject ) -> ject.setId ( ( String ) id ) ) ),
	literal ( new PredicateQualifier ( false, false, null, null, null, Object.class, null ) ),
	ontologyName ( new PredicateQualifier ( true, false, "ontologyNames", null, null, String.class, ( Object name, Ject ject ) -> ject.setOntology ( ( String ) name ) ) ) ;

	private PredicateQualifier qualifier ;

	private BasePredicate ( PredicateQualifier qualifier ) {
		this.qualifier = qualifier ;
	}

	@Override
	public String ontology ( ) {
		return "base" ;
	}

	@Override
	public PredicateQualifier qualifier ( ) {
		return qualifier ;
	}
}
