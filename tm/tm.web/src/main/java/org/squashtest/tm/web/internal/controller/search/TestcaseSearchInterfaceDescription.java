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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;


import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;

@Component
public class TestcaseSearchInterfaceDescription extends SearchInterfaceDescription {

	public SearchInputPanelModel createGeneralInfoPanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.generalinfos.panel.title", locale));
		panel.setOpen(true);
		panel.setId("general-information");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-information");

		SearchInputFieldModel idField = new SearchInputFieldModel("id", getMessageSource().internationalize("label.id",
				locale), TEXTFIELD);
		panel.addField(idField);
		SearchInputFieldModel referenceField = new SearchInputFieldModel("reference", getMessageSource().internationalize(
				"label.reference", locale), TEXTFIELD);
		panel.addField(referenceField);
		SearchInputFieldModel labelField = new SearchInputFieldModel("name", getMessageSource().internationalize(
				"label.Label", locale), TEXTFIELD);
		panel.addField(labelField);
		SearchInputFieldModel descriptionField = new SearchInputFieldModel("description",
				getMessageSource().internationalize("label.Description", locale), TEXTAREA);
		panel.addField(descriptionField);
		SearchInputFieldModel prerequisiteField = new SearchInputFieldModel("prerequisite",
				getMessageSource().internationalize("test-case.prerequisite.label", locale), TEXTAREA);
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

		SearchInputFieldModel importanceField = new SearchInputFieldModel("importance", getMessageSource().internationalize(
				"test-case.importance.label", locale), MULTISELECT);
		panel.addField(importanceField);

		Map<String, String> map = levelComboBuilder(TestCaseImportance.values()).useLocale(locale).buildMap();

		int i = 1;
		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel importanceOption = new SearchInputPossibleValueModel(entry.getValue(), i
					+ "-" + entry.getKey());
			importanceField.addPossibleValue(importanceOption);
			i++;
		}

		SearchInputFieldModel natureField = new SearchInputFieldModel("nature", getMessageSource().internationalize(
				"test-case.nature.label", locale), MULTISELECT);
		panel.addField(natureField);

		map = internationalizableComboBuilder(TestCaseNature.values()).useLocale(locale).buildMap();

		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel natureOption = new SearchInputPossibleValueModel(entry.getValue(),
					entry.getKey());
			natureField.addPossibleValue(natureOption);
		}

		SearchInputFieldModel typeField = new SearchInputFieldModel("type", getMessageSource().internationalize(
				"test-case.type.label", locale), MULTISELECT);
		panel.addField(typeField);

		map = internationalizableComboBuilder(TestCaseType.values()).useLocale(locale).buildMap();

		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel typeOption = new SearchInputPossibleValueModel(entry.getValue(),
					entry.getKey());
			typeField.addPossibleValue(typeOption);
		}

		SearchInputFieldModel statusField = new SearchInputFieldModel("status", getMessageSource().internationalize(
				"test-case.status.label", locale), MULTISELECT);
		panel.addField(statusField);

		map = levelComboBuilder(TestCaseStatus.values()).useLocale(locale).buildMap();

		int j = 1;
		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel statusOption = new SearchInputPossibleValueModel(entry.getValue(), j + "-"
					+ entry.getKey());
			statusField.addPossibleValue(statusOption);
			j++;
		}
		return panel;
	}

	public SearchInputPanelModel createAssociationPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();

		panel.setTitle(getMessageSource().internationalize("search.testcase.association.panel.title", locale));
		panel.setOpen(true);
		panel.setId("association");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-associations");

		SearchInputFieldModel requirementsField = new SearchInputFieldModel("requirements",
				getMessageSource().internationalize("search.testcase.association.requirement.label", locale), RANGE);
		panel.addField(requirementsField);

		SearchInputFieldModel iterationsField = new SearchInputFieldModel("iterations", getMessageSource().internationalize(
				"search.testcase.association.iteration.label", locale), EXISTS);
		panel.addField(iterationsField);

		iterationsField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.association.iteration.atleastone", locale), ATLEASTONE));
		iterationsField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.association.iteration.none", locale), NONE));

		SearchInputFieldModel executionsField = new SearchInputFieldModel("executions", getMessageSource().internationalize(
				"search.testcase.association.execution.label", locale), EXISTS);
		panel.addField(executionsField);

		executionsField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.association.execution.atleastone", locale), ATLEASTONE));
		executionsField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.association.execution.none", locale), NONE));

		SearchInputFieldModel issuesField = new SearchInputFieldModel("issues", getMessageSource().internationalize(
				"search.testcase.association.issue.label", locale), RANGE);
		panel.addField(issuesField);

		return panel;
	}

	public SearchInputPanelModel createPerimeterPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.perimeter.panel.title", locale));
		panel.setOpen(true);
		panel.setId("perimeter");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-perimeter");

		SearchInputFieldModel projectField = new SearchInputFieldModel("project.id", getMessageSource().internationalize(
				"search.testcase.perimeter.field.title", locale), MULTISELECT);
		panel.addField(projectField);

		List<Project> projects = this.getProjectFilterService().getAllProjects();
		for (Project project : projects) {
			SearchInputPossibleValueModel projectOption = new SearchInputPossibleValueModel(project.getName(), project
					.getId().toString());
			projectField.addPossibleValue(projectOption);
		}

		return panel;
	}

	public SearchInputPanelModel createRequirementPerimeterPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.perimeter.panel.title", locale));
		panel.setOpen(true);
		panel.setId("perimeter");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-perimeter");

		SearchInputFieldModel projectField = new SearchInputFieldModel("requirement.project.id",
				getMessageSource().internationalize("search.testcase.perimeter.field.title", locale), MULTISELECT);
		panel.addField(projectField);

		List<Project> projects = this.getProjectFilterService().getAllProjects();
		for (Project project : projects) {
			SearchInputPossibleValueModel projectOption = new SearchInputPossibleValueModel(project.getName(), project
					.getId().toString());
			projectField.addPossibleValue(projectOption);
		}

		return panel;
	}

	public SearchInputPanelModel createContentPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.content.panel.title", locale));
		panel.setOpen(true);
		panel.setId("content");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-content");

		SearchInputFieldModel teststepField = new SearchInputFieldModel("steps", getMessageSource().internationalize(
				"search.testcase.content.teststep.label", locale), RANGE);
		panel.addField(teststepField);

		SearchInputFieldModel parameterField = new SearchInputFieldModel("parameters", getMessageSource().internationalize(
				"search.testcase.content.parameter.label", locale), EXISTS);
		panel.addField(parameterField);

		parameterField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.content.parameter.atleastone", locale), ATLEASTONE));
		parameterField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.content.parameter.none", locale), NONE));

		SearchInputFieldModel datasetField = new SearchInputFieldModel("datasets", getMessageSource().internationalize(
				"search.testcase.content.dataset.label", locale), EXISTS);
		panel.addField(datasetField);

		datasetField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.content.dataset.atleastone", locale), ATLEASTONE));
		datasetField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.content.dataset.none", locale), NONE));

		SearchInputFieldModel callstepField = new SearchInputFieldModel("callsteps", getMessageSource().internationalize(
				"search.testcase.content.callstep.label", locale), EXISTS);
		panel.addField(callstepField);

		callstepField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.content.callstep.atleastone", locale), ATLEASTONE));
		callstepField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.content.callstep.none", locale), NONE));

		SearchInputFieldModel attachmentField = new SearchInputFieldModel("attachments",
				getMessageSource().internationalize("search.testcase.content.attachment.label", locale), EXISTS);
		panel.addField(attachmentField);

		attachmentField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.content.attachment.atleastone", locale), ATLEASTONE));
		attachmentField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.content.attachment.none", locale), NONE));

		return panel;
	}

	public SearchInputPanelModel createTestCaseHistoryPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.history.panel.title", locale));
		panel.setOpen(true);
		panel.setId("history");
		panel.setLocation("column3");
		panel.addCssClass("search-icon-history");

		SearchInputFieldModel createdByField = new SearchInputFieldModel("createdBy", getMessageSource().internationalize(
				"search.testcase.history.createdBy.label", locale), MULTISELECT);
		panel.addField(createdByField);

		List<String> users = getAdvancedSearchService().findAllUsersWhoCreatedTestCases();
		for (String user : users) {
			createdByField.addPossibleValue(new SearchInputPossibleValueModel(user, user));
		}

		SearchInputFieldModel createdOnField = new SearchInputFieldModel("createdOn", getMessageSource().internationalize(
				"search.testcase.history.createdOn.label", locale), DATE);
		panel.addField(createdOnField);

		SearchInputFieldModel modifiedByField = new SearchInputFieldModel("modifiedBy", getMessageSource().internationalize(
				"search.testcase.history.modifiedBy.label", locale), MULTISELECT);
		panel.addField(modifiedByField);

		users = getAdvancedSearchService().findAllUsersWhoModifiedTestCases();
		for (String user : users) {
			if (user == null || "".equals(user.trim())) {
				modifiedByField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
						"label.NeverModified", locale), ""));
			} else {
				modifiedByField.addPossibleValue(new SearchInputPossibleValueModel(user, user));
			}
		}

		SearchInputFieldModel modifiedOnField = new SearchInputFieldModel("modifiedOn", getMessageSource().internationalize(
				"search.testcase.history.modifiedOn.label", locale), DATE);
		panel.addField(modifiedOnField);

		return panel;
	}
}
