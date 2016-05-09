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
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.MultiSelectField;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

@Component
public class SearchInputInterfaceHelper {

	private static final String TEXTFIELD = "textfield";
	private static final String COMBOMULTISELECT = "combomultiselect";
	private static final String TAGS = "tags";

	@Inject
	protected InternationalizationHelper messageSource;

	@Inject
	private RequirementVersionSearchInterfaceDescription requirementVersionSearchInterfaceDescription;

	@Inject
	private TestcaseSearchInterfaceDescription testcaseVersionSearchInterfaceDescription;

	@Inject
	private CampaignSearchInterfaceDescription campaignSearchInterfaceDescription;

	@Inject
	private FeatureManager featureManager;

	@Inject
	private TestCaseAdvancedSearchService advancedSearchService;

	public SearchInputInterfaceModel getRequirementSearchInputInterfaceModel(Locale locale, boolean isMilestoneMode) {

		SearchInputInterfaceModel model = new SearchInputInterfaceModel();

		// Perimeter
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementPerimeterPanel(locale));

		// Information
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementInformationPanel(locale));

		// History
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementHistoryPanel(locale));

		// Attributes
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementAttributePanel(locale));

		// Milestones
		if (!isMilestoneMode && featureManager.isEnabled(FeatureManager.Feature.MILESTONE)) {
			model.addPanel(requirementVersionSearchInterfaceDescription.createMilestonePanel(locale));
		}

		// Version
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementVersionPanel(locale));

		// Content
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementContentPanel(locale));

		// Associations
		model.addPanel(requirementVersionSearchInterfaceDescription.createRequirementAssociationPanel(locale));

		// CUFs
		model.addPanel(createCUFPanel(locale, BindableEntity.REQUIREMENT_VERSION));

		return model;
	}

	public SearchInputInterfaceModel getTestCaseSearchInputInterfaceModel(Locale locale, boolean isMilestoneMode) {

		SearchInputInterfaceModel model = new SearchInputInterfaceModel();

		// Perimeter
		model.addPanel(testcaseVersionSearchInterfaceDescription.createPerimeterPanel(locale));

		// Information
		model.addPanel(testcaseVersionSearchInterfaceDescription.createGeneralInfoPanel(locale));

		// History
		model.addPanel(testcaseVersionSearchInterfaceDescription.createTestCaseHistoryPanel(locale));

		// Attributes
		model.addPanel(testcaseVersionSearchInterfaceDescription.createAttributePanel(locale));

		// Milestones
		if (!isMilestoneMode && featureManager.isEnabled(FeatureManager.Feature.MILESTONE)) {
			model.addPanel(testcaseVersionSearchInterfaceDescription.createMilestonePanel(locale));
		}

		// Content
		model.addPanel(testcaseVersionSearchInterfaceDescription.createContentPanel(locale));

		// Associations
		model.addPanel(testcaseVersionSearchInterfaceDescription.createAssociationPanel(locale));

		// CUF
		model.addPanel(createCUFPanel(locale, BindableEntity.TEST_CASE));

		return model;
	}

	public SearchInputInterfaceModel getCampaignSearchInputInterfaceModel(Locale locale, boolean isMilestoneMode) {

		SearchInputInterfaceModel model = new SearchInputInterfaceModel();

		// Information
		model.addPanel(campaignSearchInterfaceDescription.createGeneralInfoPanel(locale));

		// Attributes
		model.addPanel(campaignSearchInterfaceDescription.createAttributePanel(locale));

		// Milestones
		if (!isMilestoneMode && featureManager.isEnabled(FeatureManager.Feature.MILESTONE)) {
			model.addPanel(requirementVersionSearchInterfaceDescription.createMilestonePanel(locale));
		}

		model.addPanel(campaignSearchInterfaceDescription.createExecutionPanel(locale));

		return model;
	}

	private SearchInputPanelModel createCUFPanel(Locale locale, BindableEntity bindableEntity) {

		SearchInputPanelModel panel = getCustomFielModel(locale, bindableEntity);
		panel.setTitle(messageSource.internationalize("search.testcase.cuf.panel.title", locale));
		panel.setOpen(true);
		panel.setId("cuf");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-cuf");
		return panel;
	}

	private SearchInputPanelModel getCustomFielModel(Locale locale, BindableEntity bindableEntity) {
		List<CustomField> customFields = advancedSearchService
				.findAllQueryableCustomFieldsByBoundEntityType(bindableEntity);
		return convertToSearchInputPanelModel(customFields, locale);
	}

	private SearchInputPanelModel convertToSearchInputPanelModel(List<CustomField> customFields, Locale locale) {
		SearchInputPanelModel model = new SearchInputPanelModel();
		for (CustomField customField : customFields) {

			switch (customField.getInputType()) {
			case DROPDOWN_LIST:
				SingleSelectField selectField = (SingleSelectField) customField;
				model.getFields().add(convertToSearchInputFieldModel(selectField, locale));
				break;

			case PLAIN_TEXT:
				model.getFields().add(convertToSearchInputFieldModel(customField));
				break;

			case CHECKBOX:
				model.getFields().add(createCheckBoxField(customField, locale));
				break;

			case DATE_PICKER:
				model.getFields().add(createDateCustomFieldSearchModel(customField));
				break;

			case TAG:
				model.getFields().add(convertToSearchInputFieldModel((MultiSelectField) customField));
				break;

			case RICH_TEXT:
				break; // not supported for now
			}

		}
		return model;
	}

	private SearchInputFieldModel createDateCustomFieldSearchModel(CustomField customField) {

		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType("CF_TIME_INTERVAL");
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel createCheckBoxField(CustomField customField, Locale locale) {
		SearchInputFieldModel model = new SearchInputFieldModel();

		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<>();

		possibleValues
				.add(new SearchInputPossibleValueModel(messageSource.internationalize("label.True", locale), "true"));
		possibleValues
				.add(new SearchInputPossibleValueModel(messageSource.internationalize("label.False", locale), "false"));

		model.setPossibleValues(possibleValues);
		model.setInputType(COMBOMULTISELECT);
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel convertToSearchInputFieldModel(CustomField customField) {
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(TEXTFIELD);
		model.setTitle(customField.getLabel());
		model.setId(customField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel convertToSearchInputFieldModel(SingleSelectField selectField, Locale locale) {
		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<>();
		possibleValues
				.add(new SearchInputPossibleValueModel(messageSource.internationalize("label.Empty", locale), ""));
		for (CustomFieldOption option : selectField.getOptions()) {
			possibleValues.add(new SearchInputPossibleValueModel(option.getLabel(), option.getLabel()));
		}
		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(COMBOMULTISELECT);
		model.setTitle(selectField.getLabel());
		model.setPossibleValues(possibleValues);
		model.setId(selectField.getCode());
		model.setIgnoreBridge(true);
		return model;
	}

	private SearchInputFieldModel convertToSearchInputFieldModel(MultiSelectField multifield) {
		List<SearchInputPossibleValueModel> possibleValues = new ArrayList<>(multifield.getOptions().size());

		for (CustomFieldOption option : multifield.getOptions()) {
			possibleValues.add(new SearchInputPossibleValueModel(option.getLabel(), option.getLabel()));
		}

		SearchInputFieldModel model = new SearchInputFieldModel();
		model.setInputType(TAGS);
		model.setTitle(multifield.getLabel());
		model.setPossibleValues(possibleValues);
		model.setId(multifield.getCode());
		model.setIgnoreBridge(true);
		return model;

	}
}
