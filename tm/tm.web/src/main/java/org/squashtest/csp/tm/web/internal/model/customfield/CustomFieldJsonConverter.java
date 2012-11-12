/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.web.internal.model.customfield;

import java.util.Locale;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.domain.customfield.CustomFieldOption;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;
import org.squashtest.csp.tm.domain.customfield.InputType;
import org.squashtest.csp.tm.domain.customfield.SingleSelectField;

@Component
public class CustomFieldJsonConverter {

	@Inject
	private MessageSource messageSource;
	
	
	public CustomFieldJsonConverter(){
		super();
	}
	
	public CustomFieldJsonConverter(MessageSource messageSource){
		super();
		this.messageSource=messageSource;
	}
	
	public CustomFieldBindingModel toJson(CustomFieldBinding binding){
		
		CustomFieldBindingModel bindingModel = new CustomFieldBindingModel();
		
		BindableEntityModel entityModel = toJson(binding.getBoundEntity());
		
		CustomFieldModel fieldModel = toJson(binding.getCustomField());
		
		bindingModel.setId(binding.getId());
		bindingModel.setProjectId(binding.getBoundProject().getId());
		bindingModel.setBoundEntity(entityModel);
		bindingModel.setCustomField(fieldModel);
		bindingModel.setPosition(binding.getPosition());
		
		return bindingModel;
		
		
	}
	
	public BindableEntityModel toJson(BindableEntity entity){
		
		BindableEntityModel model = new BindableEntityModel();
		
		model.setEnumName(entity.toString());
		model.setFriendlyName(getMessage(entity.getI18nKey()));
		
		return model;
		
	}
	
	
	public CustomFieldModel toJson(CustomField field){
		
		CustomFieldModel model;
		
		switch(field.getInputType()){
		case DROPDOWN_LIST : model = _newSingleSelectFieldModel((SingleSelectField)field); 
							break;
							
		default : model = _newDefaultCustomFieldModel(field);
							break;
		}
		
		return model;
		
	}


	
	public InputTypeModel toJson(InputType type){
	
		InputTypeModel model = new InputTypeModel();
		
		model.setEnumName(type.toString());
		model.setFriendlyName(getMessage(type.getI18nKey()));
		
		return model;
	}
		
	
	public CustomFieldValueModel toJson(CustomFieldValue value){
		
		CustomFieldValueModel model = new CustomFieldValueModel();
		
		BindableEntityModel entityTypeModel = toJson(value.getBoundEntityType());
		CustomFieldBindingModel bindingModel = toJson(value.getBinding());
		
		model.setId(value.getId());
		model.setBoundEntityId(value.getBoundEntityId());
		model.setBoundEntityType(entityTypeModel);
		model.setBinding(bindingModel);
		
		return model;
		
	}
	
	private String getMessage(String key){		
		Locale locale = LocaleContextHolder.getLocale();		
		return messageSource.getMessage(key, null, locale);
	}
	

	private CustomFieldModel _newDefaultCustomFieldModel(CustomField field) {
		CustomFieldModel model = new CustomFieldModel();

		return _populateCustomFieldModel(model, field);
	}
	
	
	private SingleSelectFieldModel _newSingleSelectFieldModel(SingleSelectField field){
		
		SingleSelectFieldModel model = new SingleSelectFieldModel();
		
		_populateCustomFieldModel(model, field);
		
		
		for (CustomFieldOption option : field.getOptions()){
			CustomFieldOptionModel newOption = new CustomFieldOptionModel();
			newOption.setLabel(option.getLabel());
			model.addOption(newOption);
		}
	
		return model;		
	}
	
	private CustomFieldModel _populateCustomFieldModel(CustomFieldModel model, CustomField field){
		
		InputTypeModel typeModel = toJson(field.getInputType());
		
		model.setId(field.getId());
		model.setName(field.getName());
		model.setLabel(field.getLabel());
		model.setOptional(field.isOptional());
		model.setDefaultValue(field.getDefaultValue());
		model.setInputType(typeModel);		
		model.setFriendlyOptional(field.isOptional() ? getMessage("label.Yes") : getMessage("label.No"));
	
		return model;		
	}
	
}
