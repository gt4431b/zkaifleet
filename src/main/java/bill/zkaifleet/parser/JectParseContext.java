package bill.zkaifleet.parser ;

import java.lang.reflect.InvocationTargetException ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.IdentityHashMap ;
import java.util.LinkedList ;
import java.util.List ;
import java.util.Map ;
import java.util.Queue ;
import java.util.Stack ;

import bill.zkaifleet.model.BasePredicate ;
import bill.zkaifleet.model.Ject ;
import bill.zkaifleet.model.Ontology ;
import bill.zkaifleet.model.ParserRegistry ;
import bill.zkaifleet.model.Predicate ;
import bill.zkaifleet.model.PredicateQualifier ;
import bill.zkaifleet.model.RuntimeJect ;
import lombok.Data ;

@Data
public class JectParseContext {

	private final Map <String, Placeholder <Ject>> placeholders = new HashMap <> ( ) ;
	private final Map <String, Ject> identityMap = new HashMap <> ( ) ;
	private Stack <LocalParseContext> contextStack = new Stack <> ( ) ;
	private Map <String, ParserRegistry> ontologyCatalog = new HashMap <> ( ) ;
	private Visitor visitor = new Visitor ( ) ;
	private Ontology ontology ;

	public JectParseContext ( Ject current, String ontologyName, Map <String, Object> rawRemnants, Map <String, ParserRegistry> ontologyCatalog ) {
		contextStack.push ( new LocalParseContext ( current, ontologyName, rawRemnants ) ) ;
		this.ontologyCatalog = ontologyCatalog ;
		this.ontology = ( Ontology ) current ;
	}

	@SuppressWarnings ( "unchecked" )
	public void buildJects ( ) {
		LocalParseContext currentContext = contextStack.peek ( ) ;
		Ject currentJect = currentContext.current ( ) ;
		String ontologyName = currentContext.ontologyName ( ) ;
		ParserRegistry ontologyRegistry = ontologyCatalog.get ( ontologyName ) ;
		if ( ontologyRegistry == null ) {
			ontologyRegistry = ontologyCatalog.get ( "base" ) ;
		}
		Object raw = currentContext.raw ( ) ;
		try {
			if ( raw instanceof Map ) {
				Map <String, Object> rawMap = ( Map <String, Object> ) raw ;
				for ( String predicateName : rawMap.keySet ( ) ) {
					Predicate pred = ontologyRegistry.getPredicate ( predicateName, ontologyName ) ;
					PredicateQualifier qualifier = pred.qualifier ( ) ;
					Object value = rawMap.get ( predicateName ) ;
					if ( value instanceof Map ) {
						// Handle nested Ject creation
						handleNestedJectCreation ( currentContext, pred, ( Map <String, Object> ) value ) ;
					} else if ( value instanceof List children ) {
						// Handle lists of Jects
						if ( qualifier.subjectType ( ) != null ) {
							handleJestListCreation ( currentContext, pred, children ) ;
						} else if ( qualifier.scalarType ( ) != null ) {
							// Handle scalar lists
							handleScalarListCreation ( currentContext, pred, children ) ;
						} else {
							throw new IllegalArgumentException ( "Invalid predicate qualifier for list: " + predicateName ) ;
						}
					} else {
						// Handle scalar properties
						currentJect.addScalar ( pred, value ) ;
					}
				}
			} else if ( raw instanceof List ) {
				// Handle list of Jects at the root level
				Predicate pred = BasePredicate.root ;
				String predicateName = pred.name ( ) ;
				Class <? extends Ject> subjectType = ontologyRegistry.getRootSubjectType ( predicateName ) ;
				List <Object> children = ( List <Object> ) raw ;
				for ( Object child : children ) {
					if ( child instanceof Map childMap ) {
						visitor.childContext ( predicateName + "-" + visitor.increment ( ) ) ;
						Ject newInstance = subjectType.getConstructor ( ).newInstance ( ) ;
						contextStack.push ( new LocalParseContext ( newInstance, ontologyName, childMap ) ) ;
						buildJects ( ) ;
						currentJect.addTypedSubject ( pred, newInstance ) ;
						visitor.pop ( ) ;
					} else {
						throw new IllegalArgumentException ( "Invalid child type for predicate: " + predicateName ) ;
					}
				}
			} else {
				// Handle scalar properties directly on the Ject
				if ( raw != null ) {
					currentJect.addScalar ( BasePredicate.literal, raw ) ;
				}
			}
			if ( currentJect.getId ( ) != null ) {
				identityMap.put ( currentJect.getId ( ), currentJect ) ;
			}
		} catch ( InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
			throw new RuntimeException ( "Error while building Jects: " + e.getMessage ( ), e ) ;
		}
	}

	private void handleScalarListCreation ( LocalParseContext currentContext, Predicate pred, List <Object> children ) {
		Ject currentJect = currentContext.current ( ) ;
		List <Object> scalarList = new ArrayList <> ( ) ;
		for ( Object child : children ) {
			scalarList.add ( child ) ;
		}
		currentJect.addScalar ( pred, scalarList ) ;
	}

	private void handleJestListCreation ( LocalParseContext currentContext, Predicate pred, List <Object> children )
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Ject currentJect = currentContext.current ( ) ;
		String ontologyName = currentContext.ontologyName ( ) ;
		String predicateName = pred.name ( ) ;
		PredicateQualifier qualifier = pred.qualifier ( ) ;
		Class <? extends Ject> subjectType = qualifier.subjectType ( ) ;
		List <Object> parsedChildren = new ArrayList <> ( ) ;

		for ( Object child : children ) {
			visitor.childContext ( predicateName + "-" + visitor.increment ( ) ) ;
			Ject newInstance = null ;
			if ( RuntimeJect.class.equals ( subjectType ) ) {
				newInstance = new RuntimeJect ( predicateName, ontologyName ) ;
			} else {
				newInstance = subjectType.getConstructor ( ).newInstance ( ) ;
			}
			parsedChildren.add ( newInstance ) ;
			contextStack.push ( new LocalParseContext ( newInstance, ontologyName, child ) ) ;
			buildJects ( ) ;
			visitor.pop ( ) ;
		}
		if ( currentJect != null ) {
			for ( Object child : parsedChildren ) {
				if ( child instanceof Ject jectChild ) {
					currentJect.addTypedSubject ( pred, jectChild ) ;
				} else {
					throw new IllegalArgumentException ( "Invalid child type for predicate: " + predicateName ) ;
				}
			}
		}
	}

	private void handleNestedJectCreation ( LocalParseContext currentContext, Predicate pred, Map <String, Object> value )
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Ject currentJect = currentContext.current ( ) ;
		PredicateQualifier qualifier = pred.qualifier ( ) ;
		String predicateName = pred.name ( ) ;
		String ontologyName = currentContext.ontologyName ( ) ;
		Map <String, Object> childContent = ( Map <String, Object> ) value ;
		if ( childContent.containsKey ( "ref" ) ) {
			String refId = ( String ) childContent.get ( "ref" ) ;
			Placeholder <Ject> placeholder = getOrCreatePlaceholder ( refId, qualifier.subjectType ( ).getSimpleName ( ), ontologyName ) ;
			if ( currentJect != null ) {
				currentJect.addTypedSubject ( pred, placeholder ) ;
			}
		} else {
			Class <? extends Ject> subjectType = qualifier.subjectType ( ) ;
			Ject newInstance = null ;
			if ( RuntimeJect.class.equals ( subjectType ) ) {
				newInstance = new RuntimeJect ( predicateName, ontologyName ) ;
			} else {
				newInstance = subjectType.getConstructor ( ).newInstance ( ) ;
			}
			visitor.childContext ( predicateName + "-" + visitor.increment ( ) ) ;
			contextStack.push ( new LocalParseContext ( newInstance, ontologyName, childContent ) ) ;
			buildJects ( ) ;
			if ( currentJect != null ) {
				currentJect.addTypedSubject ( pred, newInstance ) ;
			}
			visitor.pop ( ) ;
		}
	}

	public Placeholder <Ject> getOrCreatePlaceholder ( String id, String typeName, String ontology ) {
		return placeholders.computeIfAbsent ( id, k -> new Placeholder <> ( id, typeName, ontology ) ) ;
	}

	public void resolveAll ( ) {
		for ( Map.Entry <String, Placeholder <Ject>> entry : placeholders.entrySet ( ) ) {
			String id = entry.getKey ( ) ;
			Placeholder <Ject> placeholder = entry.getValue ( ) ;
			Ject ject = identityMap.get ( id ) ;
			placeholder.resolve ( ject ) ; // Resolve the placeholder with the Ject from identityMap
		}
		Queue <Ject> queue = new LinkedList <> ( ) ;
		Map <Ject, String> visited = new IdentityHashMap <> ( ) ; // Track visited Jects to avoid cycles
		queue.add ( ontology ) ; // Start with the root ontology
		while ( ! queue.isEmpty ( ) ) {
			Ject current = queue.poll ( ) ;
			if ( visited.containsKey ( current ) ) {
				continue ; // Skip already visited Ject to avoid cycles
			} else {
				visited.put ( current, "X" ) ; // Mark current Ject as visited
			}
			resolveRelations ( current ) ; // Resolve relations for the current Ject
			for ( List <Ject> subjects : current.getSubjects ( ).values ( ) ) {
				queue.addAll ( subjects ); // Add all subjects to the queue for further processing
			}
		}
	}

	// Update in bill.zkaifleet.parser.OntologyParser (in resolveRelations, handle literal conversion in lists)
	public void resolveRelations ( Ject ject ) {
	    // Traverse and replace placeholders in subjects
	    for (Map.Entry<Predicate, List<Ject>> entry : ject.getSubjects ( ).entrySet()) {
			boolean literals = false ;
			boolean placeHolders = false ;
	        List<Object> resolvedList = new ArrayList<>(); // Use Object to hold Jects or scalars
	        for (Ject item : entry.getValue()) {
	            Object resolvedItem = item.resolveLiterals(); // Convert if literal
	            if (resolvedItem instanceof Placeholder) {
		        	placeHolders = true ;
	                Placeholder<?> placeholder = (Placeholder<?>) resolvedItem;
	                if (placeholder.getResolved() != null) {
	                    resolvedList.add(placeholder.getResolved().resolveLiterals());
	                } else {
	                    throw new IllegalStateException("Unresolved placeholder in relation: " + placeholder.getId());
	                }
	            } else if ( ! ( resolvedItem instanceof Ject ) ) {
	                // If resolvedItem is not a Ject, treat it as a literal scalar
	                // This is where we handle literals in the relation
	            	literals = true ;
	                resolvedList.add(resolvedItem);
	            }
	        }

	        if ( placeHolders && literals ) {
	        	throw new IllegalStateException("Cannot mix Jects and literals in the same relation: " + entry.getKey().name());
	        } else if ( placeHolders ) {
	        	List<Ject> resolvedJects = (List<Ject>) resolvedList.stream().filter(Ject.class::isInstance).map ( j -> ( Ject ) j ).toList ( ) ;
	        	entry.setValue(resolvedJects ) ; // For now, filter scalars if not Ject; evolve as needed
	        	for ( Ject jectItem : resolvedJects ) {
	        		jectItem.addIsObjectOf ( entry.getKey ( ), ject ) ; // Add back the relation to the original Ject
	        	}
	        } else if ( literals ) {
	        	ject.setScalars ( entry.getKey ( ), resolvedList ) ; // If all scalars, add as scalar
	        	ject.removeTypedSubjects ( entry.getKey ( ) ) ; // Remove the relation if only scalars
	        }
	    }

	    // Similarly for isObjectOf
	    for (Map.Entry<Predicate, List<Ject>> entry : ject.getIsObjectOf ( ).entrySet()) {
			boolean literals = false ;
			boolean jects = false ;
	        List<Object> resolvedList = new ArrayList<>();
	        for (Ject item : entry.getValue()) {
	            Object resolvedItem = item.resolveLiterals();
	            if (resolvedItem instanceof Placeholder) {
	            	jects = true ;
	                Placeholder<?> placeholder = (Placeholder<?>) resolvedItem;
	                if (placeholder.getResolved() != null) {
	                    resolvedList.add(placeholder.getResolved().resolveLiterals());
	                } else {
	                    throw new IllegalStateException("Unresolved placeholder in isObjectOf: " + placeholder.getId());
	                }
	            } else if ( ! ( resolvedItem instanceof Ject ) ) {
	            	literals = true ;
	                resolvedList.add(resolvedItem);
	            }
	        }
	        if ( jects && literals ) {
	        	throw new IllegalStateException("Cannot mix Jects and literals in the same isObjectOf relation: " + entry.getKey().name());
	        } else if ( jects ) {
	        	entry.setValue((List<Ject>) resolvedList.stream().filter(Ject.class::isInstance).map ( j -> ( Ject ) j ).toList ( ) ) ;
	        } else if ( literals ) {
	        	ject.setScalars ( entry.getKey ( ), resolvedList ) ; // If all scalars, add as scalar
	        }
	    }
	}

	public boolean hasSeen ( String id ) {
		return placeholders.containsKey ( id ) ;
	}

	private record LocalParseContext ( Ject current, String ontologyName, Object raw ) { ; }

	public void validateAnomalies ( ) {
		// TODO Auto-generated method stub
		
	}

	public Ontology getOntology ( ) {
		return ontology ;
	}
}
