package bill.zkaifleet.parser;

import java.util.HashMap;
import java.util.Map;

import bill.zkaifleet.model.Ject ;
import lombok.Data ;

@Data
public class JectParseContext {
    private final Map<String, Placeholder<Ject>> identityMap = new HashMap<>();

    public Placeholder<Ject> getOrCreatePlaceholder(String id, String typeName, String ontology) {
        return identityMap.computeIfAbsent(id, k -> new Placeholder<>(id, typeName, ontology));
    }

    public void resolveAll() {
        // Third pass logic: Iterate and swap placeholders
    }
}
