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
package org.squashtest.csp.tm.web.internal.controller.customfields;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.service.CustomFieldBindingModificationService;
import org.squashtest.csp.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.csp.tm.web.internal.model.customfields.CustomFieldBindingModel;
import org.squashtest.csp.tm.web.internal.model.customfields.CustomFieldJsonConverter;


@Controller
@RequestMapping("/custom-fields-binding")
public class CustomFieldBindingManagementController {

	private CustomFieldBindingModificationService service;

	private CustomFieldJsonConverter converter;
	
	@Inject
	private InternationalizationHelper messageSource;
	
	
	@ServiceReference
	public void setCustomFieldBindingModificationService(CustomFieldBindingModificationService service){
		this.service=service;
	}

	@PostConstruct
	public void init(){
		converter = new CustomFieldJsonConverter(messageSource);
	}
	
	
	
	@RequestMapping(method= RequestMethod.GET, params = {"projectId"})
	public List<CustomFieldBindingModel> findAllCustomFieldsForProject(@RequestParam("projectId") Long projectId){
		
		List<CustomFieldBinding> bindings = service.findCustomFieldsForProject(projectId);
		
		return toJson(bindings);
		
	}
	
	@RequestMapping(method= RequestMethod.GET, params = {"projectId", "bindableEntity"})
	public List<CustomFieldBindingModel> findAllCustomFieldsForProject(@RequestParam("projectId") Long projectId, @RequestParam("bindableEntity") String bindableEntity){
		
		List<CustomFieldBinding> bindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.valueOf(bindableEntity));
		
		return toJson(bindings);
		
	}
	
	
	private List<CustomFieldBindingModel> toJson(List<CustomFieldBinding> bindings){
		List<CustomFieldBindingModel> result = new LinkedList<CustomFieldBindingModel>();
		
		for (CustomFieldBinding binding : bindings){
			CustomFieldBindingModel model = converter.toJson(binding);
			result.add(model);
		}
		
		return result;
	}
	
}
