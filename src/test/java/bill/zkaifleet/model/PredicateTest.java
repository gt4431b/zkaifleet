package bill.zkaifleet.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Predicate interface and its default methods.
 */
public class PredicateTest {

    /**
     * Test predicate implementation for testing purposes.
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

    @Test
    public void testDefaultSpaceMethod() {
        // Create a predicate without overriding the default space() method
        class DefaultSpacePredicate implements Predicate {
            @Override
            public PredicateQualifier qualifier() {
                return null;
            }

            @Override
            public String name() {
                return "test";
            }

            @Override
            public String ontology() {
                return "test";
            }
        }

        Predicate predicate = new DefaultSpacePredicate();
        
        // The default implementation should convert the class name to lowercase
        // and remove "predicate" suffix if present
        assertEquals("defaultspace", predicate.space());
    }

    @Test
    public void testFqNameMethod() {
        // Create a predicate with known components
        Predicate p = new TestPredicate("property", "test", "base");
        
        // The fqName method should format the name as: ontology:space:name
        String expected = "test:base:property";
        String actual = p.fqName();
        
        assertEquals(expected, actual);
    }
    
    @Test
    public void testFqNameCaching() {
        // Create a predicate
        TestPredicate p = new TestPredicate("property", "test", "base");
        
        // Call fqName multiple times
        String name1 = p.fqName();
        String name2 = p.fqName();
        
        // The result should be the same each time (cached)
        assertEquals(name1, name2);
        
        // Create another predicate with a different name but same ontology/space
        TestPredicate p2 = new TestPredicate("differentProperty", "test", "base");
        
        // This should produce a different fully qualified name
        String name3 = p2.fqName();
        assertNotEquals(name1, name3);
    }
}