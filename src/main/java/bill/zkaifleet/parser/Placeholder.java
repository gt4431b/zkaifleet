package bill.zkaifleet.parser;

import java.util.List ;

import bill.zkaifleet.model.Ject ;
import bill.zkaifleet.model.Predicate ;

//In bill.zkaifleet.model

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Placeholder<T extends Ject> extends Ject {
    private T resolved = null;

    public Placeholder(String id, String typeName, String ontology) {
        super(typeName, ontology);
        setId ( id ) ;
    }

    public void resolve(T actual) {
        this.resolved = actual;
    }

    @Override
    public <U extends Ject> List<U> getTypedSubjects(Predicate pred, Class<U> type) {
        if (resolved != null) return resolved.getTypedSubjects(pred, type);
        return super.getTypedSubjects(pred, type);
    }

    @Override
    public <U extends Ject> Ject addTypedSubject(Predicate pred, U obj) {
        if (resolved != null) return resolved.addTypedSubject(pred, obj);
        return super.addTypedSubject(pred, obj);
    }

    // Override other methods similarly for consistency
}
