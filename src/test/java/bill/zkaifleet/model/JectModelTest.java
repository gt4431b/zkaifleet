package bill.zkaifleet.model ;

import static org.junit.jupiter.api.Assertions.* ;

import java.util.Arrays ;
import java.util.List ;

import org.junit.jupiter.api.Test ;

import io.quarkus.test.junit.QuarkusTest ;

/**
 * Test class for the Ject model classes.
 * <p>
 * These tests focus on the core functionality of the Ject hierarchy,
 * including relationship management, scalar properties, and literals handling.
 */
@QuarkusTest
public class JectModelTest {

    @Test
    public void testBasicJectProperties() {
        // Create a test Ject
        RuntimeJect ject = new RuntimeJect("testType", "testOntology") ;
        ject.setId("test1") ;
        ject.setDescription("Test description") ;
        ject.setEvolutionNotes("Test evolution notes") ;
        
        // Verify basic properties
        assertEquals("test1", ject.getId()) ;
        assertEquals("testType", ject.getTypeName()) ;
        assertEquals("testOntology", ject.getOntology()) ;
        assertEquals("Test description", ject.getDescription()) ;
        assertEquals("Test evolution notes", ject.getEvolutionNotes()) ;
        
        // Test toString method
        String toStr = ject.toString() ;
        assertTrue(toStr.contains("id=test1")) ;
        assertTrue(toStr.contains("typeName=testType")) ;
        assertTrue(toStr.contains("ontology=testOntology")) ;
    }
    
    @Test
    public void testSubjectRelationships() {
        // Create jects for relationship testing
        RuntimeJect parent = new RuntimeJect("parent", "testOntology") ;
        parent.setId("parent1") ;
        
        RuntimeJect child1 = new RuntimeJect("child", "testOntology") ;
        child1.setId("child1") ;
        
        RuntimeJect child2 = new RuntimeJect("child", "testOntology") ;
        child2.setId("child2") ;
        
        // Create test predicate
        Predicate childrenPred = new RuntimePredicate("children", "test", "testOntology") ;
        
        // Add children to parent
        parent.addTypedSubject(childrenPred, child1) ;
        parent.addTypedSubject(childrenPred, child2) ;
        
        // Verify parent-child relationships
        List<Ject> children = parent.getTypedSubjects(childrenPred, Ject.class) ;
        assertEquals(2, children.size()) ;
        assertTrue(children.contains(child1)) ;
        assertTrue(children.contains(child2)) ;
        
        // Verify child-parent relationships (isObjectOf)
        List<Ject> parents1 = child1.getTypedIsObjectOf(childrenPred, Ject.class) ;
        assertEquals(1, parents1.size()) ;
        assertEquals("parent1", parents1.get(0).getId()) ;
        
        List<Ject> parents2 = child2.getTypedIsObjectOf(childrenPred, Ject.class) ;
        assertEquals(1, parents2.size()) ;
        assertEquals("parent1", parents2.get(0).getId()) ;
        
        // Test getSingleTypedSubject
        Ject singleChild = parent.getSingleTypedSubject(childrenPred, Ject.class) ;
        assertNotNull(singleChild) ;
        assertTrue(singleChild.getId().equals("child1") || singleChild.getId().equals("child2")) ;
        
        // Test removing subjects
        parent.removeTypedSubjects(childrenPred) ;
        List<Ject> childrenAfterRemove = parent.getTypedSubjects(childrenPred, Ject.class) ;
        assertTrue(childrenAfterRemove.isEmpty()) ;
        
        // Verify back-references were removed too
        List<Ject> parentsAfterRemove1 = child1.getTypedIsObjectOf(childrenPred, Ject.class) ;
        assertTrue(parentsAfterRemove1.isEmpty()) ;
        
        List<Ject> parentsAfterRemove2 = child2.getTypedIsObjectOf(childrenPred, Ject.class) ;
        assertTrue(parentsAfterRemove2.isEmpty()) ;
    }
    
    @Test
    public void testScalarProperties() {
        // Create a test Ject
        RuntimeJect ject = new RuntimeJect("testType", "testOntology") ;
        
        // Create test predicates
        Predicate stringPred = new RuntimePredicate("stringProp", "test", "testOntology") ;
        Predicate intPred = new RuntimePredicate("intProp", "test", "testOntology") ;
        Predicate listPred = new RuntimePredicate("listProp", "test", "testOntology") ;
        
        // Add scalar values
        ject.addScalar(stringPred, "test string") ;
        ject.addScalar(intPred, 42) ;
        
        // Add list of scalar values
        List<Object> scalarList = Arrays.asList("item1", "item2", "item3") ;
        ject.setScalars(listPred, scalarList) ;
        
        // Verify scalar values
        assertEquals("test string", ject.getScalar(stringPred, String.class)) ;
        assertEquals(Integer.valueOf(42), ject.getScalar(intPred, Integer.class)) ;
        
        // Verify scalar list
        List<String> retrievedList = ject.getScalars(listPred, String.class) ;
        assertEquals(3, retrievedList.size()) ;
        assertEquals("item1", retrievedList.get(0)) ;
        assertEquals("item2", retrievedList.get(1)) ;
        assertEquals("item3", retrievedList.get(2)) ;
        
        // Test type mismatch exception
        assertThrows(IllegalStateException.class, () -> ject.getScalar(stringPred, Integer.class)) ;
        
        // Test multiple scalars exception
        Predicate multiPred = new RuntimePredicate("multiProp", "test", "testOntology") ;
        ject.addScalar(multiPred, "value1") ;
        ject.addScalar(multiPred, "value2") ;
        assertThrows(IllegalStateException.class, () -> ject.getScalar(multiPred, String.class)) ;
        
        // Test removing scalar values
        ject.setScalars(stringPred, null) ;
        assertNull(ject.getScalar(stringPred, String.class)) ;
        
        // Test empty list handling
        ject.setScalars(listPred, List.of()) ;
        List<String> emptyList = ject.getScalars(listPred, String.class) ;
        assertTrue(emptyList.isEmpty()) ;
    }
    
    @Test
    public void testLiteralsHandling() {
        // Create a RuntimeJect with only a literal value
        RuntimeJect literalJect = new RuntimeJect("literal", "testOntology") ;
        literalJect.addScalar(BasePredicate.literal, "test literal value") ;
        
        // Test resolveLiterals
        Object resolved = literalJect.resolveLiterals() ;
        assertEquals("test literal value", resolved) ;
        
        // Create a RuntimeJect with literal and other properties
        RuntimeJect mixedJect = new RuntimeJect("mixed", "testOntology") ;
        mixedJect.addScalar(BasePredicate.literal, "mixed literal") ;
        mixedJect.addScalar(new RuntimePredicate("otherProp", "test", "testOntology"), "other value") ;
        
        // Should not resolve to literal when there are other properties
        Object mixedResolved = mixedJect.resolveLiterals() ;
        assertSame(mixedJect, mixedResolved) ;
        
        // Create a RuntimeJect with literal and subjects
        RuntimeJect subjectJect = new RuntimeJect("withSubject", "testOntology") ;
        subjectJect.addScalar(BasePredicate.literal, "subject literal") ;
        subjectJect.addTypedSubject(
            new RuntimePredicate("child", "test", "testOntology"), 
            new RuntimeJect("child", "testOntology")
        ) ;
        
        // Should not resolve to literal when there are subjects
        Object subjectResolved = subjectJect.resolveLiterals() ;
        assertSame(subjectJect, subjectResolved) ;
    }
    
    @Test
    public void testRuntimeJectSpecificMethods() {
        // Test RuntimeJect.getLiteral() method
        RuntimeJect literalJect = new RuntimeJect("literal", "testOntology") ;
        literalJect.addScalar(BasePredicate.literal, "test literal value") ;
        
        // Verify getLiteral()
        assertEquals("test literal value", literalJect.getLiteral()) ;
        
        // Test RuntimeJect.addRuntimeSubject() method
        RuntimeJect parent = new RuntimeJect("parent", "testOntology") ;
        RuntimeJect child = new RuntimeJect("child", "testOntology") ;
        child.setId("childId") ;
        
        // Use fluent API pattern
        RuntimeJect result = parent.addRuntimeSubject("dynamicChild", child) ;
        
        // Verify fluent return
        assertSame(parent, result) ;
        
        // Verify subject was added
        List<Ject> children = parent.getTypedSubjects(
            new RuntimePredicate("dynamicChild", "runtime", "testOntology"), 
            Ject.class
        ) ;
        
        assertEquals(1, children.size()) ;
        assertEquals("childId", children.get(0).getId()) ;
    }
    
    @Test
    public void testTypeMismatchHandling() {
        // Create objects of different types
        RuntimeJect parent = new RuntimeJect("parent", "testOntology") ;
        Ontology ontology = new Ontology("testOntology") ;
        
        // Add the ontology as a child of parent
        Predicate childPred = new RuntimePredicate("child", "test", "testOntology") ;
        parent.addTypedSubject(childPred, ontology) ;
        
        // Test type mismatch when requesting wrong type
        List<RuntimeJect> typedChildren = parent.getTypedSubjects(childPred, RuntimeJect.class) ;
        assertEquals(0, typedChildren.size()) ;
        
        // Same for isObjectOf relationships
        List<RuntimeJect> typedParents = ontology.getTypedIsObjectOf(childPred, RuntimeJect.class) ;
        assertEquals(1, typedParents.size()) ; // Should successfully cast to RuntimeJect
    }
    
    @Test
    public void testSetSingleTypedSubject() {
        RuntimeJect parent = new RuntimeJect("parent", "testOntology") ;
        RuntimeJect child1 = new RuntimeJect("child", "testOntology") ;
        child1.setId("child1") ;
        RuntimeJect child2 = new RuntimeJect("child", "testOntology") ;
        child2.setId("child2") ;
        
        Predicate childPred = new RuntimePredicate("child", "test", "testOntology") ;
        
        // Set first child
        parent.setSingleTypedSubject(childPred, child1) ;
        
        // Verify first child was set
        Ject retrievedChild = parent.getSingleTypedSubject(childPred, Ject.class) ;
        assertEquals("child1", retrievedChild.getId()) ;
        
        // Set second child (should replace first)
        parent.setSingleTypedSubject(childPred, child2) ;
        
        // Verify second child replaced first
        retrievedChild = parent.getSingleTypedSubject(childPred, Ject.class) ;
        assertEquals("child2", retrievedChild.getId()) ;
        
        // Set to null (should remove relationship)
        parent.setSingleTypedSubject(childPred, null) ;
        
        // Verify relationship was removed
        assertNull(parent.getSingleTypedSubject(childPred, Ject.class)) ;
    }
}