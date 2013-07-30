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

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignExportCSVModel;
import org.squashtest.tm.domain.campaign.CampaignExportCSVModel.Row;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.customfield.CustomCustomFieldManagerService;
import org.squashtest.tm.service.library.AdvancedSearchService;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.controller.RequestHeaders;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseImportanceJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

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
	
	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;
	

	private ProjectFilterModificationService projectFilterService;
	
	@ServiceReference
	public void setProjectFilterModificationService(ProjectFilterModificationService service){
		this.projectFilterService = service;
	}

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
	

	@RequestMapping(value = "/input", method = RequestMethod.GET, headers = RequestHeaders.CONTENT_JSON)
	@ResponseBody
	public SearchInputInterfaceModel getSearchInputInterfaceModel(Locale locale){
		
		SearchInputInterfaceModel model = new SearchInputInterfaceModel();

		//General infos
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle("search.testcase.generalinfos.panel.title");
		panel.setOpen(true);
		panel.setId("general-information");
		model.addPanel(panel);

		SearchInputFieldModel labelField = new SearchInputFieldModel("test-case-label","label.Label","textfield");
		panel.addField(labelField);
		SearchInputFieldModel idField = new SearchInputFieldModel("test-case-id","label.id","textfield");
		panel.addField(idField);
		SearchInputFieldModel referenceField = new SearchInputFieldModel("test-case-reference","label.reference","textfield");
		panel.addField(referenceField);
		SearchInputFieldModel descriptionField = new SearchInputFieldModel("test-case-description","label.Description","textarea");
		panel.addField(descriptionField);
		
		//Importance
		panel = new SearchInputPanelModel();
		panel.setTitle("search.testcase.importance.panel.title");
		panel.setOpen(false);
		panel.setId("importance");
		model.addPanel(panel);
			
		SearchInputFieldModel importanceField = new SearchInputFieldModel("test-case-importance","test-case.importance.label","multiselect");
		panel.addField(importanceField);
		
		Map<String,String> map = importanceComboBuilderProvider.get().useLocale(locale).buildMap();
		for(String key : map.keySet()){
			SearchInputPossibleValueModel importanceOption = new SearchInputPossibleValueModel(map.get(key),key);
			importanceField.addPossibleValue(importanceOption);
		}
		
		//Prerequisite
		panel = new SearchInputPanelModel();
		panel.setTitle("search.testcase.prerequisite.panel.title");
		panel.setOpen(true);
		panel.setId("prerequisite");
		model.addPanel(panel);

		SearchInputFieldModel prerequisiteField = new SearchInputFieldModel("test-case-prerequisite","test-case.prerequisite.label","textarea");
		panel.addField(prerequisiteField);
		
		//Associations
		panel = new SearchInputPanelModel();
		panel.setTitle("search.testcase.association.panel.title");
		panel.setOpen(false);
		panel.setId("association");
		model.addPanel(panel);

		//Projects
		panel = new SearchInputPanelModel();
		panel.setTitle("search.testcase.project.panel.title");
		panel.setOpen(false);
		panel.setId("project");
		model.addPanel(panel);

		SearchInputFieldModel projectField = new SearchInputFieldModel("test-case-project","test-case.importance.label","multiselect");
		panel.addField(projectField);
		
		List<Project> projects = this.projectFilterService.getAllProjects();
		for(Project project : projects){
			SearchInputPossibleValueModel projectOption = new SearchInputPossibleValueModel(project.getName(),project.getId().toString());
			projectField.addPossibleValue(projectOption);
		}

		//Creation/Modification
		panel = new SearchInputPanelModel();
		panel.setTitle("search.testcase.creation.panel.title");
		panel.setOpen(true);
		panel.setId("creation");
		model.addPanel(panel);

		SearchInputFieldModel creationOrModificationField = new SearchInputFieldModel("test-case-creation","test-case.importance.label","multiselect");
		panel.addField(creationOrModificationField);
		
		SearchInputPossibleValueModel createdByOption = new SearchInputPossibleValueModel("Créé par","CREATED");
		SearchInputPossibleValueModel modifiedByOption = new SearchInputPossibleValueModel("Modifié par","MODIFIED");
		creationOrModificationField.addPossibleValue(createdByOption);
		creationOrModificationField.addPossibleValue(modifiedByOption);
			
		SearchInputFieldModel byField = new SearchInputFieldModel("test-case-by","test-case.importance.label","multiselect");
		panel.addField(byField);
		
		//CUF
		panel = getCustomFielModel();
		panel.setTitle("search.testcase.cuf.panel.title");
		panel.setOpen(false);
		panel.setId("cuf");
		model.addPanel(panel);
		
		return model;
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
	
	public SearchInputPanelModel getCustomFielModel(){
		List<CustomField> customFields = advancedSearchService.findAllQueryableCustomFieldsByBoundEntityType(BindableEntity.TEST_CASE);
		return convertToSearchInputPanelModel(customFields);
	}
	
	private SearchInputPanelModel convertToSearchInputPanelModel(List<CustomField> customFields){
		SearchInputPanelModel model = new SearchInputPanelModel();
		for(CustomField customField : customFields){
			if(org.squashtest.tm.domain.customfield.InputType.DROPDOWN_LIST.equals(customField.getInputType())){
				SingleSelectField selectField = customFieldManager.findSingleSelectFieldById(customField.getId());
				model.getFields().add(convertToSearchInputFieldModel(selectField));
			} else if(org.squashtest.tm.domain.customfield.InputType.PLAIN_TEXT.equals(customField.getInputType())){
				model.getFields().add(convertToSearchInputFieldModel(customField));
			}
		}
		return model;
	}
	
	private SearchInputFieldModel convertToSearchInputFieldModel(CustomField customField){
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType("textfield");
		model.setTitle(customField.getLabel());
		model.setInternationalized(false);
		model.setId(customField.getCode());
		return model;
	}
	private SearchInputFieldModel convertToSearchInputFieldModel(SingleSelectField selectField){
		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<SearchInputPossibleValueModel>();
		for(CustomFieldOption option : selectField.getOptions()){
			possibleValues.add(new SearchInputPossibleValueModel(option.getLabel(), option.getCode()));
		}
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType("multiselect");
		model.setTitle(selectField.getLabel());
		model.setInternationalized(false);
		model.setPossibleValues(possibleValues);
		return model;
	}
}
