package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ContactJectTest {

    @Test
    public void testContactJectCreation() {
        // Create a new ContactJect
        ContactJect contact = new ContactJect();
        
        // Verify basic properties
        assertEquals("contact", contact.getTypeName());
        assertEquals("fleet", contact.getOntology());
        
        // Set and verify properties
        contact.setId("contact-1");
        contact.setMethod("email");
        contact.setTo("example@example.com");
        
        assertEquals("contact-1", contact.getId());
        assertEquals("email", contact.getMethod());
        assertEquals("example@example.com", contact.getTo());
    }
    
    @Test
    public void testContactJectDefaultValues() {
        // Create a new ContactJect and verify default values
        ContactJect contact = new ContactJect();
        
        // Check that fields are null by default
        assertNull(contact.getMethod());
        assertNull(contact.getTo());
    }
    
    @Test
    public void testContactJectInHumanIntervention() {
        // Create a ContactJect and verify it can be part of relationships
        ContactJect contact = new ContactJect();
        contact.setId("contact-1");
        contact.setMethod("phone");
        contact.setTo("+1234567890");
        
        // Create a HumanInterventionJect that uses this contact
        HumanInterventionJect intervention = new HumanInterventionJect();
        intervention.setId("intervention-1");
        intervention.addContact(contact);
        
        // Verify the relationship was established
        assertTrue(contact.getIsObjectOf().containsKey(FleetPredicate.contact));
        assertTrue(contact.getIsObjectOf().get(FleetPredicate.contact).contains(intervention));
        assertTrue(intervention.getContacts().contains(contact));
    }
}