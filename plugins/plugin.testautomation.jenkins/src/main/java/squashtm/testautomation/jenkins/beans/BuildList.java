package squashtm.testautomation.jenkins.beans;

public class BuildList {
	
	private Build[] builds;

	public Build[] getBuilds() {
		return builds;
	}

	public void setBuilds(Build[] builds) {
		this.builds = builds;
	}
	
	public BuildList(){
		super();
	}
	
	public Build findById(int id){
		for (Build build : builds){
			if (build.hasId(id)){
				return build;
			}
		}
		return null;
	}
	
	public Build findByExternalId(String externalId){
		for (Build build : builds){
			if (build.hasExternalId(externalId)){
				return build;
			}
		}
		return null;
	}
}
