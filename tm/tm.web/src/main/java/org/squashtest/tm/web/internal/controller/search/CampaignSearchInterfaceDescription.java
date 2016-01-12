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

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;

@Component
public class CampaignSearchInterfaceDescription extends SearchInterfaceDescription {

	private static final String COLUMN_1 = "column1";

	@Inject
	private CampaignAdvancedSearchService campaignAdvancedSearchService;


	public SearchInputPanelModel createGeneralInfoPanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.generalinfos.panel.title", locale));
		panel.setOpen(true);
		panel.setId("general-information-fullsize");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-information");

		SearchInputFieldModel idField = new SearchInputFieldModel("referencedTestCase.id",
				getMessageSource().internationalize(
"referenced-tc.id", locale), TEXTFIELD);
		panel.addField(idField);
		SearchInputFieldModel referenceField = new SearchInputFieldModel("referencedTestCase.reference",
				getMessageSource()
				.internationalize("label.reference", locale), TEXTFIELDREFERENCE);
		panel.addField(referenceField);

		SearchInputFieldModel labelField = new SearchInputFieldModel("label",
				getMessageSource().internationalize("label.Label", locale), TEXTFIELD);
		panel.addField(labelField);

		return panel;
	}

	public SearchInputPanelModel createAttributePanel(Locale locale) {
		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("search.testcase.attributes.panel.title", locale));
		panel.setOpen(true);
		panel.setId("attributes");
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-attributes");

		SearchInputFieldModel importanceField = new SearchInputFieldModel("referencedTestCase.importance",
				getMessageSource()
				.internationalize("test-case.importance.label", locale), MULTISELECT);
		panel.addField(importanceField);

		List<SearchInputPossibleValueModel> importanceOptions = levelComboBuilder(TestCaseImportance.values())
				.useLocale(locale).build();
		importanceField.addPossibleValues(importanceOptions);

		// *************** Assignment ****************************

		SearchInputFieldModel assignmentField = new SearchInputFieldModel("user",
				getMessageSource()
				.internationalize("search.execution.assignation", locale), MULTIAUTOCOMPLETE);
		panel.addField(assignmentField);

		/* TODO : Get all assignmentable users */
		OptionBuilder optionBuilder = optionBuilder(locale);

		List<String> users = campaignAdvancedSearchService.findAllAuthorizedUsersForACampaign();
		for (String user : users) {
			assignmentField.addPossibleValue(optionBuilder.label(user).optionKey(user).build());
		}

		return panel;
	}

	public SearchInputPanelModel createAssociationPanel(Locale locale) {

		SearchInputPanelModel panel = new SearchInputPanelModel();

		panel.setTitle(getMessageSource().internationalize("search.testcase.association.panel.title", locale));
		panel.setOpen(true);
		panel.setId("association");
		panel.setLocation(COLUMN_1);
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
		panel.setLocation(COLUMN_1);
		panel.addCssClass("search-icon-execution");

		// Created on
		SearchInputFieldModel createdOnField = new SearchInputFieldModel("createdOn", getMessageSource()
				.internationalize("search.execution.executed.label", locale), DATE);
		panel.addField(createdOnField);



		OptionBuilder optionBuilder = optionBuilder(locale);


		SearchInputFieldModel authorizedUsersField = new SearchInputFieldModel("lastExecutedBy",
				getMessageSource().internationalize("search.execution.executedby.label", locale), MULTIAUTOCOMPLETE);
		List<String> users = campaignAdvancedSearchService.findAllAuthorizedUsersForACampaign();
		for (String user : users) {
			authorizedUsersField.addPossibleValue(optionBuilder.label(user).optionKey(user).build());
		}

		panel.addField(authorizedUsersField);

		// Status
		SearchInputFieldModel statusField = new SearchInputFieldModel("executionStatus", getMessageSource()
				.internationalize(
				"test-case.status.label", locale), MULTISELECT);
		panel.addField(statusField);
		
		//ExecutionStatus[] values = ExecutionStatus.getCanonicalStatusSet().toArray(new ExecutionStatus[ExecutionStatus.getCanonicalStatusSet().size()]);
		ExecutionStatus[] values = ExecutionStatus.values();
		List<SearchInputPossibleValueModel> statusOptions = levelComboBuilder(values)
				.useLocale(locale).build();
		statusField.addPossibleValues(statusOptions);

		// Mode
		SearchInputFieldModel executionModeField = new SearchInputFieldModel("executionMode", getMessageSource()
				.internationalize("search.execution.mode.label", locale), MULTISELECT);
		panel.addField(executionModeField);

		List<SearchInputPossibleValueModel> modeOptions = levelComboBuilder(TestCaseExecutionMode.values())
				.useLocale(locale).build();
		executionModeField.addPossibleValues(modeOptions);

		return panel;
	}

	public SearchInputPanelModel createPerimeterPanel(Locale locale) {
		return perimeterPanelBuilder(locale).cssClass("search-icon-perimeter").htmlId("project.id").build();
	}

}
