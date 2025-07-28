package bill.zkaifleet.parser ;

import io.quarkus.test.junit.QuarkusTest ;
import org.junit.jupiter.api.Test ;

import bill.zkaifleet.model.Ject ;

import static org.junit.jupiter.api.Assertions.assertEquals ;

@QuarkusTest
public class OntologyParserTest {

	@Test
	public void testParseSimpleYaml ( ) {
		// Red: Write failing test
		OntologyParser parser = new OntologyParser ( ) ;
		Ject result = parser.parse ( SIMPLE_YAML ) ;
		assertEquals ( "expected_id", result.getId ( ) ) ; // Initially fails
		// Green: Implement minimally to pass
		// Refactor: Clean up
	}

	private static final String SIMPLE_YAML = """
			id: expected_id
			name: Test Ject
			description: This is a test Ject.
			""";
}
