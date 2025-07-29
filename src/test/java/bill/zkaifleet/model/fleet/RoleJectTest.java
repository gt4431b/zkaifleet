package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class RoleJectTest {

    @Test
    public void testRoleJectCreation() {
        // Create a new RoleJect
        RoleJect role = new RoleJect();
        
        // Verify basic properties
        assertEquals("role", role.getTypeName());
        assertEquals("fleet", role.getOntology());
        
        // Set and verify properties
        role.setId("role-1");
        role.setSeniority("Senior");
        role.setModelTier("Advanced");
        
        assertEquals("role-1", role.getId());
        assertEquals("Senior", role.getSeniority());
        assertEquals("Advanced", role.getModelTier());
        
        // Test capabilities list
        assertNotNull(role.getCapabilities());
        assertTrue(role.getCapabilities().isEmpty());
        
        // Add capabilities and verify
        role.addCapability("capability1");
        role.addCapability("capability2");
        // Test duplicate prevention
        role.addCapability("capability1");
        
        assertEquals(2, role.getCapabilities().size());
        assertTrue(role.getCapabilities().contains("capability1"));
        assertTrue(role.getCapabilities().contains("capability2"));
        
        // Test escalation path list
        assertNotNull(role.getEscalationPath());
        assertTrue(role.getEscalationPath().isEmpty());
        
        // Add escalation paths and verify
        role.addEscalationPath("path1");
        role.addEscalationPath("path2");
        // Test duplicate prevention
        role.addEscalationPath("path1");
        
        assertEquals(2, role.getEscalationPath().size());
        assertTrue(role.getEscalationPath().contains("path1"));
        assertTrue(role.getEscalationPath().contains("path2"));
    }
    
    @Test
    public void testProcessRelationship() {
        // Create a role and processes
        RoleJect role = new RoleJect();
        ProcessJect process1 = new ProcessJect();
        process1.setId("process-1");
        
        ProcessJect process2 = new ProcessJect();
        process2.setId("process-2");
        
        // Add processes to role
        role.addProcess(process1);
        role.addProcess(process2);
        
        // Verify processes were added
        var processes = role.getProcesses();
        assertEquals(2, processes.size());
        assertTrue(processes.contains(process1));
        assertTrue(processes.contains(process2));
        
        // Verify backlinks
        assertTrue(process1.getIsObjectOf().containsKey(FleetPredicate.process));
        assertTrue(process2.getIsObjectOf().containsKey(FleetPredicate.process));
    }
    
    @Test
    public void testIntegrationRelationship() {
        // Create a role and integrations
        RoleJect role = new RoleJect();
        IntegrationJect integration1 = new IntegrationJect();
        integration1.setId("integration-1");
        
        IntegrationJect integration2 = new IntegrationJect();
        integration2.setId("integration-2");
        
        // Add integrations to role
        role.addIntegration(integration1);
        role.addIntegration(integration2);
        
        // Verify integrations were added
        var integrations = role.getIntegrations();
        assertEquals(2, integrations.size());
        assertTrue(integrations.contains(integration1));
        assertTrue(integrations.contains(integration2));
        
        // Verify backlinks
        assertTrue(integration1.getIsObjectOf().containsKey(FleetPredicate.integration));
        assertTrue(integration2.getIsObjectOf().containsKey(FleetPredicate.integration));
    }
    
    @Test
    public void testInteractionRelationship() {
        // Create a role and interactions
        RoleJect role = new RoleJect();
        InteractionJect interaction1 = new InteractionJect();
        interaction1.setId("interaction-1");
        
        InteractionJect interaction2 = new InteractionJect();
        interaction2.setId("interaction-2");
        
        // Add interactions to role
        role.addInteraction(interaction1);
        role.addInteraction(interaction2);
        
        // Verify interactions were added
        var interactions = role.getInteractions();
        assertEquals(2, interactions.size());
        assertTrue(interactions.contains(interaction1));
        assertTrue(interactions.contains(interaction2));
        
        // Verify backlinks
        assertTrue(interaction1.getIsObjectOf().containsKey(FleetPredicate.interaction));
        assertTrue(interaction2.getIsObjectOf().containsKey(FleetPredicate.interaction));
    }
    
    @Test
    public void testWrunkTypesHandledRelationship() {
        // Create a role and wrunk types
        RoleJect role = new RoleJect();
        WrunkJect wrunk1 = new WrunkJect();
        wrunk1.setId("wrunk-1");
        
        WrunkJect wrunk2 = new WrunkJect();
        wrunk2.setId("wrunk-2");
        
        // Add wrunk types to role
        role.addWrunkTypeHandled(wrunk1);
        role.addWrunkTypeHandled(wrunk2);
        
        // Verify wrunk types were added
        var wrunkTypes = role.getWrunkTypesHandled();
        assertEquals(2, wrunkTypes.size());
        assertTrue(wrunkTypes.contains(wrunk1));
        assertTrue(wrunkTypes.contains(wrunk2));
        
        // Verify backlinks
        assertTrue(wrunk1.getIsObjectOf().containsKey(FleetPredicate.wrunkTypeHandled));
        assertTrue(wrunk2.getIsObjectOf().containsKey(FleetPredicate.wrunkTypeHandled));
    }
}