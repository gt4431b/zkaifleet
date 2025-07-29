package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import bill.zkaifleet.model.Predicate;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class FleetParserRegistryTest {

    @Test
    public void testRegistryInitialization() {
        FleetParserRegistry registry = new FleetParserRegistry();
        
        // Verify basic properties
        assertEquals("fleet", registry.getOntologyName());
        assertEquals(50, registry.getPriority());
        
        // Verify predicates were registered
        Predicate fleetPredicate = registry.getPredicate("fleet", "fleet");
        assertNotNull(fleetPredicate);
        assertEquals(FleetPredicate.fleet, fleetPredicate);
        
        Predicate rolePredicate = registry.getPredicate("role", "fleet");
        assertNotNull(rolePredicate);
        assertEquals(FleetPredicate.role, rolePredicate);
        
        Predicate wrunkPredicate = registry.getPredicate("wrunk", "fleet");
        assertNotNull(wrunkPredicate);
        assertEquals(FleetPredicate.wrunk, wrunkPredicate);
        
        // Verify root subject types
        assertEquals(FleetJect.class, registry.getRootSubjectType("fleet"));
        
        // Verify non-existent predicate returns null
        assertNull(registry.getPredicate("nonexistent", "fleet"));
    }
    
    @Test
    public void testAllFleetPredicatesRegistered() {
        FleetParserRegistry registry = new FleetParserRegistry();
        
        // Check that all FleetPredicate enum values are registered
        for (FleetPredicate predicate : FleetPredicate.values()) {
            Predicate registeredPredicate = registry.getPredicate(predicate.name(), "fleet");
            assertNotNull(
                registeredPredicate,
                "Predicate " + predicate.name() + " should be registered"
            );
            assertEquals(predicate, registeredPredicate);
        }
    }
}