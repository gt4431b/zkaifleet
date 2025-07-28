package bill.zkaifleet.model;


public enum BasePredicate implements Predicate {
	root ;

	@Override
	public String ontology ( ) {
		return "base" ;
	}
}
	