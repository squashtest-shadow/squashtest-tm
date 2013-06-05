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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestCaseFinder;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.generic.DataTableColumnDefHelper;
import org.squashtest.tm.web.internal.controller.widget.AoColumnDef;
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

	@RequestMapping(method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getDatasetsTable(@PathVariable long testCaseId, final DataTableDrawParameters params,
			final Locale locale) {
		final TestCase testCase = testCaseFinder.findById(testCaseId);
		Sorting sorting = new DataTableMapperPagingAndSortingAdapter(params, datasetsTableMapper);
		// TODO REMOVE
		new Dataset("name", testCase);
		new Dataset("name2", testCase);
		// TODO CHANGE WITH METHOD THAT GETs Sorted Params
		PagedCollectionHolder<List<Dataset>> holder = new PagedCollectionHolder<List<Dataset>>() {

			@Override
			public long getTotalNumberOfItems() {
				return testCase.getParameters().size();
			}

			@Override
			public List<Dataset> getPagedItems() {
				return new ArrayList<Dataset>(testCase.getDatasets());
			}

			@Override
			public long getFirstItemIndex() {
				return 0;
			}
		};

		return new DatasetsDataTableModelHelper().buildDataModel(holder, params.getsEcho());
	}

	private DatatableMapper<String> datasetsTableMapper = new NameBasedMapper(3).mapAttribute(Dataset.class, "id",
			String.class, DataTableModelHelper.DEFAULT_ENTITY_ID_KEY).mapAttribute(Dataset.class, "name", String.class,
			DataTableModelHelper.NAME_KEY);

	private static class DatasetsDataTableModelHelper extends DataTableModelHelper<Dataset> {

		private DatasetsDataTableModelHelper() {
			super();
		}

		@Override
		public Map<String, Object> buildItemData(Dataset item) {
			Map<String, Object> res = new HashMap<String, Object>();
			// TODO
			res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, 5L);
			// res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put(DataTableModelHelper.NAME_KEY, item.getName());
			for (DatasetParamValue parameterValue : item.getParameterValues()) {
				res.put("parameter-" + parameterValue.getParameter().getId(), parameterValue.getParamValue());
			}
			res.put(DataTableModelHelper.DEFAULT_EMPTY_DELETE_HOLDER_KEY, "");
			return res;
		}

	}

	
}
