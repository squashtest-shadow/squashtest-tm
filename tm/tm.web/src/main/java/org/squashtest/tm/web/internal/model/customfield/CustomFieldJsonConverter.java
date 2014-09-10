/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.model.customfield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.CustomFieldValueOption;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.MultiSelectField;
import org.squashtest.tm.domain.customfield.MultiSelectFieldValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedSingleSelectField;

@Component
public class CustomFieldJsonConverter {

	@Inject
	private MessageSource messageSource;

	public CustomFieldJsonConverter() {
		super();
	}

	public CustomFieldJsonConverter(MessageSource messageSource) {
		super();
		this.messageSource = messageSource;
	}


	// ************ simple  jsonifications ********************

	public RenderingLocationModel toJson(RenderingLocation location){

		RenderingLocationModel model = new RenderingLocationModel();

		model.setEnumName(location.toString());
		model.setFriendlyName(getMessage(location.getI18nKey()));

		return model;

	}

	public RenderingLocationModel[] toJson(Collection<RenderingLocation> values){
		RenderingLocationModel[] modelArray = new RenderingLocationModel[values.size()];
		int i=0;
		for (RenderingLocation location : values){
			modelArray[i++]=toJson(location);
		}
		return modelArray;
	}


	public CustomFieldBindingModel toJson(CustomFieldBinding binding) {

		CustomFieldBindingModel bindingModel = new CustomFieldBindingModel();

		BindableEntityModel entityModel = toJson(binding.getBoundEntity());
		RenderingLocationModel[] locationArrayModel = toJson(binding.getRenderingLocations());
		CustomFieldModel fieldModel = toJson(binding.getCustomField());

		bindingModel.setId(binding.getId());
		bindingModel.setProjectId(binding.getBoundProject().getId());
		bindingModel.setBoundEntity(entityModel);
		bindingModel.setCustomField(fieldModel);
		bindingModel.setRenderingLocations(locationArrayModel);
		bindingModel.setPosition(binding.getPosition());

		return bindingModel;

	}

	public BindableEntityModel toJson(BindableEntity entity) {

		BindableEntityModel model = new BindableEntityModel();

		model.setEnumName(entity.toString());
		model.setFriendlyName(getMessage(entity.getI18nKey()));

		return model;

	}


	public InputTypeModel toJson(InputType type) {

		InputTypeModel model = new InputTypeModel();

		model.setEnumName(type.toString());
		model.setFriendlyName(getMessage(type.getI18nKey()));

		return model;
	}


	// ************ custom field and custom field values jsonifications ***************

	public CustomFieldModel toJson(CustomField field) {

		CustomFieldModel model;

		switch (field.getInputType()) {

		case DATE_PICKER:
			model = createDatePickerFieldModel(field);
			break;

		case DROPDOWN_LIST:
			model = createSingleSelectFieldModel((SingleSelectField) field); //NOSONAR a CustomField which has InputType == DROPDOWN_LIST is always a SingleSelectField
			break;

		case TAG :
			model = createMultiSelectFieldModel((MultiSelectField) field);	// NOSONAR a CustomField which has InputType == TAG is always a MultiSelectField
			break;

		default:
			model = createDefaultCustomFieldModel(field);
			break;
		}

		return model;

	}

	public CustomFieldValueModel toJson(CustomFieldValue value) {

		CustomFieldValueModel model = new CustomFieldValueModel();

		BindableEntityModel entityTypeModel = toJson(value.getBoundEntityType());
		CustomFieldBindingModel bindingModel = toJson(value.getBinding());

		model.setId(value.getId());
		model.setBoundEntityId(value.getBoundEntityId());
		model.setBoundEntityType(entityTypeModel);
		model.setBinding(bindingModel);
		model.setValue(value.getValue());

		return model;

	}

	public CustomFieldValueModel toJson(MultiSelectFieldValue value){
		CustomFieldValueModel model = toJson((CustomFieldValue)value);
		List<String>  options = new ArrayList<String>(value.getOptions().size());
		for (CustomFieldValueOption option : value.getOptions()){
			options.add(option.getOption());
		}
		model.setOptionValues(options);
		return model;
	}

	private CustomFieldModel createDefaultCustomFieldModel(CustomField field) {
		CustomFieldModel model = new CustomFieldModel();

		return populateCustomFieldModel(model, field);
	}


	private SingleSelectFieldModel createSingleSelectFieldModel(SingleSelectField field) {

		SingleSelectFieldModel model = new SingleSelectFieldModel();

		populateCustomFieldModel(model, field);

		for (CustomFieldOption option : field.getOptions()) {
			CustomFieldOptionModel newOption = new CustomFieldOptionModel();
			newOption.setLabel(option.getLabel());
			model.addOption(newOption);
		}

		return model;
	}


	// note : for now this is mostly the same than for the SingleSelectField.
	private MultiSelectFieldModel createMultiSelectFieldModel(MultiSelectField field){

		MultiSelectFieldModel model = new MultiSelectFieldModel();

		populateCustomFieldModel(model, field);

		for (CustomFieldOption option : field.getOptions()) {
			CustomFieldOptionModel newOption = new CustomFieldOptionModel();
			newOption.setLabel(option.getLabel());
			model.addOption(newOption);
		}

		for (String value : field.getDefaultValue().split(";")){
			model.addDefaultValueSelectedOption(value);
		}

		return model;

	}


	private DatePickerFieldModel createDatePickerFieldModel(CustomField field){

		Locale locale = LocaleContextHolder.getLocale();
		DatePickerFieldModel model = new DatePickerFieldModel();

		populateCustomFieldModel(model, field);

		model.setFormat(getMessage("squashtm.dateformatShort.datepicker"));
		model.setLocale(locale.toString());

		return model;

	}


	private CustomFieldModel populateCustomFieldModel(CustomFieldModel model, CustomField field) {

		InputTypeModel typeModel = toJson(field.getInputType());

		model.setId(field.getId());
		model.setName(field.getName());
		model.setLabel(field.getLabel());
		model.setOptional(field.isOptional());
		model.setDefaultValue(field.getDefaultValue());
		model.setInputType(typeModel);
		model.setFriendlyOptional(field.isOptional() ? getMessage("label.Yes") : getMessage("label.No"));
		model.setCode(field.getCode());

		return model;
	}

	// *********************** denormalized field values **************************



	public CustomFieldModel toCustomFieldJsonModel(DenormalizedFieldValue field) {

		CustomFieldModel model;

		switch (field.getInputType()) {

		case DATE_PICKER:
			model = createDatePickerFieldModel(field);
			break;

		case DROPDOWN_LIST:
			model = createSingleSelectFieldModel((DenormalizedSingleSelectField) field); //NOSONAR a DenormalizedFieldValue which has InputType == DROPDOWN_LIST is always a DenormalizedSingleSelectField
			break;

		default:
			model = createDefaultCustomFieldModel(field);
			break;
		}

		return model;

	}


	public CustomFieldValueModel toJson(DenormalizedFieldValue value) {

		CustomFieldValueModel model = new CustomFieldValueModel();

		// pseudo custom field binding
		CustomFieldBindingModel bindingModel = new CustomFieldBindingModel();
		bindingModel.setPosition(value.getPosition());
		bindingModel.setRenderingLocations(toJson(value.getRenderingLocations()));

		// pseudo input type
		InputTypeModel inputTypeModel = new InputTypeModel();
		inputTypeModel.setEnumName(value.getInputType().name());
		inputTypeModel.setFriendlyName(value.getInputType().name());

		// pseudo custom field
		CustomFieldModel customFieldModel = toCustomFieldJsonModel(value);

		// pseudo bindable entity
		BindableEntityModel bindableEntityModel = new BindableEntityModel();
		bindableEntityModel.setEnumName(value.getDenormalizedFieldHolderType().name());
		bindableEntityModel.setFriendlyName(value.getDenormalizedFieldHolderType().name());
		bindingModel.setBoundEntity(bindableEntityModel);


		// wire all the stuff
		model.setId(value.getId());
		model.setBoundEntityId(value.getDenormalizedFieldHolderId());
		model.setBinding(bindingModel);
		model.setValue(value.getValue());

		bindingModel.setCustomField(customFieldModel);


		return model;

	}


	private CustomFieldModel createSingleSelectFieldModel(DenormalizedSingleSelectField field){

		SingleSelectFieldModel model = new SingleSelectFieldModel();

		populateCustomFieldModel(model, field);

		for (CustomFieldOption option : field.getOptions()) {
			CustomFieldOptionModel newOption = new CustomFieldOptionModel();
			newOption.setLabel(option.getLabel());
			model.addOption(newOption);
		}

		return model;
	}


	private CustomFieldModel createDefaultCustomFieldModel(DenormalizedFieldValue value) {

		CustomFieldModel customFieldModel = new CustomFieldModel();
		populateCustomFieldModel(customFieldModel, value);

		return customFieldModel;
	}

	private CustomFieldModel createDatePickerFieldModel(DenormalizedFieldValue value) {

		Locale locale = LocaleContextHolder.getLocale();
		DatePickerFieldModel model = new DatePickerFieldModel();
		populateCustomFieldModel(model, value);

		model.setFormat(getMessage("squashtm.dateformatShort.datepicker"));
		model.setLocale(locale.toString());

		return model;
	}



	private CustomFieldModel populateCustomFieldModel(CustomFieldModel customFieldModel, DenormalizedFieldValue value) {

		InputTypeModel inputTypeModel = new InputTypeModel();
		inputTypeModel.setEnumName(value.getInputType().name());
		inputTypeModel.setFriendlyName(value.getInputType().name());

		customFieldModel.setCode(value.getCode());
		customFieldModel.setId(value.getId());
		customFieldModel.setInputType(inputTypeModel);
		customFieldModel.setLabel(value.getLabel());
		customFieldModel.setOptional(true);
		customFieldModel.setDenormalized(true);

		return customFieldModel;
	}



	// ***************** other things ******************************

	private String getMessage(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, locale);
	}

}
