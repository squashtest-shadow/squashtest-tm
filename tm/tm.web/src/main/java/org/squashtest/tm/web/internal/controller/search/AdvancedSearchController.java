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
package org.squashtest.tm.web.internal.controller.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.customfield.CustomCustomFieldManagerService;
import org.squashtest.tm.service.library.AdvancedSearchService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

@Controller
@RequestMapping("/advanced-search")
public class AdvancedSearchController {

	@Inject
	private CustomCustomFieldManagerService customFieldManager;
	
	@Inject
	private AdvancedSearchService advancedSearchService;

	@Inject
	private PermissionEvaluationService permissionService;
	
	@Inject
	private InternationalizationHelper messageSource;
	
	private DatatableMapper<String> testCaseSearchResultMapper = new NameBasedMapper(12)
	.mapAttribute(Project.class, "name", String.class, "project-name")
	.mapAttribute(TestCase.class, "id", Long.class, "test-case-id")	
	.mapAttribute(TestCase.class, "reference", String.class, "test-case-ref")	
	.mapAttribute(TestCase.class, "label", String.class, "test-case-label")
	.mapAttribute(TestCase.class, "importance", TestCaseImportance.class, "test-case-weight")	
	//.mapAttribute(TestCase.class, "weight", Long.class, "test-case-requirement-nb")
	//.mapAttribute(TestCase.class, "weight", Long.class, "test-case-teststep-nb")
	//.mapAttribute(TestCase.class, "weight", Long.class, "test-case-iteration-nb")
	//.mapAttribute(TestCase.class, "weight", Long.class, "test-case-attachment-nb")
	.mapAttribute(TestCase.class, "audit.createdBy", String.class, "test-case-created-by")
	.mapAttribute(TestCase.class, "audit.lastModifiedBy", String.class, "test-case-modified-by");


	@RequestMapping( method = RequestMethod.GET, params = "testcase")
	public String getTestCaseSearchTab(Model model) {
		return "search/test-case-search-input.frag.html";
	}
	
	@RequestMapping(value = "/results", method = RequestMethod.GET, params = "testcase")
	public String getTestCaseSearchResultPage(Model model) {
		return "search/test-case-search-result.html";
	} 
	
	@RequestMapping(value = "/table", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getTableModel(final DataTableDrawParameters params, final Locale locale) {

		PagingAndSorting paging = new DataTableMapperPagingAndSortingAdapter(params, testCaseSearchResultMapper);

		PagedCollectionHolder<List<TestCase>> holder = advancedSearchService.searchForTestCases(paging);

		return new TestCaseSearchResultDataTableModelHelper(locale, messageSource, permissionService).buildDataModel(holder, params.getsEcho());
	}
	
	private static final class TestCaseSearchResultDataTableModelHelper extends DataTableModelBuilder<TestCase> {
		private InternationalizationHelper messageSource;
		private Locale locale;
		private PermissionEvaluationService permissionService;
		
		private TestCaseSearchResultDataTableModelHelper(Locale locale, InternationalizationHelper messageSource, PermissionEvaluationService permissionService) {
			this.locale = locale;
			this.messageSource = messageSource;
			this.permissionService = permissionService;
		}

		private String formatImportance(TestCaseImportance importance, Locale locale) {
			return messageSource.internationalize(importance, locale);
		}
		
		private boolean isTestCaseEditable(TestCase item){
			return permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", item);
		}
		
		@Override
		public Map<String, Object> buildItemData(TestCase item) {
			final AuditableMixin auditable = (AuditableMixin) item;
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("project-name", item.getProject().getName());
			res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put("test-case-id", item.getId());
			res.put("test-case-ref", item.getReference());
			res.put("test-case-label", item.getName());
			res.put("editable", isTestCaseEditable(item));
			res.put("test-case-weight", formatImportance(item.getImportance(), locale));
			res.put("test-case-requirement-nb", item.getVerifiedRequirementVersions().size());
			res.put("test-case-teststep-nb", item.getSteps().size());
			res.put("test-case-iteration-nb","");
			res.put("test-case-attachment-nb", item.getAllAttachments().size());
			res.put("test-case-created-by", auditable.getCreatedBy());
			res.put("test-case-modified-by", auditable.getLastModifiedBy());
			res.put("empty-openinterface2-holder", " ");
			res.put("empty-opentree-holder", " ");
			return res;
		}
	}
	
	@RequestMapping(value = "/customFields", method = RequestMethod.GET)
	public SearchInputPanelModel GetCustomFielModel(){
		List<CustomField> customFields = advancedSearchService.findAllQueryableCustomFieldsByBoundEntityType(BindableEntity.TEST_CASE);
		return convertToSearchInputPanelModel(customFields);
	}
	
	private SearchInputPanelModel convertToSearchInputPanelModel(List<CustomField> customFields){
		SearchInputPanelModel model = new SearchInputPanelModel();
		for(CustomField customField : customFields){
			if(org.squashtest.tm.domain.customfield.InputType.DROPDOWN_LIST.equals(customField.getInputType())){
				SingleSelectField selectField = customFieldManager.findSingleSelectFieldById(customField.getId());
				model.getFields().add(convertToSearchInputFieldModel(selectField));
			} 
		}
		return model;
	}
	
	private SearchInputFieldModel convertToSearchInputFieldModel(SingleSelectField selectField){
		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<SearchInputPossibleValueModel>();
		for(CustomFieldOption option : selectField.getOptions()){
			possibleValues.add(new SearchInputPossibleValueModel(option.getLabel(), option.getCode()));
		}
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType("multiselect");
		model.setName(selectField.getLabel());
		model.setPossibleValues(possibleValues);
		return model;
	}
		
	private String convertCustomFieldInputType(org.squashtest.tm.domain.customfield.InputType inputType){
		
		String result = "textfield";
		
		if(org.squashtest.tm.domain.customfield.InputType.CHECKBOX.equals(inputType)){
			result = "multiselect";
		} else if (org.squashtest.tm.domain.customfield.InputType.DATE_PICKER.equals(inputType)){
			result = "datefields";
		} else if (org.squashtest.tm.domain.customfield.InputType.DROPDOWN_LIST.equals(inputType)){
			result = "multiselect";
		} 
		
		return result;
	}
}
