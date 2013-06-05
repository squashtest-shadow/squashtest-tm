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
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestCaseFinder;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.generic.DataTableColumnDefHelper;
import org.squashtest.tm.web.internal.controller.widget.AoColumnDef;
import org.squashtest.tm.web.internal.helper.JsonHelper;
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
@RequestMapping("/test-cases/{testCaseId}/parameters")
@Controller
public class TestCaseParametersController {
	@Inject
	private TestCaseFinder testCaseFinder;
	@Inject
	private PermissionEvaluationService permissionEvaluationService;


	/**
	 * 
	 */
	private static final String TEST_CASE = "testCase";

	
	@RequestMapping(value = "/panel")
	public String getParameters(@PathVariable("testCaseId") long testCaseId, Model model) {
		
		// the main entities
		TestCase testCase = testCaseFinder.findById(testCaseId);
		//TODO
		List<Long> paramIds = IdentifiedUtil.extractIds(testCase.getParameters());
		Set<Parameter> directAndCalledParameters = testCase.getParameters();
		// end TODO
		boolean editable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", testCase);
		List<AoColumnDef> columnDefs = new DatasetsTableColumnDefHelper().getAoColumnDefs(paramIds,editable);

		// populate the model
		model.addAttribute(TEST_CASE, testCase);
		model.addAttribute("datasetsAoColumnDefs", JsonHelper.serialize(columnDefs));
		model.addAttribute("directAndCalledParameters", directAndCalledParameters);
		// return
		return "test-cases-tabs/parameters-tab.html";

	}
	
	@RequestMapping(method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getParametersTable(@PathVariable long testCaseId,
			final DataTableDrawParameters params, final Locale locale) {
		final TestCase testCase = testCaseFinder.findById(testCaseId);
		Sorting sorting = new DataTableMapperPagingAndSortingAdapter(params,
				parametersTableMapper);
		//TODO REMOVE
		new Parameter("name", testCase); 
		new Parameter("name2", testCase);
		//TODO CHANGE WITH METHOD THAT GETs Sorted Params
		PagedCollectionHolder<List<Parameter>> holder = new PagedCollectionHolder<List<Parameter>>() {
			
			@Override
			public long getTotalNumberOfItems() {
				return testCase.getParameters().size();
			}
			
			@Override
			public List<Parameter> getPagedItems() {
				return new ArrayList<Parameter>(testCase.getParameters());
			}
			
			@Override
			public long getFirstItemIndex() {
				return 0;
			}
		};

		return new ParametersDataTableModelHelper().buildDataModel(
				holder, params.getsEcho());
	}
	
	
	/**
	 * Will add a new parameter to the test case
	 * 
	 * @param testCaseId : the id of the test case that will hold the new parameter
	 * @param parameter : the parameter to add with it's set name and description
	 */
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	@ResponseBody
	public void newParameter(@PathVariable long testCaseId, @ModelAttribute("add-parameter") Parameter parameter) {
		TestCase testCase = testCaseFinder.findById(testCaseId);
		parameter.setTestCase(testCase);
		testCase.addParameter(parameter);
	}

	
	
	
	
	private DatatableMapper<String> parametersTableMapper = new NameBasedMapper(3)
	.mapAttribute(Parameter.class, "id", String.class, DataTableModelHelper.DEFAULT_ENTITY_ID_KEY)
	.mapAttribute(Parameter.class, "name", String.class, DataTableModelHelper.NAME_KEY)
	.mapAttribute(TestCase.class, "name", String.class, "test-case-name");
	
	private static class ParametersDataTableModelHelper extends DataTableModelHelper<Parameter> {

		private ParametersDataTableModelHelper() {
			super();
		}

		@Override
		public Map<String, Object> buildItemData(Parameter item) {
			Map<String, Object> res = new HashMap<String, Object>();
			TestCase testCase = item.getTestCase();
			Project project = testCase.getProject();
			String testCaseName = testCase.getName()+" ("+project.getName()+')';
			if(testCase.getReference().length()>0){
				testCaseName = testCase.getReference()+'-'+testCaseName;
			}
			
			//TODO
			res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, 5L);
			//res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put(DataTableModelHelper.NAME_KEY, item.getName());
			res.put("description", item.getDescription());
			res.put("test-case-name", testCaseName);
			res.put(DataTableModelHelper.DEFAULT_EMPTY_DELETE_HOLDER_KEY, "");
			return res;
		}
		
		
	}
	

}
