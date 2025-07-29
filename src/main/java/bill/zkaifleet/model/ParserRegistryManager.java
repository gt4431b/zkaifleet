package bill.zkaifleet.model;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * Manager for parser registries that handles discovery and lookup.
 * <p>
 * This class uses the Java ServiceLoader mechanism to discover ParserRegistry
 * implementations at runtime, and provides methods to look up the appropriate
 * registry for a given ontology.
 */
@Slf4j
public class ParserRegistryManager {

    private static final ParserRegistryManager INSTANCE = new ParserRegistryManager();
    
    private final Map<String, ParserRegistry> registryCache = new HashMap<>();
    private final ParserRegistry baseRegistry;
    
    /**
     * Creates a new ParserRegistryManager and initializes it with discovered registries.
     */
    private ParserRegistryManager() {
        baseRegistry = new BaseParserRegistry();
        loadRegistries();
    }
    
    /**
     * Gets the singleton instance of ParserRegistryManager.
     * 
     * @return The ParserRegistryManager instance
     */
    public static ParserRegistryManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Gets the appropriate registry for an ontology.
     * 
     * @param ontologyName The ontology name
     * @return The parser registry for the ontology, or the base registry if none is found
     */
    public ParserRegistry getRegistry(String ontologyName) {
        ParserRegistry registry = registryCache.get(ontologyName);
        if (registry == null) {
            log.debug("No specific registry found for ontology '{}', using base registry", ontologyName);
            return baseRegistry;
        }
        return registry;
    }
    
    /**
     * Registers a parser registry for an ontology.
     * 
     * @param registry The registry to register
     */
    public void registerRegistry(ParserRegistry registry) {
        if (registry != null) {
            String ontologyName = registry.getOntologyName();
            ParserRegistry existing = registryCache.get(ontologyName);
            
            if (existing == null || registry.getPriority() > existing.getPriority()) {
                registryCache.put(ontologyName, registry);
                log.info("Registered parser registry for ontology '{}': {}", 
                    ontologyName, registry.getClass().getSimpleName());
            }
        }
    }
    
    /**
     * Loads parser registries using the ServiceLoader mechanism.
     */
    private void loadRegistries() {
        log.debug("Loading parser registries via ServiceLoader");
        ServiceLoader<ParserRegistry> loader = ServiceLoader.load(ParserRegistry.class);
        
        int count = 0;
        for (ParserRegistry registry : loader) {
            registerRegistry(registry);
            count++;
        }
        
        log.info("Loaded {} parser registries: {}", count, 
            registryCache.values().stream()
                .map(r -> r.getClass().getSimpleName())
                .collect(Collectors.joining(", ")));
    }
    
    /**
     * Gets all registered parser registries.
     * 
     * @return Map of ontology names to parser registries
     */
    public Map<String, ParserRegistry> getAllRegistries() {
        return new HashMap<>(registryCache);
    }
}