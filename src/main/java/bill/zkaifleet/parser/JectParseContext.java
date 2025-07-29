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
import bill.zkaifleet.model.RuntimePredicate;
import lombok.Data ;
import lombok.extern.slf4j.Slf4j;

/**
 * Context class for parsing YAML data into a graph of Ject objects.
 * <p>
 * JectParseContext is responsible for interpreting YAML data structures and
 * converting them into a connected graph of Ject objects, maintaining references
 * and resolving placeholders.
 * 
 * <h2>Implementation Notes</h2>
 * <ul>
 *   <li>Within YAML files, attribute names are used as predicates in the subject-predicate-object model</li>
 *   <li>First-class Ject objects (like FleetJect) are preferred, but if unavailable, RuntimeJects 
 *       and RuntimePredicates are used to build the graph</li>
 *   <li>Predicate values should be homogeneous - cannot mix Ject types, nor Ject types with scalar types</li>
 *   <li>Maps in YAML are never treated as scalars, but are interpreted as Jects</li>
 *   <li>If a predicate doesn't exist, queries for that relationship should return null, not an empty RuntimePredicate</li>
 *   <li>Placeholders are temporary objects that should only ever contain one scalar value (the id)</li>
 *   <li>The raw data for parsing must be a Map&lt;String, Object&gt; as returned by YAML parsing.
 *       Lists or scalar values cannot be directly used - they must be wrapped in a Map</li>
 *   <li>When retrieving predicates or storing/retrieving values, always use the same predicate instance
 *       to ensure consistent behavior. Prefer getting predicates from a registry rather than creating new instances</li>
 *   <li>RuntimePredicates should always have appropriate qualifier configurations that specify both subject types
 *       and scalar types to handle all possible values correctly</li>
 *   <li>When testing, mock predicates must correctly implement qualifier() with appropriate subjectType and scalarType</li>
 * </ul>
 */
@Data
@Slf4j
public class JectParseContext {

	private final Map <String, Placeholder <Ject>> placeholders = new HashMap <> ( ) ;
	private final Map <String, Ject> identityMap = new HashMap <> ( ) ;
	private Stack <LocalParseContext> contextStack = new Stack <> ( ) ;
	private Map <String, ParserRegistry> ontologyCatalog = new HashMap <> ( ) ;
	private Visitor visitor = new Visitor ( ) ;
	private Ontology ontology ;

	/**
	 * Creates a new JectParseContext for building Jects from raw data.
	 * 
	 * @param current The current Ject being processed, typically an Ontology instance
	 * @param ontologyName The name of the ontology being processed
	 * @param rawRemnants The raw data from YAML parsing to be processed into Jects.
	 *                    IMPORTANT: This must always be a Map&lt;String, Object&gt; as returned by YAML parsing.
	 *                    Lists or scalar values cannot be directly used here - they must be wrapped in a Map.
	 * @param ontologyCatalog The catalog of parser registries used for resolving types
	 */
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
					
					// Handle null predicate by creating a RuntimePredicate
					if (pred == null) {
						pred = new RuntimePredicate(predicateName, "unknown", ontologyName);
						log.debug("Created runtime predicate '{}' for ontology '{}'", predicateName, ontologyName);
					}
					
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
	    // Create a copy of the subjects to avoid concurrent modification
	    Map<Predicate, List<Ject>> subjectsCopy = new HashMap<>(ject.getSubjects()) ;
	    
	    // Traverse and replace placeholders in subjects
	    for (Map.Entry<Predicate, List<Ject>> entry : subjectsCopy.entrySet()) {
			boolean literals = false ;
			boolean placeHolders = false ;
			boolean jects = false ;
	        List<Object> resolvedList = new ArrayList<>() ; // Use Object to hold Jects or scalars
	        for (Ject item : entry.getValue()) {
	            Object resolvedItem = item.resolveLiterals() ; // Convert if literal
	            if (resolvedItem instanceof Placeholder) {
		        	placeHolders = true ;
	                Placeholder<?> placeholder = (Placeholder<?>) resolvedItem ;
	                if (placeholder.getResolved() != null) {
	                    resolvedList.add(placeholder.getResolved().resolveLiterals()) ;
	                } else {
	                    throw new IllegalStateException("Unresolved placeholder in relation: " + placeholder.getId()) ;
	                }
	            } else if ( ! ( resolvedItem instanceof Ject ) ) {
	                // If resolvedItem is not a Ject, treat it as a literal scalar
	                // This is where we handle literals in the relation
	            	literals = true ;
	                resolvedList.add(resolvedItem) ;
	            } else {
	                // Regular Ject object
	                jects = true ;
	                resolvedList.add(resolvedItem) ;
	            }
	            
	            // Check for mixed content (Jects and literals in the same list)
	            if ((literals && jects) || (literals && placeHolders)) {
	                throw new IllegalStateException("Mixed content detected in list for predicate: " + entry.getKey().name() +
	                        ". Lists must contain either all Jects or all scalar values.") ;
	            }
	        }

	        if ( placeHolders && literals ) {
	        	throw new IllegalStateException("Cannot mix Jects and literals in the same relation: " + entry.getKey().name()) ;
	        } else if ( literals ) {
	        	ject.setScalars ( entry.getKey ( ), resolvedList ) ; // If all scalars, add as scalar
	        	ject.removeTypedSubjects ( entry.getKey ( ) ) ; // Remove the relation if only scalars
	        } else {
	        	List<Ject> resolvedJects = new ArrayList<>() ;
	        	for (Object obj : resolvedList) {
	        	    if (obj instanceof Ject jectObj) {
	        	        resolvedJects.add(jectObj) ;
	        	    } else if (obj instanceof Map) {
	        	        // If we encounter a Map that's not a Ject, that's an error - we don't allow mixed content
	        	        throw new IllegalStateException("Mixed content detected in list for predicate: " + entry.getKey().name() +
	        	                ". Found a Map that is not a Ject object.") ;
	        	    }
	        	}
	        	
	        	// Replace the original list
	        	if (ject.getSubjects().containsKey(entry.getKey())) {
	        	    ject.getSubjects().put(entry.getKey(), new ArrayList<>(resolvedJects)) ;
	        	    
	        	    // Update back references
	        	    for (Ject jectItem : resolvedJects) {
	        	        jectItem.addIsObjectOf(entry.getKey(), ject) ;
	        	    }
	        	}
	        }
	    }

	    // Create a copy of isObjectOf to avoid concurrent modification
	    Map<Predicate, List<Ject>> isObjectOfCopy = new HashMap<>(ject.getIsObjectOf()) ;
	    
	    // Similarly for isObjectOf
	    for (Map.Entry<Predicate, List<Ject>> entry : isObjectOfCopy.entrySet()) {
			boolean literals = false ;
			boolean jects = false ;
	        List<Object> resolvedList = new ArrayList<>() ;
	        for (Ject item : entry.getValue()) {
	            Object resolvedItem = item.resolveLiterals() ;
	            if (resolvedItem instanceof Placeholder) {
	            	jects = true ;
	                Placeholder<?> placeholder = (Placeholder<?>) resolvedItem ;
	                if (placeholder.getResolved() != null) {
	                    resolvedList.add(placeholder.getResolved().resolveLiterals()) ;
	                } else {
	                    throw new IllegalStateException("Unresolved placeholder in isObjectOf: " + placeholder.getId()) ;
	                }
	            } else if ( ! ( resolvedItem instanceof Ject ) ) {
	            	literals = true ;
	                resolvedList.add(resolvedItem) ;
	            } else {
	                // Regular Ject object
	                jects = true ;
	                resolvedList.add(resolvedItem) ;
	            }
	            
	            // Check for mixed content in isObjectOf
	            if (literals && jects) {
	                throw new IllegalStateException("Mixed content detected in isObjectOf list for predicate: " + 
	                        entry.getKey().name() + ". Lists must contain either all Jects or all scalar values.") ;
	            }
	        }
	        
	        if ( jects && literals ) {
	        	throw new IllegalStateException("Cannot mix Jects and literals in the same isObjectOf relation: " + entry.getKey().name()) ;
	        } else if ( literals ) {
	        	ject.setScalars ( entry.getKey ( ), resolvedList ) ; // If all scalars, add as scalar
	        	// Also remove from isObjectOf
	        	if (ject.getIsObjectOf().containsKey(entry.getKey())) {
	        	    ject.getIsObjectOf().remove(entry.getKey()) ;
	        	}
	        } else {
	        	List<Ject> resolvedJects = new ArrayList<>() ;
	        	for (Object obj : resolvedList) {
	        	    if (obj instanceof Ject jectObj) {
	        	        resolvedJects.add(jectObj) ;
	        	    } else if (obj instanceof Map) {
	        	        // If we encounter a Map that's not a Ject, that's an error
	        	        throw new IllegalStateException("Mixed content detected in isObjectOf list for predicate: " + 
	        	                entry.getKey().name() + ". Found a Map that is not a Ject object.") ;
	        	    }
	        	}
	        	
	        	// Replace the original list
	        	if (ject.getIsObjectOf().containsKey(entry.getKey())) {
	        	    ject.getIsObjectOf().put(entry.getKey(), new ArrayList<>(resolvedJects)) ;
	        	}
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
