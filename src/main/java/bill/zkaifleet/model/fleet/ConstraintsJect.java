package bill.zkaifleet.model.fleet;

import com.fasterxml.jackson.annotation.JsonInclude ;

import bill.zkaifleet.model.Ject ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConstraintsJect extends Ject {
	private Integer tokenBudget ;
	private String focus ;
	private Double confidenceThreshold ;

	public ConstraintsJect ( ) {
		super ( "constraints", "fleet" ) ;
	}
}
