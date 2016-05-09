/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.search.advanced.searchinterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Component
public class RequirementVersionSearchInterfaceDescription extends SearchInterfaceDescription {

	private static final String COLUMN_1 = "column1";

	@Inject
	private RequirementVersionAdvancedSearchService advancedSearchService;

	@Inject
	private ProjectFinder projectFinder;


	public SearchInputPanelModel createRequirementInformationPanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.generalinfos.panel.title", locale));
		panel.setOpen(true);
		panel.setId("general-information");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-information");

		SearchInputFieldModel idField = new SearchInputFieldModel("requirement.id", getMessageSource()
				.internationalize("label.id", locale), TEXTFIELDID);
		panel.addField(idField);

		SearchInputFieldModel referenceField = new SearchInputFieldModel("reference", getMessageSource()
				.internationalize("label.reference", locale), TEXTFIELDREFERENCE);
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
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-attributes");

		SearchInputFieldModel criticalityField = new SearchInputFieldModel("criticality", getMessageSource()
				.internationalize("requirement.criticality.label", locale), MULTISELECT);
		panel.addField(criticalityField);

		List<SearchInputPossibleValueModel> importanceOptions = levelComboBuilder(RequirementCriticality.values())
				.useLocale(locale).build();
		criticalityField.addPossibleValues(importanceOptions);


		SearchInputFieldModel categoryField = buildCategoryFieldModel(locale);
		panel.addField(categoryField);


		SearchInputFieldModel statusField = new SearchInputFieldModel("status", getMessageSource().internationalize(
				"requirement.status.combo.label", locale), MULTISELECT);
		panel.addField(statusField);

		List<SearchInputPossibleValueModel> statusOptions = levelComboBuilder(RequirementStatus.values()).useLocale(
				locale).build();
		statusField.addPossibleValues(statusOptions);

		return panel;
	}

	public SearchInputPanelModel createRequirementVersionPanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.requirement.versions.panel.title", locale));
		panel.setOpen(true);
		panel.setId("versions");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-version");

		SearchInputFieldModel versionField = new SearchInputFieldModel("isCurrentVersion", getMessageSource()
				.internationalize("search.requirement.content.version.label", locale), RADIOBUTTON);
		versionField.setIgnoreBridge(true);
		panel.addField(versionField);

		OptionBuilder optionBuilder = optionBuilder(locale);
		versionField.addPossibleValue(optionBuilder.labelI18nKey("search.requirement.allVersions").optionKey(EMPTY)
				.selected().build());
		versionField.addPossibleValue(optionBuilder.labelI18nKey("search.requirement.onlyLastVersion")
				.optionKey(ATLEASTONE).build());

		return panel;
	}

	public SearchInputPanelModel createRequirementContentPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.content.panel.title", locale));
		panel.setOpen(true);
		panel.setId("content");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-content");

		SearchInputFieldModel descriptionField = new SearchInputFieldModel("hasDescription", "", EXISTS);
		panel.addField(descriptionField);

		OptionBuilder optionBuilder = optionBuilder(locale);
		descriptionField.addPossibleValue(optionBuilder.labelI18nKey("search.requirement.emptyDescription")
				.optionKey(NONE).build());

		descriptionField.addPossibleValue(optionBuilder.labelI18nKey("search.requirement.nonemptyDescription")
				.optionKey(ATLEASTONE).build());

		SearchInputFieldModel attachmentField = new SearchInputFieldModel("attachments", getMessageSource()
				.internationalize("search.testcase.content.attachment.label", locale), EXISTS);
		panel.addField(attachmentField);

		attachmentField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.content.attachment.atleastone")
				.optionKey(ATLEASTONE).build());
		attachmentField.addPossibleValue(optionBuilder.labelI18nKey("search.testcase.content.attachment.none")
				.optionKey(NONE).build());

		return panel;
	}

	public SearchInputPanelModel createRequirementAssociationPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();

		panel.setTitle(getMessageSource().internationalize("search.testcase.association.panel.title", locale));
		panel.setOpen(true);
		panel.setId("association");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-associations");

		SearchInputFieldModel testcasesField = new SearchInputFieldModel("testcases", getMessageSource()
				.internationalize("search.requirement.association.testcase.label", locale), RANGE);
		panel.addField(testcasesField);

		SearchInputFieldModel childRequirementsField = new SearchInputFieldModel("requirement.children",
				getMessageSource().internationalize("search.requirement.association.childRequirement.label", locale),
				EXISTS);
		panel.addField(childRequirementsField);

		OptionBuilder optionBuilder = optionBuilder(locale);
		childRequirementsField.addPossibleValue(optionBuilder
				.labelI18nKey("search.requirement.association.childRequirement.atleastone").optionKey(ATLEASTONE)
				.build());

		childRequirementsField.addPossibleValue(optionBuilder
				.labelI18nKey("search.requirement.association.childRequirement.none").optionKey(NONE).build());

		SearchInputFieldModel parentRequirementsField = new SearchInputFieldModel("parent", getMessageSource()
				.internationalize("search.requirement.association.parentRequirement.label", locale), EXISTS);
		panel.addField(parentRequirementsField);

		parentRequirementsField.addPossibleValue(optionBuilder
				.labelI18nKey("search.requirement.association.parentRequirement.atleastone").optionKey(ATLEASTONE)
				.build());

		parentRequirementsField.addPossibleValue(optionBuilder
				.labelI18nKey("search.requirement.association.parentRequirement.none").optionKey(NONE).build());

		return panel;
	}

	public SearchInputPanelModel createRequirementHistoryPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.history.panel.title", locale));
		panel.setOpen(true);
		panel.setId("history");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-history-blue");

		OptionBuilder optionBuilder = optionBuilder(locale);

		SearchInputFieldModel createdByField = new SearchInputFieldModel("createdBy", getMessageSource()
				.internationalize("search.testcase.history.createdBy.label", locale), MULTIAUTOCOMPLETE);
		panel.addField(createdByField);

		List<String> users = advancedSearchService.findAllUsersWhoCreatedRequirementVersions();
		for (String user : users) {
			createdByField.addPossibleValue(optionBuilder.label(user).optionKey(user).build());
		}

		SearchInputFieldModel createdOnField = new SearchInputFieldModel("createdOn", getMessageSource()
				.internationalize("search.testcase.history.createdOn.label", locale), DATE);
		panel.addField(createdOnField);

		SearchInputFieldModel modifiedByField = new SearchInputFieldModel("lastModifiedBy", getMessageSource()
				.internationalize("search.testcase.history.modifiedBy.label", locale), MULTIAUTOCOMPLETE);
		panel.addField(modifiedByField);

		users = advancedSearchService.findAllUsersWhoModifiedRequirementVersions();
		for (String user : users) {
			if (StringUtils.isBlank(user)) {
				modifiedByField.addPossibleValue(optionBuilder.labelI18nKey("label.NeverModified").optionKey("")
						.build());
			} else {
				modifiedByField.addPossibleValue(optionBuilder.label(user).optionKey(user).build());
			}
		}

		SearchInputFieldModel modifiedOnField = new SearchInputFieldModel("lastModifiedOn", getMessageSource()
				.internationalize("search.testcase.history.modifiedOn.label", locale), DATE);
		panel.addField(modifiedOnField);

		return panel;
	}

	public SearchInputPanelModel createRequirementPerimeterPanel(Locale locale) {
		return perimeterPanelBuilder(locale).cssClass("search-icon-perimeter-blue").htmlId("requirement.project.id")
				.build();
	}

	private SearchInputFieldModel buildCategoryFieldModel(Locale locale){


		SearchInputFieldModel categoryField = new SearchInputFieldModel("category", getMessageSource().internationalize(
				"requirement.category.label", locale), MULTICASCADEFLAT);


		Collection<Project> readableProjects = projectFinder.findAllReadable();

		Collection<InfoList> categories = new ArrayList<>(readableProjects.size());

		for (Project p : readableProjects){
			categories.add(p.getRequirementCategories());
		}


		if (!categories.isEmpty()) {
			Collections.sort((List<InfoList>) categories, new Comparator<InfoList>() {
				@Override
				public int compare(final InfoList object1, final InfoList object2) {
					return object1.getLabel().compareTo(object2.getLabel());
				}
			});
		}

		populateInfoListFieldModel(categoryField, categories, locale);

		return categoryField;

	}

	private static Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>() {
		@Override
		public int compare(String str1, String str2) {
			int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
			if (res == 0) {
				res = str1.compareTo(str2);
			}
			return res;
		}
	};

	// get ready to puke !
	private void populateInfoListFieldModel(SearchInputFieldModel model, Collection<InfoList> infoLists, Locale locale){

		InternationalizationHelper messages = getMessageSource();
		Map<String, SearchInputPossibleValueModel> listsByListCode = new HashMap<>();

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

		model.setPossibleValues(new ArrayList<>(listsByListCode.values()));

	}
}
