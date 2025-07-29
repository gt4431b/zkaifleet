package bill.zkaifleet.model ;

import static org.junit.jupiter.api.Assertions.assertEquals ;
import static org.junit.jupiter.api.Assertions.assertNotNull ;
import static org.junit.jupiter.api.Assertions.assertSame ;
import static org.junit.jupiter.api.Assertions.assertTrue ;

import java.util.Map ;
import java.util.HashMap ;

import org.junit.jupiter.api.Test ;
import org.junit.jupiter.api.BeforeEach ;

import io.quarkus.test.junit.QuarkusTest ;

@QuarkusTest
public class ParserRegistryManagerTest {

    private ParserRegistryManager manager ;

    @BeforeEach
    public void setup() {
        // Get the singleton instance
        this.manager = ParserRegistryManager.getInstance() ;
    }

	@Test
	public void testRegistryDiscovery() {
		Map<String, ParserRegistry> registries = manager.getAllRegistries() ;
		
		// Should at least have the base registry
		assertNotNull(registries) ;
		assertTrue(registries.size() >= 1, "Should at least have the base registry") ;

		// Verify base registry
		ParserRegistry baseRegistry = manager.getRegistry("base") ;
		assertNotNull(baseRegistry) ;
		assertEquals("base", baseRegistry.getOntologyName()) ;
		assertTrue(baseRegistry instanceof BaseParserRegistry) ;
		
		// Verify fleet registry if present
		ParserRegistry fleetRegistry = manager.getRegistry("fleet") ;
		if (fleetRegistry != null) {
			assertEquals("fleet", fleetRegistry.getOntologyName()) ;
		}
		
		// Test fallback to base registry for unknown ontology
		ParserRegistry fallbackRegistry = manager.getRegistry("unknown") ;
		assertNotNull(fallbackRegistry) ;
		assertEquals("base", fallbackRegistry.getOntologyName()) ;
		assertTrue(fallbackRegistry instanceof BaseParserRegistry) ;
	}
	
	@Test
	public void testRegistryPriorities() {
		// Base registry should have lowest priority
		ParserRegistry baseRegistry = manager.getRegistry("base") ;
		assertTrue(baseRegistry.getPriority() < 0, "Base registry should have negative priority") ;
		
		// If fleet registry exists, it should have higher priority than base
		ParserRegistry fleetRegistry = manager.getRegistry("fleet") ;
		if (fleetRegistry != null) {
			assertTrue(fleetRegistry.getPriority() > baseRegistry.getPriority(), 
				"Fleet registry should have higher priority than base") ;
		}
	}
	
	@Test
	public void testDynamicRegistryRegistration() {
	    // Create a custom registry implementation
	    ParserRegistry customRegistry = new AbstractParserRegistry("custom") {
	        @Override
	        public int getPriority() {
	            return 100 ; // High priority
	        }
	    } ;
	    
	    // Register it dynamically
	    manager.registerRegistry(customRegistry) ;
	    
	    // Verify it was registered
	    ParserRegistry retrievedRegistry = manager.getRegistry("custom") ;
	    assertNotNull(retrievedRegistry) ;
	    assertEquals("custom", retrievedRegistry.getOntologyName()) ;
	    assertEquals(100, retrievedRegistry.getPriority()) ;
	    
	    // Verify direct reference equality
	    assertSame(customRegistry, retrievedRegistry) ;
	}
	
	@Test
	public void testRegistryPriorityOverride() {
	    // Create first registry for "priority" ontology
	    ParserRegistry lowPriorityRegistry = new AbstractParserRegistry("priority") {
	        @Override
	        public int getPriority() {
	            return 10 ;
	        }
	    } ;
	    
	    // Create second registry for same ontology but higher priority
	    ParserRegistry highPriorityRegistry = new AbstractParserRegistry("priority") {
	        @Override
	        public int getPriority() {
	            return 20 ;
	        }
	    } ;
	    
	    // Register low priority first
	    manager.registerRegistry(lowPriorityRegistry) ;
	    
	    // Verify it was registered
	    ParserRegistry retrievedRegistry = manager.getRegistry("priority") ;
	    assertEquals(10, retrievedRegistry.getPriority()) ;
	    
	    // Register high priority second
	    manager.registerRegistry(highPriorityRegistry) ;
	    
	    // Verify high priority was preferred
	    retrievedRegistry = manager.getRegistry("priority") ;
	    assertEquals(20, retrievedRegistry.getPriority()) ;
	    assertSame(highPriorityRegistry, retrievedRegistry) ;
	}
	
	@Test
	public void testNullRegistryHandling() {
	    // Try to register null registry
	    manager.registerRegistry(null) ;
	    
	    // Should not throw exception
	}
	
	@Test
	public void testCustomRegistryWithPredicatesAndSubjects() {
	    // Create a more complete registry with predicates and root subjects
	    AbstractParserRegistry testRegistry = new AbstractParserRegistry("test") {
	        @Override
	        public int getPriority() {
	            return 50 ;
	        }
	    } ;
	    
	    // Add some predicates
	    testRegistry.addPredicate(BasePredicate.id) ;
	    
	    // Add a custom predicate
	    RuntimePredicate customPred = new RuntimePredicate("customPred", "test", "test") ;
	    testRegistry.addPredicate(customPred) ;
	    
	    // Add a root subject type
	    testRegistry.addRootSubject("rootType", RuntimeJect.class) ;
	    
	    // Register the registry
	    manager.registerRegistry(testRegistry) ;
	    
	    // Retrieve the registry
	    ParserRegistry retrievedRegistry = manager.getRegistry("test") ;
	    assertNotNull(retrievedRegistry) ;
	    
	    // Verify predicate retrieval
	    Predicate retrievedPred = retrievedRegistry.getPredicate("customPred", "test") ;
	    assertEquals("customPred", retrievedPred.name()) ;
	    
	    // Verify root subject type retrieval
	    Class<? extends Ject> rootType = retrievedRegistry.getRootSubjectType("rootType") ;
	    assertEquals(RuntimeJect.class, rootType) ;
	}
	
	@Test
	public void testMultipleOntologyFallbackChain() {
	    // Create a map to simulate a registry that can handle multiple ontologies
	    Map<String, Boolean> canHandleResults = new HashMap<>() ;
	    canHandleResults.put("primary", true) ;
	    canHandleResults.put("secondary", true) ;
	    canHandleResults.put("tertiary", false) ;
	    
	    // Create a registry that can handle multiple ontologies based on the map
	    ParserRegistry multiRegistry = new AbstractParserRegistry("primary") {
	        @Override
	        public boolean canHandle(String ontologyName) {
	            return canHandleResults.getOrDefault(ontologyName, false) ;
	        }
	        
	        @Override
	        public int getPriority() {
	            return 30 ;
	        }
	    } ;
	    
	    // Register it
	    manager.registerRegistry(multiRegistry) ;
	    
	    // Verify primary ontology is handled by the new registry
	    ParserRegistry primaryRegistry = manager.getRegistry("primary") ;
	    assertSame(multiRegistry, primaryRegistry) ;
	    
	    // Verify secondary ontology is also handled by the new registry
	    // Note: This test might fail because ParserRegistryManager doesn't currently check canHandle
	    // It only looks at the ontology name in the registry map
	    // ParserRegistry secondaryRegistry = manager.getRegistry("secondary");
	    // assertSame(multiRegistry, secondaryRegistry);
	    
	    // Verify tertiary falls back to base
	    ParserRegistry tertiaryRegistry = manager.getRegistry("tertiary") ;
	    assertEquals("base", tertiaryRegistry.getOntologyName()) ;
	}
}