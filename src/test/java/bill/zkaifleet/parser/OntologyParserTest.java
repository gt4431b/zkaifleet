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
        Ject root = parser.parse(SIMPLE_YAML);

        assertInstanceOf(Ontology.class, root);
        Ontology ontology = (Ontology) root;
        assertEquals("test", ontology.getOntologyName());

        List<Ject> roots = ontology.getRoots();
        assertEquals(2, roots.size());

        // j1
        RuntimeJect j1 = (RuntimeJect) roots.get(0);
        assertEquals("j1", j1.getId());
        assertEquals("unknown", j1.getTypeName());
        assertEquals("value123", j1.getScalar("scalarProp"));
        List<Object> listProp = (List<Object>) j1.getScalarAs("listProp", List.class);
        assertEquals(Arrays.asList("itemA", "itemB"), listProp);

        // Ref to j2
        List<Ject> refProp = j1.getTypedSubjects(createPred("refProp"), Ject.class);
        assertEquals(1, refProp.size());
        assertEquals("j2", refProp.get(0).getId());

        // j2 with nested
        RuntimeJect j2 = (RuntimeJect) roots.get(1);
        assertEquals("j2", j2.getId());
        List<Ject> nested = j2.getTypedSubjects(createPred("nestedJect"), Ject.class);
        assertEquals(1, nested.size());
        RuntimeJect j3 = (RuntimeJect) nested.get(0);
        assertEquals("j3", j3.getId());
        assertEquals("nestedValue", j3.getScalar("scalar"));

        // Bidirectional check
        List<Ject> j2Refs = refProp.get(0).getTypedIsObjectOf(createPred("refProp"), Ject.class);
        assertTrue(j2Refs.contains(j1));
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
