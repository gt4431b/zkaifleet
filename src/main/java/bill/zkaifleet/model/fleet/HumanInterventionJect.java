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
public class HumanInterventionJect extends Ject {
    private List<String> thresholds = new ArrayList<>(); // e.g., confidenceBelow: 0.7

    public HumanInterventionJect ( ) {
        super ( "humanIntervention", "fleet");
    }

    // Typed relation (e.g., to contacts)
    public List<ContactJect> getContacts() {
        return getTypedSubjects(FleetPredicate.contact, ContactJect.class);
    }

    public HumanInterventionJect addContact(ContactJect contact) {
        addTypedSubject(FleetPredicate.contact, contact);
        return this;
    }
}