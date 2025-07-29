package bill.zkaifleet.model;

import java.util.List;

/**
 * Interface for ontology parser registries that define predicates and root subject types.
 * <p>
 * Parser registries are responsible for:
 * <ul>
 *   <li>Mapping predicate names to Predicate instances</li>
 *   <li>Defining root subject types for parsing</li>
 *   <li>Supporting ontology-specific parsing rules</li>
 * </ul>
 * Implementations can be discovered via Java's ServiceLoader mechanism.
 */
public interface ParserRegistry {

    /**
     * Gets the ontology name this registry handles.
     * 
     * @return The ontology name
     */
    String getOntologyName();
    
    /**
     * Adds a root subject type for a predicate.
     * 
     * @param predicateName The predicate name
     * @param rootSubjectType The Ject class to use for this predicate
     */
    void addRootSubject(String predicateName, Class<? extends Ject> rootSubjectType);
    
    /**
     * Adds a predicate to this registry.
     * 
     * @param p The predicate to add
     */
    void addPredicate(Predicate p);
    
    /**
     * Adds multiple predicates to this registry.
     * 
     * @param predicatesList The list of predicates to add
     */
    void addPredicates(List<Predicate> predicatesList);
    
    /**
     * Adds all predicates from an enum to this registry.
     * 
     * @param predicatesEnum The enum class containing predicates
     */
    void addPredicatesEnums(Class<Enum<?>> predicatesEnum);
    
    /**
     * Gets a predicate by name, or creates a RuntimePredicate if not found.
     * 
     * @param key The predicate name
     * @param ontologyName The ontology name for runtime predicates
     * @return The predicate instance
     */
    Predicate getPredicate(String key, String ontologyName);
    
    /**
     * Gets the root subject type for a predicate.
     * 
     * @param predicateName The predicate name
     * @return The Ject class to use for this predicate
     */
    Class<? extends Ject> getRootSubjectType(String predicateName);
    
    /**
     * Checks if this registry can handle the given ontology.
     * 
     * @param ontologyName The ontology name to check
     * @return true if this registry can handle the ontology
     */
    default boolean canHandle(String ontologyName) {
        return getOntologyName().equals(ontologyName);
    }
    
    /**
     * Gets the priority of this registry. Higher priority registries are preferred.
     * 
     * @return The priority (default is 0)
     */
    default int getPriority() {
        return 0;
    }
}