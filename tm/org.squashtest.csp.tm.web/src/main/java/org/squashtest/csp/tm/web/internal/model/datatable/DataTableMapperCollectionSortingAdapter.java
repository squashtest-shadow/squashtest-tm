package org.squashtest.csp.tm.web.internal.model.datatable;

import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;
/**
 * 
 * @author Gregory Fouquet
 *
 */
public class DataTableMapperCollectionSortingAdapter implements CollectionSorting {
	private final PagingAndSorting delegate;  

	public DataTableMapperCollectionSortingAdapter(DataTableDrawParameters drawParams, DataTableMapper mapper) {
		delegate = new DataTableMapperPagingAndSortingAdapter(drawParams, mapper);
	}

	@Override
	public int getMaxNumberOfItems() {
		return delegate.getPageSize();
	}

	public String getSortedAttribute() {
		return delegate.getSortedAttribute();
	}

	public String getSortingOrder() {
		return delegate.getSortingOrder().getCode();
	}

	public int getFirstItemIndex() {
		return delegate.getFirstItemIndex();
	}

	public int getPageSize() {
		return delegate.getPageSize();
	}

}
