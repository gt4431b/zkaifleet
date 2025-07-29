package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class BootstrapAgentJectTest {

    @Test
    public void testBootstrapAgentJectCreation() {
        // Create a new BootstrapAgentJect
        BootstrapAgentJect agent = new BootstrapAgentJect();
        
        // Verify basic properties
        assertEquals("bootstrapAgent", agent.getTypeName());
        assertEquals("fleet", agent.getOntology());
        
        // Set and verify properties
        agent.setId("agent-1");
        agent.setDescription("Test Agent Description");
        agent.setConfidenceThreshold(0.85);
        
        assertEquals("agent-1", agent.getId());
        assertEquals("Test Agent Description", agent.getDescription());
        assertEquals(0.85, agent.getConfidenceThreshold());
        
        // Test capabilities list
        assertNotNull(agent.getCapabilities());
        assertTrue(agent.getCapabilities().isEmpty());
        
        // Add capabilities and verify
        agent.getCapabilities().add("capability1");
        agent.getCapabilities().add("capability2");
        
        assertEquals(2, agent.getCapabilities().size());
        assertTrue(agent.getCapabilities().contains("capability1"));
        assertTrue(agent.getCapabilities().contains("capability2"));
        
        // Test escalation path list
        assertNotNull(agent.getEscalationPath());
        assertTrue(agent.getEscalationPath().isEmpty());
        
        // Add escalation paths and verify
        agent.getEscalationPath().add("path1");
        agent.getEscalationPath().add("path2");
        
        assertEquals(2, agent.getEscalationPath().size());
        assertTrue(agent.getEscalationPath().contains("path1"));
        assertTrue(agent.getEscalationPath().contains("path2"));
    }
    
    @Test
    public void testProcessRelationship() {
        // Create an agent and processes
        BootstrapAgentJect agent = new BootstrapAgentJect();
        ProcessJect process1 = new ProcessJect();
        process1.setId("process-1");
        
        ProcessJect process2 = new ProcessJect();
        process2.setId("process-2");
        
        // Add processes to agent
        agent.addProcess(process1);
        agent.addProcess(process2);
        
        // Verify processes were added
        var processes = agent.getProcesses();
        assertEquals(2, processes.size());
        assertTrue(processes.contains(process1));
        assertTrue(processes.contains(process2));
        
        // Verify backlinks
        assertTrue(process1.getIsObjectOf().containsKey(FleetPredicate.process));
        assertTrue(process2.getIsObjectOf().containsKey(FleetPredicate.process));
    }
    
    @Test
    public void testWrunkTypesHandledRelationship() {
        // Create an agent and wrunk types
        BootstrapAgentJect agent = new BootstrapAgentJect();
        WrunkJect wrunk1 = new WrunkJect();
        wrunk1.setId("wrunk-1");
        
        WrunkJect wrunk2 = new WrunkJect();
        wrunk2.setId("wrunk-2");
        
        // Add wrunk types to agent
        agent.addWrunkTypeHandled(wrunk1);
        agent.addWrunkTypeHandled(wrunk2);
        
        // Verify wrunk types were added
        var wrunkTypes = agent.getWrunkTypesHandled();
        assertEquals(2, wrunkTypes.size());
        assertTrue(wrunkTypes.contains(wrunk1));
        assertTrue(wrunkTypes.contains(wrunk2));
        
        // Verify backlinks
        assertTrue(wrunk1.getIsObjectOf().containsKey(FleetPredicate.wrunkTypeHandled));
        assertTrue(wrunk2.getIsObjectOf().containsKey(FleetPredicate.wrunkTypeHandled));
    }
}