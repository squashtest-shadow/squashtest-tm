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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Provider;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.customfield.CustomCustomFieldManagerService;
import org.squashtest.tm.service.library.AdvancedSearchService;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.controller.RequestHeaders;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseImportanceJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseNatureJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseStatusJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseTypeJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

@Controller
@RequestMapping("/advanced-search")
public class AdvancedSearchController {

	private static String TEXTFIELD = "textfield";
	private static String TEXTAREA = "textarea";
	private static String RANGE = "range";
	private static String EXISTS = "exists";
	private static String DATE = "date";
	private static String MULTISELECT = "multiselect";
	private static String ATLEASTONE = "1";
	private static String NONE = "0";
	
	@Inject
	private CustomCustomFieldManagerService customFieldManager;
	
	@Inject
	private AdvancedSearchService advancedSearchService;

	@Inject
	private PermissionEvaluationService permissionService;
	
	@Inject
	private IterationModificationService iterationService;
	
	@Inject
	private InternationalizationHelper messageSource;
	
	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;

	@Inject
	private Provider<TestCaseNatureJeditableComboDataBuilder> natureComboBuilderProvider;

	@Inject
	private Provider<TestCaseTypeJeditableComboDataBuilder> typeComboBuilderProvider;
	
	@Inject
	private Provider<TestCaseStatusJeditableComboDataBuilder> statusComboBuilderProvider;
	
	private ProjectFilterModificationService projectFilterService;
	
	@ServiceReference
	public void setProjectFilterModificationService(ProjectFilterModificationService service){
		this.projectFilterService = service;
	}

	private DatatableMapper<String> testCaseSearchResultMapper = new NameBasedMapper(11)
	.mapAttribute("project-name", "name", Project.class)
	.mapAttribute("test-case-id", "id", TestCase.class)	
	.mapAttribute("test-case-ref", "reference", TestCase.class)	
	.mapAttribute("test-case-label", "label", TestCase.class)
	.mapAttribute("test-case-weight", "importance", TestCase.class)	
	//.mapAttribute(TestCase.class, "verifiedRequirementVersions.size", Long.class, "test-case-requirement-nb")
	//.mapAttribute(TestCase.class, "steps.size", Long.class, "test-case-teststep-nb")
	//.mapAttribute(TestCase.class, "weight", Long.class, "test-case-iteration-nb")
	//.mapAttribute(TestCase.class, "allAttachments.size", Long.class, "test-case-attachment-nb")	
	.mapAttribute("test-case-created-by", "audit.createdBy", TestCase.class)
	.mapAttribute("test-case-modified-by", "audit.lastModifiedBy", TestCase.class);


	@RequestMapping( method = RequestMethod.GET, params = "testcase")
	public String getTestCaseSearchTab(Model model) {
		return "test-case-search-input.html";
	}

	@RequestMapping( method = RequestMethod.POST, params = "testcase")
	public String getTestCaseSearchTab(Model model,  @RequestParam String searchModel) {
		model.addAttribute("searchModel", searchModel);
		return "test-case-search-input.html";
	}
	
	@RequestMapping(value = "/results", method = RequestMethod.POST, params = "testcase")
	public String getTestCaseSearchResultPage(Model model, @RequestParam String searchModel) {
		model.addAttribute("searchModel", searchModel);
		return "test-case-search-result.html";
	} 
	
	@RequestMapping(value = "/table", method = RequestMethod.POST, params =  { "model", RequestParams.S_ECHO_PARAM})
	@ResponseBody
	public DataTableModel getTableModel(final DataTableDrawParameters params, final Locale locale, @RequestParam(value = "model") String model) throws JsonParseException, JsonMappingException, IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);

		PagingAndSorting paging = new DataTableSorting(params, testCaseSearchResultMapper);

		PagedCollectionHolder<List<TestCase>> holder = advancedSearchService.searchForTestCases(searchModel, paging);

		return new TestCaseSearchResultDataTableModelHelper(locale, messageSource, permissionService, iterationService).buildDataModel(holder, params.getsEcho());
	}
	
	private SearchInputPanelModel createGeneralInfoPanel(Locale locale){
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize("search.testcase.generalinfos.panel.title", locale));
		panel.setOpen(true);
		panel.setId("general-information");
		panel.setLocation("column1");
		
		SearchInputFieldModel idField = new SearchInputFieldModel("id",messageSource.internationalize("label.id",locale),TEXTFIELD);
		panel.addField(idField);
		SearchInputFieldModel referenceField = new SearchInputFieldModel("reference",messageSource.internationalize("label.reference",locale),TEXTFIELD);
		panel.addField(referenceField);
		SearchInputFieldModel labelField = new SearchInputFieldModel("name",messageSource.internationalize("label.Label",locale),TEXTFIELD);
		panel.addField(labelField);
		SearchInputFieldModel descriptionField = new SearchInputFieldModel("description",messageSource.internationalize("label.Description",locale),TEXTAREA);
		panel.addField(descriptionField);
		SearchInputFieldModel prerequisiteField = new SearchInputFieldModel("prerequisite",messageSource.internationalize("test-case.prerequisite.label",locale),TEXTAREA);
		panel.addField(prerequisiteField);
		
		return panel;
	}
	
	private SearchInputPanelModel createAttributePanel(Locale locale){
		
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize("search.testcase.attributes.panel.title",locale));
		panel.setOpen(true);
		panel.setId("attributes");
		panel.setLocation("column1");
		
		SearchInputFieldModel importanceField = new SearchInputFieldModel("importance",messageSource.internationalize("test-case.importance.label",locale),MULTISELECT);
		panel.addField(importanceField);
		
		Map<String,String> map = importanceComboBuilderProvider.get().useLocale(locale).buildMap();
		
		for(Entry<String, String> entry : map.entrySet()){
			SearchInputPossibleValueModel importanceOption = new SearchInputPossibleValueModel(entry.getValue(),entry.getKey());
			importanceField.addPossibleValue(importanceOption);
		}
		
		SearchInputFieldModel natureField = new SearchInputFieldModel("nature",messageSource.internationalize("test-case.nature.label",locale),MULTISELECT);
		panel.addField(natureField);
		
		map = natureComboBuilderProvider.get().useLocale(locale).buildMap();
		
		for(Entry<String, String> entry : map.entrySet()){
			SearchInputPossibleValueModel natureOption = new SearchInputPossibleValueModel(entry.getValue(),entry.getKey());
			natureField.addPossibleValue(natureOption);
		}
		
		SearchInputFieldModel typeField = new SearchInputFieldModel("type",messageSource.internationalize("test-case.type.label",locale),MULTISELECT);
		panel.addField(typeField);
		
		map = typeComboBuilderProvider.get().useLocale(locale).buildMap();
		
		for(Entry<String, String> entry : map.entrySet()){
			SearchInputPossibleValueModel typeOption = new SearchInputPossibleValueModel(entry.getValue(),entry.getKey());
			typeField.addPossibleValue(typeOption);
		}
		
		SearchInputFieldModel statusField = new SearchInputFieldModel("status",messageSource.internationalize("test-case.status.label",locale),MULTISELECT);
		panel.addField(statusField);	
		
		map = statusComboBuilderProvider.get().useLocale(locale).buildMap();
		
		for(Entry<String, String> entry : map.entrySet()){
			SearchInputPossibleValueModel statusOption = new SearchInputPossibleValueModel(entry.getValue(),entry.getKey());
			statusField.addPossibleValue(statusOption);
		}
		return panel;
	}
	
	private SearchInputPanelModel createAssociationPanel(Locale locale){
		
		SearchInputPanelModel panel = new SearchInputPanelModel();
		
		panel.setTitle(messageSource.internationalize("search.testcase.association.panel.title",locale));
		panel.setOpen(true);
		panel.setId("association");
		panel.setLocation("column2");
		
		SearchInputFieldModel requirementsField = new SearchInputFieldModel("requirements",messageSource.internationalize("search.testcase.association.requirement.label", locale),RANGE);
		panel.addField(requirementsField);
		
		SearchInputFieldModel iterationsField = new SearchInputFieldModel("iterations",messageSource.internationalize("search.testcase.association.iteration.label", locale),EXISTS);
		panel.addField(iterationsField);

		iterationsField.addPossibleValue(new SearchInputPossibleValueModel(messageSource.internationalize("search.testcase.association.iteration.atleastone",locale), ATLEASTONE));
		iterationsField.addPossibleValue(new SearchInputPossibleValueModel(messageSource.internationalize("search.testcase.association.iteration.none",locale), NONE));
		
		SearchInputFieldModel executionsField = new SearchInputFieldModel("executions",messageSource.internationalize("search.testcase.association.execution.label",locale),EXISTS);
		panel.addField(executionsField);

		executionsField.addPossibleValue(new SearchInputPossibleValueModel(messageSource.internationalize("search.testcase.association.execution.atleastone",locale), ATLEASTONE));
		executionsField.addPossibleValue(new SearchInputPossibleValueModel(messageSource.internationalize("search.testcase.association.execution.none",locale), NONE));
		
		SearchInputFieldModel issuesField = new SearchInputFieldModel("issues",messageSource.internationalize("search.testcase.association.issue.label",locale),RANGE);
		panel.addField(issuesField);
		
		return panel;
	}
	
	private SearchInputPanelModel createPerimeterPanel(Locale locale){
	
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize("search.testcase.perimeter.panel.title",locale));
		panel.setOpen(true);
		panel.setId("perimeter");
		panel.setLocation("column2");
	
		SearchInputFieldModel projectField = new SearchInputFieldModel("project.id",messageSource.internationalize("search.testcase.perimeter.field.title",locale),MULTISELECT);
		panel.addField(projectField);
		
		List<Project> projects = this.projectFilterService.getAllProjects();
		for(Project project : projects){
			SearchInputPossibleValueModel projectOption = new SearchInputPossibleValueModel(project.getName(),project.getId().toString());
			projectField.addPossibleValue(projectOption);
		}
		
		return panel;
	}

	private SearchInputPanelModel createContentPanel(Locale locale){
		
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize("search.testcase.content.panel.title",locale));
		panel.setOpen(true);
		panel.setId("content");
		panel.setLocation("column2");
		
		SearchInputFieldModel teststepField = new SearchInputFieldModel("steps",messageSource.internationalize("search.testcase.content.teststep.label",locale),RANGE);
		panel.addField(teststepField);

		SearchInputFieldModel parameterField = new SearchInputFieldModel("parameters",messageSource.internationalize("search.testcase.content.parameter.label",locale),EXISTS);
		panel.addField(parameterField);
		
		parameterField.addPossibleValue(new SearchInputPossibleValueModel(messageSource.internationalize("search.testcase.content.parameter.atleastone",locale), ATLEASTONE));
		parameterField.addPossibleValue(new SearchInputPossibleValueModel(messageSource.internationalize("search.testcase.content.parameter.none",locale), NONE));
		
		SearchInputFieldModel datasetField = new SearchInputFieldModel("datasets",messageSource.internationalize("search.testcase.content.dataset.label",locale),EXISTS);
		panel.addField(datasetField);

		datasetField.addPossibleValue(new SearchInputPossibleValueModel(messageSource.internationalize("search.testcase.content.dataset.atleastone",locale), ATLEASTONE));
		datasetField.addPossibleValue(new SearchInputPossibleValueModel(messageSource.internationalize("search.testcase.content.dataset.none",locale), NONE));
		
		SearchInputFieldModel callstepField = new SearchInputFieldModel("callsteps",messageSource.internationalize("search.testcase.content.callstep.label",locale),EXISTS);
		panel.addField(callstepField);
		
		callstepField.addPossibleValue(new SearchInputPossibleValueModel(messageSource.internationalize("search.testcase.content.callstep.atleastone",locale), ATLEASTONE));
		callstepField.addPossibleValue(new SearchInputPossibleValueModel(messageSource.internationalize("search.testcase.content.callstep.none",locale), NONE));
		
		SearchInputFieldModel attachmentField = new SearchInputFieldModel("attachments",messageSource.internationalize("search.testcase.content.attachment.label",locale),EXISTS);
		panel.addField(attachmentField);

		attachmentField.addPossibleValue(new SearchInputPossibleValueModel(messageSource.internationalize("search.testcase.content.attachment.atleastone",locale), ATLEASTONE));
		attachmentField.addPossibleValue(new SearchInputPossibleValueModel(messageSource.internationalize("search.testcase.content.attachment.none",locale), NONE));
		
		return panel;
	}
	
	private SearchInputPanelModel createHistoryPanel(Locale locale){
		
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize("search.testcase.history.panel.title",locale));
		panel.setOpen(true);
		panel.setId("history");
		panel.setLocation("column3");
	
		SearchInputFieldModel createdByField = new SearchInputFieldModel("createdBy",messageSource.internationalize("search.testcase.history.createdBy.label",locale),MULTISELECT);
		panel.addField(createdByField);

		List<String> users = advancedSearchService.findAllUsersWhoCreatedTestCases();
		for(String user : users){
			createdByField.addPossibleValue(new SearchInputPossibleValueModel(user, user));
		}
		
		SearchInputFieldModel createdOnField = new SearchInputFieldModel("createdOn",messageSource.internationalize("search.testcase.history.createdOn.label",locale),DATE);
		panel.addField(createdOnField);
		
		SearchInputFieldModel modifiedByField = new SearchInputFieldModel("modifiedBy",messageSource.internationalize("search.testcase.history.modifiedBy.label",locale),MULTISELECT);
		panel.addField(modifiedByField);

		users = advancedSearchService.findAllUsersWhoModifiedTestCases();
		for(String user : users){
			modifiedByField.addPossibleValue(new SearchInputPossibleValueModel(user, user));
		}
		
		SearchInputFieldModel modifiedOnField = new SearchInputFieldModel("modifiedOn",messageSource.internationalize("search.testcase.history.modifiedOn.label",locale),DATE);
		panel.addField(modifiedOnField);
		
		return panel;
	}
	
	private SearchInputPanelModel createCUFPanel(Locale locale){
		
		SearchInputPanelModel panel = getCustomFielModel(locale);
		panel.setTitle(messageSource.internationalize("search.testcase.cuf.panel.title",locale));
		panel.setOpen(true);
		panel.setId("cuf");
		panel.setLocation("column3");
		return panel;
	}
	
	@RequestMapping(value = "/input", method = RequestMethod.GET, headers = RequestHeaders.CONTENT_JSON)
	@ResponseBody
	public SearchInputInterfaceModel getSearchInputInterfaceModel(Locale locale){
		
		SearchInputInterfaceModel model = new SearchInputInterfaceModel();

		//Information
		model.addPanel(createGeneralInfoPanel(locale));
		
		//Attributes
		model.addPanel(createAttributePanel(locale));
		
		//Perimeter
		model.addPanel(createPerimeterPanel(locale));

		//Content
		model.addPanel(createContentPanel(locale));

		//Associations
		model.addPanel(createAssociationPanel(locale));

		//Historique
		model.addPanel(createHistoryPanel(locale));
		
		//CUF
		model.addPanel(createCUFPanel(locale));
		
		return model;
	}
	
	private static final class TestCaseSearchResultDataTableModelHelper extends DataTableModelBuilder<TestCase> {
		private InternationalizationHelper messageSource;
		private Locale locale;
		private PermissionEvaluationService permissionService;
		private IterationModificationService iterationService;
		
		private TestCaseSearchResultDataTableModelHelper(Locale locale, InternationalizationHelper messageSource, PermissionEvaluationService permissionService, IterationModificationService iterationService) {
			this.locale = locale;
			this.messageSource = messageSource;
			this.permissionService = permissionService;
			this.iterationService = iterationService;
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
			res.put("test-case-iteration-nb", iterationService.findIterationContainingTestCase(item.getId()).size());
			res.put("test-case-attachment-nb", item.getAllAttachments().size());
			res.put("test-case-created-by", auditable.getCreatedBy());
			res.put("test-case-modified-by", auditable.getLastModifiedBy());
			res.put("empty-openinterface2-holder", " ");
			res.put("empty-opentree-holder", " ");
			return res;
		}
	}
	

	public SearchInputPanelModel getCustomFielModel(Locale locale){
		List<CustomField> customFields = advancedSearchService.findAllQueryableCustomFieldsByBoundEntityType(BindableEntity.TEST_CASE);
		return convertToSearchInputPanelModel(customFields, locale);
	}
	
	private SearchInputPanelModel convertToSearchInputPanelModel(List<CustomField> customFields, Locale locale){
		SearchInputPanelModel model = new SearchInputPanelModel();
		for(CustomField customField : customFields){
			if(org.squashtest.tm.domain.customfield.InputType.DROPDOWN_LIST.equals(customField.getInputType())){
				SingleSelectField selectField = customFieldManager.findSingleSelectFieldById(customField.getId());
				model.getFields().add(convertToSearchInputFieldModel(selectField));
			} else if(org.squashtest.tm.domain.customfield.InputType.PLAIN_TEXT.equals(customField.getInputType())){
				model.getFields().add(convertToSearchInputFieldModel(customField));
			} else if(org.squashtest.tm.domain.customfield.InputType.CHECKBOX.equals(customField.getInputType())){
				model.getFields().add(createCheckBoxField(customField, locale));
			} else if(org.squashtest.tm.domain.customfield.InputType.DATE_PICKER.equals(customField.getInputType())){
				model.getFields().add(createDatePickerField(customField,locale));
			}
		}
		return model;
	}
	
	private SearchInputFieldModel createDatePickerField(CustomField customField, Locale locale) {

		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(DATE);
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel createCheckBoxField(CustomField customField, Locale locale){
		SearchInputFieldModel model = new SearchInputFieldModel();

		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<SearchInputPossibleValueModel>();
		
		possibleValues.add(new SearchInputPossibleValueModel(messageSource.internationalize("squashtm.yesno.true",locale), "true"));
		possibleValues.add(new SearchInputPossibleValueModel(messageSource.internationalize("squashtm.yesno.false",locale), "false"));
		
		model.setPossibleValues(possibleValues);
		model.setInputType(MULTISELECT);
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}
	
	private SearchInputFieldModel convertToSearchInputFieldModel(CustomField customField){
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(TEXTFIELD);
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}
	private SearchInputFieldModel convertToSearchInputFieldModel(SingleSelectField selectField){
		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<SearchInputPossibleValueModel>();
		for(CustomFieldOption option : selectField.getOptions()){
			possibleValues.add(new SearchInputPossibleValueModel(option.getLabel(), option.getCode()));
		}
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(MULTISELECT);
		model.setTitle(selectField.getLabel());
		model.setPossibleValues(possibleValues);
		model.setId(selectField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}
}
