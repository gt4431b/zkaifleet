package bill.zkaifleet.parser;

import java.util.HashSet ;
import java.util.List ;
import java.util.Map ;
import java.util.Set ;
import java.util.UUID ;

import bill.zkaifleet.context.CycleDetectedException ;
import bill.zkaifleet.model.Ject ;
import bill.zkaifleet.model.Ontology ;
import bill.zkaifleet.model.RuntimeJect ;
import lombok.extern.slf4j.Slf4j ;
import org.yaml.snakeyaml.Yaml;

@Slf4j
public class OntologyParser {
    private final Yaml yaml = new Yaml();

    public Ject parse(String yamlContent) {
        Map<String, Object> raw = yaml.load(yamlContent);

        // Pass 1: Detect duplicates
        detectDuplicates(raw);

        // Pass 2: Build placeholders
        JectParseContext context = new JectParseContext();
        Ject root = buildJects(raw, context, new HashSet<>());

        // Pass 3: Resolve
        context.resolveAll();

        // Validation
        validateAnomalies(root);

        return root;
    }

    private void detectDuplicates(Map<String, Object> raw) {
        Set<String> ids = new HashSet<>();
        extractIds(raw, ids);
        if (ids.size() != countIdOccurrences(raw)) {
            throw new IllegalArgumentException("Duplicate IDs detected");
        }
    }

    @SuppressWarnings ( "unchecked" )
	private void extractIds(Map<String, Object> raw, Set<String> ids) {
        for (Object value : raw.values()) {
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
            ids.add((String) raw.get("id"));
        }
    }

    @SuppressWarnings ( "unchecked" )
	private int countIdOccurrences(Map<String, Object> raw) {
        int count = 0;
        for (Object value : raw.values()) {
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

    private Ject buildJects(Map<String, Object> raw, JectParseContext context, Set<String> visited) {
        String id = (String) raw.get("id");
        String typeName = (String) raw.getOrDefault("type", "Runtime");
        String ontologyName = (String) raw.getOrDefault("ontology", "fleet");

        if (id != null && !visited.add(id)) {
            throw new CycleDetectedException("Cycle detected at ID: " + id);
        }

        Ject ject;
        if (raw.containsKey("ontology") && !raw.containsKey("id") && !raw.containsKey("type")) {
            ject = new Ontology(UUID.randomUUID().toString());
            ((Ontology) ject).setOntologyName(ontologyName);
        } else {
            ject = new RuntimeJect(id != null ? id : UUID.randomUUID().toString(), typeName, ontologyName);
        }
        if (id != null) {
            context.getOrCreatePlaceholder(id, typeName, ontologyName).resolve(ject);
        }

        // Scalars and relations
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!"id".equals(key) && !"type".equals(key) && !"ontology".equals(key)) {
                if (value instanceof Map) {
                    // Check if it's a ref
                    @SuppressWarnings ( "unchecked" )
					Map<String, Object> mapValue = (Map<String, Object>) value;
                    if (mapValue.containsKey("ref")) {
                        String refId = (String) mapValue.get("ref");
                        Placeholder<Ject> ref = context.getOrCreatePlaceholder(refId, "Runtime", ontologyName);
                        ((RuntimeJect) ject).addRuntimeSubject(key, ref);
                    } else {
                        // Nested Ject
                        Ject nested = buildJects(mapValue, context, visited);
                        ((RuntimeJect) ject).addRuntimeSubject(key, nested);
                    }
                } else if (value instanceof List) {
                    // List: If items are maps, recurse as Jects
                    List<?> list = (List<?>) value;
                    if (!list.isEmpty() && list.get(0) instanceof Map) {
                        for (Object item : list) {
                            if (item instanceof Map) {
                                @SuppressWarnings ( "unchecked" )
								Ject listItem = buildJects((Map<String, Object>) item, context, visited);
                                if (ject instanceof Ontology && "jects".equals(key)) {
                                    ((Ontology) ject).addRoot(listItem);
                                } else {
                                    ((RuntimeJect) ject).addRuntimeSubject(key, listItem);
                                }
                            }
                        }
                    } else {
                        // Simple list scalar
                        ((RuntimeJect) ject).addScalar(key, value);
                    }
                } else {
                    // Simple scalar
                    ((RuntimeJect) ject).addScalar(key, value);
                }
            }
        }

        return ject;
    }

    private void validateAnomalies(Ject root) {
        if (!(root instanceof Ontology) && root.getId() == null) {
            throw new IllegalStateException("Non-root Ject missing id");
        }
    }
}
