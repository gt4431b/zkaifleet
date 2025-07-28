package bill.zkaifleet.model.fleet;

import bill.zkaifleet.model.Predicate ;

//Enum for first-class Predicates (expanded for ontology elements)
public enum FleetPredicate implements Predicate {
	id, role, process, wrunk, integration, capabilities, escalationPath, confidenceThreshold, visionStatement, humanIntervention, contact, breakdown, task, evaluationAmendment, evaluation, contactPerson, contactEmail, contactPhone, contactAddress, contactType, contactRole, bootstrapAgent, interaction, wrunkTypeHandled;

    public String ontology() { return "fleet"; }
}
	// Note: The ontology is set to "fleet" for all predicates in this enum.