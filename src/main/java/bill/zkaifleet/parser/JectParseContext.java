package bill.zkaifleet.parser ;

import java.lang.reflect.InvocationTargetException ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;
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

	private final Map <String, Placeholder <Ject>> identityMap = new HashMap <> ( ) ;
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
		return identityMap.computeIfAbsent ( id, k -> new Placeholder <> ( id, typeName, ontology ) ) ;
	}

	public void resolveAll ( ) {
		// Third pass logic: Iterate and swap placeholders
		
	}

	public boolean hasSeen ( String id ) {
		return identityMap.containsKey ( id ) ;
	}

	private record LocalParseContext ( Ject current, String ontologyName, Object raw ) { ; }

	public void validateAnomalies ( ) {
		// TODO Auto-generated method stub
		
	}

	public Ontology getOntology ( ) {
		return ontology ;
	}
}
