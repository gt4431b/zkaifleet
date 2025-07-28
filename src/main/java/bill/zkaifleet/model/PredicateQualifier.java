package bill.zkaifleet.model;

import java.util.List ;
import java.util.function.BiConsumer ;

public record PredicateQualifier ( boolean single, boolean required, String pluralName, List <Class <? extends Ject>> objectTypes, Class <? extends Ject> subjectType, Class <?> scalarType, BiConsumer <?, ? extends Ject> setter ) {
}
