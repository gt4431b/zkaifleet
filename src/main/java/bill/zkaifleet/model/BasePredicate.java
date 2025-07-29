package bill.zkaifleet.model;

import java.util.List ;

/**
 * Base predicates that are common across all ontologies.
 * <p>
 * These predicates provide fundamental relationships and properties for the ontology system.
 * 
 * <h2>Implementation Notes</h2>
 * <ul>
 *   <li>Each ontology can define its own "id" predicate that is separate from BasePredicate.id</li>
 *   <li>When parsing YAML with JectParseContext, predicates are resolved from the context's ontology registry first</li>
 *   <li>BasePredicate.id has a specific meaning in the base ontology and should not be assumed to be 
 *       the same as an "id" predicate from another ontology</li>
 *   <li>When retrieving an ID value, use the predicate from the same ontology that was used to set it</li>
 *   <li>Different ID predicates from different ontologies and spaces may have different dedicated meanings</li>
 * </ul>
 */
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