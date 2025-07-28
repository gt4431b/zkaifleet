package bill.zkaifleet.model.fleet ;

import bill.zkaifleet.model.Ject ;

import java.util.List ;

import com.fasterxml.jackson.annotation.JsonInclude ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WrunkJect extends Ject {
    private String description;
    private int timeSpent;
    private int tokenCost;
    private double confidenceScore;
    private double gridlockScore;

    public WrunkJect(String id) {
        super(id, "wrunct", "fleet");
    }

    // Typed relation (e.g., to evaluations)
    public List<EvaluationAmendmentJect> getEvaluationAmendments() {
        return getTypedSubjects(FleetPredicate.evaluationAmendment, EvaluationAmendmentJect.class);
    }

    public WrunkJect addEvaluationAmendment(EvaluationAmendmentJect amendment) {
        addTypedSubject(FleetPredicate.evaluationAmendment, amendment);
        return this;
    }
}