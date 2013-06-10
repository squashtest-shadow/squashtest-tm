/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.testcase.parameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SinglePageCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.squashtest.tm.service.testcase.ParameterFinder;
import org.squashtest.tm.service.testcase.TestCaseFinder;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

/**
 * @author mpagnon
 * 
 */
@RequestMapping("/test-cases/{testCaseId}/datasets")
@Controller
public class TestCaseDatasetsController {
	@Inject
	private TestCaseFinder testCaseFinder;
	@Inject 
	private DatasetModificationService datasetModificationService;
	@Inject
	private ParameterFinder parameterFinder;
	@RequestMapping(method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getDatasetsTable(@PathVariable long testCaseId, final DataTableDrawParameters params,
			final Locale locale) {
		List<Dataset> datasetsList = getSortedDatasets(testCaseId, params);
		PagedCollectionHolder<List<Dataset>> holder = new SinglePageCollectionHolder<List<Dataset>>(datasetsList);
		return new DatasetsDataTableModelHelper().buildDataModel(holder, params.getsEcho());
	}

	private List<Dataset> getSortedDatasets(long testCaseId, final DataTableDrawParameters params) {
		final TestCase testCase = testCaseFinder.findById(testCaseId);
		Sorting sorting = new DataTableMapperPagingAndSortingAdapter(params, datasetsTableMapper);
		Set<Dataset> datasets = testCase.getDatasets();
		List<Dataset> datasetsList = new ArrayList<Dataset>(datasets);
		if (sorting.getSortedAttribute() != null && sorting.getSortedAttribute().equals("Parameter.name")) {
			Collections.sort(datasetsList, new DatasetNameComparator(sorting.getSortOrder()));
		}else{
			Collections.sort(datasetsList, new DatasetNameComparator(SortOrder.ASCENDING));
		}
		return datasetsList;
	}

	private static final class DatasetNameComparator implements Comparator<Dataset> {

		private SortOrder sortOrder;

		private DatasetNameComparator(SortOrder sortOrder) {
			this.sortOrder = sortOrder;
		}

		@Override
		public int compare(Dataset o1, Dataset o2) {
			int ascResult = o1.getName().compareTo(o2.getName());
			if(this.sortOrder.equals(SortOrder.ASCENDING)){
				return ascResult;
			}else{
				return - ascResult;
			}
		}
	}

	private DatatableMapper<String> datasetsTableMapper = new NameBasedMapper(3).mapAttribute(Dataset.class, "id",
			String.class, DataTableModelHelper.DEFAULT_ENTITY_ID_KEY).mapAttribute(Dataset.class, "name", String.class,
			DataTableModelHelper.NAME_KEY);

	private final static class DatasetsDataTableModelHelper extends DataTableModelHelper<Dataset> {

		private DatasetsDataTableModelHelper() {
			super();
		}

		@Override
		public Map<String, Object> buildItemData(Dataset item) {
			Map<String, Object> res = new HashMap<String, Object>();
			res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put(DataTableModelHelper.NAME_KEY, item.getName());
			for (DatasetParamValue parameterValue : item.getParameterValues()) {
				res.put("parameter-" + parameterValue.getParameter().getId(), parameterValue.getParamValue());
			}
			res.put(DataTableModelHelper.DEFAULT_EMPTY_DELETE_HOLDER_KEY, "");
			return res;
		}

	}
	
	/**
	 * Will add a new dataset to the test case
	 * 
	 * @param testCaseId
	 *            : the id of the test case that will hold the new dataset
	 * @param dataset
	 *            : the dataset to add with it's set name
	 */
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	@ResponseBody
	public void newDataset(@PathVariable long testCaseId,@Valid @RequestBody  NewDataset dataset) {
		TestCase testCase = testCaseFinder.findById(testCaseId);
		datasetModificationService.persist(dataset.createTransientEntity(testCase, parameterFinder));
	}
}
