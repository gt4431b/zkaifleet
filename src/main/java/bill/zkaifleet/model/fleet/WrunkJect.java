package bill.zkaifleet.model.fleet ;

import bill.zkaifleet.model.Ject ;

import java.util.ArrayList ;
import java.util.List ;

import com.fasterxml.jackson.annotation.JsonInclude ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode ( callSuper = true )
@JsonInclude ( JsonInclude.Include.NON_NULL )
public class WrunkJect extends Ject {

	private List <String> fields = new ArrayList <> ( ) ;
	private String storage ;

	public WrunkJect ( ) {
		super ( "wrunct", "fleet" ) ;
	}
}
