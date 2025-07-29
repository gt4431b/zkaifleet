package bill.zkaifleet.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import bill.zkaifleet.model.*;
import java.util.*;

/**
 * Simple parser tests for improving coverage
 */
public class ParserCoverageTest {
    
    @Test
    public void testMixedContentDetection() {
        // Test that mixed content (Jects and scalars) in lists is properly detected and rejected
        String yamlWithMixedContent = """
                ontology: test
                id: mixed
                mixedList:
                  - stringItem
                  - 42
                  - {id: nestedObj, value: objValue}
                """;
                
        OntologyParser parser = new OntologyParser();
        
        // This should throw an exception due to mixed content
        Exception exception = assertThrows(
            IllegalStateException.class,
            () -> parser.parse(yamlWithMixedContent)
        );
        
        assertTrue(exception.getMessage().contains("Mixed content"), 
                   "Exception should mention mixed content");
    }
    
    @Test
    public void testNestedObjects() {
        // Test handling of nested objects
        String yamlWithNestedObjects = """
                ontology: test
                id: nested
                parent:
                  id: parent1
                  value: parentValue
                """;
                
        OntologyParser parser = new OntologyParser();
        Ject root = parser.parse(yamlWithNestedObjects);
        
        assertNotNull(root, "Root object should not be null");
        assertTrue(root instanceof Ontology, "Root should be an Ontology");
        assertEquals("nested", root.getId(), "Root ID should match");
    }
    
    @Test
    public void testCustomRegistryHandling() {
        // Test with a custom parser registry
        Map<String, ParserRegistry> customCatalog = new HashMap<>();
        ParserRegistry testRegistry = new BaseParserRegistry() {
            @Override
            public String getOntologyName() {
                return "custom";
            }
        };
        customCatalog.put("custom", testRegistry);
        
        OntologyParser parser = new OntologyParser(customCatalog);
        
        String yaml = """
                ontology: custom
                id: customTest
                """;
                
        Ject root = parser.parse(yaml);
        
        assertNotNull(root, "Root object should not be null");
        assertEquals("customTest", root.getId(), "Root ID should match");
    }
    
    @Test
    public void testUnresolvedReference() {
        // Test behavior with unresolved references
        String yamlWithUnresolvedRef = """
                ontology: test
                id: unresolved
                reference: {ref: nonExistent}
                """;
                
        OntologyParser parser = new OntologyParser();
        
        // This should throw an exception due to unresolved reference
        Exception exception = assertThrows(
            IllegalStateException.class,
            () -> parser.parse(yamlWithUnresolvedRef)
        );
        
        assertTrue(exception.getMessage().contains("Unresolved"), 
                   "Exception should mention unresolved reference");
    }
    
    @Test
    public void testDuplicateIdDetection() {
        // Test detection of duplicate IDs
        String yamlWithDuplicateIds = """
                ontology: test
                id: duplicateTest
                obj1:
                  id: duplicate
                obj2:
                  id: duplicate
                """;
                
        OntologyParser parser = new OntologyParser();
        
        // This should throw an exception due to duplicate IDs
        Exception exception = assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse(yamlWithDuplicateIds)
        );
        
        assertTrue(exception.getMessage().contains("Duplicate"), 
                   "Exception should mention duplicate IDs");
    }
}