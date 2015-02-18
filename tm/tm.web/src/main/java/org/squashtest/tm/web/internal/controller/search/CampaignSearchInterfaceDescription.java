/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.web.internal.controller.search.SearchInterfaceDescription.OptionBuilder;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Component
public class CampaignSearchInterfaceDescription extends SearchInterfaceDescription {


	@Inject
	private ProjectFinder projectFinder;

	public SearchInputPanelModel createGeneralInfoPanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.generalinfos.panel.title", locale));
		panel.setOpen(true);
		panel.setId("general-information-fullsize");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-information");

		SearchInputFieldModel labelField = new SearchInputFieldModel("name", getMessageSource().internationalize(
				"label.Label", locale), TEXTFIELD);
		panel.addField(labelField);
		SearchInputFieldModel referenceField = new SearchInputFieldModel("reference", getMessageSource()
				.internationalize("label.reference", locale), TEXTFIELDREFERENCE);
		panel.addField(referenceField);
		SearchInputFieldModel descriptionField = new SearchInputFieldModel("description", getMessageSource()
				.internationalize("label.Description", locale), TEXTAREA);
		panel.addField(descriptionField);
		SearchInputFieldModel prerequisiteField = new SearchInputFieldModel("prerequisite", getMessageSource()
				.internationalize("test-case.prerequisite.label", locale), TEXTAREA);
		panel.addField(prerequisiteField);

		return panel;
	}

	public SearchInputPanelModel createAttributePanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.attributes.panel.title", locale));
		panel.setOpen(true);
		panel.setId("attributes");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-attributes");

		SearchInputFieldModel importanceField = new SearchInputFieldModel("importance", getMessageSource()
				.internationalize("test-case.importance.label", locale), MULTISELECT);
		panel.addField(importanceField);

		List<SearchInputPossibleValueModel> importanceOptions = levelComboBuilder(TestCaseImportance.values())
				.useLocale(locale).build();
		importanceField.addPossibleValues(importanceOptions);

		// **************** /natures and types ************************


		SearchInputFieldModel natureField = buildNatureFieldModel(locale);
		panel.addField(natureField);

		SearchInputFieldModel typeField = buildTypeFieldModel(locale);
		panel.addField(typeField);


		// *************** /natures and types ****************************

		SearchInputFieldModel assignmentField = new SearchInputFieldModel("assignment", getMessageSource()
				.internationalize("search.execution.assignation", locale), MULTIAUTOCOMPLETE);
		panel.addField(assignmentField);

		/* TODO : Get all assignmentable users */

		return panel;
	}

	public SearchInputPanelModel createAssociationPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();

		panel.setTitle(getMessageSource().internationalize("search.testcase.association.panel.title", locale));
		panel.setOpen(true);
		panel.setId("association");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-associations");

		SearchInputFieldModel requirementsField = new SearchInputFieldModel("requirements", getMessageSource()
				.internationalize("search.testcase.association.requirement.label", locale), RANGE);
		panel.addField(requirementsField);

		SearchInputFieldModel iterationsField = new SearchInputFieldModel("iterations", getMessageSource()
				.internationalize("search.testcase.association.iteration.label", locale), EXISTS);
		panel.addField(iterationsField);

		OptionBuilder optionBuilder = optionBuilder(locale);
		iterationsField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.association.iteration.atleastone")
				.optionKey(ATLEASTONE).build());
		iterationsField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.association.iteration.none")
				.optionKey(NONE).build());

		SearchInputFieldModel executionsField = new SearchInputFieldModel("executions", getMessageSource()
				.internationalize("search.testcase.association.execution.label", locale), EXISTS);
		panel.addField(executionsField);

		executionsField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.association.execution.atleastone")
				.optionKey(ATLEASTONE).build());
		executionsField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.association.execution.none")
				.optionKey(NONE).build());

		SearchInputFieldModel issuesField = new SearchInputFieldModel("issues", getMessageSource().internationalize(
				"search.testcase.association.issue.label", locale), RANGE);
		panel.addField(issuesField);

		return panel;
	}

	public SearchInputPanelModel createExecutionPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.execution.label", locale));
		panel.setOpen(true);
		panel.setId("execution");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-execution");

		SearchInputFieldModel createdOnField = new SearchInputFieldModel("createdOn", getMessageSource()
				.internationalize("search.execution.executed.label", locale), DATE);
		panel.addField(createdOnField);

		OptionBuilder optionBuilder = optionBuilder(locale);

		SearchInputFieldModel authorizedUsersField = new SearchInputFieldModel("authorizedUsersField",
				getMessageSource()
				.internationalize("search.execution.executedby.label", locale), MULTIAUTOCOMPLETE);
		panel.addField(authorizedUsersField);

		/*
		 * TODO List<String> users = advancedSearchService.findAllAuthorizedUsersForACampaign(); for (String user :
		 * users) { createdByField.addPossibleValue(optionBuilder.label(user).optionKey(user).build()); }
		 */

		SearchInputFieldModel statusField = new SearchInputFieldModel("status", getMessageSource().internationalize(
				"test-case.status.label", locale), MULTISELECT);
		panel.addField(statusField);

		List<SearchInputPossibleValueModel> statusOptions = levelComboBuilder(ExecutionStatus.values())
				.useLocale(locale).build();
		statusField.addPossibleValues(statusOptions);

		/* TODO : get the mode list */
		SearchInputFieldModel executionModeField = new SearchInputFieldModel("executionMode", getMessageSource()
				.internationalize("search.execution.mode.label", locale), EXISTSBEFORE);
		panel.addField(executionModeField);

		List<SearchInputPossibleValueModel> modeOptions = levelComboBuilder(TestCaseExecutionMode.values())
				.useLocale(locale).build();
		executionModeField.addPossibleValues(modeOptions);


		SearchInputFieldModel lastExecutionField = new SearchInputFieldModel("lastExecution", getMessageSource()
				.internationalize(
				"search.testcase.content.dataset.label", locale), EXISTS);
		panel.addField(lastExecutionField);
		/*
		 * TODO LastExecution
		 */

		SearchInputFieldModel issuesField = new SearchInputFieldModel("issues", getMessageSource().internationalize(
				"search.testcase.association.issue.label", locale), RANGE);
		panel.addField(issuesField);
		/*
		 * TODO issue
		 */

		return panel;
	}

	public SearchInputPanelModel createPerimeterPanel(Locale locale) {
		return perimeterPanelBuilder(locale).cssClass("search-icon-perimeter").htmlId("project.id").build();
	}

	public SearchInputPanelModel createRequirementPerimeterPanel(Locale locale) {
		return perimeterPanelBuilder(locale).cssClass("search-icon-perimeter").htmlId("requirement.project.id").build();
	}

	public SearchInputPanelModel createContentPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.content.panel.title", locale));
		panel.setOpen(true);
		panel.setId("content");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-content");

		SearchInputFieldModel teststepField = new SearchInputFieldModel("steps", getMessageSource().internationalize(
				"label.customField.bindableEntity.EXECUTION_STEP", locale), RANGE);
		panel.addField(teststepField);

		SearchInputFieldModel datasetField = new SearchInputFieldModel("datasets", getMessageSource().internationalize(
				"search.testcase.content.dataset.label", locale), EXISTS);
		panel.addField(datasetField);

		OptionBuilder optionBuilder = optionBuilder(locale);

		datasetField.addPossibleValue(optionBuilder
				.labelI18nKey("search.requirement.association.childRequirement.atleastone").optionKey(ATLEASTONE)
				.build());

		datasetField.addPossibleValue(optionBuilder
				.labelI18nKey("search.requirement.association.childRequirement.none").optionKey(NONE).build());

		SearchInputFieldModel attachmentField = new SearchInputFieldModel("attachments", getMessageSource()
				.internationalize("search.testcase.content.attachment.label", locale), EXISTS);
		panel.addField(attachmentField);

		attachmentField.addPossibleValue(optionBuilder
				.labelI18nKey("search.requirement.association.childRequirement.atleastone").optionKey(ATLEASTONE)
				.build());

		attachmentField.addPossibleValue(optionBuilder
				.labelI18nKey("search.requirement.association.childRequirement.none").optionKey(NONE).build());

		return panel;
	}


	private SearchInputFieldModel buildNatureFieldModel(Locale locale){


		SearchInputFieldModel natureField = new SearchInputFieldModel("nature", getMessageSource().internationalize(
				"test-case.nature.label", locale), MULTICASCADEFLAT);


		Collection<Project> readableProjects = projectFinder.findAllReadable();

		Collection<InfoList> natures = new ArrayList<InfoList>(readableProjects.size());

		for (Project p : readableProjects){
			natures.add(p.getTestCaseNatures());
		}

		populateInfoListFieldModel(natureField, natures, locale);

		return natureField;

	}

	private SearchInputFieldModel buildTypeFieldModel(Locale locale){


		SearchInputFieldModel typeField = new SearchInputFieldModel("type", getMessageSource().internationalize(
				"test-case.type.label", locale), MULTICASCADEFLAT);


		Collection<Project> readableProjects = projectFinder.findAllReadable();

		Collection<InfoList> types = new ArrayList<InfoList>(readableProjects.size());

		for (Project p : readableProjects){
			types.add(p.getTestCaseTypes());
		}

		populateInfoListFieldModel(typeField, types, locale);

		return typeField;

	}


	// get ready to puke !
	private void populateInfoListFieldModel(SearchInputFieldModel model, Collection<InfoList> infoLists, Locale locale){

		InternationalizationHelper messages = getMessageSource();
		Map<String, SearchInputPossibleValueModel> listsByListCode = new HashMap<String, SearchInputPossibleValueModel>();

		for (InfoList list : infoLists){
			if (! listsByListCode.containsKey(list.getCode())){

				String listName = messages.getMessage(list.getLabel(), null, list.getLabel(), locale);
				String listCode = list.getCode();
				SearchInputPossibleValueModel listValues = new SearchInputPossibleValueModel(listName, listCode);

				SearchInputFieldModel subInput = new SearchInputFieldModel();

				for (InfoListItem item : list.getItems()){
					String itemName = messages.getMessage(item.getLabel(), null, item.getLabel(), locale);
					String itemCode = item.getCode();
					subInput.addPossibleValue(new SearchInputPossibleValueModel(itemName, itemCode));
				}

				listValues.setSubInput(subInput);

				listsByListCode.put(list.getCode(), listValues);

			}
		}

		model.setPossibleValues(new ArrayList<SearchInputPossibleValueModel>(listsByListCode.values()));

	}
}
