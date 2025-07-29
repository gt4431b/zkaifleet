package bill.zkaifleet.parser ;

import java.util.HashMap ;
import java.util.HashSet ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.Set ;
import java.util.UUID ;

import bill.zkaifleet.model.BasePredicate ;
import bill.zkaifleet.model.Ject ;
import bill.zkaifleet.model.Ontology ;
import bill.zkaifleet.model.ParserRegistry ;
import bill.zkaifleet.model.ParserRegistryManager ;
import lombok.extern.slf4j.Slf4j ;
import org.yaml.snakeyaml.Yaml ;

/**
 * Parser for YAML-based ontology specifications that creates a graph of Ject objects.
 * <p>
 * The parser works in three passes:
 * <ol>
 *   <li>Detect duplicates - ensures all IDs in the YAML are unique</li>
 *   <li>Build placeholders - creates the object structure with placeholders for references</li>
 *   <li>Resolve references - converts placeholders to actual object references</li>
 * </ol>
 */
@Slf4j
public class OntologyParser {

	private final Yaml yaml = new Yaml() ;
	private final Map<String, ParserRegistry> ontologyCatalog ;

	/**
	 * Constructor initializes the parser with registries from the ParserRegistryManager.
	 */
	public OntologyParser() {
		log.debug("Initializing OntologyParser with ParserRegistryManager") ;
		ParserRegistryManager registryManager = ParserRegistryManager.getInstance() ;
		this.ontologyCatalog = new HashMap<>(registryManager.getAllRegistries()) ;
		log.debug("Loaded {} parser registries", ontologyCatalog.size()) ;
	}
	
	/**
	 * Constructor with explicit registry catalog for testing.
	 *
	 * @param ontologyCatalog Map of ontology names to parser registries
	 */
	public OntologyParser(Map<String, ParserRegistry> ontologyCatalog) {
		log.debug("Initializing OntologyParser with custom registry catalog") ;
		this.ontologyCatalog = ontologyCatalog ;
	}
	
	/**
	 * Registers an additional parser registry.
	 *
	 * @param registry The parser registry to register
	 */
	public void registerRegistry(ParserRegistry registry) {
		if (registry != null) {
			String ontologyName = registry.getOntologyName() ;
			ontologyCatalog.put(ontologyName, registry) ;
			log.debug("Registered parser registry for ontology: {}", ontologyName) ;
		}
	}
	
	/**
	 * Parse YAML content into a graph of Ject objects.
	 *
	 * @param yamlContent The YAML content to parse
	 * @return The root Ject object (Ontology)
	 * @throws IllegalArgumentException if the YAML is malformed or contains duplicate IDs
	 * @throws IllegalStateException if references cannot be resolved
	 */
	public Ject parse(String yamlContent) {
		log.debug("Parsing YAML content of length {}", yamlContent.length()) ;
		Map<String, Object> raw = yaml.load(yamlContent) ;
		log.debug("Loaded raw YAML structure with {} top-level keys", raw.size()) ;

		// Pass 1: Detect duplicates
		log.debug("Pass 1: Detecting duplicate IDs") ;
		detectDuplicates(raw) ;

		String ontologyName = (String) raw.get("ontology") ;
		if (ontologyName == null || ontologyName.isEmpty()) {
			log.error("Ontology name is missing or empty") ;
			throw new IllegalArgumentException("Ontology name is required") ;
		}
		log.debug("Ontology name: {}", ontologyName) ;
		
		String id = (String) raw.get("id") ;
		id = id == null ? UUID.randomUUID().toString() : id ;
		log.debug("Using ID: {}", id) ;
		
		Ontology retVal = new Ontology(ontologyName) ;
		retVal.addScalar(BasePredicate.id, id) ;
		retVal.addScalar(BasePredicate.ontologyName, ontologyName) ;
		Map<String, Object> rawRemnants = new LinkedHashMap<>(raw) ;
		rawRemnants.remove("ontology") ;
		rawRemnants.remove("id") ;

		// Pass 2: Build placeholders
		log.debug("Pass 2: Building object structure with placeholders") ;
		JectParseContext context = new JectParseContext(retVal, ontologyName, rawRemnants, ontologyCatalog) ;
		context.buildJects() ;

		// Pass 3: Resolve
		log.debug("Pass 3: Resolving references") ;
		context.resolveAll() ;

		// Validation
		log.debug("Final validation") ;
		context.validateAnomalies() ;
		log.info("Successfully parsed ontology '{}' with ID '{}'", ontologyName, id) ;

		return context.getOntology() ;
	}

	/**
	 * Detect duplicate IDs in the YAML structure.
	 *
	 * @param raw The raw YAML structure as a Map
	 * @throws IllegalArgumentException if duplicate IDs are detected
	 */
	private void detectDuplicates(Map<String, Object> raw) {
		Set<String> ids = new HashSet<>();
		extractIds(raw, ids);
		int totalIds = countIdOccurrences(raw);
		log.debug("Found {} unique IDs out of {} total", ids.size(), totalIds);
		
		if (ids.size() != totalIds) {
			log.error("Duplicate IDs detected: {} unique IDs out of {} total", ids.size(), totalIds);
			throw new IllegalArgumentException("Duplicate IDs detected");
		}
	}

	/**
	 * Extract all IDs from the YAML structure into a Set.
	 *
	 * @param raw The raw YAML structure as a Map
	 * @param ids The Set to store unique IDs
	 */
	@SuppressWarnings("unchecked")
	private void extractIds(Map<String, Object> raw, Set<String> ids) {
		for (Map.Entry<String, Object> entry : raw.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map) {
				extractIds((Map<String, Object>) value, ids);
			} else if (value instanceof List) {
				for (Object item : (List<?>) value) {
					if (item instanceof Map) {
						extractIds((Map<String, Object>) item, ids);
					}
				}
			}
		}
		if (raw.containsKey("id")) {
			String id = (String) raw.get("id");
			if (ids.contains(id)) {
				log.warn("Duplicate ID detected: {}", id);
			}
			ids.add(id);
		}
	}

	/**
	 * Count the total number of ID occurrences in the YAML structure.
	 *
	 * @param raw The raw YAML structure as a Map
	 * @return The total number of IDs
	 */
	@SuppressWarnings("unchecked")
	private int countIdOccurrences(Map<String, Object> raw) {
		int count = 0;
		for (Map.Entry<String, Object> entry : raw.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map) {
				count += countIdOccurrences((Map<String, Object>) value);
			} else if (value instanceof List) {
				for (Object item : (List<?>) value) {
					if (item instanceof Map) {
						count += countIdOccurrences((Map<String, Object>) item);
					}
				}
			}
		}
		if (raw.containsKey("id")) {
			count++;
		}
		return count;
	}

	/**
	 * Validate anomalies in the Ject graph.
	 *
	 * @param root The root Ject object
	 * @throws IllegalStateException if anomalies are detected
	 */
	public void validateAnomalies(Ject root) {
		if (!(root instanceof Ontology) && root.getId() == null) {
			log.error("Non-root Ject missing ID");
			throw new IllegalStateException("Non-root Ject missing id");
		}
	}
}