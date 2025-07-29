package bill.zkaifleet.parser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import bill.zkaifleet.model.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Additional tests for JectParseContext to improve coverage
 */
@QuarkusTest
public class JectParseContextAdditionalTest {
    
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
        
        assertTrue(exception.getMessage().contains("Mixed content detected"), 
                   "Exception should mention mixed content");
    }
    
    @Test
    public void testNestedObjectHandling() {
        // Test handling of deeply nested objects
        String yamlWithNestedObjects = """
                ontology: test
                id: nested
                parent:
                  id: parent1
                  child:
                    id: child1
                    grandchild:
                      id: grandchild1
                      value: nestedValue
                """;
                
        OntologyParser parser = new OntologyParser();
        Ject root = parser.parse(yamlWithNestedObjects);
        
        assertNotNull(root, "Root object should not be null");
        assertTrue(root instanceof Ontology, "Root should be an Ontology");
        
        // Navigate through the nested objects
        Ontology ontology = (Ontology) root;
        
        // Check that parent exists in subjects
        Map<Predicate, List<Ject>> subjects = ontology.getSubjects();
        boolean foundParent = false;
        Ject parent = null;
        
        for (Map.Entry<Predicate, List<Ject>> entry : subjects.entrySet()) {
            if ("parent".equals(entry.getKey().name()) && !entry.getValue().isEmpty()) {
                foundParent = true;
                parent = entry.getValue().get(0);
                break;
            }
        }
        
        assertTrue(foundParent, "Should find parent relationship");
        assertNotNull(parent, "Parent should not be null");
        assertEquals("parent1", parent.getId(), "Parent ID should match");
        
        // Check child
        Map<Predicate, List<Ject>> childSubjects = parent.getSubjects();
        boolean foundChild = false;
        Ject child = null;
        
        for (Map.Entry<Predicate, List<Ject>> entry : childSubjects.entrySet()) {
            if ("child".equals(entry.getKey().name()) && !entry.getValue().isEmpty()) {
                foundChild = true;
                child = entry.getValue().get(0);
                break;
            }
        }
        
        assertTrue(foundChild, "Should find child relationship");
        assertNotNull(child, "Child should not be null");
        assertEquals("child1", child.getId(), "Child ID should match");
        
        // Check grandchild
        Map<Predicate, List<Ject>> grandchildSubjects = child.getSubjects();
        boolean foundGrandchild = false;
        Ject grandchild = null;
        
        for (Map.Entry<Predicate, List<Ject>> entry : grandchildSubjects.entrySet()) {
            if ("grandchild".equals(entry.getKey().name()) && !entry.getValue().isEmpty()) {
                foundGrandchild = true;
                grandchild = entry.getValue().get(0);
                break;
            }
        }
        
        assertTrue(foundGrandchild, "Should find grandchild relationship");
        assertNotNull(grandchild, "Grandchild should not be null");
        assertEquals("grandchild1", grandchild.getId(), "Grandchild ID should match");
        
        // Check value property
        Map<Predicate, List<Object>> scalarProps = grandchild.getScalars();
        boolean foundValue = false;
        List<Object> valueProperties = null;
        
        for (Map.Entry<Predicate, List<Object>> entry : scalarProps.entrySet()) {
            if ("value".equals(entry.getKey().name())) {
                foundValue = true;
                valueProperties = entry.getValue();
                break;
            }
        }
        
        assertTrue(foundValue, "Should find value property");
        assertNotNull(valueProperties, "Value properties should not be null");
        assertFalse(valueProperties.isEmpty(), "Value properties should not be empty");
        assertEquals("nestedValue", valueProperties.get(0), "Value property should match");
    }
    
    @Test
    public void testMapHandling() {
        // Test that maps are properly converted to Jects rather than being treated as scalars
        String yamlWithMap = """
                ontology: test
                id: mapTest
                mapObject:
                  id: map1
                  key1: value1
                  key2: value2
                """;
                
        OntologyParser parser = new OntologyParser();
        Ject root = parser.parse(yamlWithMap);
        
        assertNotNull(root, "Root object should not be null");
        assertTrue(root instanceof Ontology, "Root should be an Ontology");
        
        // Check that the map was converted to a Ject
        Ontology ontology = (Ontology) root;
        
        // Find mapObject in subjects
        Map<Predicate, List<Ject>> subjects = ontology.getSubjects();
        boolean foundMapObject = false;
        Ject mapObject = null;
        
        for (Map.Entry<Predicate, List<Ject>> entry : subjects.entrySet()) {
            if ("mapObject".equals(entry.getKey().name()) && !entry.getValue().isEmpty()) {
                foundMapObject = true;
                mapObject = entry.getValue().get(0);
                break;
            }
        }
        
        assertTrue(foundMapObject, "Should find mapObject relationship");
        assertNotNull(mapObject, "Map object should not be null");
        assertEquals("map1", mapObject.getId(), "Map ID should match");
        
        // Check key1 and key2 scalar properties
        Map<Predicate, List<Object>> scalarProps = mapObject.getScalars();
        
        boolean foundKey1 = false;
        List<Object> key1Values = null;
        boolean foundKey2 = false;
        List<Object> key2Values = null;
        
        for (Map.Entry<Predicate, List<Object>> entry : scalarProps.entrySet()) {
            if ("key1".equals(entry.getKey().name())) {
                foundKey1 = true;
                key1Values = entry.getValue();
            } else if ("key2".equals(entry.getKey().name())) {
                foundKey2 = true;
                key2Values = entry.getValue();
            }
        }
        
        assertTrue(foundKey1, "Should find key1 property");
        assertNotNull(key1Values, "Key1 values should not be null");
        assertFalse(key1Values.isEmpty(), "Key1 values should not be empty");
        assertEquals("value1", key1Values.get(0), "Key1 value should match");
        
        assertTrue(foundKey2, "Should find key2 property");
        assertNotNull(key2Values, "Key2 values should not be null");
        assertFalse(key2Values.isEmpty(), "Key2 values should not be empty");
        assertEquals("value2", key2Values.get(0), "Key2 value should match");
    }
    
    @Test
    public void testEmptyOrNonExistentRelation() {
        // Test behavior when querying non-existent relationships
        String yaml = """
                ontology: test
                id: emptyRelation
                existingRel:
                  id: child1
                """;
                
        OntologyParser parser = new OntologyParser();
        Ject root = parser.parse(yaml);
        
        assertNotNull(root, "Root object should not be null");
        assertTrue(root instanceof Ontology, "Root should be an Ontology");
        
        // Check that non-existent relation is not in subjects map
        Ontology ontology = (Ontology) root;
        Map<Predicate, List<Ject>> subjects = ontology.getSubjects();
        
        boolean foundNonExistent = false;
        for (Map.Entry<Predicate, List<Ject>> entry : subjects.entrySet()) {
            if ("nonExistentRel".equals(entry.getKey().name())) {
                foundNonExistent = true;
                break;
            }
        }
        
        assertFalse(foundNonExistent, "Non-existent relationship should not be in subjects map");
        
        // Check that existing relation is in subjects map
        boolean foundExisting = false;
        Ject child = null;
        
        for (Map.Entry<Predicate, List<Ject>> entry : subjects.entrySet()) {
            if ("existingRel".equals(entry.getKey().name())) {
                foundExisting = true;
                if (!entry.getValue().isEmpty()) {
                    child = entry.getValue().get(0);
                }
                break;
            }
        }
        
        assertTrue(foundExisting, "Existing relationship should be in subjects map");
        assertNotNull(child, "Child should not be null");
        assertEquals("child1", child.getId(), "Child ID should match");
    }
}