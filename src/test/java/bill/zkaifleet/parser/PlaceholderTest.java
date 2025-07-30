package bill.zkaifleet.parser;

import bill.zkaifleet.model.BasePredicate;
import bill.zkaifleet.model.RuntimeJect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Placeholder functionality.
 * 
 * Placeholders are temporary objects used during parsing that hold an ID until they
 * can be resolved to an actual object.
 */
public class PlaceholderTest {
    
    @Test
    public void testPlaceholderCreation() {
        // Create a placeholder
        Placeholder<RuntimeJect> placeholder = new Placeholder<>("test-id", "TestType", "test-ontology");
        
        // Verify initial state
        assertEquals("test-id", placeholder.getId());
        assertEquals("TestType", placeholder.getTypeName());
        assertEquals("test-ontology", placeholder.getOntology());
        assertNull(placeholder.getResolved());
    }
    
    @Test
    public void testPlaceholderResolution() {
        // Create a placeholder
        Placeholder<RuntimeJect> placeholder = new Placeholder<>("test-id", "TestType", "test-ontology");
        
        // Create an actual object to resolve the placeholder to
        RuntimeJect actual = new RuntimeJect("TestType", "test-ontology");
        actual.setId("test-id");
        
        // Resolve the placeholder
        placeholder.resolve(actual);
        
        // Verify the placeholder is resolved
        assertNotNull(placeholder.getResolved());
        assertEquals(actual, placeholder.getResolved());
    }
    
    @Test
    public void testGetTypedSubjectsWithResolved() {
        // Create a placeholder
        Placeholder<RuntimeJect> placeholder = new Placeholder<>("test-id", "TestType", "test-ontology");
        
        // Create an actual object to resolve the placeholder to
        RuntimeJect actual = new RuntimeJect("TestType", "test-ontology");
        actual.setId("test-id");
        
        // Add a subject to the actual object
        RuntimeJect childJect = new RuntimeJect("ChildType", "test-ontology");
        childJect.setId("child-id");
        actual.addTypedSubject(BasePredicate.literal, childJect);
        
        // Resolve the placeholder
        placeholder.resolve(actual);
        
        // Get subjects through the placeholder
        List<RuntimeJect> children = placeholder.getTypedSubjects(BasePredicate.literal, RuntimeJect.class);
        
        // Verify the subjects are returned from the resolved object
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals("child-id", children.get(0).getId());
    }
    
    @Test
    public void testGetTypedSubjectsWithoutResolved() {
        // Create a placeholder
        Placeholder<RuntimeJect> placeholder = new Placeholder<>("test-id", "TestType", "test-ontology");
        
        // Add a subject directly to the placeholder
        RuntimeJect childJect = new RuntimeJect("ChildType", "test-ontology");
        childJect.setId("child-id");
        placeholder.addTypedSubject(BasePredicate.literal, childJect);
        
        // Get subjects from the placeholder (not resolved)
        List<RuntimeJect> children = placeholder.getTypedSubjects(BasePredicate.literal, RuntimeJect.class);
        
        // Verify the subjects are returned from the placeholder itself
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals("child-id", children.get(0).getId());
    }
    
    @Test
    public void testAddTypedSubjectWithResolved() {
        // Create a placeholder
        Placeholder<RuntimeJect> placeholder = new Placeholder<>("test-id", "TestType", "test-ontology");
        
        // Create an actual object to resolve the placeholder to
        RuntimeJect actual = new RuntimeJect("TestType", "test-ontology");
        actual.setId("test-id");
        
        // Resolve the placeholder
        placeholder.resolve(actual);
        
        // Add a subject through the placeholder
        RuntimeJect childJect = new RuntimeJect("ChildType", "test-ontology");
        childJect.setId("child-id");
        placeholder.addTypedSubject(BasePredicate.literal, childJect);
        
        // Verify the subject was added to the resolved object
        List<RuntimeJect> children = actual.getTypedSubjects(BasePredicate.literal, RuntimeJect.class);
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals("child-id", children.get(0).getId());
    }
}