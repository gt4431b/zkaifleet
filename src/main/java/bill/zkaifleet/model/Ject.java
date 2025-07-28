package bill.zkaifleet.model ;

//In bill.zkaifleet.model (base package for models)

//bill.zkaifleet.model.Ject
import com.fasterxml.jackson.annotation.JsonInclude ;

import lombok.Getter ;
import lombok.Setter ;
import lombok.EqualsAndHashCode ;

import java.lang.reflect.InvocationTargetException ;
import java.lang.reflect.Method ;
import java.util.* ;

@Getter
@Setter
@EqualsAndHashCode
@JsonInclude ( JsonInclude.Include.NON_NULL )
public abstract class Ject {

	private String id ;
	private String description ;
	private String evolutionNotes ;
	protected final String typeName ;
	protected final String ontology ;
	protected final Map <Predicate, List <Ject>> subjects = new HashMap <> ( ) ;
	protected final Map <Predicate, List <Ject>> isObjectOf = new HashMap <> ( ) ;
	protected final Map <Predicate, List <Object>> scalars = new HashMap <> ( ) ;

	public Ject ( String typeName, String ontology ) {
		this.typeName = typeName ;
		this.ontology = ontology ;
	}

	// Typed get helper
	public <T extends Ject> List <T> getTypedSubjects ( Predicate pred, Class <T> type ) {
		List <Ject> raw = subjects.getOrDefault ( pred, Collections.emptyList ( ) ) ;
		List <T> typed = new ArrayList <> ( ) ;
		for ( Ject item : raw ) {
			if ( type.isInstance ( item ) ) {
				typed.add ( type.cast ( item ) ) ;
			} else {
				// Log or throw for safety; e.g., throw new IllegalStateException("Type mismatch
				// for " + pred.fqName());
			}
		}
		return typed ;
	}

	// Typed add helper (fluent)
	public <T extends Ject> Ject addTypedSubject ( Predicate pred, T obj ) {
		subjects.computeIfAbsent ( pred, k -> new ArrayList <> ( ) ).add ( obj ) ;
		obj.addIsObjectOf ( pred, this ) ;
		return this ; // Fluent
	}

	public <C extends Ject> C getSingleTypedSubject ( Predicate p, Class <C> czz ) {
		List <C> cand = getTypedSubjects ( p, czz ) ;
		return cand.isEmpty ( ) ? null : cand.get ( 0 ) ;
	}

	public void setSingleTypedSubject ( Predicate p, Ject j ) {
		if ( j != null ) {
			addTypedSubject ( p, j ) ;
		} else {
			removeTypedSubjects ( p ) ;
		}
	}

	protected void removeTypedSubjects ( Predicate p ) {
		List <Ject> items = subjects.get ( p ) ;
		if ( items != null ) {
			for ( Ject item : items ) {
				item.removeIsObjectOf ( p, this ) ;
			}
			subjects.remove ( p ) ;
		}
	}

	protected void removeIsObjectOf ( Predicate p, Ject ject ) {
		List <Ject> items = isObjectOf.get ( p ) ;
		if ( items != null ) {
			items.remove ( ject ) ;
			if ( items.isEmpty ( ) ) {
				isObjectOf.remove ( p ) ;
			}
		}
	}

	protected void addIsObjectOf ( Predicate pred, Ject subj ) {
		isObjectOf.computeIfAbsent ( pred, k -> new ArrayList <> ( ) ).add ( subj ) ;
	}

	public <T extends Ject> List <T> getTypedIsObjectOf ( Predicate pred, Class <T> type ) {
		List <Ject> raw = isObjectOf.getOrDefault ( pred, Collections.emptyList ( ) ) ;
		List <T> typed = new ArrayList <> ( ) ;
		for ( Ject item : raw ) {
			if ( type.isInstance ( item ) ) {
				typed.add ( type.cast ( item ) ) ;
			} else {
				// Similar mismatch handling
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
	public void addScalar ( Predicate pred, Object scalar ) {
		scalars.computeIfAbsent ( pred, k -> new ArrayList <> ( ) ).add ( scalar ) ;
		// If the predicate has a qualifier, we could also handle it here
		// For example, if pred.qualifier() is not null, we might want to do something specific
		// This is commented out as it depends on the specific use case
		if ( pred.qualifier ( ) != null && pred.qualifier ( ).setter ( ) != null ) {
			// If the predicate has a setter, we can use it to set the scalar value
			pred.qualifier ( ).setter ( ).accept ( scalar, this ) ;
		} else {
			if ( ! attemptSet ( pred, scalar, "set" ) ) {
				attemptSet ( pred, scalar, "add" ) ;
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
}
