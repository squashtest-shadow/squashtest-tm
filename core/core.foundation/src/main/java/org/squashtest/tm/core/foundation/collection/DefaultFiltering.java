package org.squashtest.tm.core.foundation.collection;

import org.springframework.util.StringUtils;

public class DefaultFiltering implements Filtering{
	
	public static final DefaultFiltering NO_FILTERING = new DefaultFiltering("", null);
	
	
	private String filteredAttribute;
	private String filter;

	public DefaultFiltering() {
		super();
	}


	public DefaultFiltering(String filteredAttribute, String filter) {
		super();
		this.filteredAttribute = filteredAttribute;
		this.filter = filter;
	}
	

	@Override
	public boolean usesFiltering() {
		return StringUtils.hasLength(filter);
	}

	@Override
	public String getFilter() {
		return filter;
	}

	@Override
	public String getFilteredAttribute() {
		return filteredAttribute;
	}

	public void setFilteredAttribute(String filteredAttribute) {
		this.filteredAttribute = filteredAttribute;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	
	
}
