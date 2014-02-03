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

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;

@Component
public class RequirementVersionSearchInterfaceDescription extends SearchInterfaceDescription {
	@Inject
	private RequirementVersionAdvancedSearchService advancedSearchService;

	public SearchInputPanelModel createRequirementInformationPanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.generalinfos.panel.title", locale));
		panel.setOpen(true);
		panel.setId("general-information");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-information");

		SearchInputFieldModel idField = new SearchInputFieldModel("requirement.id", getMessageSource()
				.internationalize("label.id", locale), TEXTFIELD);
		panel.addField(idField);

		SearchInputFieldModel referenceField = new SearchInputFieldModel("reference", getMessageSource()
				.internationalize("label.reference", locale), TEXTFIELD);
		panel.addField(referenceField);

		SearchInputFieldModel labelField = new SearchInputFieldModel("name", getMessageSource().internationalize(
				"label.Label", locale), TEXTFIELD);
		panel.addField(labelField);

		SearchInputFieldModel descriptionField = new SearchInputFieldModel("description", getMessageSource()
				.internationalize("label.Description", locale), TEXTAREA);
		panel.addField(descriptionField);

		return panel;
	}

	public SearchInputPanelModel createRequirementAttributePanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.attributes.panel.title", locale));
		panel.setOpen(true);
		panel.setId("attributes");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-attributes");

		SearchInputFieldModel criticalityField = new SearchInputFieldModel("criticality", getMessageSource()
				.internationalize("requirement.criticality.label", locale), MULTISELECT);
		panel.addField(criticalityField);

		Map<String, String> map = levelComboBuilder(RequirementCriticality.values()).useLocale(locale).buildMap();

		int i = 1;
		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel importanceOption = new SearchInputPossibleValueModel(entry.getValue(), i
					+ "-" + entry.getKey());
			criticalityField.addPossibleValue(importanceOption);
			i++;
		}

		SearchInputFieldModel categoryField = new SearchInputFieldModel("category", getMessageSource()
				.internationalize("requirement.category.label", locale), MULTISELECT);
		panel.addField(categoryField);

		map = internationalizableComboBuilder(RequirementCategory.values()).useLocale(locale).buildMap();

		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel natureOption = new SearchInputPossibleValueModel(entry.getValue(),
					entry.getKey());
			categoryField.addPossibleValue(natureOption);
		}

		SearchInputFieldModel statusField = new SearchInputFieldModel("status", getMessageSource().internationalize(
				"requirement.status.combo.label", locale), MULTISELECT);
		panel.addField(statusField);

		map = levelComboBuilder(RequirementStatus.values()).useLocale(locale).buildMap();

		int j = 1;
		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel statusOption = new SearchInputPossibleValueModel(entry.getValue(), j + "-"
					+ entry.getKey());
			statusField.addPossibleValue(statusOption);
			j++;
		}

		return panel;
	}

	public SearchInputPanelModel createRequirementVersionPanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.requirement.versions.panel.title", locale));
		panel.setOpen(true);
		panel.setId("versions");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-version");

		SearchInputFieldModel versionField = new SearchInputFieldModel("isCurrentVersion", getMessageSource()
				.internationalize("search.requirement.content.version.label", locale), RADIOBUTTON);
		versionField.setIgnoreBridge(true);
		panel.addField(versionField);

		versionField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.requirement.allVersions", locale), EMPTY, true));

		versionField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.requirement.onlyLastVersion", locale), ATLEASTONE));

		return panel;
	}

	public SearchInputPanelModel createRequirementContentPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.content.panel.title", locale));
		panel.setOpen(true);
		panel.setId("content");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-content");

		SearchInputFieldModel descriptionField = new SearchInputFieldModel("hasDescription", "", EXISTS);
		panel.addField(descriptionField);

		descriptionField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.requirement.emptyDescription", locale), NONE));

		descriptionField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.requirement.nonemptyDescription", locale), ATLEASTONE));

		SearchInputFieldModel attachmentField = new SearchInputFieldModel("attachments", getMessageSource()
				.internationalize("search.testcase.content.attachment.label", locale), EXISTS);
		panel.addField(attachmentField);

		attachmentField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.content.attachment.atleastone", locale), ATLEASTONE));
		attachmentField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.testcase.content.attachment.none", locale), NONE));

		return panel;
	}

	public SearchInputPanelModel createRequirementAssociationPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();

		panel.setTitle(getMessageSource().internationalize("search.testcase.association.panel.title", locale));
		panel.setOpen(true);
		panel.setId("association");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-associations");

		SearchInputFieldModel testcasesField = new SearchInputFieldModel("testcases", getMessageSource()
				.internationalize("search.requirement.association.testcase.label", locale), RANGE);
		panel.addField(testcasesField);

		SearchInputFieldModel childRequirementsField = new SearchInputFieldModel("requirement.children",
				getMessageSource().internationalize("search.requirement.association.childRequirement.label", locale),
				EXISTS);
		panel.addField(childRequirementsField);

		childRequirementsField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.requirement.association.childRequirement.atleastone", locale), ATLEASTONE));

		childRequirementsField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.requirement.association.childRequirement.none", locale), NONE));

		SearchInputFieldModel parentRequirementsField = new SearchInputFieldModel("parent", getMessageSource()
				.internationalize("search.requirement.association.parentRequirement.label", locale), EXISTS);
		panel.addField(parentRequirementsField);

		parentRequirementsField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.requirement.association.parentRequirement.atleastone", locale), ATLEASTONE));

		parentRequirementsField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
				"search.requirement.association.parentRequirement.none", locale), NONE));

		return panel;
	}

	public SearchInputPanelModel createRequirementHistoryPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.history.panel.title", locale));
		panel.setOpen(true);
		panel.setId("history");
		panel.setLocation("column3");
		panel.addCssClass("search-icon-history-blue");

		SearchInputFieldModel createdByField = new SearchInputFieldModel("createdBy", getMessageSource()
				.internationalize("search.testcase.history.createdBy.label", locale), MULTISELECT);
		panel.addField(createdByField);

		List<String> users = advancedSearchService.findAllUsersWhoCreatedRequirementVersions();
		for (String user : users) {
			createdByField.addPossibleValue(new SearchInputPossibleValueModel(user, user));
		}

		SearchInputFieldModel createdOnField = new SearchInputFieldModel("createdOn", getMessageSource()
				.internationalize("search.testcase.history.createdOn.label", locale), DATE);
		panel.addField(createdOnField);

		SearchInputFieldModel modifiedByField = new SearchInputFieldModel("modifiedBy", getMessageSource()
				.internationalize("search.testcase.history.modifiedBy.label", locale), MULTISELECT);
		panel.addField(modifiedByField);

		users = advancedSearchService.findAllUsersWhoModifiedRequirementVersions();
		for (String user : users) {
			if (user == null || "".equals(user.trim())) {
				modifiedByField.addPossibleValue(new SearchInputPossibleValueModel(getMessageSource().internationalize(
						"label.NeverModified", locale), ""));
			} else {
				modifiedByField.addPossibleValue(new SearchInputPossibleValueModel(user, user));
			}
		}

		SearchInputFieldModel modifiedOnField = new SearchInputFieldModel("modifiedOn", getMessageSource()
				.internationalize("search.testcase.history.modifiedOn.label", locale), DATE);
		panel.addField(modifiedOnField);

		return panel;
	}

	public SearchInputPanelModel createRequirementPerimeterPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.perimeter.panel.title", locale));
		panel.setOpen(true);
		panel.setId("perimeter");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-perimeter-blue");

		SearchInputFieldModel projectField = new SearchInputFieldModel("requirement.project.id", getMessageSource()
				.internationalize("search.testcase.perimeter.field.title", locale), MULTISELECT);
		panel.addField(projectField);

		List<Project> projects = this.getProjectFilterService().getAllProjects();
		for (Project project : projects) {
			SearchInputPossibleValueModel projectOption = new SearchInputPossibleValueModel(project.getName(), project
					.getId().toString());
			projectField.addPossibleValue(projectOption);
		}

		return panel;
	}
}
