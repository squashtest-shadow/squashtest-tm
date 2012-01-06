package org.squashtest.csp.tm.domain.library;

/**
 * POJO holding basic informations regarding nodes, when one do not need the full data held in the Session cache.
 * 
 * 
 * @author bsiri
 *
 */
public class NodeReference {
	
	private Long id;
	private String name;
	private boolean directory;
	
	
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public boolean isDirectory(){
		return directory;
	}

	public NodeReference(Long id, String name, boolean isDirectory) {
		super();
		this.id = id;
		this.name = name;
		this.directory = isDirectory;
	}
	
	
	/**
	 * this one accepts an object array formatted as { Long, String, Boolean }
	 * 
	 * @param rawData
	 */
	public NodeReference(Object[] rawData){
		super();
		this.id = (Long) rawData[0];
		this.name = (String) rawData[1];
		this.directory = (Boolean) rawData[2];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (directory ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		NodeReference other = (NodeReference) obj;
		if (directory != other.directory)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}


	

	
}
