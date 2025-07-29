package bill.zkaifleet.parser ;

import io.quarkus.test.junit.QuarkusTest ;
import org.junit.jupiter.api.Test ;
import org.junit.jupiter.api.BeforeEach ;

import bill.zkaifleet.model.Ject ;
import bill.zkaifleet.model.Ontology ;
import bill.zkaifleet.model.Predicate ;
import bill.zkaifleet.model.RuntimeJect ;
import bill.zkaifleet.model.RuntimePredicate ;
import bill.zkaifleet.model.BaseParserRegistry ;
import bill.zkaifleet.model.ParserRegistry ;

import static org.junit.jupiter.api.Assertions.* ;
	
import java.util.Arrays ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

@QuarkusTest
public class OntologyParserTest {

	private OntologyParser parser ;
	
	@BeforeEach
	public void setup() {
		this.parser = new OntologyParser() ;
	}

	@Test
	public void testParseSimpleYamlToJectTree() {
	    String yamlContent = SIMPLE_YAML ;
	    Ject root = parser.parse(yamlContent) ;

	    assertInstanceOf(Ontology.class, root) ;
	    Ontology ontology = (Ontology) root ;
	    assertEquals("test", ontology.getOntology()) ;

	    // "jects" as relation (list of Jects)
	    List<Ject> jects = ontology.getTypedSubjects(createPred("jects"), Ject.class) ;
	    assertEquals(2, jects.size()) ;

	    // j1
	    RuntimeJect j1 = (RuntimeJect) jects.get(0) ;
	    assertEquals("j1", j1.getId()) ;
	    assertEquals("jects", j1.getTypeName()) ;
	    assertEquals("value123", j1.getScalar(createPred("scalarProp"), Object.class)) ;
	    assertEquals("unknown", j1.getScalar(createPred("type"), Object.class)) ;
	    List<String> listProp = j1.getScalars(createPred("listProp"), String.class) ;
	    assertEquals(Arrays.asList("itemA", "itemB"), listProp)	;

	    // Ref to j2 as relation
	    List<Ject> refProp = j1.getTypedSubjects(createPred("refProp"), Ject.class) ;
	    assertEquals(1, refProp.size()) ;
	    assertEquals("j2", refProp.get(0).getId()) ;

	    // j2 with nested
	    RuntimeJect j2 = (RuntimeJect) jects.get(1) ;
	    assertEquals("j2", j2.getId()) ;
	    List<Ject> nested = j2.getTypedSubjects(createPred("nestedJect"), Ject.class) ;
	    assertEquals(1, nested.size()) ;
	    RuntimeJect j3 = (RuntimeJect) nested.get(0) ;
	    assertEquals("j3", j3.getId()) ;
	    assertEquals("nestedValue", j3.getScalar(createPred("scalar"), Object.class)) ;
	}
	
	@Test
	public void testDuplicateIds() {
	    String yamlWithDuplicates = """
	            ontology: test
	            jects:
	              - id: duplicate
	                type: type1
	              - id: duplicate
	                type: type2
	            """ ;
	            
	    IllegalArgumentException exception = assertThrows(
	        IllegalArgumentException.class,
	        () -> parser.parse(yamlWithDuplicates)
	    ) ;
	    
	    assertTrue(exception.getMessage().contains("Duplicate IDs detected")) ;
	}
	
	@Test
	public void testCyclicReferences() {
	    String yamlWithCycle = """
	            ontology: test
	            jects:
	              - id: j1
	                type: type1
	                refToJ2: {ref: j2}
	              - id: j2
	                type: type2
	                refBackToJ1: {ref: j1}
	            """ ;
	            
	    // Should not throw exception but correctly handle cycles
	    Ject root = parser.parse(yamlWithCycle) ;
	    assertInstanceOf(Ontology.class, root) ;
	    
	    Ontology ontology = (Ontology) root ;
	    List<Ject> jects = ontology.getTypedSubjects(createPred("jects"), Ject.class) ;
	    assertEquals(2, jects.size()) ;
	    
	    RuntimeJect j1 = (RuntimeJect) jects.stream()
	        .filter(j -> "j1".equals(j.getId()))
	        .findFirst()
	        .orElse(null) ;
	    
	    RuntimeJect j2 = (RuntimeJect) jects.stream()
	        .filter(j -> "j2".equals(j.getId()))
	        .findFirst()
	        .orElse(null) ;
	        
	    assertNotNull(j1) ;
	    assertNotNull(j2) ;
	    
	    // Check j1 -> j2 reference
	    List<Ject> refToJ2 = j1.getTypedSubjects(createPred("refToJ2"), Ject.class) ;
	    assertEquals(1, refToJ2.size()) ;
	    assertEquals("j2", refToJ2.get(0).getId()) ;
	    
	    // Check j2 -> j1 reference
	    List<Ject> refBackToJ1 = j2.getTypedSubjects(createPred("refBackToJ1"), Ject.class) ;
	    assertEquals(1, refBackToJ1.size()) ;
	    assertEquals("j1", refBackToJ1.get(0).getId()) ;
	}
	
	@Test
	public void testUnresolvedReference() {
	    String yamlWithUnresolvedRef = """
	            ontology: test
	            jects:
	              - id: j1
	                type: type1
	                refToNonExistent: {ref: nonExistent}
	            """ ;
	            
	    IllegalStateException exception = assertThrows(
	        IllegalStateException.class,
	        () -> parser.parse(yamlWithUnresolvedRef)
	    ) ;
	    
	    assertTrue(exception.getMessage().contains("Unresolved placeholder")) ;
	}
	
	@Test
	public void testMissingOntology() {
	    String yamlWithoutOntology = """
	            id: root
	            jects:
	              - id: j1
	                type: type1
	            """ ;
	            
	    IllegalArgumentException exception = assertThrows(
	        IllegalArgumentException.class,
	        () -> parser.parse(yamlWithoutOntology)
	    ) ;
	    
	    assertTrue(exception.getMessage().contains("Ontology name is required")) ;
	}
	
	@Test
	public void testMalformedYaml() {
	    String malformedYaml = """
	            ontology: test
	            jects:
	              - id: j1
	                type: type1
	              : malformed
	            """ ;
	            
	    assertThrows(
	        Exception.class,
	        () -> parser.parse(malformedYaml)
	    ) ;
	}
	
	@Test
	public void testRegisterCustomRegistry() {
	    // Create a custom parser registry for testing
	    Map<String, ParserRegistry> customCatalog = new HashMap<>() ;
	    ParserRegistry testRegistry = new BaseParserRegistry() ;
	    customCatalog.put("test", testRegistry) ;
	    
	    // Create parser with custom registry
	    OntologyParser customParser = new OntologyParser(customCatalog) ;
	    
	    // Register an additional registry
	    ParserRegistry additionalRegistry = new BaseParserRegistry() {
	        @Override
	        public String getOntologyName() {
	            return "additional" ;
	        }
	    } ;
	    customParser.registerRegistry(additionalRegistry) ;
	    
	    // Test with YAML that uses the test ontology
	    String yaml = """
	            ontology: test
	            id: testId
	            name: Test Ontology
	            """ ;
	            
	    Ject result = customParser.parse(yaml) ;
	    assertNotNull(result) ;
	    assertEquals("testId", result.getId()) ;
	}
	
	@Test
	public void testComplexNestedStructures() {
	    String complexYaml = """
	            ontology: test
	            id: complex
	            jects:
	              - id: parent1
	                type: parent
	                children:
	                  - id: child1
	                    type: child
	                    grandchildren:
	                      - id: grandchild1
	                        type: grandchild
	                        value: nestedValue1
	                      - id: grandchild2
	                        type: grandchild
	                        value: nestedValue2
	                  - id: child2
	                    type: child
	                    refToParent: {ref: parent1}
	              - id: parent2
	                type: parent
	                refToChild: {ref: child1}
	            """ ;
	            
	    Ject root = parser.parse(complexYaml) ;
	    assertInstanceOf(Ontology.class, root) ;
	    
	    Ontology ontology = (Ontology) root ;
	    assertEquals("complex", ontology.getId()) ;
	    
	    // Check first level structure
	    List<Ject> jects = ontology.getTypedSubjects(createPred("jects"), Ject.class) ;
	    assertEquals(2, jects.size()) ;
	    
	    // Find parent1
	    RuntimeJect parent1 = (RuntimeJect) jects.stream()
	        .filter(j -> "parent1".equals(j.getId()))
	        .findFirst()
	        .orElse(null) ;
	    assertNotNull(parent1) ;
	    
	    // Check children of parent1
	    List<Ject> children = parent1.getTypedSubjects(createPred("children"), Ject.class) ;
	    assertEquals(2, children.size()) ;
	    
	    // Find child1
	    RuntimeJect child1 = (RuntimeJect) children.stream()
	        .filter(j -> "child1".equals(j.getId()))
	        .findFirst()
	        .orElse(null) ;
	    assertNotNull(child1) ;
	    
	    // Check grandchildren of child1
	    List<Ject> grandchildren = child1.getTypedSubjects(createPred("grandchildren"), Ject.class) ;
	    assertEquals(2, grandchildren.size()) ;
	    
	    // Check grandchild values
	    Map<String, String> grandchildValues = new HashMap<>() ;
	    for (Ject gc : grandchildren) {
	        grandchildValues.put(gc.getId(), ((RuntimeJect)gc).getScalar(createPred("value"), String.class)) ;
	    }
	    
	    assertEquals("nestedValue1", grandchildValues.get("grandchild1")) ;
	    assertEquals("nestedValue2", grandchildValues.get("grandchild2")) ;
	    
	    // Check circular reference from child2 back to parent1
	    RuntimeJect child2 = (RuntimeJect) children.stream()
	        .filter(j -> "child2".equals(j.getId()))
	        .findFirst()
	        .orElse(null) ;
	    assertNotNull(child2) ;
	    
	    List<Ject> refToParent = child2.getTypedSubjects(createPred("refToParent"), Ject.class) ;
	    assertEquals(1, refToParent.size()) ;
	    assertEquals("parent1", refToParent.get(0).getId()) ;
	    
	    // Check parent2's reference to child1
	    RuntimeJect parent2 = (RuntimeJect) jects.stream()
	        .filter(j -> "parent2".equals(j.getId()))
	        .findFirst()
	        .orElse(null) ;
	    assertNotNull(parent2) ;
	    
	    List<Ject> refToChild = parent2.getTypedSubjects(createPred("refToChild"), Ject.class) ;
	    assertEquals(1, refToChild.size()) ;
	    assertEquals("child1", refToChild.get(0).getId()) ;
	}
	
	@Test
	public void testListsWithMixedContent() {
	    String yamlWithMixedLists = """
	            ontology: test
	            id: mixedLists
	            mixedList:
	              - stringItem
	              - 42
	              - true
	              - {id: nestedObject, value: objectValue}
	            objectsList:
	              - id: obj1
	                value: val1
	              - id: obj2
	                value: val2
	            """ ;
	            
	    // Mixed content should be rejected with an appropriate exception
	    IllegalStateException exception = assertThrows(
	        IllegalStateException.class,
	        () -> parser.parse(yamlWithMixedLists)
	    );
	    
	    // Verify the exception contains information about mixed content
	    assertTrue(exception.getMessage().contains("Mixed content detected"), 
	               "Exception should mention mixed content");
	    assertTrue(exception.getMessage().contains("mixedList"), 
	               "Exception should identify the problematic predicate");
	}
	
	@Test
	public void testHomogeneousLists() {
	    // Test with lists that are properly homogeneous (all scalars or all objects)
	    String yamlWithValidLists = """
	            ontology: test
	            id: validLists
	            scalarList:
	              - stringItem
	              - 42
	              - true
	            objectsList:
	              - id: obj1
	                value: val1
	              - id: obj2
	                value: val2
	            """ ;
	            
	    Ject root = parser.parse(yamlWithValidLists);
	    assertInstanceOf(Ontology.class, root);
	    
	    Ontology ontology = (Ontology) root;
	    assertEquals("validLists", ontology.getId());
	    
	    // Check scalar list - should be properly handled
	    List<Object> scalarList = ontology.getScalars(createPred("scalarList"), Object.class);
	    assertEquals(3, scalarList.size());
	    assertEquals("stringItem", scalarList.get(0));
	    assertEquals(42, scalarList.get(1));
	    assertEquals(true, scalarList.get(2));
	    
	    // Check objects list - should be converted to Ject objects
	    List<Ject> objectsList = ontology.getTypedSubjects(createPred("objectsList"), Ject.class);
	    assertEquals(2, objectsList.size());
	    
	    Map<String, String> values = new HashMap<>();
	    for (Ject obj : objectsList) {
	        values.put(obj.getId(), ((RuntimeJect)obj).getScalar(createPred("value"), String.class));
	    }
	    
	    assertEquals("val1", values.get("obj1"));
	    assertEquals("val2", values.get("obj2"));
	}
	
	@Test
	public void testValidateAnomalies() {
	    // Create a Ject without ID and validate
	    RuntimeJect jectWithoutId = new RuntimeJect("testType", "testOntology") ;
	    
	    IllegalStateException exception = assertThrows(
	        IllegalStateException.class,
	        () -> parser.validateAnomalies(jectWithoutId)
	    ) ;
	    
	    assertTrue(exception.getMessage().contains("missing id")) ;
	    
	    // An Ontology without ID should not throw exception
	    Ontology ontology = new Ontology("testOntology") ;
	    assertDoesNotThrow(() -> parser.validateAnomalies(ontology)) ;
	    
	    // A Ject with ID should not throw exception
	    RuntimeJect jectWithId = new RuntimeJect("testType", "testOntology") ;
	    jectWithId.setId("testId") ;
	    assertDoesNotThrow(() -> parser.validateAnomalies(jectWithId)) ;
	}

	private Predicate createPred(String name) {
        return new RuntimePredicate(name, "unknown", "test") ;
    }

    private static final String SIMPLE_YAML = """
            ontology: test
            jects:
              - id: j1
                type: unknown
                scalarProp: value123
                refProp: {ref: j2}
                listProp: [itemA, itemB]
              - id: j2
                type: unknown2
                nestedJect:
                  id: j3
                  type: nested
                  scalar: nestedValue
            """ ;
}
