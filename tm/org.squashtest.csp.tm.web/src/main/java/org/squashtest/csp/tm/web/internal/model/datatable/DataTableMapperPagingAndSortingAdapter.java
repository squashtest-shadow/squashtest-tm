package org.squashtest.csp.tm.web.internal.model.datatable;

import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.core.infrastructure.collection.SortOrder;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;
/**
 * PagingAndSortingAdapter backed by a DataTableDrawParameters and a DataTableMapper (for sorting purposes).
 * @author Gregory Fouquet
 *
 */
public class DataTableMapperPagingAndSortingAdapter extends DataTableDrawParametersPagingAdapter implements
		PagingAndSorting {
	private final DataTableDrawParameters params;
	private final DataTableMapper mapper;

	public DataTableMapperPagingAndSortingAdapter(DataTableDrawParameters drawParams, DataTableMapper mapper) {
		super(drawParams);
		this.params = drawParams;
		this.mapper = mapper;
	}

	@Override
	public String getSortedAttribute() {
		return mapper.pathAt(params.getiSortCol_0());
	}

	@Override
	public SortOrder getSortingOrder() {
		return SortOrder.coerceFromCode(params.getsSortDir_0());
	}

}
