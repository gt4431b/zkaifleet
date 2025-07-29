package bill.zkaifleet.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Ontology class.
 */
public class OntologyTest {

    @Test
    public void testDefaultConstructor() {
        // Create an ontology with the default constructor
        Ontology ontology = new Ontology();
        
        // Default constructor should set the ontology to "base"
        assertEquals("ontology", ontology.getTypeName());
        assertEquals("base", ontology.getOntology());
    }
    
    @Test
    public void testConstructorWithOntologyName() {
        // Create an ontology with a specific ontology name
        Ontology ontology = new Ontology("test");
        
        // Constructor should set the ontology to the specified value
        assertEquals("ontology", ontology.getTypeName());
        assertEquals("test", ontology.getOntology());
    }
    
    @Test
    public void testAddAndGetRoots() {
        // Create an ontology
        Ontology ontology = new Ontology("test");
        
        // Create some test root objects
        RuntimeJect root1 = new RuntimeJect("RootType1", "test");
        root1.setId("root1");
        RuntimeJect root2 = new RuntimeJect("RootType2", "test");
        root2.setId("root2");
        
        // Add the roots to the ontology
        ontology.addRoot(root1);
        ontology.addRoot(root2);
        
        // Get the roots from the ontology
        List<Ject> roots = ontology.getRoots();
        
        // Verify the roots were added correctly
        assertNotNull(roots);
        assertEquals(2, roots.size());
        assertTrue(roots.contains(root1));
        assertTrue(roots.contains(root2));
    }
    
    @Test
    public void testAddRootReturnsSelf() {
        // Create an ontology
        Ontology ontology = new Ontology("test");
        
        // Create a test root object
        RuntimeJect root = new RuntimeJect("RootType", "test");
        root.setId("root");
        
        // The addRoot method should return the ontology itself for method chaining
        Ontology returnedOntology = ontology.addRoot(root);
        
        // Verify the returned ontology is the same instance
        assertSame(ontology, returnedOntology);
    }
    
    @Test
    public void testToString() {
        // Create an ontology
        Ontology ontology = new Ontology("test");
        ontology.setId("test-id");
        
        // Get the string representation
        String string = ontology.toString();
        
        // Verify the string contains the expected values
        assertTrue(string.contains("id='test-id'"));
        assertTrue(string.contains("typeName='ontology'"));
        assertTrue(string.contains("ontology='test'"));
    }
}