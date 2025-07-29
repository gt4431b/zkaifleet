package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class IntegrationJectTest {

    @Test
    public void testIntegrationJectCreation() {
        // Create a new IntegrationJect
        IntegrationJect integration = new IntegrationJect();
        
        // Verify basic properties
        assertEquals("integration", integration.getTypeName());
        assertEquals("fleet", integration.getOntology());
        
        // Set and verify properties
        integration.setId("integration-1");
        integration.setType("rag");
        integration.setRepo("github.com/org/repo");
        
        assertEquals("integration-1", integration.getId());
        assertEquals("rag", integration.getType());
        assertEquals("github.com/org/repo", integration.getRepo());
    }
    
    @Test
    public void testRoleRelationship() {
        // Create an integration and roles
        IntegrationJect integration = new IntegrationJect();
        RoleJect role1 = new RoleJect();
        role1.setId("role-1");
        
        RoleJect role2 = new RoleJect();
        role2.setId("role-2");
        
        // Add roles to integration
        integration.addRole(role1);
        integration.addRole(role2);
        
        // Verify roles were added
        var roles = integration.getRoles();
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
        IntegrationJect integration = new IntegrationJect();
        RoleJect role1 = new RoleJect();
        role1.setId("role-1");
        
        RoleJect role2 = new RoleJect();
        role2.setId("role-2");
        
        // Chain method calls
        integration
            .addRole(role1)
            .addRole(role2);
        
        // Verify roles were added
        var roles = integration.getRoles();
        assertEquals(2, roles.size());
    }
}