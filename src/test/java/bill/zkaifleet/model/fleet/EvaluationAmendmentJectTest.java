package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class EvaluationAmendmentJectTest {

    @Test
    public void testEvaluationAmendmentJectCreation() {
        // Create a new EvaluationAmendmentJect
        EvaluationAmendmentJect amendment = new EvaluationAmendmentJect();
        
        // Verify basic properties
        assertEquals("evaluationAmendment", amendment.getTypeName());
        assertEquals("fleet", amendment.getOntology());
        
        // Set and verify ID
        amendment.setId("amendment-1");
        assertEquals("amendment-1", amendment.getId());
    }
    
    @Test
    public void testEvaluationAmendmentJectSimpleRelationship() {
        // Create an EvaluationAmendmentJect
        EvaluationAmendmentJect amendment = new EvaluationAmendmentJect();
        amendment.setId("amendment-1");
        
        // Create a FleetJect to contain the amendment
        FleetJect fleet = new FleetJect();
        fleet.setId("fleet-1");
        
        // Test a generic subject-object relationship
        fleet.addTypedSubject(FleetPredicate.role, amendment);
        
        // Verify the backlink was established
        assertTrue(amendment.getIsObjectOf().containsKey(FleetPredicate.role));
        assertTrue(amendment.getIsObjectOf().get(FleetPredicate.role).contains(fleet));
    }
}