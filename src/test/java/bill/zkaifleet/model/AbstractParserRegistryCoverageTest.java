package bill.zkaifleet.model;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

/**
 * Coverage tests for AbstractParserRegistry to improve code coverage
 * and test edge cases.
 */
@QuarkusTest
public class AbstractParserRegistryCoverageTest {

    // Test implementation of AbstractParserRegistry
    private static class TestRegistry extends AbstractParserRegistry {
        public TestRegistry(String ontologyName) {
            super(ontologyName);
        }
    }
    
    // Test implementation of a Predicate for testing
    private static class TestPredicate implements Predicate {
        private final String name;
        private final PredicateQualifier qualifier;
        
        public TestPredicate(String name, PredicateQualifier qualifier) {
            this.name = name;
            this.qualifier = qualifier;
        }
        
        @Override
        public String name() {
            return name;
        }
        
        @Override
        public String space() {
            return "test";
        }
        
        @Override
        public String ontology() {
            return "test";
        }
        
        @Override
        public PredicateQualifier qualifier() {
            return qualifier;
        }
    }
    
    private AbstractParserRegistry registry;
    
    @BeforeEach
    public void setup() {
        registry = new TestRegistry("testOntology");
    }
    
    /**
     * Test adding and retrieving root subjects.
     */
    @Test
    public void testRootSubjects() {
        // Add root subjects
        registry.addRootSubject("testPredicate", RuntimeJect.class);
        assertEquals(RuntimeJect.class, registry.getRootSubjectType("testPredicate"));
        
        // Test with null or empty predicate name
        registry.addRootSubject(null, RuntimeJect.class);
        registry.addRootSubject("", RuntimeJect.class);
        assertNull(registry.getRootSubjectType(null));
        assertNull(registry.getRootSubjectType(""));
        
        // Test with null class
        registry.addRootSubject("nullClass", null);
        assertNull(registry.getRootSubjectType("nullClass"));
    }
    
    /**
     * Test adding and retrieving predicates.
     */
    @Test
    public void testPredicates() {
        // Create a test predicate without a qualifier
        Predicate simplePredicate = new TestPredicate("simple", null);
        registry.addPredicate(simplePredicate);
        
        // Verify retrieval
        assertEquals(simplePredicate, registry.getPredicate("simple", "testOntology"));
        
        // Test with null predicate
        registry.addPredicate(null);
        
        // Create a predicate with a qualifier
        PredicateQualifier qualifier = new PredicateQualifier(
            false, false, "simplePredicates", null, null, null, null);
        Predicate predicateWithQualifier = new TestPredicate("complexPredicate", qualifier);
        
        registry.addPredicate(predicateWithQualifier);
        
        // Verify retrieval by both name and plural name
        assertEquals(predicateWithQualifier, registry.getPredicate("complexPredicate", "testOntology"));
        assertEquals(predicateWithQualifier, registry.getPredicate("simplePredicates", "testOntology"));
        
        // Add duplicate predicate - should not overwrite
        Predicate duplicatePredicate = new TestPredicate("simple", null);
        registry.addPredicate(duplicatePredicate);
        
        // Original should still be retrieved
        assertSame(simplePredicate, registry.getPredicate("simple", "testOntology"));
    }
    
    /**
     * Test adding predicates from a list.
     */
    @Test
    public void testAddPredicatesFromList() {
        // Create test predicates
        Predicate predicate1 = new TestPredicate("pred1", null);
        Predicate predicate2 = new TestPredicate("pred2", null);
        
        // Add using list method
        registry.addPredicates(Arrays.asList(predicate1, predicate2));
        
        // Verify both were added
        assertEquals(predicate1, registry.getPredicate("pred1", "testOntology"));
        assertEquals(predicate2, registry.getPredicate("pred2", "testOntology"));
        
        // Test with null list
        registry.addPredicates(null);
        
        // Test with list containing null
        registry.addPredicates(Arrays.asList(new TestPredicate("pred3", null), null));
        assertNotNull(registry.getPredicate("pred3", "testOntology"));
    }
    
    /**
     * Test adding predicates from an enum.
     */
    @SuppressWarnings ( {
			"unchecked", "rawtypes"
	} )
	@Test
    public void testAddPredicatesFromEnum() {
        // Test with a non-Predicate enum
        registry.addPredicatesEnums((Class) TestEnum.class);
        
        // Nothing should be added since TestEnum doesn't implement Predicate
        assertNull(registry.getPredicate("VALUE1", "testOntology"));
        
        // Test with a Predicate enum
        registry.addPredicatesEnums((Class) TestPredicateEnum.class);
        
        // Should add both enum values as predicates
        assertNotNull(registry.getPredicate("ENUM_PRED1", "testOntology"));
        assertNotNull(registry.getPredicate("ENUM_PRED2", "testOntology"));
        
        // Test with null
        registry.addPredicatesEnums(null);
    }
    
    // Regular enum for testing (doesn't implement Predicate)
    private enum TestEnum {
        VALUE1, VALUE2
    }
    
    // Enum implementing Predicate for testing
    private enum TestPredicateEnum implements Predicate {
        ENUM_PRED1, ENUM_PRED2;
        
        @Override
        public String space() {
            return "test";
        }
        
        @Override
        public String ontology() {
            return "test";
        }
        
        @Override
        public PredicateQualifier qualifier() {
            return null;
        }
    }
}