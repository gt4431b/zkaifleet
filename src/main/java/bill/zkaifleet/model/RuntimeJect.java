package bill.zkaifleet.model ;

//In bill.zkaifleet.model

// bill.zkaifleet.model.RuntimeJect
import com.fasterxml.jackson.annotation.JsonInclude ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;
import lombok.ToString ;

/**
 * A runtime implementation of Ject that can hold any predicate and scalar values.
 * <p>
 * RuntimeJect provides a flexible container for dynamic properties that aren't
 * defined in a strictly typed manner. It's useful for parsing arbitrary YAML
 * structures without requiring predefined schema classes.
 * 
 * <h2>Implementation Notes</h2>
 * <ul>
 *   <li>RuntimeJects are used when specific typed Jects aren't available</li>
 *   <li>Values within a predicate must be homogeneous - cannot mix different Ject types or mix Jects with scalar values</li>
 *   <li>A RuntimeJect with only a 'literal' scalar and no subjects is treated as a literal scalar value</li>
 *   <li>Maps in YAML are never treated as scalars, but are always interpreted as RuntimeJects</li>
 *   <li>RuntimeJects maintain bidirectional relationships - when A has B as a subject, B has A in its isObjectOf collection</li>
 * </ul>
 */
@Data
@EqualsAndHashCode ( callSuper = true )
@ToString(callSuper = true)
@JsonInclude ( JsonInclude.Include.NON_NULL )
public class RuntimeJect extends Ject {

	/**
	 * Creates a new RuntimeJect with the specified type name and ontology.
	 *
	 * @param typeName The type name of this Ject
	 * @param ontology The ontology this Ject belongs to
	 */
	public RuntimeJect ( String typeName, String ontology ) {
		super ( typeName, ontology ) ;
	}

	/**
	 * Gets the literal value of this RuntimeJect.
	 *
	 * @return The literal value, or null if not present
	 */
	public Object getLiteral() {
	    return getScalar(BasePredicate.literal, Object.class);
	}

	/**
	 * Adds a subject with a runtime predicate to this RuntimeJect.
	 *
	 * @param predKey The predicate key
	 * @param obj The object to add as a subject
	 * @return This RuntimeJect instance for method chaining
	 */
	public RuntimeJect addRuntimeSubject(String predKey, Ject obj) {
	    Predicate pred = new RuntimePredicate(predKey, "runtime", ontology);
	    addTypedSubject(pred, obj);
	    return this;
	}
}