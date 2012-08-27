package squashtm.testautomation.jenkins.beans;

public class Build {
	
	private Action[] actions;
	private int number;
	private boolean building;
	
	
	public Action[] getActions() {
		return actions;
	}

	public void setActions(Action[] actions) {
		this.actions = actions;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public boolean isBuilding() {
		return building;
	}

	public void setBuilding(boolean building) {
		this.building = building;
	}
	
	public Build(){
		super();
	}
	
	public boolean hasExternalId(String externalId){
		Parameter extIdParam = Parameter.newExtIdParameter(externalId);
		
		for (Action action : actions){
			if (action.hasParameter(extIdParam)){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasId(int id){
		return (this.getNumber() == id);
	}
	
}
