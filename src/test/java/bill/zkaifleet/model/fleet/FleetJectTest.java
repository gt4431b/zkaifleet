package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class FleetJectTest {

    @Test
    public void testFleetJectCreation() {
        // Create a new FleetJect
        FleetJect fleet = new FleetJect();
        
        // Verify basic properties
        assertEquals("fleet", fleet.getTypeName());
        assertEquals("fleet", fleet.getOntology());
        
        // Set and verify properties
        fleet.setName("Test Fleet");
        fleet.setVersion("1.0.0");
        
        assertEquals("Test Fleet", fleet.getName());
        assertEquals("1.0.0", fleet.getVersion());
    }
    
    @Test
    public void testBootstrapAgentRelationship() {
        // Create the Fleet and agent
        FleetJect fleet = new FleetJect();
        BootstrapAgentJect agent = new BootstrapAgentJect();
        agent.setId("agent-1");
        agent.setDescription("Test Agent");
        
        // Set the relationship
        fleet.setBootstrapAgent(agent);
        
        // Verify the relationship
        BootstrapAgentJect retrievedAgent = fleet.getBootstrapAgent();
        assertNotNull(retrievedAgent);
        assertEquals("agent-1", retrievedAgent.getId());
        assertEquals("Test Agent", retrievedAgent.getDescription());
        
        // Verify backlink
        assertTrue(agent.getIsObjectOf().containsKey(FleetPredicate.bootstrapAgent));
        assertEquals(fleet, agent.getIsObjectOf().get(FleetPredicate.bootstrapAgent).get(0));
    }
    
    @Test
    public void testVisionStatementRelationship() {
        // Create the Fleet and vision statement
        FleetJect fleet = new FleetJect();
        VisionStatementJect vision = new VisionStatementJect();
        vision.setId("vision-1");
        
        // Set the relationship
        fleet.setVisionStatement(vision);
        
        // Verify the relationship
        VisionStatementJect retrievedVision = fleet.getVisionStatement();
        assertNotNull(retrievedVision);
        assertEquals("vision-1", retrievedVision.getId());
        
        // Verify backlink
        assertTrue(vision.getIsObjectOf().containsKey(FleetPredicate.visionStatement));
        assertEquals(fleet, vision.getIsObjectOf().get(FleetPredicate.visionStatement).get(0));
    }
    
    @Test
    public void testRolesRelationship() {
        // Create the Fleet and roles
        FleetJect fleet = new FleetJect();
        RoleJect role1 = new RoleJect();
        role1.setId("role-1");
        
        RoleJect role2 = new RoleJect();
        role2.setId("role-2");
        
        // Add roles to fleet
        fleet.addRole(role1);
        fleet.addRole(role2);
        
        // Verify relationships
        var roles = fleet.getRoles();
        assertEquals(2, roles.size());
        assertTrue(roles.contains(role1));
        assertTrue(roles.contains(role2));
        
        // Verify backlinks
        assertTrue(role1.getIsObjectOf().containsKey(FleetPredicate.role));
        assertTrue(role2.getIsObjectOf().containsKey(FleetPredicate.role));
        assertEquals(fleet, role1.getIsObjectOf().get(FleetPredicate.role).get(0));
        assertEquals(fleet, role2.getIsObjectOf().get(FleetPredicate.role).get(0));
    }
    
    @Test
    public void testWrunksRelationship() {
        // Create the Fleet and wrunks
        FleetJect fleet = new FleetJect();
        WrunkJect wrunk1 = new WrunkJect();
        wrunk1.setId("wrunk-1");
        wrunk1.setStorage("storage-1");
        wrunk1.getFields().add("field1");
        
        WrunkJect wrunk2 = new WrunkJect();
        wrunk2.setId("wrunk-2");
        wrunk2.setStorage("storage-2");
        wrunk2.getFields().add("field2");
        
        // Add wrunks to fleet
        fleet.addWrunk(wrunk1);
        fleet.addWrunk(wrunk2);
        
        // Verify relationships
        var wrunks = fleet.getWrunks();
        assertEquals(2, wrunks.size());
        assertTrue(wrunks.contains(wrunk1));
        assertTrue(wrunks.contains(wrunk2));
        
        // Verify backlinks
        assertTrue(wrunk1.getIsObjectOf().containsKey(FleetPredicate.wrunk));
        assertTrue(wrunk2.getIsObjectOf().containsKey(FleetPredicate.wrunk));
        assertEquals(fleet, wrunk1.getIsObjectOf().get(FleetPredicate.wrunk).get(0));
        assertEquals(fleet, wrunk2.getIsObjectOf().get(FleetPredicate.wrunk).get(0));
    }
    
    @Test
    public void testProcessesRelationship() {
        // Create the Fleet and processes
        FleetJect fleet = new FleetJect();
        ProcessJect process1 = new ProcessJect();
        process1.setId("process-1");
        
        ProcessJect process2 = new ProcessJect();
        process2.setId("process-2");
        
        // Add processes to fleet
        fleet.addProcess(process1);
        fleet.addProcess(process2);
        
        // Verify relationships
        var processes = fleet.getProcesses();
        assertEquals(2, processes.size());
        assertTrue(processes.contains(process1));
        assertTrue(processes.contains(process2));
        
        // Verify backlinks
        assertTrue(process1.getIsObjectOf().containsKey(FleetPredicate.process));
        assertTrue(process2.getIsObjectOf().containsKey(FleetPredicate.process));
        assertEquals(fleet, process1.getIsObjectOf().get(FleetPredicate.process).get(0));
        assertEquals(fleet, process2.getIsObjectOf().get(FleetPredicate.process).get(0));
    }
    
    @Test
    public void testIntegrationsRelationship() {
        // Create the Fleet and integrations
        FleetJect fleet = new FleetJect();
        IntegrationJect integration1 = new IntegrationJect();
        integration1.setId("integration-1");
        
        IntegrationJect integration2 = new IntegrationJect();
        integration2.setId("integration-2");
        
        // Add integrations to fleet
        fleet.addIntegration(integration1);
        fleet.addIntegration(integration2);
        
        // Verify relationships
        var integrations = fleet.getIntegrations();
        assertEquals(2, integrations.size());
        assertTrue(integrations.contains(integration1));
        assertTrue(integrations.contains(integration2));
        
        // Verify backlinks
        assertTrue(integration1.getIsObjectOf().containsKey(FleetPredicate.integration));
        assertTrue(integration2.getIsObjectOf().containsKey(FleetPredicate.integration));
        assertEquals(fleet, integration1.getIsObjectOf().get(FleetPredicate.integration).get(0));
        assertEquals(fleet, integration2.getIsObjectOf().get(FleetPredicate.integration).get(0));
    }
    
    @Test
    public void testHumanInterventionRelationship() {
        // Create the Fleet and human intervention
        FleetJect fleet = new FleetJect();
        HumanInterventionJect humanIntervention = new HumanInterventionJect();
        humanIntervention.setId("human-intervention-1");
        
        // Set the relationship
        fleet.setHumanIntervention(humanIntervention);
        
        // Verify the relationship
        HumanInterventionJect retrievedIntervention = fleet.getHumanIntervention();
        assertNotNull(retrievedIntervention);
        assertEquals("human-intervention-1", retrievedIntervention.getId());
        
        // Verify backlink
        assertTrue(humanIntervention.getIsObjectOf().containsKey(FleetPredicate.humanIntervention));
        assertEquals(fleet, humanIntervention.getIsObjectOf().get(FleetPredicate.humanIntervention).get(0));
    }
}