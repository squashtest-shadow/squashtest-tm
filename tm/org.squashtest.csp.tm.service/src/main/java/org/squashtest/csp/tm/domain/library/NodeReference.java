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
	
	
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	

	public NodeReference(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	/**
	 * this one accepts an object array formatted as { Long, String }
	 * 
	 * @param rawData
	 */
	public NodeReference(Object[] rawData){
		super();
		this.id = (Long) rawData[0];
		this.name = (String) rawData[1];
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
