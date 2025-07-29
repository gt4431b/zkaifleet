package bill.zkaifleet.parser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import bill.zkaifleet.model.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Additional coverage tests for the parser focusing on complex edge cases
 */
@QuarkusTest
public class OntologyParserCoverageTest {
    
    /**
     * Test handling different types of scalar values (boolean, number, string)
     */
    @Test
    public void testDifferentScalarTypes() {
        String yaml = """
                ontology: test
                id: scalarTest
                booleanProp: true
                numericProp: 42
                property: string value
                """;
                
        OntologyParser parser = new OntologyParser();
        Ject root = parser.parse(yaml);
        
        assertNotNull(root, "Root object should not be null");
        assertTrue(root instanceof Ontology, "Root should be an Ontology");
        Ontology ontology = (Ontology) root;
        
        // Find predicates by name in the predicate map
        Predicate booleanPred = findPredicateByName(ontology, "booleanProp");
        Predicate numericPred = findPredicateByName(ontology, "numericProp");
        Predicate propertyPred = findPredicateByName(ontology, "property");
        
        assertNotNull(booleanPred, "Boolean predicate should exist");
        assertNotNull(numericPred, "Numeric predicate should exist");
        assertNotNull(propertyPred, "Property predicate should exist");
        
        // Verify scalar values were stored with correct types
        assertEquals(Boolean.TRUE, ontology.getScalar(booleanPred, Boolean.class));
        assertEquals(42, ontology.getScalar(numericPred, Number.class));
        assertEquals("string value", ontology.getScalar(propertyPred, String.class));
    }
    
    /**
     * Test handling deeply nested structures with multiple levels
     */
    @Test
    public void testDeepNestedStructure() {
        String yaml = """
                ontology: test
                id: deepNested
                level1:
                  id: level1
                  property: top value
                  child:
                    id: level2
                    property: middle value
                    child:
                      id: level3
                      property: deepest value
                """;
                
        OntologyParser parser = new OntologyParser();
        Ject root = parser.parse(yaml);
        
        assertNotNull(root, "Root object should not be null");
        assertTrue(root instanceof Ontology, "Root should be an Ontology");
        Ontology ontology = (Ontology) root;
        
        // Find level1 object
        Predicate level1Pred = findPredicateByName(ontology, "level1");
        assertNotNull(level1Pred, "Level1 predicate should exist");
        
        List<Ject> level1Objects = ontology.getTypedSubjects(level1Pred, Ject.class);
        assertEquals(1, level1Objects.size(), "Should have one level1 object");
        Ject level1 = level1Objects.get(0);
        
        // Verify level1 properties
        Predicate propertyPred = findPredicateByName(level1, "property");
        assertEquals("top value", level1.getScalar(propertyPred, String.class));
        
        // Get level2
        Predicate childPred = findPredicateByName(level1, "child");
        List<Ject> level2Objects = level1.getTypedSubjects(childPred, Ject.class);
        assertEquals(1, level2Objects.size(), "Should have one level2 child");
        Ject level2 = level2Objects.get(0);
        
        // Verify level2 properties
        assertEquals("level2", level2.getId(), "Level2 ID should match");
        assertEquals("middle value", level2.getScalar(propertyPred, String.class));
        
        // Get level3
        List<Ject> level3Objects = level2.getTypedSubjects(childPred, Ject.class);
        assertEquals(1, level3Objects.size(), "Should have one level3 child");
        Ject level3 = level3Objects.get(0);
        
        // Verify level3 properties
        assertEquals("level3", level3.getId(), "Level3 ID should match");
        assertEquals("deepest value", level3.getScalar(propertyPred, String.class));
    }
    
    /**
     * Test handling complex reference resolution with multiple paths to the same object
     */
    @Test
    public void testComplexReferenceResolution() {
        // Updated to use the correct reference syntax with "ref" instead of "$ref"
        String yaml = """
                ontology: test
                id: complexRefs
                shared:
                  id: shared
                  property: shared value
                parent1:
                  id: parent1
                  child: 
                    ref: shared
                parent2:
                  id: parent2
                  child: 
                    ref: shared
                parent3:
                  id: parent3
                  child: 
                    ref: shared
                """;
                
        OntologyParser parser = new OntologyParser();
        Ject root = parser.parse(yaml);
        
        assertNotNull(root, "Root object should not be null");
        assertTrue(root instanceof Ontology, "Root should be an Ontology");
        Ontology ontology = (Ontology) root;
        
        // Find shared object
        Predicate sharedPred = findPredicateByName(ontology, "shared");
        List<Ject> sharedObjects = ontology.getTypedSubjects(sharedPred, Ject.class);
        assertEquals(1, sharedObjects.size(), "Should have one shared object");
        Ject sharedObject = sharedObjects.get(0);
        
        // Find parents
        Predicate parent1Pred = findPredicateByName(ontology, "parent1");
        Predicate parent2Pred = findPredicateByName(ontology, "parent2");
        Predicate parent3Pred = findPredicateByName(ontology, "parent3");
        
        List<Ject> parent1Objects = ontology.getTypedSubjects(parent1Pred, Ject.class);
        List<Ject> parent2Objects = ontology.getTypedSubjects(parent2Pred, Ject.class);
        List<Ject> parent3Objects = ontology.getTypedSubjects(parent3Pred, Ject.class);
        
        assertEquals(1, parent1Objects.size(), "Should have one parent1");
        assertEquals(1, parent2Objects.size(), "Should have one parent2");
        assertEquals(1, parent3Objects.size(), "Should have one parent3");
        
        Ject parent1 = parent1Objects.get(0);
        Ject parent2 = parent2Objects.get(0);
        Ject parent3 = parent3Objects.get(0);
        
        // Get children from each parent
        Predicate childPred = findPredicateByName(parent1, "child");
        List<Ject> children1 = parent1.getTypedSubjects(childPred, Ject.class);
        List<Ject> children2 = parent2.getTypedSubjects(childPred, Ject.class);
        List<Ject> children3 = parent3.getTypedSubjects(childPred, Ject.class);
        
        assertEquals(1, children1.size(), "Parent1 should have one child");
        assertEquals(1, children2.size(), "Parent2 should have one child");
        assertEquals(1, children3.size(), "Parent3 should have one child");
        
        // Verify all parents reference the same shared child (identity check)
        Ject child1 = children1.get(0);
        Ject child2 = children2.get(0);
        Ject child3 = children3.get(0);
        
        assertSame(child1, child2, "Child1 and Child2 should be the same instance");
        assertSame(child1, child3, "Child1 and Child3 should be the same instance");
        assertSame(sharedObject, child1, "Referenced child should be the shared object");
    }
    
    /**
     * Test handling of circular references
     */
    @Test
    public void testCircularReferences() {
        // Updated to use the correct reference syntax with "ref" instead of "$ref"
        String yaml = """
                ontology: test
                id: circularRefs
                nodeA:
                  id: nodeA
                  next:
                    ref: nodeB
                nodeB:
                  id: nodeB
                  next:
                    ref: nodeA
                """;
                
        OntologyParser parser = new OntologyParser();
        Ject root = parser.parse(yaml);
        
        assertNotNull(root, "Root object should not be null");
        assertTrue(root instanceof Ontology, "Root should be an Ontology");
        Ontology ontology = (Ontology) root;
        
        // Find nodeA and nodeB
        Predicate nodeAPred = findPredicateByName(ontology, "nodeA");
        Predicate nodeBPred = findPredicateByName(ontology, "nodeB");
        
        List<Ject> nodeAObjects = ontology.getTypedSubjects(nodeAPred, Ject.class);
        List<Ject> nodeBObjects = ontology.getTypedSubjects(nodeBPred, Ject.class);
        
        assertEquals(1, nodeAObjects.size(), "Should have one nodeA");
        assertEquals(1, nodeBObjects.size(), "Should have one nodeB");
        
        Ject nodeA = nodeAObjects.get(0);
        Ject nodeB = nodeBObjects.get(0);
        
        // Get next references
        Predicate nextPred = findPredicateByName(nodeA, "next");
        List<Ject> nextFromA = nodeA.getTypedSubjects(nextPred, Ject.class);
        List<Ject> nextFromB = nodeB.getTypedSubjects(nextPred, Ject.class);
        
        assertEquals(1, nextFromA.size(), "NodeA should have one next reference");
        assertEquals(1, nextFromB.size(), "NodeB should have one next reference");
        
        // Verify the circular references
        assertSame(nodeB, nextFromA.get(0), "Next from A should be B");
        assertSame(nodeA, nextFromB.get(0), "Next from B should be A");
    }
    
    /**
     * Test homogeneous collections - separation of scalars and Jects
     */
    @Test
    public void testHomogeneousSeparation() {
        String yaml = """
                ontology: test
                id: homogeneousList
                jectList:
                  - id: item1
                    value: value1
                  - id: item2
                    value: value2
                scalarList:
                  - string1
                  - string2
                """;
                
        OntologyParser parser = new OntologyParser();
        Ject root = parser.parse(yaml);
        
        assertNotNull(root, "Root object should not be null");
        assertTrue(root instanceof Ontology, "Root should be an Ontology");
        Ontology ontology = (Ontology) root;
        
        // Find the ject list and verify it contains Jects
        Predicate jectListPred = findPredicateByName(ontology, "jectList");
        List<Ject> jectList = ontology.getTypedSubjects(jectListPred, Ject.class);
        assertEquals(2, jectList.size(), "Should have two Jects in jectList");
        
        // Find the scalar list and verify it contains strings
        Predicate scalarListPred = findPredicateByName(ontology, "scalarList");
        List<String> scalarList = ontology.getScalars(scalarListPred, String.class);
        assertNotNull(scalarList, "Scalar list should exist");
        assertEquals(2, scalarList.size(), "Should have two strings in scalarList");
        assertTrue(scalarList.contains("string1"), "List should contain string1");
        assertTrue(scalarList.contains("string2"), "List should contain string2");
    }
    
    /**
     * Helper method to find a predicate by name in a Ject
     */
    private Predicate findPredicateByName(Ject ject, String name) {
        // First check subject predicates
        for (Predicate pred : ject.getSubjectPredicates()) {
            if (name.equals(pred.name())) {
                return pred;
            }
        }
        
        // Then check scalar predicates
        for (Predicate pred : ject.getScalarPredicates()) {
            if (name.equals(pred.name())) {
                return pred;
            }
        }
        
        // Finally check isObjectOf predicates
        for (Predicate pred : ject.getIsObjectOfPredicates()) {
            if (name.equals(pred.name())) {
                return pred;
            }
        }
        
        return null;
    }
}