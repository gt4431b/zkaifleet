package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ConstraintsJectTest {

    @Test
    public void testConstraintsJectCreation() {
        // Create a new ConstraintsJect
        ConstraintsJect constraints = new ConstraintsJect();
        
        // Verify basic properties
        assertEquals("constraints", constraints.getTypeName());
        assertEquals("fleet", constraints.getOntology());
        
        // Set and verify properties
        constraints.setId("constraints-1");
        constraints.setTokenBudget(4000);
        constraints.setFocus("precision");
        constraints.setConfidenceThreshold(0.85);
        
        assertEquals("constraints-1", constraints.getId());
        assertEquals(Integer.valueOf(4000), constraints.getTokenBudget());
        assertEquals("precision", constraints.getFocus());
        assertEquals(0.85, constraints.getConfidenceThreshold());
    }
    
    @Test
    public void testConstraintsJectDefaultValues() {
        // Create a new ConstraintsJect and verify default values
        ConstraintsJect constraints = new ConstraintsJect();
        
        // Check that fields are null by default
        assertNull(constraints.getTokenBudget());
        assertNull(constraints.getFocus());
        assertNull(constraints.getConfidenceThreshold());
    }
    
    @Test
    public void testConstraintsJectWithConfidenceThreshold() {
        // Create a ConstraintsJect
        ConstraintsJect constraints = new ConstraintsJect();
        constraints.setId("constraints-1");
        
        // Verify that the confidenceThreshold predicate works as expected
        Double threshold = 0.75;
        
        // Use the predicate qualifier's setter directly
        FleetPredicate.confidenceThreshold.qualifier().setter().accept(threshold, constraints);
        
        // Verify the value was set correctly
        assertEquals(threshold, constraints.getConfidenceThreshold());
    }
}