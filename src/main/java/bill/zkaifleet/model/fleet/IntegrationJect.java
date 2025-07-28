package bill.zkaifleet.model.fleet;


import java.util.List ;

//Chunk 8: IntegrationJect
import com.fasterxml.jackson.annotation.JsonInclude;

import bill.zkaifleet.model.Ject ;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntegrationJect extends Ject {
 private String type; // e.g., mcp, rag
 private String repo; // For git, etc.

 public IntegrationJect ( ) {
     super ( "integration", "fleet");
 }

 // Typed relation (e.g., to roles using it)
 public List<RoleJect> getRoles() {
     return getTypedSubjects(FleetPredicate.role, RoleJect.class);
 }

 public IntegrationJect addRole(RoleJect role) {
     addTypedSubject(FleetPredicate.role, role);
     return this;
 }
}