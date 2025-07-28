package bill.zkaifleet.model.fleet;

import com.fasterxml.jackson.annotation.JsonInclude ;

import bill.zkaifleet.model.Ject ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactJect extends Ject {

	private String method ;
	private String to ;

	public ContactJect ( String id ) {
		super ( id, "contact", "fleet" ) ;
	}
}
