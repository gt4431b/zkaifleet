package bill.zkaifleet.model.fleet;

import java.util.List ;

import com.fasterxml.jackson.annotation.JsonInclude ;

import bill.zkaifleet.model.Ject ;
import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VisionStatementJect extends Ject {
    private String inputMethod;
    private String distribution;

    public VisionStatementJect(String id) {
        super(id, "visionStatement", "fleet");
    }

    // Typed relation (e.g., to breakdowns/tasks)
    public List<TaskJect> getBreakdowns() {
        return getTypedSubjects(FleetPredicate.breakdown, TaskJect.class);
    }

    public VisionStatementJect addBreakdown(TaskJect task) {
        addTypedSubject(FleetPredicate.breakdown, task);
        return this;
    }
}