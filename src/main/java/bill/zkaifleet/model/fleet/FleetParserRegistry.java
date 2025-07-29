package bill.zkaifleet.model.fleet;

import java.util.List ;

import bill.zkaifleet.model.AbstractParserRegistry ;

/**
 * Fleet-specific parser registry that provides predicates and types for the fleet ontology.
 * <p>
 * This registry handles parsing of fleet-related YAML structures, registering
 * the appropriate predicates and subject types.
 */
public class FleetParserRegistry extends AbstractParserRegistry {

	/**
	 * Creates a new FleetParserRegistry for the "fleet" ontology.
	 */
	public FleetParserRegistry() {
		super("fleet") ;
		initialize() ;
	}
	
	/**
	 * Initializes the registry with fleet-specific predicates and root subject types.
	 */
	private void initialize() {
		// Register fleet predicates
		this.addPredicates(List.of(FleetPredicate.class.getEnumConstants())) ;
		
		// Register root subject types
		this.addRootSubject(FleetPredicate.fleet.name(), FleetJect.class) ;
	}
	
	@Override
	public int getPriority() {
		// Fleet registry has medium priority
		return 50 ;
	}
}