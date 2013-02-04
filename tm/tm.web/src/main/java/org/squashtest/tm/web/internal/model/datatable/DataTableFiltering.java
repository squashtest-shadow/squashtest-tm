package org.squashtest.tm.web.internal.model.datatable;

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.core.foundation.collection.Filtering;

public class DataTableFiltering implements Filtering{

	private final DataTableDrawParameters params;
	
	
	public DataTableFiltering(DataTableDrawParameters params) {
		super();
		this.params = params;
	}

	@Override
	public boolean usesFiltering() {
		return ! StringUtils.isBlank(params.getsSearch());
	}

	@Override
	public String getFilter() {
		return params.getsSearch();
	}


	// for now we do not filter for specific attributes, we filter for all of them
	@Override
	public String getFilteredAttribute() {
		return null;
	}

	
	
}
