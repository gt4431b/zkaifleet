package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ProcessJectTest {

    @Test
    public void testProcessJectCreation() {
        // Create a new ProcessJect
        ProcessJect process = new ProcessJect();
        
        // Verify basic properties
        assertEquals("process", process.getTypeName());
        assertEquals("fleet", process.getOntology());
        
        // Set and verify properties
        process.setId("process-1");
        process.setName("Data Processing");
        process.setDescription("Handles data processing operations");
        
        assertEquals("process-1", process.getId());
        assertEquals("Data Processing", process.getName());
        assertEquals("Handles data processing operations", process.getDescription());
    }
    
    @Test
    public void testProcessJectDefaultValues() {
        // Create a new ProcessJect and verify default values
        ProcessJect process = new ProcessJect();
        
        // Check that fields are null by default
        assertNull(process.getName());
        assertNull(process.getDescription());
    }
    
    @Test
    public void testProcessJectRelationships() {
        // Create a ProcessJect and verify it can be part of relationships
        ProcessJect process = new ProcessJect();
        process.setId("process-1");
        
        // Create a BootstrapAgentJect that handles this process
        BootstrapAgentJect agent = new BootstrapAgentJect();
        agent.setId("agent-1");
        agent.addProcess(process);
        
        // Verify the relationship was established
        assertTrue(process.getIsObjectOf().containsKey(FleetPredicate.process));
        assertTrue(process.getIsObjectOf().get(FleetPredicate.process).contains(agent));
        assertTrue(agent.getProcesses().contains(process));
    }
}