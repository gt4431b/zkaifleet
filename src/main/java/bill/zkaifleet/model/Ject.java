package bill.zkaifleet.model ;

//In bill.zkaifleet.model (base package for models)

//bill.zkaifleet.model.Ject
import com.fasterxml.jackson.annotation.JsonInclude ;

import lombok.Getter ;
import lombok.Setter ;
import lombok.EqualsAndHashCode ;

import java.util.* ;

@Getter
@Setter
@EqualsAndHashCode
@JsonInclude ( JsonInclude.Include.NON_NULL )
public abstract class Ject {

	private final String id ;
	private String description ;
	private String evolutionNotes ;
	protected final String typeName ;
	protected final String ontology ;
	protected final Map <Predicate, List <Ject>> subjects = new HashMap <> ( ) ;
	protected final Map <Predicate, List <Ject>> isObjectOf = new HashMap <> ( ) ;

	public Ject ( String id, String typeName, String ontology ) {
		this.id = id ;
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

	protected <C extends Ject> C getSingleTypedSubject ( Predicate p, Class <C> czz ) {
		List <C> cand = getTypedSubjects ( p, czz ) ;
		return cand.isEmpty ( ) ? null : cand.get ( 0 ) ;
	}

	protected void setSingleTypedSubject ( Predicate p, Ject j ) {
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

	protected <T extends Ject> List <T> getTypedIsObjectOf ( Predicate pred, Class <T> type ) {
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
}
