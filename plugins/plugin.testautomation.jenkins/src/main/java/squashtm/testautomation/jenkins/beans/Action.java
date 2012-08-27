package squashtm.testautomation.jenkins.beans;

public class Action {
	
	private Parameter[] parameters;

	public Parameter[] getParameters() {
		return parameters;
	}

	public void setParameters(Parameter[] parameters) {
		this.parameters = parameters;
	}
	
	public Action(){
		super();
	}
	
	public boolean hasParameter(Parameter parameter){
		for (Parameter param : parameters){
			if (param.equals(parameter)){
				return true;
			}
		}
		return false;
	}
	
}
