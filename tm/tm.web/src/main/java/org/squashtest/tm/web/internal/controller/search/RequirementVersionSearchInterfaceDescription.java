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
import javax.inject.Provider;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.library.AdvancedSearchService;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.web.internal.controller.requirement.RequirementCategoryComboDataBuilder;
import org.squashtest.tm.web.internal.controller.requirement.RequirementCriticalityComboDataBuilder;
import org.squashtest.tm.web.internal.controller.requirement.RequirementStatusComboDataBuilder;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Component
public class RequirementVersionSearchInterfaceDescription {

	private static final String TEXTFIELD = "textfield";
	private static final String TEXTAREA = "textarea";
	private static final String RANGE = "range";
	private static final String EXISTS = "exists";
	private static final String DATE = "date";
	private static final String MULTISELECT = "multiselect";
	private static final String RADIOBUTTON = "radiobutton";
	private static final String ATLEASTONE = "1";
	private static final String NONE = "0";
	private static final String EMPTY = "";
	
	@Inject
	private InternationalizationHelper messageSource;
	
	@Inject
	private AdvancedSearchService advancedSearchService;
	
	@Inject
	private Provider<RequirementCriticalityComboDataBuilder> criticalityComboBuilderProvider;
	
	@Inject
	private Provider<RequirementCategoryComboDataBuilder> categoryComboBuilderProvider;
	
	@Inject
	private Provider<RequirementStatusComboDataBuilder> reqStatusComboBuilderProvider;
	
	private ProjectFilterModificationService projectFilterService;

	@ServiceReference
	public void setProjectFilterModificationService(
			ProjectFilterModificationService service) {
		this.projectFilterService = service;
	}
	
	public SearchInputPanelModel createRequirementInformationPanel(Locale locale){
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize("search.testcase.generalinfos.panel.title", locale));
		panel.setOpen(true);
		panel.setId("general-information");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-information");
		
		SearchInputFieldModel idField = new SearchInputFieldModel("requirement.id",
				messageSource.internationalize("label.id", locale), TEXTFIELD);
		panel.addField(idField);
		
		SearchInputFieldModel referenceField = new SearchInputFieldModel(
				"reference", messageSource.internationalize("label.reference",
						locale), TEXTFIELD);
		panel.addField(referenceField);
		
		SearchInputFieldModel labelField = new SearchInputFieldModel("name",
				messageSource.internationalize("label.Label", locale),
				TEXTFIELD);
		panel.addField(labelField);
		
		SearchInputFieldModel descriptionField = new SearchInputFieldModel(
				"description", messageSource.internationalize(
						"label.Description", locale), TEXTAREA);
		panel.addField(descriptionField);
		
		return panel;
	}
	
	public SearchInputPanelModel createRequirementAttributePanel(Locale locale){
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize("search.testcase.attributes.panel.title", locale));
		panel.setOpen(true);
		panel.setId("attributes");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-attributes");
		
		SearchInputFieldModel criticalityField = new SearchInputFieldModel(
				"criticality", messageSource.internationalize(
						"requirement.criticality.label", locale), MULTISELECT);
		panel.addField(criticalityField);

		Map<String, String> map = criticalityComboBuilderProvider.get()
				.useLocale(locale).buildMap();

		int i = 1;
		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel importanceOption = new SearchInputPossibleValueModel(
					entry.getValue(), i + "-" + entry.getKey());
			criticalityField.addPossibleValue(importanceOption);
			i++;
		}

		SearchInputFieldModel categoryField = new SearchInputFieldModel("category",
				messageSource
						.internationalize("requirement.category.label", locale),
				MULTISELECT);
		panel.addField(categoryField);

		map = categoryComboBuilderProvider.get().useLocale(locale).buildMap();

		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel natureOption = new SearchInputPossibleValueModel(
					entry.getValue(), entry.getKey());
			categoryField.addPossibleValue(natureOption);
		}

		SearchInputFieldModel statusField = new SearchInputFieldModel("status",
				messageSource
						.internationalize("requirement.status.combo.label", locale),
				MULTISELECT);
		panel.addField(statusField);

		map = reqStatusComboBuilderProvider.get().useLocale(locale).buildMap();

		int j = 1;
		for (Entry<String, String> entry : map.entrySet()) {
			SearchInputPossibleValueModel statusOption = new SearchInputPossibleValueModel(
					entry.getValue(), j + "-" + entry.getKey());
			statusField.addPossibleValue(statusOption);
			j++;
		}
		 
		return panel;
	}
	
	public  SearchInputPanelModel createRequirementVersionPanel(Locale locale){
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize("search.requirement.versions.panel.title", locale));
		panel.setOpen(true);
		panel.setId("versions");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-version");
		
		SearchInputFieldModel versionField = new SearchInputFieldModel(
				"isCurrentVersion", messageSource.internationalize(
						"search.requirement.content.version.label", locale),
				RADIOBUTTON);
		versionField.setIgnoreBridge(true);
		panel.addField(versionField);

		versionField
		.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.requirement.allVersions",
						locale), EMPTY, true));
		
		versionField
				.addPossibleValue(new SearchInputPossibleValueModel(
						messageSource.internationalize(
								"search.requirement.onlyLastVersion",
								locale), ATLEASTONE));
	

		
		return panel;
	}
	
	public  SearchInputPanelModel createRequirementContentPanel(Locale locale){

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize("search.testcase.content.panel.title", locale));
		panel.setOpen(true);
		panel.setId("content");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-content");
		
		SearchInputFieldModel descriptionField = new SearchInputFieldModel(
				"hasDescription", "", EXISTS);
		panel.addField(descriptionField);

		descriptionField
		.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.requirement.emptyDescription",
						locale), NONE));
		
		descriptionField
				.addPossibleValue(new SearchInputPossibleValueModel(
						messageSource.internationalize(
								"search.requirement.nonemptyDescription",
								locale), ATLEASTONE));
	

		SearchInputFieldModel attachmentField = new SearchInputFieldModel(
				"attachments", messageSource.internationalize(
						"search.testcase.content.attachment.label", locale),
				EXISTS);
		panel.addField(attachmentField);

		attachmentField
				.addPossibleValue(new SearchInputPossibleValueModel(
						messageSource.internationalize(
								"search.testcase.content.attachment.atleastone",
								locale), ATLEASTONE));
		attachmentField
				.addPossibleValue(new SearchInputPossibleValueModel(
						messageSource.internationalize(
								"search.testcase.content.attachment.none",
								locale), NONE));

		
		return panel;
	}
	
	public  SearchInputPanelModel createRequirementAssociationPanel(Locale locale){
		
		SearchInputPanelModel panel = new SearchInputPanelModel();

		panel.setTitle(messageSource.internationalize("search.testcase.association.panel.title", locale));
		panel.setOpen(true);
		panel.setId("association");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-associations");
		
		SearchInputFieldModel testcasesField = new SearchInputFieldModel(
				"testcases",
				messageSource
						.internationalize(
								"search.requirement.association.testcase.label",
								locale), RANGE);
		panel.addField(testcasesField);

		SearchInputFieldModel childRequirementsField = new SearchInputFieldModel(
				"requirement.children",
				messageSource.internationalize("search.requirement.association.childRequirement.label", locale),
				EXISTS);
		panel.addField(childRequirementsField);
		
		childRequirementsField
		.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.requirement.association.childRequirement.atleastone",
						locale), ATLEASTONE));
		
		childRequirementsField
		.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.requirement.association.childRequirement.none",
						locale), NONE));

		SearchInputFieldModel parentRequirementsField = new SearchInputFieldModel(
				"parent",
				messageSource.internationalize("search.requirement.association.parentRequirement.label", locale),
				EXISTS);
		panel.addField(parentRequirementsField);
		
		parentRequirementsField
		.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.requirement.association.parentRequirement.atleastone",
						locale), ATLEASTONE));
		
		parentRequirementsField
		.addPossibleValue(new SearchInputPossibleValueModel(
				messageSource.internationalize(
						"search.requirement.association.parentRequirement.none",
						locale), NONE));
		
		
		return panel;
	}
	
	public  SearchInputPanelModel createRequirementHistoryPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize(
				"search.testcase.history.panel.title", locale));
		panel.setOpen(true);
		panel.setId("history");
		panel.setLocation("column3");
		panel.addCssClass("search-icon-history-blue");

		SearchInputFieldModel createdByField = new SearchInputFieldModel(
				"createdBy", messageSource.internationalize(
						"search.testcase.history.createdBy.label", locale),
				MULTISELECT);
		panel.addField(createdByField);

		List<String> users = advancedSearchService.findAllUsersWhoCreatedRequirementVersions();
		for (String user : users) {
			createdByField.addPossibleValue(new SearchInputPossibleValueModel(
					user, user));
		}

		SearchInputFieldModel createdOnField = new SearchInputFieldModel(
				"createdOn", messageSource.internationalize(
						"search.testcase.history.createdOn.label", locale),
				DATE);
		panel.addField(createdOnField);

		SearchInputFieldModel modifiedByField = new SearchInputFieldModel(
				"modifiedBy", messageSource.internationalize(
						"search.testcase.history.modifiedBy.label", locale),
				MULTISELECT);
		panel.addField(modifiedByField);

		users = advancedSearchService.findAllUsersWhoModifiedRequirementVersions();
		for (String user : users) {
			if (user == null || "".equals(user.trim())) {
				modifiedByField
						.addPossibleValue(new SearchInputPossibleValueModel(
								messageSource.internationalize(
										"label.NeverModified", locale), ""));
			} else {
				modifiedByField
						.addPossibleValue(new SearchInputPossibleValueModel(
								user, user));
			}
		}
		
		SearchInputFieldModel modifiedOnField = new SearchInputFieldModel(
				"modifiedOn", messageSource.internationalize(
						"search.testcase.history.modifiedOn.label", locale),
				DATE);
		panel.addField(modifiedOnField);

		return panel;
	}
	
	public  SearchInputPanelModel createRequirementPerimeterPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(messageSource.internationalize(
				"search.testcase.perimeter.panel.title", locale));
		panel.setOpen(true);
		panel.setId("perimeter");
		panel.setLocation("column2");
		panel.addCssClass("search-icon-perimeter-blue");

		SearchInputFieldModel projectField = new SearchInputFieldModel(
				"requirement.project.id", messageSource.internationalize(
						"search.testcase.perimeter.field.title", locale),
				MULTISELECT);
		panel.addField(projectField);

		List<Project> projects = this.projectFilterService.getAllProjects();
		for (Project project : projects) {
			SearchInputPossibleValueModel projectOption = new SearchInputPossibleValueModel(
					project.getName(), project.getId().toString());
			projectField.addPossibleValue(projectOption);
		}

		return panel;
	}
}
