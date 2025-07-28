package bill.zkaifleet.model.fleet ;

import java.util.List ;

import bill.zkaifleet.model.Ject ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode ( callSuper = true )
public class FleetJect extends Ject {

	private String name ;
	private String version ;

	public FleetJect ( String id ) {
		super ( id, "fleet", "fleet" ) ;
	}

	public BootstrapAgentJect getBootstrapAgent ( ) {
		return getSingleTypedSubject ( FleetPredicate.bootstrapAgent, BootstrapAgentJect.class ) ;
	}

	public FleetJect setBootstrapAgent ( BootstrapAgentJect agent ) {
		setSingleTypedSubject ( FleetPredicate.bootstrapAgent, agent ) ;
		return this ;
	}

	public VisionStatementJect getVisionStatement ( ) {
		return getSingleTypedSubject ( FleetPredicate.visionStatement, VisionStatementJect.class ) ;
	}

	public FleetJect setVisionStatement ( VisionStatementJect visionStatement ) {
		setSingleTypedSubject ( FleetPredicate.visionStatement, visionStatement ) ;
		return this ;
	}

	public List <RoleJect> getRoles ( ) {
		return getTypedSubjects ( FleetPredicate.role, RoleJect.class ) ;
	}

	public FleetJect addRole ( RoleJect role ) {
		addTypedSubject ( FleetPredicate.role, role ) ;
		return this ;
	}

	public List <WrunkJect> getWrunks ( ) {
		return getTypedSubjects ( FleetPredicate.wrunk, WrunkJect.class ) ;
	}

	public FleetJect addWrunk ( WrunkJect wrunk ) {
		addTypedSubject ( FleetPredicate.wrunk, wrunk ) ;
		return this ;
	}

	public List <ProcessJect> getProcesses ( ) {
		return getTypedSubjects ( FleetPredicate.process, ProcessJect.class ) ;
	}

	public FleetJect addProcess ( ProcessJect process ) {
		addTypedSubject ( FleetPredicate.process, process ) ;
		return this ;
	}

	public List <IntegrationJect> getIntegrations ( ) {
		return getTypedSubjects ( FleetPredicate.integration, IntegrationJect.class ) ;
	}

	public FleetJect addIntegration ( IntegrationJect integration ) {
		addTypedSubject ( FleetPredicate.integration, integration ) ;
		return this ;
	}

	public HumanInterventionJect getHumanIntervention ( ) {
		return getSingleTypedSubject ( FleetPredicate.humanIntervention, HumanInterventionJect.class ) ;
	}

	public FleetJect setHumanIntervention ( HumanInterventionJect humanIntervention ) {
		setSingleTypedSubject ( FleetPredicate.humanIntervention, humanIntervention ) ;
		return this ;
	}
}
