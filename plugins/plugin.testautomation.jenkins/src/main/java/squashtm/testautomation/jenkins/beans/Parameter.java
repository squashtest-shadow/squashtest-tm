package squashtm.testautomation.jenkins.beans;

public class Parameter {
	
	private String name;
	private String value;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return 
		value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	

	public Parameter(){
		super();
	}

	public Parameter(String name, String value){
		super();
		this.name=name;
		this.value=value;
	}
	
	static public Parameter operationTestListParameter(){
		return new Parameter("operation", "test-list");
	}
	
	static public Parameter newExtIdParameter(String externalId){
		return new Parameter("externalJobId", externalId);
	}
	
		
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameter other = (Parameter) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	
}
