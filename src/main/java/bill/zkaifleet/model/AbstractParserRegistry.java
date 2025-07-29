package bill.zkaifleet.model;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base implementation of the ParserRegistry interface.
 * <p>
 * Provides common functionality for parser registries, including predicate
 * and root subject management. Concrete subclasses only need to implement
 * initialization logic specific to their ontology.
 */
@Slf4j
public abstract class AbstractParserRegistry implements ParserRegistry {

    @Getter
    private final String ontologyName;
    
    private final Map<String, Class<? extends Ject>> rootSubjects = new HashMap<>();
    private final Map<String, Predicate> predicates = new HashMap<>();

    /**
     * Creates a new parser registry for the specified ontology.
     *
     * @param ontologyName The ontology name this registry handles
     */
    protected AbstractParserRegistry(String ontologyName) {
        this.ontologyName = ontologyName;
        log.debug("Initialized AbstractParserRegistry for ontology: {}", ontologyName);
    }
    
    @Override
    public void addRootSubject(String predicateName, Class<? extends Ject> rootSubjectType) {
        if (predicateName != null && !predicateName.isEmpty() && rootSubjectType != null) {
            rootSubjects.put(predicateName, rootSubjectType);
            log.debug("Added root subject type {} for predicate '{}'", 
                rootSubjectType.getSimpleName(), predicateName);
        }
    }

    @Override
    public void addPredicate(Predicate p) {
        if (p != null && !predicates.containsKey(p.name())) {
            predicates.put(p.name(), p);
            if (p.qualifier() != null) {
                PredicateQualifier qualifier = p.qualifier();
                if (qualifier.pluralName() != null && !qualifier.pluralName().isEmpty()) {
                    predicates.put(qualifier.pluralName(), p);
                    log.debug("Added predicate '{}' with plural name '{}'", 
                        p.name(), qualifier.pluralName());
                }
            }
        }
    }

    @Override
    public void addPredicates(List<Predicate> predicatesList) {
        if (predicatesList != null) {
            for (Predicate predicate : predicatesList) {
                addPredicate(predicate);
            }
        }
    }

    @Override
    public void addPredicatesEnums(Class<Enum<?>> predicatesEnum) {
        if (predicatesEnum != null) {
            for (Enum<?> predicate : predicatesEnum.getEnumConstants()) {
                if (predicate instanceof Predicate) {
                    addPredicate((Predicate) predicate);
                }
            }
            log.debug("Added predicates from enum class: {}", predicatesEnum.getSimpleName());
        }
    }

    @Override
    public Predicate getPredicate(String key, String contextOntology) {
        return predicates.get(key);
    }

    @Override
    public Class<? extends Ject> getRootSubjectType(String predicateName) {
        return rootSubjects.get(predicateName);
    }
}