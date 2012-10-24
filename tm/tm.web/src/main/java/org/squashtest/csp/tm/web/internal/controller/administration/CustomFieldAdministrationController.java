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

package org.squashtest.csp.tm.web.internal.controller.administration;

import java.util.List;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.InputType;
import org.squashtest.csp.tm.service.customfield.CustomFieldManagerService;
import org.squashtest.tm.core.foundation.collection.DefaultPaging;

/**
 * Controller for the Custom Fields management pages.
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/administration/custom-fields")
public class CustomFieldAdministrationController {
	
	private CustomFieldManagerService customFieldManagerService;
	
	@ServiceReference
	public void setCustomFieldManagerService(CustomFieldManagerService customFieldManagerService){
		this.customFieldManagerService = customFieldManagerService;
	}
	
	@ModelAttribute("inputTypes")
	public InputType[] populateInputTypes() {
		return InputType.values();
	}
	@ModelAttribute("customFieldsPageSize")
	public long populateCustomFieldsPageSize() {
		return DefaultPaging.FIRST_PAGE.getPageSize();
	}

	/**
	 * Shows the custom fields manager.
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showManager(Model model) {
		
//		CustomField cuf1 = new CustomField(InputType.PLAIN_TEXT);
//		cuf1.setDefaultValue("default value");
//		cuf1.setLabel("label");
//		cuf1.setName("name");
//		cuf1.setOptional(true);
//		List<CustomField> customFields = Arrays.asList(cuf1);
		List<CustomField> customFields = customFieldManagerService.findAllOrderedByName();
		model.addAttribute("customFields", customFields);

		return "custom-field-manager.html";
	}
}
