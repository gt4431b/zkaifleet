package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import bill.zkaifleet.model.PredicateQualifier;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class FleetPredicateTest {

    @Test
    public void testPredicateQualifiers() {
        // Test role predicate
        PredicateQualifier roleQualifier = FleetPredicate.role.qualifier();
        assertFalse(roleQualifier.single());
        assertTrue(roleQualifier.required());
        assertEquals("roles", roleQualifier.pluralName());
        assertEquals(FleetJect.class, roleQualifier.objectTypes().get(0));
        assertEquals(RoleJect.class, roleQualifier.subjectType());
        assertNull(roleQualifier.scalarType());
        assertNull(roleQualifier.setter());
        
        // Test id predicate
        PredicateQualifier idQualifier = FleetPredicate.id.qualifier();
        assertTrue(idQualifier.single());
        assertTrue(idQualifier.required());
        assertNull(idQualifier.pluralName());
        assertNull(idQualifier.objectTypes());
        assertNull(idQualifier.subjectType());
        assertEquals(String.class, idQualifier.scalarType());
        assertNotNull(idQualifier.setter());
        
        // Test wrunk predicate
        PredicateQualifier wrunkQualifier = FleetPredicate.wrunk.qualifier();
        assertFalse(wrunkQualifier.single());
        assertTrue(wrunkQualifier.required());
        assertEquals("wrunks", wrunkQualifier.pluralName());
        assertEquals(FleetJect.class, wrunkQualifier.objectTypes().get(0));
        assertEquals(WrunkJect.class, wrunkQualifier.subjectType());
        assertNull(wrunkQualifier.scalarType());
        assertNull(wrunkQualifier.setter());
        
        // Test capability predicate
        PredicateQualifier capabilityQualifier = FleetPredicate.capability.qualifier();
        assertFalse(capabilityQualifier.single());
        assertTrue(capabilityQualifier.required());
        assertEquals("capabilities", capabilityQualifier.pluralName());
        assertEquals(RoleJect.class, capabilityQualifier.objectTypes().get(0));
        assertNull(capabilityQualifier.subjectType());
        assertEquals(String.class, capabilityQualifier.scalarType());
        assertNotNull(capabilityQualifier.setter());
    }
    
    @Test
    public void testPredicateOntology() {
        // All predicates should have the same ontology
        for (FleetPredicate predicate : FleetPredicate.values()) {
            assertEquals("fleet", predicate.ontology());
        }
    }
}