package bill.zkaifleet.model;

import java.util.List ;

public enum BasePredicate implements Predicate {

	root ( new PredicateQualifier ( false, false, null, List.of ( Ontology.class ), null, null, null ) ),
	literal ( new PredicateQualifier ( false, false, null, null, null, Object.class, null ) ) ;

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
