package bill.zkaifleet.model.fleet;

import bill.zkaifleet.model.Ject ;

public class ProcessJect extends Ject {

	private String name;
	private String description;

	public ProcessJect(String id) {
		super(id, "ProcessJect", "fleet");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
