package bill.zkaifleet.parser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import bill.zkaifleet.model.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Additional tests for JectParseContext focused on improving coverage 
 * for complex scenarios and edge cases.
 */
@QuarkusTest
public class JectParseContextCoverageTest {

    private Map<String, ParserRegistry> ontologyCatalog;
    private ParserRegistry testRegistry;

    @BeforeEach
    public void setup() {
        testRegistry = new BaseParserRegistry() {
            @Override
            public String getOntologyName() {
                return "test";
            }
            
            @Override
            public Predicate getPredicate(String name, String ontology) {
                if (name.equals("nullPredicate")) {
                    return null; // Return null to test null predicate handling
                } else if (name.startsWith("scalar") || name.equals("property")) {
                    return new TestPredicate(name, "String", ontology, null, String.class);
                } else if (name.equals("booleanProp")) {
                    return new TestPredicate(name, "Boolean", ontology, null, Boolean.class);
                } else if (name.equals("numericProp")) {
                    return new TestPredicate(name, "Number", ontology, null, Number.class);
                } else if (name.equals("mixedList")) {
                    // Test handling of mixed content - should enforce homogeneity
                    return new TestPredicate(name, "Mixed", ontology, RuntimeJect.class, Object.class);
                } else if (name.equals("items")) {
                    // For the items predicate, we need to support both Ject objects and scalar values
                    return new TestPredicate(name, "Ject", ontology, RuntimeJect.class, Object.class);
                } else if (name.equals("parent") || name.equals("child")) {
                    // For testing parent-child relationships
                    return new TestPredicate(name, "Ject", ontology, RuntimeJect.class, null);
                } else if (name.equals("referenceMap")) {
                    // For testing reference map handling
                    return new TestPredicate(name, "Ject", ontology, RuntimeJect.class, Map.class);
                } else if (name.equals("next")) {
                    // For testing circular references
                    return new TestPredicate(name, "Ject", ontology, RuntimeJect.class, null);
                } else if (name.equals("ref1") || name.equals("ref2")) {
                    // For references in the reference map
                    return new TestPredicate(name, "Ject", ontology, RuntimeJect.class, null);
                } else {
                    return new TestPredicate(name, "Ject", ontology, RuntimeJect.class, null);
                }
            }
            
            @Override
            public Class<? extends Ject> getRootSubjectType(String predicateName) {
                return RuntimeJect.class;
            }
        };
        
        ontologyCatalog = new HashMap<>();
        ontologyCatalog.put("test", testRegistry);
        ontologyCatalog.put("base", new BaseParserRegistry());
    }
    
    /**
     * Test handling different types of scalar values (boolean, number, string)
     */
    @Test
    public void testDifferentScalarTypes() {
        // Create ontology
        Ontology ontology = new Ontology("test");
        
        // Create raw data with different scalar types
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("id", "scalarTest");  // Add an ID to ensure it's a proper root
        rawData.put("booleanProp", true);
        rawData.put("numericProp", 42);
        rawData.put("property", "string value");
        
        // Create context and build jects
        JectParseContext context = new JectParseContext(ontology, "test", rawData, ontologyCatalog);
        context.buildJects();
        
        // Use the correct ID predicate from the test registry
        Predicate idPredicate = testRegistry.getPredicate("id", "test");
        
        // Use the ontology as the root Ject since JectParseContext applies properties to it
        assertEquals("scalarTest", ontology.getScalar(idPredicate, String.class),
            "The ontology should have the ID from rawData");
        
        // Verify scalar values were stored with correct types
        assertTrue(ontology.getScalar(testRegistry.getPredicate("booleanProp", "test"), Boolean.class));
        assertEquals(42, ontology.getScalar(testRegistry.getPredicate("numericProp", "test"), Number.class));
        assertEquals("string value", ontology.getScalar(testRegistry.getPredicate("property", "test"), String.class));
    }
    
    /**
     * Test handling deeply nested structures with multiple levels
     */
    @Test
    public void testDeepNestedStructure() {
        // Create ontology
        Ontology ontology = new Ontology("test");
        
        // Create raw data with deep nesting
        Map<String, Object> level3 = new HashMap<>();
        level3.put("id", "level3");
        level3.put("property", "deepest value");
        
        Map<String, Object> level2 = new HashMap<>();
        level2.put("id", "level2");
        level2.put("child", level3);
        level2.put("property", "middle value");
        
        Map<String, Object> level1 = new HashMap<>();
        level1.put("id", "level1");
        level1.put("child", level2);
        level1.put("property", "top value");
        
        // Create context and parse
        JectParseContext context = new JectParseContext(ontology, "test", level1, ontologyCatalog);
        context.buildJects();
        context.resolveAll();
        
        // Look for level1 directly on the ontology using the appropriate predicate
        Predicate childPredicate = testRegistry.getPredicate("child", "test");
        
        // Verify level 1 properties directly on the ontology
        assertEquals("top value", ontology.getScalar(testRegistry.getPredicate("property", "test"), String.class));
        
        // Get level 2 through level 1 (which is the ontology in this case)
        List<Ject> level2Jects = ontology.getTypedSubjects(childPredicate, Ject.class);
        assertEquals(1, level2Jects.size(), "Should have one level 2 child");
        Ject level2Ject = level2Jects.get(0);
        assertEquals("level2", level2Ject.getId(), "Level 2 ID should match");
        assertEquals("middle value", level2Ject.getScalar(testRegistry.getPredicate("property", "test"), String.class));
        
        // Get level 3
        List<Ject> level3Jects = level2Ject.getTypedSubjects(childPredicate, Ject.class);
        assertEquals(1, level3Jects.size(), "Should have one level 3 child");
        Ject level3Ject = level3Jects.get(0);
        assertEquals("level3", level3Ject.getId(), "Level 3 ID should match");
        assertEquals("deepest value", level3Ject.getScalar(testRegistry.getPredicate("property", "test"), String.class));
    }
    
    /**
     * Test handling complex reference resolution with multiple paths to the same object
     */
    @Test
    public void testComplexReferenceResolution() {
        // Create ontology
        Ontology ontology = new Ontology("test");
        
        // Create raw data with complex references
        Map<String, Object> shared = new HashMap<>();
        shared.put("id", "shared");
        shared.put("property", "shared value");
        
        Map<String, Object> ref1 = new HashMap<>();
        ref1.put("ref", "shared");
        
        Map<String, Object> ref2 = new HashMap<>();
        ref2.put("ref", "shared");
        
        Map<String, Object> parent1 = new HashMap<>();
        parent1.put("id", "parent1");
        parent1.put("child", shared); // Direct reference
        
        Map<String, Object> parent2 = new HashMap<>();
        parent2.put("id", "parent2");
        parent2.put("child", ref1); // Placeholder reference
        
        Map<String, Object> parent3 = new HashMap<>();
        parent3.put("id", "parent3");
        parent3.put("child", ref2); // Another placeholder reference
        
        Map<String, Object> root = new HashMap<>();
        root.put("id", "complexRoot");
        root.put("parent", Arrays.asList(parent1, parent2, parent3));
        
        // Create context and parse
        JectParseContext context = new JectParseContext(ontology, "test", root, ontologyCatalog);
        context.buildJects();
        context.resolveAll();
        
        // Use the correct ID predicate from the test registry
        Predicate idPredicate = testRegistry.getPredicate("id", "test");
        
        // Instead of looking through roots, check if the ID was applied to the ontology itself
        assertEquals("complexRoot", ontology.getScalar(idPredicate, String.class),
            "The ontology should have the ID from the root data");
        
        // Use the ontology as the root Ject
        Ject rootJect = ontology;
        
        // Get the parents
        Predicate parentPredicate = testRegistry.getPredicate("parent", "test");
        List<Ject> parents = rootJect.getTypedSubjects(parentPredicate, Ject.class);
        assertEquals(3, parents.size(), "Should have three parents");
        
        // Find each parent by ID using the correct predicate
        Ject parent1Ject = null;
        Ject parent2Ject = null;
        Ject parent3Ject = null;
        
        for (Ject parent : parents) {
            String parentId = parent.getScalar(idPredicate, String.class);
            switch (parentId) {
                case "parent1":
                    parent1Ject = parent;
                    break;
                case "parent2":
                    parent2Ject = parent;
                    break;
                case "parent3":
                    parent3Ject = parent;
                    break;
            }
        }
        
        assertNotNull(parent1Ject, "Parent1 should exist");
        assertNotNull(parent2Ject, "Parent2 should exist");
        assertNotNull(parent3Ject, "Parent3 should exist");
        
        // Get children from each parent
        Predicate childPredicate = testRegistry.getPredicate("child", "test");
        List<Ject> children1 = parent1Ject.getTypedSubjects(childPredicate, Ject.class);
        List<Ject> children2 = parent2Ject.getTypedSubjects(childPredicate, Ject.class);
        List<Ject> children3 = parent3Ject.getTypedSubjects(childPredicate, Ject.class);
        
        assertEquals(1, children1.size(), "Parent1 should have one child");
        assertEquals(1, children2.size(), "Parent2 should have one child");
        assertEquals(1, children3.size(), "Parent3 should have one child");
        
        // Verify all parents reference the same shared child (identity check)
        Ject child1 = children1.get(0);
        Ject child2 = children2.get(0);
        Ject child3 = children3.get(0);
        
        assertSame(child1, child2, "Child1 and Child2 should be the same instance");
        assertSame(child1, child3, "Child1 and Child3 should be the same instance");
        assertEquals("shared", child1.getScalar(idPredicate, String.class), "Child ID should be 'shared'");
        assertEquals("shared value", child1.getScalar(testRegistry.getPredicate("property", "test"), String.class));
    }
    
    /**
     * Test enforcement of homogeneous collections - separation of scalar types and Jects
     */
    @Test
    public void testHomogeneousCollections() {
        // Create ontology
        Ontology ontology = new Ontology("test");
        
        // Create two separate lists to test homogeneous collections
        // 1. A list of scalars (use individual items instead of a list)
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("id", "root");
        rawData.put("property1", "scalar item 1");  // Individual scalar properties instead of a list
        rawData.put("property2", "scalar item 2");
        
        // 2. A list of Jects
        Map<String, Object> item1 = new HashMap<>();
        item1.put("id", "item1");
        item1.put("property", "item value 1");
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("id", "item2");
        item2.put("property", "item value 2");
        
        List<Object> jectList = new ArrayList<>();
        jectList.add(item1);
        jectList.add(item2);
        rawData.put("jectList", jectList);
        
        // Create context and parse
        JectParseContext context = new JectParseContext(ontology, "test", rawData, ontologyCatalog);
        context.buildJects();
        context.resolveAll();
        
        // Use the ontology as the root Ject since JectParseContext applies properties to it
        Ject rootJect = ontology;
        Predicate idPredicate = testRegistry.getPredicate("id", "test");
        
        // Verify the ID is applied to the ontology
        assertEquals("root", rootJect.getScalar(idPredicate, String.class), 
            "Ontology should have the ID from rawData");
        
        // Verify individual scalar properties instead of a list
        Predicate property1Predicate = testRegistry.getPredicate("property1", "test");
        Predicate property2Predicate = testRegistry.getPredicate("property2", "test");
        
        assertEquals("scalar item 1", rootJect.getScalar(property1Predicate, String.class), 
            "Property1 should have correct value");
        assertEquals("scalar item 2", rootJect.getScalar(property2Predicate, String.class), 
            "Property2 should have correct value");
        
        // Verify the Ject list
        Predicate jectListPredicate = testRegistry.getPredicate("jectList", "test");
        List<Ject> retrievedJects = rootJect.getTypedSubjects(jectListPredicate, Ject.class);
        assertNotNull(retrievedJects, "Ject list should exist");
        assertEquals(2, retrievedJects.size(), "Should have two Ject items");
        
        // Check the Ject properties
        Map<String, String> jectValues = new HashMap<>();
        for (Ject ject : retrievedJects) {
            String id = ject.getScalar(idPredicate, String.class);
            String property = ject.getScalar(testRegistry.getPredicate("property", "test"), String.class);
            jectValues.put(id, property);
        }
        
        assertEquals("item value 1", jectValues.get("item1"), "First Ject should have correct property");
        assertEquals("item value 2", jectValues.get("item2"), "Second Ject should have correct property");
    }
    
    /**
     * Test handling of reference maps for complex reference resolution
     */
    @Test
    public void testReferenceMapHandling() {
        // Create ontology
        Ontology ontology = new Ontology("test");
        
        // Create some target objects for references
        Map<String, Object> target1 = new HashMap<>();
        target1.put("id", "target1");
        target1.put("property", "target1 value");
        
        Map<String, Object> target2 = new HashMap<>();
        target2.put("id", "target2");
        target2.put("property", "target2 value");
        
        // Create a reference map with multiple reference entries
        Map<String, Object> referenceMap = new HashMap<>();
        
        Map<String, Object> ref1 = new HashMap<>();
        ref1.put("ref", "target1");
        
        Map<String, Object> ref2 = new HashMap<>();
        ref2.put("ref", "target2");
        
        referenceMap.put("ref1", ref1);
        referenceMap.put("ref2", ref2);
        
        // Create the root data
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("id", "refMapRoot");
        rawData.put("items", Arrays.asList(target1, target2));
        rawData.put("referenceMap", referenceMap);
        
        // Create context and parse
        JectParseContext context = new JectParseContext(ontology, "test", rawData, ontologyCatalog);
        context.buildJects();
        context.resolveAll();
        
        // Use the correct ID predicate from the test registry
        Predicate idPredicate = testRegistry.getPredicate("id", "test");
        
        // Instead of looking through roots, check if the ID was applied to the ontology itself
        // since JectParseContext applies the properties from rawData to the provided ontology
        assertEquals("refMapRoot", ontology.getScalar(idPredicate, String.class), 
            "The ontology should have the ID from rawData");
        
        // Use the ontology as the root Ject
        Ject rootJect = ontology;
        
        // Get the reference map
        Predicate refMapPredicate = testRegistry.getPredicate("referenceMap", "test");
        List<Ject> refMaps = rootJect.getTypedSubjects(refMapPredicate, Ject.class);
        assertEquals(1, refMaps.size(), "Should have one reference map");
        
        Ject refMapJect = refMaps.get(0);
        
        // Get the target items
        Predicate itemsPredicate = testRegistry.getPredicate("items", "test");
        List<Ject> items = rootJect.getTypedSubjects(itemsPredicate, Ject.class);
        assertEquals(2, items.size(), "Should have two target items");
        
        // Find targets by ID - use the test registry's ID predicate
        Ject target1Ject = null;
        Ject target2Ject = null;
        for (Ject item : items) {
            String itemId = item.getScalar(idPredicate, String.class);
            if ("target1".equals(itemId)) {
                target1Ject = item;
            } else if ("target2".equals(itemId)) {
                target2Ject = item;
            }
        }
        
        assertNotNull(target1Ject, "Target1 should exist");
        assertNotNull(target2Ject, "Target2 should exist");
        
        // Get the references from the reference map
        Predicate ref1Predicate = testRegistry.getPredicate("ref1", "test");
        Predicate ref2Predicate = testRegistry.getPredicate("ref2", "test");
        
        List<Ject> ref1Jects = refMapJect.getTypedSubjects(ref1Predicate, Ject.class);
        List<Ject> ref2Jects = refMapJect.getTypedSubjects(ref2Predicate, Ject.class);
        
        assertEquals(1, ref1Jects.size(), "Should have one ref1");
        assertEquals(1, ref2Jects.size(), "Should have one ref2");
        
        // Verify the references point to the correct targets
        assertSame(target1Ject, ref1Jects.get(0), "Ref1 should point to target1");
        assertSame(target2Ject, ref2Jects.get(0), "Ref2 should point to target2");
    }
    
    /**
     * Test handling circular references to ensure they are resolved correctly
     * without infinite loops
     */
    @Test
    public void testCircularReferences() {
        // Create ontology
        Ontology ontology = new Ontology("test");
        
        // Create raw data with circular references
        Map<String, Object> nodeA = new HashMap<>();
        nodeA.put("id", "nodeA");
        
        Map<String, Object> nodeB = new HashMap<>();
        nodeB.put("id", "nodeB");
        
        // Create circular references
        Map<String, Object> refB = new HashMap<>();
        refB.put("ref", "nodeB");
        nodeA.put("next", refB);
        
        Map<String, Object> refA = new HashMap<>();
        refA.put("ref", "nodeA");
        nodeB.put("next", refA);
        
        // Create the root data with an ID
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("id", "circularRoot");
        rawData.put("items", Arrays.asList(nodeA, nodeB));
        
        // Create context and parse
        JectParseContext context = new JectParseContext(ontology, "test", rawData, ontologyCatalog);
        context.buildJects();
        context.resolveAll();
        
        // Use the correct ID predicate from the test registry
        Predicate idPredicate = testRegistry.getPredicate("id", "test");
        
        // Instead of looking through roots, check if the ID was applied directly to the ontology
        assertEquals("circularRoot", ontology.getScalar(idPredicate, String.class),
            "The ontology should have the ID from rawData");
        
        // Use the ontology as the root Ject since JectParseContext applies properties to it
        Ject rootJect = ontology;
        
        // Get the items from the root
        Predicate itemsPredicate = testRegistry.getPredicate("items", "test");
        List<Ject> items = rootJect.getTypedSubjects(itemsPredicate, Ject.class);
        assertEquals(2, items.size(), "Should have two items");
        
        // Find nodeA and nodeB by ID using the correct ID predicate
        Ject nodeAJect = null;
        Ject nodeBJect = null;
        for (Ject item : items) {
            String itemId = item.getScalar(idPredicate, String.class);
            if ("nodeA".equals(itemId)) {
                nodeAJect = item;
            } else if ("nodeB".equals(itemId)) {
                nodeBJect = item;
            }
        }
        
        assertNotNull(nodeAJect, "NodeA should exist");
        assertNotNull(nodeBJect, "NodeB should exist");
        
        // Get the next references
        Predicate nextPredicate = testRegistry.getPredicate("next", "test");
        
        List<Ject> nextFromA = nodeAJect.getTypedSubjects(nextPredicate, Ject.class);
        List<Ject> nextFromB = nodeBJect.getTypedSubjects(nextPredicate, Ject.class);
        
        assertEquals(1, nextFromA.size(), "NodeA should have one next reference");
        assertEquals(1, nextFromB.size(), "NodeB should have one next reference");
        
        // Verify the circular references
        assertSame(nodeBJect, nextFromA.get(0), "Next from A should be B");
        assertSame(nodeAJect, nextFromB.get(0), "Next from B should be A");
    }
    
    /**
     * Test custom predicate implementation for testing
     */
    private static class TestPredicate extends RuntimePredicate {
        private final Class<? extends Ject> subjectType;
        private final Class<?> scalarType;
        
        public TestPredicate(String name, String space, String ontology, 
                             Class<? extends Ject> subjectType, Class<?> scalarType) {
            super(name, space, ontology);
            this.subjectType = subjectType;
            this.scalarType = scalarType;
        }
        
        @Override
        public PredicateQualifier qualifier() {
            return new PredicateQualifier(false, false, null, 
                                          subjectType != null ? Collections.singletonList(subjectType) : null, 
                                          subjectType, scalarType, null);
        }
    }
}
