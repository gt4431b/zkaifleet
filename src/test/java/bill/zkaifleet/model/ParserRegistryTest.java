package bill.zkaifleet.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the ParserRegistry interface.
 */
public class ParserRegistryTest {

    /**
     * Simple implementation of ParserRegistry for testing.
     */
    private static class TestParserRegistry implements ParserRegistry {
        private final String ontologyName;
        private final Map<String, Predicate> predicates = new HashMap<>();
        private final Map<String, Class<? extends Ject>> rootSubjects = new HashMap<>();

        public TestParserRegistry(String ontologyName) {
            this.ontologyName = ontologyName;
        }

        @Override
        public String getOntologyName() {
            return ontologyName;
        }

        @Override
        public void addRootSubject(String predicateName, Class<? extends Ject> rootSubjectType) {
            rootSubjects.put(predicateName, rootSubjectType);
        }

        @Override
        public void addPredicate(Predicate p) {
            predicates.put(p.name(), p);
        }

        @Override
        public void addPredicates(List<Predicate> predicatesList) {
            for (Predicate p : predicatesList) {
                addPredicate(p);
            }
        }

        @Override
        public void addPredicatesEnums(Class<Enum<?>> predicatesEnum) {
            // Simple implementation for testing
            // In a real implementation, this would use reflection to get enum values
        }

        @Override
        public Predicate getPredicate(String key, String ontologyName) {
            Predicate p = predicates.get(key);
            if (p == null) {
                // Create a RuntimePredicate if not found
                return new RuntimePredicate(key, "test", ontologyName);
            }
            return p;
        }
        
        @Override
        public Class<? extends Ject> getRootSubjectType(String predicateName) {
            return rootSubjects.get(predicateName);
        }

        // Helper methods for testing
        public Map<String, Predicate> getPredicates() {
            return predicates;
        }

        public Map<String, Class<? extends Ject>> getRootSubjects() {
            return rootSubjects;
        }
    }

    private TestParserRegistry registry;

    @BeforeEach
    public void setUp() {
        registry = new TestParserRegistry("test");
    }

    @Test
    public void testGetOntologyName() {
        assertEquals("test", registry.getOntologyName());
    }

    @Test
    public void testAddRootSubject() {
        // Add a root subject
        registry.addRootSubject("rootPredicate", RuntimeJect.class);
        
        // Verify it was added correctly
        Map<String, Class<? extends Ject>> rootSubjects = registry.getRootSubjects();
        assertEquals(1, rootSubjects.size());
        assertEquals(RuntimeJect.class, rootSubjects.get("rootPredicate"));
    }
    
    @Test
    public void testGetRootSubjectType() {
        // Add a root subject
        registry.addRootSubject("rootPredicate", RuntimeJect.class);
        
        // Get the root subject type
        Class<? extends Ject> type = registry.getRootSubjectType("rootPredicate");
        
        // Verify it's the correct type
        assertEquals(RuntimeJect.class, type);
        
        // Test getting a non-existent root subject type
        assertNull(registry.getRootSubjectType("nonExistentPredicate"));
    }

    @Test
    public void testAddPredicate() {
        // Create a test predicate
        TestPredicate predicate = new TestPredicate("testPredicate", "test", "base");
        
        // Add it to the registry
        registry.addPredicate(predicate);
        
        // Verify it was added correctly
        Map<String, Predicate> predicates = registry.getPredicates();
        assertEquals(1, predicates.size());
        assertSame(predicate, predicates.get("testPredicate"));
    }

    @Test
    public void testAddPredicates() {
        // Create test predicates
        TestPredicate p1 = new TestPredicate("predicate1", "test", "base");
        TestPredicate p2 = new TestPredicate("predicate2", "test", "base");
        
        // Add them to the registry
        registry.addPredicates(Arrays.asList(p1, p2));
        
        // Verify they were added correctly
        Map<String, Predicate> predicates = registry.getPredicates();
        assertEquals(2, predicates.size());
        assertSame(p1, predicates.get("predicate1"));
        assertSame(p2, predicates.get("predicate2"));
    }

    @Test
    public void testGetExistingPredicate() {
        // Create and add a test predicate
        TestPredicate predicate = new TestPredicate("existingPredicate", "test", "base");
        registry.addPredicate(predicate);
        
        // Get the predicate from the registry
        Predicate retrieved = registry.getPredicate("existingPredicate", "test");
        
        // Verify it's the same predicate we added
        assertSame(predicate, retrieved);
    }

    @Test
    public void testGetNonExistentPredicate() {
        // Try to get a predicate that doesn't exist
        Predicate created = registry.getPredicate("nonExistentPredicate", "test");
        
        // Verify a RuntimePredicate was created
        assertNotNull(created);
        assertTrue(created instanceof RuntimePredicate);
        assertEquals("nonExistentPredicate", created.name());
        assertEquals("test", created.ontology());
    }
    
    @Test
    public void testCanHandle() {
        // Test with matching ontology name
        assertTrue(registry.canHandle("test"));
        
        // Test with non-matching ontology name
        assertFalse(registry.canHandle("other"));
    }
    
    /**
     * Test predicate implementation for testing.
     */
    private static class TestPredicate implements Predicate {
        private final String name;
        private final String ontology;
        private final String space;

        public TestPredicate(String name, String ontology, String space) {
            this.name = name;
            this.ontology = ontology;
            this.space = space;
        }

        @Override
        public PredicateQualifier qualifier() {
            return null; // Not needed for these tests
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String space() {
            return space;
        }

        @Override
        public String ontology() {
            return ontology;
        }
    }
}