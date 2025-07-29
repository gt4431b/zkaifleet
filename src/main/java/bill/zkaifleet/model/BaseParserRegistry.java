package bill.zkaifleet.model;

/**
 * The base parser registry that provides fundamental predicates and types.
 * <p>
 * This registry provides core functionality that is common across all ontologies,
 * and serves as a fallback when no specialized registry is available for an ontology.
 */
public class BaseParserRegistry extends AbstractParserRegistry {

    /**
     * Creates a new BaseParserRegistry for the "base" ontology.
     */
    public BaseParserRegistry() {
        super("base");
        initialize();
    }
    
    /**
     * Initializes the registry with base predicates and root subject types.
     */
    private void initialize() {
        // Register base predicates
        addPredicate(BasePredicate.id);
        addPredicate(BasePredicate.ontologyName);
        addPredicate(BasePredicate.literal);
        addPredicate(BasePredicate.root);
        
        // Register root subject types
        addRootSubject("root", RuntimeJect.class);
    }
    
    @Override
    public boolean canHandle(String ontologyName) {
        // Base registry can handle any ontology as a fallback
        return true;
    }
    
    @Override
    public int getPriority() {
        // Base registry has lowest priority
        return -100;
    }
}