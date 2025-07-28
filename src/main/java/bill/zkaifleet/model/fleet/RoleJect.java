package bill.zkaifleet.model.fleet ;

import java.util.ArrayList ;
import java.util.List ;

import bill.zkaifleet.model.Ject ;

import com.fasterxml.jackson.annotation.JsonInclude ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode ( callSuper = true )
@JsonInclude ( JsonInclude.Include.NON_NULL )
public class RoleJect extends Ject {

	private String seniority ;
	private String modelTier ;
	private List <String> capabilities = new ArrayList <> ( ) ;

	public RoleJect ( ) {
		super ( "role", "fleet" ) ;
	}

	public List <ProcessJect> getProcesses ( ) {
		return getTypedSubjects ( FleetPredicate.process, ProcessJect.class ) ;
	}

	public RoleJect addProcess ( ProcessJect process ) {
		addTypedSubject ( FleetPredicate.process, process ) ;
		return this ;
	}

	public List <IntegrationJect> getIntegrations ( ) {
		return getTypedSubjects ( FleetPredicate.integration, IntegrationJect.class ) ;
	}

	public RoleJect addIntegration ( IntegrationJect integration ) {
		addTypedSubject ( FleetPredicate.integration, integration ) ;
		return this ;
	}

	public List <InteractionJect> getInteractions ( ) {
		return getTypedSubjects ( FleetPredicate.interaction, InteractionJect.class ) ;
	}

	public void addInteraction ( InteractionJect interaction ) {
		addTypedSubject ( FleetPredicate.interaction, interaction ) ;
	}

	public List <WrunkJect> getWrunkTypesHandled ( ) {
		return getTypedSubjects ( FleetPredicate.wrunkTypeHandled, WrunkJect.class ) ;
	}

	public RoleJect addWrunkTypeHandled ( WrunkJect wrunkType ) {
		addTypedSubject ( FleetPredicate.wrunkTypeHandled, wrunkType ) ;
		return this ;
	}

	private List <String> escalationPath = new ArrayList <> ( ) ;

	public void addCapability ( String c ) {
		if ( !capabilities.contains ( c ) ) {
			capabilities.add ( c ) ;
		}
	}

	public void addEscalationPath ( String c ) {
		if ( !escalationPath.contains ( c ) ) {
			escalationPath.add ( c ) ;
		}
	}
}
