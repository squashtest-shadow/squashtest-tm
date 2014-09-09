/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.testcase;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.squashtest.tm.domain.testcase.TestCase;

public class TestCaseFormModel {
	/**
	 * Note : the following validation annotations are never called, a custom validator will be invoked for this.
	 * 
	 */
	
	/*@NotBlank
	@NotNull*/
	private String name;	
	
	private String reference;
	private String description;

	
	
	/*@NotNull
	@NotEmpty*/
	private Map<Long, String> customFields = new HashMap<Long, String>();
	
	
	public String getName() {
		return name;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public String getReference() {
		return reference;
	}
	
	public void setReference(String reference) {
		this.reference = reference;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public Map<Long, String> getCustomFields() {
		return customFields;
	}
	
	public void setCustomFields(Map<Long, String> customFields) {
		this.customFields = customFields;
	}
	
	public TestCase getTestCase(){
		TestCase newTC = new TestCase();
		newTC.setName(name);
		newTC.setDescription(description);
		newTC.setReference(reference);
		return newTC;
	}
	
	
	
	public static class TestCaseFormModelValidator implements Validator {
		
		private MessageSource messageSource;
		
		public void setMessageSource(MessageSource messageSource){
			this.messageSource = messageSource;
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return (clazz.equals(TestCaseFormModel.class));
		}

		@Override
		public void validate(Object target, Errors errors) {
			
			Locale locale = LocaleContextHolder.getLocale();
			String notBlank = messageSource.getMessage("message.notBlank", null, locale);
			String lengthMax = messageSource.getMessage("message.lengthMax", new Object[]{"50"}, locale);
			
			TestCaseFormModel model = (TestCaseFormModel) target;
			
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "message.notBlank", notBlank);
			
			if (model.reference.length()>50){
				errors.rejectValue("reference", "message.lengthMax", lengthMax);
			}
			
			for (Entry<Long, String> entry : model.getCustomFields().entrySet()){
				String value = entry.getValue();
				if (value.trim().isEmpty()){
					errors.rejectValue("customFields["+entry.getKey()+"]", "message.notBlank", notBlank);
				}
			}
			

		}

	}
	
	
}
