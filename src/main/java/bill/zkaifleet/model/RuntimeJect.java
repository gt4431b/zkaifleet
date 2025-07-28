package bill.zkaifleet.model ;

//In bill.zkaifleet.model

// bill.zkaifleet.model.RuntimeJect
import com.fasterxml.jackson.annotation.JsonInclude ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode ( callSuper = true )
@JsonInclude ( JsonInclude.Include.NON_NULL )
public class RuntimeJect extends Ject {

	public RuntimeJect ( String typeName, String ontology ) {
		super ( typeName, ontology ) ;
	}

	public Object getLiteral() {
	    return getScalar(BasePredicate.literal, Object.class);
	}

	public RuntimeJect addRuntimeSubject(String predKey, Ject obj) {
	    Predicate pred = new RuntimePredicate(predKey, "runtime", ontology);
	    addTypedSubject(pred, obj);
	    return this;
	}
}
