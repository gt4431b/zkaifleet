package bill.zkaifleet.parser ;

import io.quarkus.test.junit.QuarkusTest ;
import org.junit.jupiter.api.Test ;

import bill.zkaifleet.model.Ject ;
import bill.zkaifleet.model.Ontology ;
import bill.zkaifleet.model.Predicate ;
import bill.zkaifleet.model.RuntimeJect ;
import bill.zkaifleet.model.RuntimePredicate ;

import static org.junit.jupiter.api.Assertions.* ;
	
import java.util.Arrays ;
import java.util.List ;

@QuarkusTest
public class OntologyParserTest {

	@SuppressWarnings ( "unchecked" )
	@Test
	public void testParseSimpleYamlToJectTree() {
	    OntologyParser parser = new OntologyParser();
	    String yamlContent = SIMPLE_YAML;
	    Ject root = parser.parse(yamlContent);

	    assertInstanceOf(Ontology.class, root);
	    Ontology ontology = (Ontology) root;
	    assertEquals("test", ontology.getOntology());

	    // "jects" as relation (list of Jects)
	    List<Ject> jects = ontology.getTypedSubjects(createPred("jects"), Ject.class);
	    assertEquals(2, jects.size());

	    // j1
	    RuntimeJect j1 = (RuntimeJect) jects.get(0);
	    assertEquals("j1", j1.getId());
	    assertEquals("jects", j1.getTypeName());
	    assertEquals("value123", j1.getScalar(createPred("scalarProp"), Object.class));
	    assertEquals("unknown", j1.getScalar(createPred("type"), Object.class));
	    List<Object> listProp = (List<Object>) j1.getScalar(createPred("listProp"), List.class);
	    assertEquals(Arrays.asList("itemA", "itemB"), listProp);

	    // Ref to j2 as relation
	    List<Ject> refProp = j1.getTypedSubjects(createPred("refProp"), Ject.class);
	    assertEquals(1, refProp.size());
	    assertEquals("j2", refProp.get(0).getId());

	    // j2 with nested
	    RuntimeJect j2 = (RuntimeJect) jects.get(1);
	    assertEquals("j2", j2.getId());
	    List<Ject> nested = j2.getTypedSubjects(createPred("nestedJect"), Ject.class);
	    assertEquals(1, nested.size());
	    RuntimeJect j3 = (RuntimeJect) nested.get(0);
	    assertEquals("j3", j3.getId());
	    assertEquals("nestedValue", j3.getScalar(createPred("scalar"), Object.class));
	}

	private Predicate createPred(String name) {
        return new RuntimePredicate(name, "unknown", "test");
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
            """;
}
