package bill.zkaifleet.model;

import java.util.List ;

import com.fasterxml.jackson.annotation.JsonInclude ;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ontology extends Ject {

	private String ontologyName ;

	public Ontology(String id) {
		super(id, "ontology", "base");
	}

	public List<Ject> getRoots() {
        return getTypedSubjects(BasePredicate.root, Ject.class);
    }

    public Ontology addRoot(Ject root) {
        addTypedSubject(BasePredicate.root, root);
        return this;
    }

	@Override
	public String toString() {
		return "Ontology{" +
				"id='" + getId() + '\'' +
				", typeName='" + getTypeName() + '\'' +
				", ontology='" + getOntology() + '\'' +
				'}';
	}
}
