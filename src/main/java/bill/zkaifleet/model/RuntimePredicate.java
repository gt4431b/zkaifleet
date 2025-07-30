package bill.zkaifleet.model;

import java.util.Collections ;

//In bill.zkaifleet.model

import com.fasterxml.jackson.annotation.JsonInclude ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;

/**
 * A runtime-generated predicate that doesn't need to be pre-defined in a registry.
 * <p>
 * RuntimePredicates are created dynamically during parsing when a predicate name
 * is encountered that doesn't match any registered predicate in the ontology registry.
 * 
 * <h2>Implementation Notes</h2>
 * <ul>
 *   <li>If a predicate doesn't exist, queries for that relationship should return null, not an empty RuntimePredicate</li>
 *   <li>Predicate values should be homogeneous - cannot mix Ject types, nor Ject types with scalar types</li>
 *   <li>Predicates act as the edges in the ontology graph, connecting Ject nodes</li>
 *   <li>When creating RuntimePredicates, always ensure they have appropriate qualifiers to define subject and scalar types</li>
 * </ul>
 */
@Data
@EqualsAndHashCode
@JsonInclude ( JsonInclude.Include.NON_NULL )
public class RuntimePredicate implements Predicate {

	private final String name ;
	private final String space ;
	private final String ontology ;

	public RuntimePredicate ( String name, String space, String ontology ) {
		this.name = name ;
		this.space = space ;
		this.ontology = ontology ;
	}

	public String name ( ) {
		return name ;
	}

	public String space ( ) {
		return space ;
	}

	@Override
	public String ontology ( ) {
		return ontology ;
	}

	@Override
	public PredicateQualifier qualifier ( ) {
		return new PredicateQualifier ( false, false, null, 
		                             Collections.singletonList ( RuntimeJect.class ), 
		                             RuntimeJect.class, Object.class, null ) ;
	}
}