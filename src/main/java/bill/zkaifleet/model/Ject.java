package bill.zkaifleet.model ;

//In bill.zkaifleet.model (base package for models)

//bill.zkaifleet.model.Ject
import com.fasterxml.jackson.annotation.JsonInclude ;

import lombok.Getter ;
import lombok.Setter ;
import lombok.EqualsAndHashCode ;
import lombok.extern.slf4j.Slf4j ;

import java.lang.reflect.InvocationTargetException ;
import java.lang.reflect.Method ;
import java.util.* ;

/**
 * The base class for all entities in the ontology graph.
 * <p>
 * Ject (derived from "object" and "subject") is the fundamental building block
 * of the ontology system. It represents a node in the graph that can have:
 * <ul>
 *   <li>Identity (id, type, ontology)</li>
 *   <li>Relations to other Jects (subjects)</li>
 *   <li>Backlinks from other Jects (isObjectOf)</li>
 *   <li>Scalar properties (strings, numbers, etc.)</li>
 * </ul>
 * This class implements a graph-like structure where each node can be
 * connected to other nodes through named predicates.
 * 
 * <h2>Implementation Notes</h2>
 * <ul>
 *   <li>Values within a predicate must be homogeneous - cannot mix Ject types or mix Jects with scalar values</li>
 *   <li>Maps are never treated as scalars, but are always interpreted as Jects</li>
 *   <li>When a predicate doesn't exist in a Ject, querying that predicate should return null, not an empty collection</li>
 *   <li>RuntimeJects are used when specific typed Jects aren't available</li>
 *   <li>Simple properties are stored as scalar values (Strings, numbers, etc.)</li>
 *   <li>Bidirectional relationships are maintained - when A has B as a subject, B has A in its isObjectOf collection</li>
 * </ul>
 */
@Getter
@Setter
@EqualsAndHashCode
@Slf4j
@JsonInclude ( JsonInclude.Include.NON_NULL )
public abstract class Ject {

	private String id ;
	private String description ;
	private String evolutionNotes ;
	protected final String typeName ;
	protected String ontology ;
	protected final Map <Predicate, List <Ject>> subjects = new LinkedHashMap <> ( ) ;
	protected final Map <Predicate, List <Ject>> isObjectOf = new LinkedHashMap <> ( ) ;
	protected final Map <Predicate, List <Object>> scalars = new LinkedHashMap <> ( ) ;

	/**
	 * Creates a new Ject with the specified type name and ontology.
	 *
	 * @param typeName The type name of this Ject
	 * @param ontology The ontology this Ject belongs to
	 */
	public Ject ( String typeName, String ontology ) {
		this.typeName = typeName ;
		this.ontology = ontology ;
	}

	/**
	 * Gets subjects connected to this Ject by a specific predicate and casts them to the specified type.
	 *
	 * @param <T> The target type for the subjects
	 * @param pred The predicate connecting this Ject to its subjects
	 * @param type The class object for type T
	 * @return A list of subjects cast to type T
	 */
	public <T extends Ject> List <T> getTypedSubjects ( Predicate pred, Class <T> type ) {
		List <Ject> raw = subjects.getOrDefault ( pred, Collections.emptyList ( ) ) ;
		List <T> typed = new ArrayList <> ( ) ;
		for ( Ject item : raw ) {
			if ( type.isInstance ( item ) ) {
				typed.add ( type.cast ( item ) ) ;
			} else {
				log.warn("Type mismatch for predicate {}: expected {}, got {}", 
				    pred.name(), type.getSimpleName(), item.getClass().getSimpleName());
			}
		}
		return typed ;
	}

	/**
	 * Adds a typed subject to this Ject via the specified predicate.
	 *
	 * @param <T> The type of the subject
	 * @param pred The predicate to connect this Ject to the subject
	 * @param obj The subject to add
	 * @return This Ject instance for method chaining
	 */
	public <T extends Ject> Ject addTypedSubject ( Predicate pred, T obj ) {
		subjects.computeIfAbsent ( pred, k -> new ArrayList <> ( ) ).add ( obj ) ;
		obj.addIsObjectOf ( pred, this ) ;
		return this ; // Fluent
	}

	/**
	 * Gets the first subject connected to this Ject by a specific predicate and casts it to the specified type.
	 *
	 * @param <C> The target type for the subject
	 * @param p The predicate connecting this Ject to the subject
	 * @param czz The class object for type C
	 * @return The first subject cast to type C, or null if no subjects exist
	 */
	public <C extends Ject> C getSingleTypedSubject ( Predicate p, Class <C> czz ) {
		List <C> cand = getTypedSubjects ( p, czz ) ;
		return cand.isEmpty ( ) ? null : cand.get ( 0 ) ;
	}

	/**
	 * Sets a single subject for a predicate, replacing any existing subjects.
	 *
	 * @param p The predicate
	 * @param j The subject to set, or null to remove all subjects
	 */
	public void setSingleTypedSubject ( Predicate p, Ject j ) {
		// Remove existing subjects for this predicate first
		removeTypedSubjects ( p ) ;
		
		// Add the new subject if not null
		if ( j != null ) {
			addTypedSubject ( p, j ) ;
		}
	}

	/**
	 * Removes all subjects connected to this Ject by a specific predicate.
	 *
	 * @param p The predicate
	 */
	public void removeTypedSubjects ( Predicate p ) {
		List <Ject> items = subjects.get ( p ) ;
		if ( items != null ) {
			for ( Ject item : items ) {
				item.removeIsObjectOf ( p, this ) ;
			}
			subjects.remove ( p ) ;
		}
	}

	/**
	 * Removes this Ject from the isObjectOf list of another Ject.
	 *
	 * @param p The predicate
	 * @param ject The Ject from which to remove this Ject
	 */
	protected void removeIsObjectOf ( Predicate p, Ject ject ) {
		List <Ject> items = isObjectOf.get ( p ) ;
		if ( items != null ) {
			items.remove ( ject ) ;
			if ( items.isEmpty ( ) ) {
				isObjectOf.remove ( p ) ;
			}
		}
	}

	/**
	 * Adds this Ject to the isObjectOf list of another Ject.
	 *
	 * @param pred The predicate
	 * @param subj The Ject to which this Ject is added
	 */
	public void addIsObjectOf ( Predicate pred, Ject subj ) {
		isObjectOf.computeIfAbsent ( pred, k -> new ArrayList <> ( ) ).add ( subj ) ;
	}

	/**
	 * Gets Jects that have this Ject as their subject through a specific predicate.
	 *
	 * @param <T> The target type for the Jects
	 * @param pred The predicate
	 * @param type The class object for type T
	 * @return A list of Jects cast to type T
	 */
	public <T extends Ject> List <T> getTypedIsObjectOf ( Predicate pred, Class <T> type ) {
		List <Ject> raw = isObjectOf.getOrDefault ( pred, Collections.emptyList ( ) ) ;
		List <T> typed = new ArrayList <> ( ) ;
		for ( Ject item : raw ) {
			if ( type.isInstance ( item ) ) {
				typed.add ( type.cast ( item ) ) ;
			} else {
				log.warn("Type mismatch in isObjectOf for predicate {}: expected {}, got {}", 
				    pred.name(), type.getSimpleName(), item.getClass().getSimpleName());
			}
		}
		return typed ;
	}

	public <T> T getScalar ( Predicate pred, Class <T> type ) {
		List <T> scalarsList = getScalars ( pred, type ) ;
		if ( scalarsList.isEmpty ( ) ) {
			return null ; // or throw an exception if preferred
		}
		if ( scalarsList.size ( ) > 1 ) {
			throw new IllegalStateException ( "Multiple scalars found for predicate: " + pred.name ( ) ) ;
		}
		return scalarsList.get ( 0 ) ;
	}

	public <T> List <T> getScalars ( Predicate pred, Class <T> type ) {
		List <Object> raw = scalars.getOrDefault ( pred, Collections.emptyList ( ) ) ;
		List <T> typed = new ArrayList <> ( ) ;
		for ( Object item : raw ) {
			if ( type.isInstance ( item ) ) {
				typed.add ( type.cast ( item ) ) ;
			} else {
				throw new IllegalStateException ( "Type mismatch for scalar: " + pred.name ( ) ) ;
			}
		}
		return typed ;
	}

	public void setScalars ( Predicate pred, List <Object> values ) {
		if ( values == null || values.isEmpty ( ) ) {
			scalars.remove ( pred ) ; // Clear if empty
		} else {
			scalars.put ( pred, new ArrayList <> ( values ) ) ;
		}
	}

	public void addScalar ( Predicate pred, Object scalar ) {
		scalars.computeIfAbsent ( pred, k -> new ArrayList <> ( ) ).add ( scalar ) ;
		// If the predicate has a qualifier, we could also handle it here
		// For example, if pred.qualifier() is not null, we might want to do something specific
		// This is commented out as it depends on the specific use case
		if ( pred.qualifier ( ) != null && pred.qualifier ( ).setter ( ) != null ) {
			// If the predicate has a setter, we can use it to set the scalar value
			pred.qualifier ( ).setter ( ).accept ( scalar, this ) ;
		} else {
			if ( ! attemptSet ( pred, scalar, "add" ) ) {
				attemptSet ( pred, scalar, "set" ) ;
			}
		}
	}

	private boolean attemptSet ( Predicate pred, Object scalar, String m ) {
		try {
			Method setter = getClass ( ).getMethod ( m + pred.name ( ).substring ( 0, 1 ).toUpperCase ( ) + pred.name ( ).substring ( 1 ), scalar.getClass ( ) ) ;
			setter.invoke ( this, scalar ) ;
			return true ;
		} catch ( NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e ) {
			return false ; // Method not found or invocation failed
		}
	}

	public Object resolveLiterals() {
	    if (this instanceof RuntimeJect runtime && isLiteral(runtime)) {
	        // Return the literal value directly (no ScalarJect)
	        return runtime.getScalar(BasePredicate.literal, Object.class);
	    }
	    return this; // Not a literal, return self
	}

	private boolean isLiteral(RuntimeJect ject) {
	    // Check if only one scalar "literal" and no subjects/isObjectOf
	    return ject.getScalars().size() == 1 && ject.getScalars().containsKey(BasePredicate.literal) 
	           && ject.getSubjects().isEmpty() ;
	}

	/**
	 * Provides a string representation of this Ject.
	 *
	 * @return A string with the Ject's key attributes
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + 
		    "[id=" + id + 
		    ", typeName=" + typeName + 
		    ", ontology=" + ontology + 
		    ", subjectCount=" + subjects.size() + 
		    ", isObjectOfCount=" + isObjectOf.size() + 
		    ", scalarCount=" + scalars.size() + 
		    "]";
	}

	/**
	 * Gets all predicates that connect this Ject to subjects.
	 *
	 * @return A list of predicates that connect this Ject to subjects
	 */
	public List<Predicate> getSubjectPredicates() {
		return new ArrayList<>(subjects.keySet());
	}

	/**
	 * Gets all predicates that connect other Jects to this Ject.
	 *
	 * @return A list of predicates that connect other Jects to this Ject
	 */
	public List<Predicate> getIsObjectOfPredicates() {
		return new ArrayList<>(isObjectOf.keySet());
	}

	/**
	 * Gets all predicates that connect this Ject to scalar values.
	 *
	 * @return A list of predicates that connect this Ject to scalar values
	 */
	public List<Predicate> getScalarPredicates() {
		return new ArrayList<>(scalars.keySet());
	}
}