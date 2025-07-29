package bill.zkaifleet.model.fleet;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

@QuarkusTest
public class WrunkJectTest {

    @Test
    public void testWrunkJectCreation() {
        // Create a new WrunkJect
        WrunkJect wrunk = new WrunkJect();
        
        // Verify basic properties
        assertEquals("wrunct", wrunk.getTypeName());
        assertEquals("fleet", wrunk.getOntology());
        
        // Set and verify properties
        wrunk.setId("wrunk-1");
        wrunk.setStorage("test-storage");
        
        assertEquals("wrunk-1", wrunk.getId());
        assertEquals("test-storage", wrunk.getStorage());
        
        // Test fields list
        assertNotNull(wrunk.getFields());
        assertTrue(wrunk.getFields().isEmpty());
        
        // Add fields and verify
        wrunk.getFields().add("field1");
        wrunk.getFields().add("field2");
        
        assertEquals(2, wrunk.getFields().size());
        assertTrue(wrunk.getFields().contains("field1"));
        assertTrue(wrunk.getFields().contains("field2"));
        
        // Test setting fields directly
        List<String> newFields = Arrays.asList("field3", "field4");
        wrunk.setFields(newFields);
        
        assertEquals(2, wrunk.getFields().size());
        assertTrue(wrunk.getFields().contains("field3"));
        assertTrue(wrunk.getFields().contains("field4"));
    }
}