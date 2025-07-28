package bill.zkaifleet.model ;

//In bill.zkaifleet.model

import com.fasterxml.jackson.annotation.JsonInclude ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode
@JsonInclude ( JsonInclude.Include.NON_NULL )
public class RuntimePredicate implements Predicate {

	private final String name ;
	private final String space ;
	private final String ontology ;

	public RuntimePredicate ( String name, String space, String ontology ) {
		this.name = name ;
		this.space = space ;
		this.ontology = ontology ;
	}

	public String name ( ) {
		return name ;
	}

	public String space ( ) {
		return space ;
	}

	@Override
	public String ontology ( ) {
		return ontology ;
	}
}
