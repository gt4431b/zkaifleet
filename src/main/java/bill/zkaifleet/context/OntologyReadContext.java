package bill.zkaifleet.context;

import java.util.HashSet ;
import java.util.Set ;
import java.util.function.Predicate ;

import bill.zkaifleet.model.Ject ;
import lombok.Data ;
	
@Data
public class OntologyReadContext {
    private final Set<String> seen = new HashSet<>();

    public void traverse(Ject ject, Predicate <Ject> visitor) {
        String key = ject.getTypeName() + ":" + ject.hashCode();
        if (!seen.add(key)) {
            throw new CycleDetectedException("Cycle at " + key);
        }
        visitor.test(ject);
        seen.remove(key);
    }
}
