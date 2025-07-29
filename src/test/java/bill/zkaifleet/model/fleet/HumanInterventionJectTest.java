package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class HumanInterventionJectTest {

    @Test
    public void testHumanInterventionJectCreation() {
        // Create a new HumanInterventionJect
        HumanInterventionJect intervention = new HumanInterventionJect();
        
        // Verify basic properties
        assertEquals("humanIntervention", intervention.getTypeName());
        assertEquals("fleet", intervention.getOntology());
        
        // Set and verify properties
        intervention.setId("intervention-1");
        assertEquals("intervention-1", intervention.getId());
        
        // Test thresholds list
        assertNotNull(intervention.getThresholds());
        assertTrue(intervention.getThresholds().isEmpty());
        
        // Add thresholds and verify
        intervention.getThresholds().add("confidenceBelow: 0.7");
        intervention.getThresholds().add("timeExceeded: 300s");
        
        assertEquals(2, intervention.getThresholds().size());
        assertTrue(intervention.getThresholds().contains("confidenceBelow: 0.7"));
        assertTrue(intervention.getThresholds().contains("timeExceeded: 300s"));
    }
    
    @Test
    public void testContactRelationship() {
        // Create an intervention and contacts
        HumanInterventionJect intervention = new HumanInterventionJect();
        ContactJect contact1 = new ContactJect();
        contact1.setId("contact-1");
        
        ContactJect contact2 = new ContactJect();
        contact2.setId("contact-2");
        
        // Add contacts to intervention
        intervention.addContact(contact1);
        intervention.addContact(contact2);
        
        // Verify contacts were added
        var contacts = intervention.getContacts();
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(contact1));
        assertTrue(contacts.contains(contact2));
        
        // Verify backlinks
        assertTrue(contact1.getIsObjectOf().containsKey(FleetPredicate.contact));
        assertTrue(contact2.getIsObjectOf().containsKey(FleetPredicate.contact));
    }
    
    @Test
    public void testFluentInterface() {
        // Test the fluent interface pattern
        HumanInterventionJect intervention = new HumanInterventionJect();
        ContactJect contact1 = new ContactJect();
        contact1.setId("contact-1");
        
        ContactJect contact2 = new ContactJect();
        contact2.setId("contact-2");
        
        // Chain method calls
        intervention
            .addContact(contact1)
            .addContact(contact2);
        
        // Verify contacts were added
        var contacts = intervention.getContacts();
        assertEquals(2, contacts.size());
    }
}