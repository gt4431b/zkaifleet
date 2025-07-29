package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TaskJectTest {

    @Test
    public void testTaskJectCreation() {
        // Create a new TaskJect
        TaskJect task = new TaskJect();
        
        // Verify basic properties
        assertEquals("task", task.getTypeName());
        assertEquals("fleet", task.getOntology());
        
        // Set and verify ID
        task.setId("task-1");
        assertEquals("task-1", task.getId());
    }
    
    @Test
    public void testTaskJectSimpleRelationship() {
        // Create a TaskJect
        TaskJect task = new TaskJect();
        task.setId("task-1");
        
        // Create a FleetJect to contain the task
        FleetJect fleet = new FleetJect();
        fleet.setId("fleet-1");
        
        // Test a generic subject-object relationship
        fleet.addTypedSubject(FleetPredicate.role, task);
        
        // Verify the backlink was established
        assertTrue(task.getIsObjectOf().containsKey(FleetPredicate.role));
        assertTrue(task.getIsObjectOf().get(FleetPredicate.role).contains(fleet));
    }
}