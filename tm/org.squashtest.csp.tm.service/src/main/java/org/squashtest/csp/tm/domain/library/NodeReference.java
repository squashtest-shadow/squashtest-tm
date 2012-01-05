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
	
}
