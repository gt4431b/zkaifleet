package bill.zkaifleet.model.fleet;

import java.util.List ;

import bill.zkaifleet.model.Ject ;
import bill.zkaifleet.model.Predicate ;
import bill.zkaifleet.model.PredicateQualifier ;

//Enum for first-class Predicates (expanded for ontology elements)
public enum FleetPredicate implements Predicate {
	// singleton, required, // pluralName, objectTypes, subjectTypes, scalarType
	id ( new PredicateQualifier ( true, true, null, null, null, String.class, ( String s, Ject j ) -> j.setId ( s ) ) ),
	role ( new PredicateQualifier ( false, true, "roles", List.of ( FleetJect.class ), RoleJect.class, null, null ) ),
	process ( new PredicateQualifier ( false, true, "processes", List.of ( FleetJect.class ), ProcessJect.class, null, null ) ),
	wrunk ( new PredicateQualifier ( false, true, "wrunks", List.of ( FleetJect.class ), WrunkJect.class, null, null ) ),
	integration ( new PredicateQualifier ( false, true, "integrations", List.of ( FleetJect.class ), IntegrationJect.class, null, null ) ),
	capability ( new PredicateQualifier ( false, true, "capabilities", List.of ( RoleJect.class ), null, String.class, ( String c, RoleJect f ) -> f.addCapability ( c ) ) ),
	escalationPath ( new PredicateQualifier ( true, false, null, List.of ( BootstrapAgentJect.class, RoleJect.class ), null, String.class, ( String c, RoleJect f ) -> f.addEscalationPath ( c ) ) ),
	confidenceThreshold ( new PredicateQualifier ( true, true, null, List.of ( ConstraintsJect.class ), null, Double.class, ( Double d, ConstraintsJect f ) -> f.setConfidenceThreshold ( d ) ) ),
	visionStatement ( new PredicateQualifier ( true, true, null, List.of ( FleetJect.class ), VisionStatementJect.class, null, null ) ),
	humanIntervention ( new PredicateQualifier ( true, false, null, List.of ( FleetJect.class ), HumanInterventionJect.class, null, null ) ),
	contact ( new PredicateQualifier ( false, true, "contacts", List.of ( HumanInterventionJect.class ), ContactJect.class, null, null ) ),
	// breakdown, 
	// task,
	// evaluationAmendment, evaluation, contactPerson, contactEmail, contactPhone, contactAddress, contactType, contactRole,
	bootstrapAgent ( new PredicateQualifier ( true, true, null, List.of ( FleetJect.class ), BootstrapAgentJect.class, null, null ) ),
	interaction ( new PredicateQualifier ( false, true, "interactions", List.of ( FleetJect.class ), InteractionJect.class, null, null ) ),
	wrunkTypeHandled ( new PredicateQualifier ( false, true, "wrunkTypesHandled", List.of ( RoleJect.class ), WrunkJect.class, null, null ) )
	;

	private PredicateQualifier qualifier ;
	private FleetPredicate ( PredicateQualifier qualifier ) {
		this.qualifier = qualifier ;
	}

	public String ontology() { return "fleet"; }

	public PredicateQualifier qualifier() {
		return qualifier;
	}
}
