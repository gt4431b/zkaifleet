package bill.zkaifleet.parser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import bill.zkaifleet.model.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Test class focused on basic coverage for JectParseContext
 */
@QuarkusTest
public class JectParseContextBasicCoverageTest {

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
                if (name.equals("property")) {
                    return new TestPredicate(name, "String", ontology, null, String.class);
                } else if (name.equals("child")) {
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
    
    @Test
    public void testBasicNestedStructure() {
        // Create ontology
        Ontology ontology = new Ontology("test");
        
        // Create raw data with nested objects
        Map<String, Object> child = new HashMap<>();
        child.put("id", "child");
        child.put("property", "child value");
        
        Map<String, Object> parent = new HashMap<>();
        parent.put("id", "parent");
        parent.put("child", child);
        parent.put("property", "parent value");
        
        // Create JectParseContext and build the Jects
        JectParseContext context = new JectParseContext(ontology, "test", parent, ontologyCatalog);
        context.buildJects();
        
        // Debug: Print the entire structure
        System.out.println("Ontology ID: " + ontology.getId());
        System.out.println("Ontology Properties: " + ontology.getScalars());
        System.out.println("Ontology Subjects: " + ontology.getSubjects());
        
        // Get the id predicate from the test ontology, not using BasePredicate.id
        Predicate idPredicate = testRegistry.getPredicate("id", "test");
        
        // The JectParseContext should have stored the parent properties on the ontology
        // and created a child object relationship
        assertEquals("parent", ontology.getScalar(idPredicate, String.class), 
            "The ID from the parent map should be applied to the ontology using the test ontology's id predicate");
        assertEquals("parent value", ontology.getScalar(testRegistry.getPredicate("property", "test"), String.class),
            "The property from the parent map should be applied to the ontology");
        
        // Get child object through the parent-child relationship
        Predicate childPredicate = testRegistry.getPredicate("child", "test");
        List<Ject> children = ontology.getTypedSubjects(childPredicate, Ject.class);
        assertEquals(1, children.size(), "Should have one child");
        
        Ject childJect = children.get(0);
        // Use the same id predicate for the child
        assertEquals("child", childJect.getScalar(idPredicate, String.class), "Child ID should match");
        assertEquals("child value", childJect.getScalar(testRegistry.getPredicate("property", "test"), String.class),
            "Child property should match");
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