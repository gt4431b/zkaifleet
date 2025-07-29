package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class InteractionJectTest {

    @Test
    public void testInteractionJectCreation() {
        // Create a new InteractionJect
        InteractionJect interaction = new InteractionJect();
        
        // Verify basic properties
        assertEquals("interaction", interaction.getTypeName());
        assertEquals("fleet", interaction.getOntology());
        
        // Set and verify properties
        interaction.setId("interaction-1");
        interaction.setWith("agent-123");
        interaction.setHow("notifyReview");
        
        assertEquals("interaction-1", interaction.getId());
        assertEquals("agent-123", interaction.getWith());
        assertEquals("notifyReview", interaction.getHow());
    }
    
    @Test
    public void testRoleRelationship() {
        // Create an interaction and roles
        InteractionJect interaction = new InteractionJect();
        RoleJect role1 = new RoleJect();
        role1.setId("role-1");
        
        RoleJect role2 = new RoleJect();
        role2.setId("role-2");
        
        // Add roles to interaction
        interaction.addRole(role1);
        interaction.addRole(role2);
        
        // Verify roles were added
        var roles = interaction.getRoles();
        assertEquals(2, roles.size());
        assertTrue(roles.contains(role1));
        assertTrue(roles.contains(role2));
        
        // Verify backlinks
        assertTrue(role1.getIsObjectOf().containsKey(FleetPredicate.role));
        assertTrue(role2.getIsObjectOf().containsKey(FleetPredicate.role));
    }
    
    @Test
    public void testFluentInterface() {
        // Test the fluent interface pattern
        InteractionJect interaction = new InteractionJect();
        RoleJect role1 = new RoleJect();
        role1.setId("role-1");
        
        RoleJect role2 = new RoleJect();
        role2.setId("role-2");
        
        // Chain method calls
        interaction
            .addRole(role1)
            .addRole(role2);
        
        // Verify roles were added
        var roles = interaction.getRoles();
        assertEquals(2, roles.size());
    }
}