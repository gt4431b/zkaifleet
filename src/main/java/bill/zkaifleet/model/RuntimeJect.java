package bill.zkaifleet.model ;

//In bill.zkaifleet.model

// bill.zkaifleet.model.RuntimeJect
import com.fasterxml.jackson.annotation.JsonInclude ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;

import java.util.HashMap ;
import java.util.Map ;

@Data
@EqualsAndHashCode ( callSuper = true )
@JsonInclude ( JsonInclude.Include.NON_NULL )
public class RuntimeJect extends Ject {

//	private final Map <String, Object> scalarProperties = new HashMap <> ( ) ;

	public RuntimeJect ( String typeName, String ontology ) {
		super ( typeName, ontology ) ;
	}
/*
	public void addScalar ( String key, Object value ) {
		scalarProperties.put ( key, value ) ;
	}

	public Object getScalar ( String key ) {
		return scalarProperties.get ( key ) ;
	}

	public <T> T getScalarAs ( String key, Class <T> type ) {
		Object val = scalarProperties.get ( key ) ;
		if ( val != null && type.isInstance ( val ) ) {
			return type.cast ( val ) ;
		}
		throw new IllegalStateException ( "Invalid type for scalar: " + key ) ;
	}
*/
	public RuntimeJect addRuntimeSubject(String predKey, Ject obj) {
	    Predicate pred = new RuntimePredicate(predKey, "runtime", ontology);
	    addTypedSubject(pred, obj);
	    return this;
	}
}
