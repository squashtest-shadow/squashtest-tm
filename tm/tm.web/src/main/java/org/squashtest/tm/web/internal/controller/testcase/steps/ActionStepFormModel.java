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
package org.squashtest.tm.web.internal.controller.testcase.steps;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.squashtest.tm.domain.testcase.ActionTestStep;


public class ActionStepFormModel {

	/**
	 * Note : the following validation annotations are never called, a custom validator will be invoked for this.
	 * 
	 */
	
	/*@NotBlank
	 * @NotNull
	 */
	private String action="";
	
	private String expectedResult="";
	
	/*@NotNull
	@NotEmpty*/
	private Map<Long, String> customFields = new HashMap<Long, String>();

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}

	public Map<Long, String> getCustomFields() {
		return customFields;
	}

	public void setCustomFields(Map<Long, String> customFields) {
		this.customFields = customFields;
	}
	
	public ActionTestStep getActionTestStep(){
		ActionTestStep newStep = new ActionTestStep();
		newStep.setAction(action);
		newStep.setExpectedResult(expectedResult);
		return newStep;
	}
	
	public static class ActionStepFormModelValidator implements Validator{
		
		private MessageSource messageSource;

		public void setMessageSource(MessageSource messageSource) {
			this.messageSource = messageSource;
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return (clazz.equals(ActionStepFormModel.class));
		}

		@Override
		public void validate(Object target, Errors errors) {
			Locale locale = LocaleContextHolder.getLocale();
			String notBlank = messageSource.getMessage("message.notBlank",null, locale);
			
			ActionStepFormModel model = (ActionStepFormModel) target;
			
			for (Entry<Long, String> entry : model.getCustomFields().entrySet()){
				String value = entry.getValue();
				if (value.trim().isEmpty()){
					errors.rejectValue("customFields["+entry.getKey()+"]", "message.notBlank", notBlank);
				}
			}
			
		}
		
		
	}
	
}
