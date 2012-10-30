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

import java.util.List;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
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
}
