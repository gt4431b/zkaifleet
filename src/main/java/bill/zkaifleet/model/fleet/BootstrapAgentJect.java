package bill.zkaifleet.model.fleet;

import java.util.ArrayList ;
import java.util.List ;

import com.fasterxml.jackson.annotation.JsonInclude ;

import bill.zkaifleet.model.Ject ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BootstrapAgentJect extends Ject {
    private double confidenceThreshold; // Example scalar
    private String description ;
    private List <String> capabilities = new ArrayList <> ( ) ; // Example list of capabilities
    private List <String> escalationPath = new ArrayList <> ( ) ;

    public BootstrapAgentJect(String id) {
        super(id, "bootstrapAgent", "fleet");
    }

    // Typed relation example (e.g., to processes it handles)
    public List<ProcessJect> getProcesses() {
        return getTypedSubjects(FleetPredicate.process, ProcessJect.class);
    }

    public BootstrapAgentJect addProcess(ProcessJect process) {
        addTypedSubject(FleetPredicate.process, process);
        return this;
    }

	public List <WrunkJect> getWrunkTypesHandled ( ) {
		return getTypedSubjects ( FleetPredicate.wrunkTypeHandled, WrunkJect.class ) ;
	}

	public BootstrapAgentJect addWrunkTypeHandled ( WrunkJect wrunkType ) {
		addTypedSubject ( FleetPredicate.wrunkTypeHandled, wrunkType ) ;
		return this ;
	}
}
