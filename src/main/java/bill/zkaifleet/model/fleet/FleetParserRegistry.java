package bill.zkaifleet.model.fleet;

import java.util.List ;

import bill.zkaifleet.model.ParserRegistry ;

public class FleetParserRegistry extends ParserRegistry {

	public FleetParserRegistry ( ) {
		super ( "fleet" ) ;
		this.addPredicates ( List.of ( FleetPredicate.class.getEnumConstants ( ) ) ) ;
		this.addRootSubject ( FleetPredicate.fleet.name ( ), FleetJect.class ) ;
	}
}
