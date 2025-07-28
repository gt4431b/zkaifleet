package bill.zkaifleet.model.fleet;

import java.util.List ;

import com.fasterxml.jackson.annotation.JsonInclude ;

import bill.zkaifleet.model.Ject ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InteractionJect extends Ject {
    private String with; // e.g., other agent ID
    private String how; // e.g., notifyReview

    public InteractionJect ( ) {
        super ( "interaction", "fleet");
    }

    // Typed relation (e.g., to roles involved)
    public List<RoleJect> getRoles() {
        return getTypedSubjects(FleetPredicate.role, RoleJect.class);
    }

    public InteractionJect addRole(RoleJect role) {
        addTypedSubject(FleetPredicate.role, role);
        return this;
    }
}