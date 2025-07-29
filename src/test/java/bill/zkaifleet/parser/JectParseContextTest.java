package bill.zkaifleet.parser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import bill.zkaifleet.model.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

@QuarkusTest
public class JectParseContextTest {

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
                // Return custom predicates with appropriate types based on the name
                if (name.startsWith("scalar") || name.equals("property")) {
                    return new TestPredicate(name, "String", ontology, null, String.class);
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
    
    // A custom predicate implementation for testing that allows specifying subject and scalar types
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
            return new PredicateQualifier(false, false, null, Collections.emptyList(), 
                                        subjectType, scalarType, null);
        }
    }

    @Test
    public void testScalarListHandling() {
        // Create a simple ontology
        Ontology ontology = new Ontology("test");
        ontology.addScalar(BasePredicate.id, "root");
        
        // Create raw YAML equivalent with scalar list
        Map<String, Object> rawData = new HashMap<>();
        // YAML parses lists as a single value containing multiple items
        // Let's use a raw list here directly
        List<String> scalarData = Arrays.asList("item1", "item2", "item3");
        rawData.put("scalarList", scalarData);
        
        // Create JectParseContext
        JectParseContext context = new JectParseContext(ontology, "test", rawData, ontologyCatalog);
        
        // Process
        context.buildJects();
        context.resolveAll();
        
        // Verify scalar list was processed correctly
        // Access the raw scalar data directly rather than through the predicate
        Map<Predicate, List<Object>> scalarMap = ontology.getScalars();
        boolean foundScalarList = false;
        List<Object> scalarList = null;
        
        for (Map.Entry<Predicate, List<Object>> entry : scalarMap.entrySet()) {
            if ("scalarList".equals(entry.getKey().name())) {
                foundScalarList = true;
                scalarList = entry.getValue();
                break;
            }
        }
        
        assertTrue(foundScalarList, "Scalar list should be found");
        assertNotNull(scalarList, "Scalar list should not be null");
        
        // Check if the scalar list was processed as a list of individual items
        // or as a single item that contains a list
        if (scalarList.size() == 1 && scalarList.get(0) instanceof List) {
            // The scalar was processed as a single value containing a list
            @SuppressWarnings("unchecked")
            List<String> innerList = (List<String>) scalarList.get(0);
            assertEquals(3, innerList.size(), "Inner list should have 3 items");
            assertEquals("item1", innerList.get(0), "First item should match");
            assertEquals("item2", innerList.get(1), "Second item should match");
            assertEquals("item3", innerList.get(2), "Third item should match");
        } else {
            // The scalar was processed as individual items
            assertEquals(3, scalarList.size(), "Scalar list should have 3 items");
            assertEquals("item1", scalarList.get(0), "First item should match");
            assertEquals("item2", scalarList.get(1), "Second item should match");
            assertEquals("item3", scalarList.get(2), "Third item should match");
        }
    }
    
    @Test
    public void testJectListHandling() {
        // Create a simple ontology
        Ontology ontology = new Ontology("test");
        ontology.addScalar(BasePredicate.id, "root");
        
        // Create raw YAML equivalent with a list of Jects
        Map<String, Object> rawData = new HashMap<>();
        List<Map<String, Object>> jectList = new ArrayList<>();
        
        Map<String, Object> ject1 = new HashMap<>();
        ject1.put("id", "j1");
        ject1.put("type", "TestJect");
        
        Map<String, Object> ject2 = new HashMap<>();
        ject2.put("id", "j2");
        ject2.put("type", "TestJect");
        
        jectList.add(ject1);
        jectList.add(ject2);
        rawData.put("jectList", jectList);
        
        // Create JectParseContext
        JectParseContext context = new JectParseContext(ontology, "test", rawData, ontologyCatalog);
        
        // Process
        context.buildJects();
        context.resolveAll();
        
        // Verify Ject list was processed correctly
        TestPredicate jectPred = new TestPredicate("jectList", "Ject", "test", RuntimeJect.class, null);
        List<Ject> parsedJectList = ontology.getTypedSubjects(jectPred, Ject.class);
        assertNotNull(parsedJectList, "Ject list should not be null");
        assertEquals(2, parsedJectList.size(), "Ject list should have 2 items");
        assertEquals("j1", parsedJectList.get(0).getId());
        assertEquals("j2", parsedJectList.get(1).getId());
    }
    
    @Test
    public void testRuntimeJectCreation() {
        // Create a simple ontology
        Ontology ontology = new Ontology("test");
        ontology.addScalar(BasePredicate.id, "root");
        
        // Create raw YAML equivalent with nested structure
        Map<String, Object> rawData = new HashMap<>();
        Map<String, Object> nestedJect = new HashMap<>();
        nestedJect.put("id", "nested1");
        nestedJect.put("property", "value");
        rawData.put("unknownPredicate", nestedJect);
        
        // Create JectParseContext
        JectParseContext context = new JectParseContext(ontology, "test", rawData, ontologyCatalog);
        
        // Process
        context.buildJects();
        context.resolveAll();
        
        // Verify RuntimeJect was created for unknown predicate
        // Use getSubjects() to get the raw map of subjects
        Map<Predicate, List<Ject>> subjects = ontology.getSubjects();
        boolean foundSubject = false;
        Ject subject = null;
        
        for (Map.Entry<Predicate, List<Ject>> entry : subjects.entrySet()) {
            if ("unknownPredicate".equals(entry.getKey().name()) && !entry.getValue().isEmpty()) {
                foundSubject = true;
                subject = entry.getValue().get(0);
                break;
            }
        }
        
        assertTrue(foundSubject, "Should find the subject for unknownPredicate");
        assertNotNull(subject, "Subject should not be null");
        assertTrue(subject instanceof RuntimeJect, "Subject should be a RuntimeJect");
        assertEquals("nested1", subject.getId());
        
        // Check for the property in a more direct way
        Map<Predicate, List<Object>> scalarProps = subject.getScalars();
        boolean foundProperty = false;
        List<Object> propertyValues = null;
        
        for (Map.Entry<Predicate, List<Object>> entry : scalarProps.entrySet()) {
            if ("property".equals(entry.getKey().name())) {
                foundProperty = true;
                propertyValues = entry.getValue();
                break;
            }
        }
        
        assertTrue(foundProperty, "Should find the property");
        assertNotNull(propertyValues, "Property values should not be null");
        assertFalse(propertyValues.isEmpty(), "Property values should not be empty");
        assertEquals("value", propertyValues.get(0), "Property value should match");
    }
    
    @Test
    public void testReferenceHandling() {
        // Create a simple ontology
        Ontology ontology = new Ontology("test");
        ontology.addScalar(BasePredicate.id, "root");
        
        // Create raw YAML equivalent with references
        Map<String, Object> rawData = new HashMap<>();
        
        Map<String, Object> ject1 = new HashMap<>();
        ject1.put("id", "j1");
        ject1.put("type", "TestJect");
        
        Map<String, Object> reference = new HashMap<>();
        reference.put("ref", "j2");
        ject1.put("referenceTo", reference);
        
        Map<String, Object> ject2 = new HashMap<>();
        ject2.put("id", "j2");
        ject2.put("type", "TestJect");
        
        List<Map<String, Object>> jectList = Arrays.asList(ject1, ject2);
        rawData.put("jects", jectList);
        
        // Create JectParseContext
        JectParseContext context = new JectParseContext(ontology, "test", rawData, ontologyCatalog);
        
        // Process
        context.buildJects();
        context.resolveAll();
        
        // Verify reference was resolved correctly
        TestPredicate jectsPred = new TestPredicate("jects", "Ject", "test", RuntimeJect.class, null);
        List<Ject> parsedJects = ontology.getTypedSubjects(jectsPred, Ject.class);
        assertEquals(2, parsedJects.size());
        
        Ject j1 = parsedJects.stream()
                .filter(j -> "j1".equals(j.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(j1, "j1 should exist");
        
        TestPredicate refPred = new TestPredicate("referenceTo", "Ject", "test", RuntimeJect.class, null);
        List<Ject> references = j1.getTypedSubjects(refPred, Ject.class);
        assertEquals(1, references.size(), "Should have one reference");
        assertEquals("j2", references.get(0).getId(), "Reference should be to j2");
    }
    
    @Test
    public void testValidateAnomalies() {
        // Create a simple ontology
        Ontology ontology = new Ontology("test");
        ontology.addScalar(BasePredicate.id, "root");
        
        // Create raw YAML equivalent
        Map<String, Object> rawData = new HashMap<>();
        
        // Create JectParseContext
        JectParseContext context = new JectParseContext(ontology, "test", rawData, ontologyCatalog);
        
        // Process
        context.buildJects();
        context.resolveAll();
        
        // This should not throw an exception
        context.validateAnomalies();
        
        // Verify we can get the ontology
        assertSame(ontology, context.getOntology());
    }

    @Test
    public void testPlaceholderResolution() {
        // Create a simple ontology
        Ontology ontology = new Ontology("test");
        ontology.addScalar(BasePredicate.id, "root");
        
        // Create a placeholder and test getOrCreatePlaceholder
        JectParseContext context = new JectParseContext(ontology, "test", new HashMap<>(), ontologyCatalog);
        Placeholder<Ject> placeholder = context.getOrCreatePlaceholder("test-id", "TestJect", "test");
        
        assertNotNull(placeholder);
        assertEquals("test-id", placeholder.getId());
        assertEquals("TestJect", placeholder.getTypeName());
        assertEquals("test", placeholder.getOntology());
        
        // Getting the same placeholder should return the same instance
        Placeholder<Ject> samePlaceholder = context.getOrCreatePlaceholder("test-id", "TestJect", "test");
        assertSame(placeholder, samePlaceholder);
    }
    
    @Test
    public void testRootListHandling() {
        // Create a simple ontology
        Ontology ontology = new Ontology("test");
        ontology.addScalar(BasePredicate.id, "root");
        
        // Create test data
        // In YAML, this would be a list at some level in the hierarchy
        // But to JectParseContext, the raw data is always a Map<String, Object>
        Map<String, Object> rawData = new HashMap<>();
        
        // Create a list of items
        List<Map<String, Object>> jectList = new ArrayList<>();
        
        Map<String, Object> item1 = new HashMap<>();
        item1.put("id", "item1");
        item1.put("value", "value1");
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("id", "item2");
        item2.put("value", "value2");
        
        jectList.add(item1);
        jectList.add(item2);
        
        // Add the list to the map under a predicate key that will use the list handling
        rawData.put("items", jectList);
        
        // Create JectParseContext with the proper Map
        JectParseContext context = new JectParseContext(ontology, "test", rawData, ontologyCatalog);
        
        // Process
        context.buildJects();
        context.resolveAll();
        
        // Verify the list was processed correctly
        // We should find the items under the "items" predicate, not BasePredicate.root
        Map<Predicate, List<Ject>> subjects = ontology.getSubjects();
        boolean foundItemsPredicate = false;
        List<Ject> items = null;
        
        for (Map.Entry<Predicate, List<Ject>> entry : subjects.entrySet()) {
            if ("items".equals(entry.getKey().name())) {
                foundItemsPredicate = true;
                items = entry.getValue();
                break;
            }
        }
        
        assertTrue(foundItemsPredicate, "Should find the 'items' predicate");
        assertNotNull(items, "Items list should not be null");
        assertEquals(2, items.size(), "Should have two items");
        
        // Verify the items were processed correctly
        boolean foundItem1 = false;
        boolean foundItem2 = false;
        
        for (Ject item : items) {
            if ("item1".equals(item.getId())) {
                foundItem1 = true;
                // Access scalar values properly
                Map<Predicate, List<Object>> scalarMap = item.getScalars();
                boolean foundValue = false;
                for (Map.Entry<Predicate, List<Object>> entry : scalarMap.entrySet()) {
                    if ("value".equals(entry.getKey().name())) {
                        foundValue = true;
                        List<Object> values = entry.getValue();
                        assertFalse(values.isEmpty(), "Values should not be empty");
                        assertEquals("value1", values.get(0), "Value should match");
                    }
                }
                assertTrue(foundValue, "Should find the value property");
            } else if ("item2".equals(item.getId())) {
                foundItem2 = true;
                // Access scalar values properly
                Map<Predicate, List<Object>> scalarMap = item.getScalars();
                boolean foundValue = false;
                for (Map.Entry<Predicate, List<Object>> entry : scalarMap.entrySet()) {
                    if ("value".equals(entry.getKey().name())) {
                        foundValue = true;
                        List<Object> values = entry.getValue();
                        assertFalse(values.isEmpty(), "Values should not be empty");
                        assertEquals("value2", values.get(0), "Value should match");
                    }
                }
                assertTrue(foundValue, "Should find the value property");
            }
        }
        
        assertTrue(foundItem1, "Should find item1");
        assertTrue(foundItem2, "Should find item2");
    }
    
    @Test
    public void testRegistryFallback() {
        // Create a simple ontology
        Ontology ontology = new Ontology("unknown");
        ontology.addScalar(BasePredicate.id, "root");
        
        // Create raw YAML equivalent with nested structure
        Map<String, Object> rawData = new HashMap<>();
        Map<String, Object> nestedJect = new HashMap<>();
        nestedJect.put("id", "nested1");
        nestedJect.put("property", "value");
        rawData.put("unknownPredicate", nestedJect);
        
        // Create JectParseContext with an unknown ontology name
        // This should cause it to fall back to the "base" registry
        JectParseContext context = new JectParseContext(ontology, "unknown", rawData, ontologyCatalog);
        
        // Process
        context.buildJects();
        context.resolveAll();
        
        // Verify RuntimeJect was created using the base registry fallback
        Map<Predicate, List<Ject>> subjects = ontology.getSubjects();
        boolean foundSubject = false;
        Ject subject = null;
        
        for (Map.Entry<Predicate, List<Ject>> entry : subjects.entrySet()) {
            if ("unknownPredicate".equals(entry.getKey().name()) && !entry.getValue().isEmpty()) {
                foundSubject = true;
                subject = entry.getValue().get(0);
                break;
            }
        }
        
        assertTrue(foundSubject, "Should find the subject for unknownPredicate using base registry");
        assertNotNull(subject, "Subject should not be null");
        assertTrue(subject instanceof RuntimeJect, "Subject should be a RuntimeJect");
        assertEquals("nested1", subject.getId());
    }
    
    @Test
    public void testHasSeen() {
        // Create a simple ontology
        Ontology ontology = new Ontology("test");
        ontology.addScalar(BasePredicate.id, "root");
        
        // Create JectParseContext
        JectParseContext context = new JectParseContext(ontology, "test", new HashMap<>(), ontologyCatalog);
        
        // Test hasSeen before adding any placeholders
        assertFalse(context.hasSeen("nonexistent"), "Should not have seen nonexistent placeholder");
        
        // Add a placeholder and test hasSeen
        context.getOrCreatePlaceholder("test-id", "TestJect", "test");
        assertTrue(context.hasSeen("test-id"), "Should have seen the placeholder");
        assertFalse(context.hasSeen("another-id"), "Should not have seen another placeholder");
    }
}