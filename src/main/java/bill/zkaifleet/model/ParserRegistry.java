package bill.zkaifleet.model;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import lombok.Data ;

@Data
public class ParserRegistry {

	private String ontologyName ;

	private Map <String, Class <? extends Ject>> rootSubjects = new HashMap <> ( ) ;
	private Map <String, Predicate> predicates = new HashMap <> ( ) ;

	public ParserRegistry ( String ontologyName ) {
		this.ontologyName = ontologyName ;
	}

	public void addRootSubject ( String predicateName, Class <? extends Ject> rootSubjectType ) {
		if ( predicateName != null && !predicateName.isEmpty ( ) && rootSubjectType != null ) {
			rootSubjects.put ( predicateName, rootSubjectType ) ;
		}
	}

	public void addPredicate ( Predicate p ) {
		if ( p != null && !predicates.containsKey(p.toString()) ) {
			predicates.put ( p.name ( ), p);
			if ( p.qualifier ( ) != null ) {
				PredicateQualifier qualifier = p.qualifier ( ) ;
				if ( qualifier.pluralName ( ) != null && !qualifier.pluralName ( ).isEmpty ( ) ) {
					predicates.put ( qualifier.pluralName ( ), p ) ;
				}
			}
		}
	}

	public void addPredicates ( List <Predicate> predicatesList ) {
		if ( predicatesList != null ) {
			for ( Predicate predicate : predicatesList ) {
				addPredicate ( predicate ) ;
			}
		}
	}

	public void addPredicatesEnums ( Class <Enum <?>> predicatesEnum ) {
		if ( predicatesEnum != null ) {
			for ( Enum <?> predicate : predicatesEnum.getEnumConstants ( ) ) {
				if ( predicate instanceof Predicate ) {
					addPredicate ( ( Predicate ) predicate ) ;
				}
			}
		}
	}

	public Predicate getPredicate ( String key, String ontologyName2 ) {
		Predicate retVal = predicates.get(key);
		if ( retVal == null ) {
			retVal = new RuntimePredicate ( key, "unknown", ontologyName2 ) ;
		}
		return retVal ;
	}

	public Class <? extends Ject> getRootSubjectType ( String predicateName ) {
		return rootSubjects.get(predicateName);
	}
}
