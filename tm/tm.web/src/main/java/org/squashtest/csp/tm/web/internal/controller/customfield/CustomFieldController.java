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

package org.squashtest.csp.tm.web.internal.controller.customfield;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.service.customfield.CustomFieldManagerService;
import org.squashtest.tm.core.foundation.collection.DefaultPaging;

/**
 * Controller for the Custom Fields resources.
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/custom-fields")
public class CustomFieldController {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomFieldController.class);

	private static final String CUSTOM_FIELD = "customField";

	@Inject
	private CustomFieldManagerService customFieldManager;
	
	@ModelAttribute("customFieldOptionsPageSize")
	public long populateCustomFieldsPageSize() {
		return DefaultPaging.FIRST_PAGE.getPageSize();
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public void createNew(@RequestBody NewCustomField field) {
		LOGGER.info(ToStringBuilder.reflectionToString(field));
		customFieldManager.persist(field.createTransientEntity());
	}
	
	/**
	 * Shows the custom field modification page.
	 * @param customFieldId the id of the custom field to show
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/{customFieldId}", method = RequestMethod.GET)
	public String showCustomFieldModificationPage(@PathVariable Long customFieldId , Model model){
		CustomField customField = customFieldManager.findById(customFieldId);
		model.addAttribute(CUSTOM_FIELD, customField);
		return "custom-field-modification.html";
	}

	@RequestMapping(value = "/name/{name}", params = "id")
	@ResponseBody
	public Object getIdByName(@PathVariable String name) {
		CustomField field = customFieldManager.findByName(name);
		
		if (field != null) {
			Map<String, Long> res =  new HashMap<String, Long>(1);
			res.put("id", field.getId());
			return res;
		} else {
			return null;
		}
	}
	
	/**
	 * Changes the label of the concerned custom field
	 * @param customFieldId the id of the concerned custom field
	 * @param label the new label
	 * @return
	 */
	@RequestMapping(value="/{customFieldId}", method = RequestMethod.POST, params = {"id=cuf-label", "value"})
	@ResponseBody
	public void changeLabel(@PathVariable Long customFieldId , @RequestParam("value") String label){
		customFieldManager.changeLabel(customFieldId, label);
	}
	
	/**
	 * Changes the name of the concerned custom field
	 * @param customFieldId the id of the concerned custom field
	 * @param name the new name
	 * @return
	 */
	@RequestMapping(value="/{customFieldId}/name", method = RequestMethod.POST, params = { "value"})
	@ResponseBody
	public void changeName(@PathVariable Long customFieldId , @RequestParam("value") String name){
		customFieldManager.changeName(customFieldId, name);
	}
	
	/**
	 * Changes the whether the custom-field is optional or not.
	 * @param customFieldId the id of the concerned custom field
	 * @param optional : true if the custom field is optional
	 * @return
	 */
	@RequestMapping(value="/{customFieldId}/optional", method = RequestMethod.POST, params = { "value"})
	@ResponseBody
	public void changeOptional(@PathVariable Long customFieldId , @RequestParam("value") Boolean optional){
		customFieldManager.changeOptional(customFieldId, optional);
	}
	
	/**
	 * Changes the default value of the concerned custom-field's option
	 * 
	 * @param customFieldId : the id of concerned custom-field
	 * @param defaultValue : the new default-value for the custom-field
	 */
	@RequestMapping(value="/{customFieldId}", method = RequestMethod.POST, params = {"id=cuf-default-value", "value"})
	@ResponseBody
	public void changeDefaultValue(@PathVariable Long customFieldId , @RequestParam("value") String defaultValue){
		customFieldManager.changeDefaultValue(customFieldId, defaultValue);
	}
	
	/**
	 * Changes the label of the concerned custom-field's option
	 * 
	 * @param customFieldId : the id of the concerned custom-field
	 * @param optionLabel : the label of the concerned custom-field's option
	 * @param newLabel : the new label for the concerned custom-field's option
	 * @return
	 */
	@RequestMapping(value="/{customFieldId}/options/{optionLabel}/label", method = RequestMethod.POST, params = {"value"})
	@ResponseBody
	public void changeOptionLabel(@PathVariable Long customFieldId , @PathVariable String optionLabel ,@RequestParam("value") String newLabel){
		customFieldManager.changeOptionLabel(customFieldId, optionLabel, newLabel);
	}
	
	/**
	 * Adds an option to the concerned custom-field
	 * 
	 * @param customFieldId : the id of the concerned custom-field
	 * @param label: the label of the new option
	 */
	@RequestMapping(value="/{customFieldId}/options/new", method = RequestMethod.POST, params = {"value"})
	@ResponseBody
	public void addOption(@PathVariable Long customFieldId, @RequestParam("value") String label){
		customFieldManager.addOption(customFieldId, label);
	}
	
	
	
}
