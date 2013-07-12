/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.requirement;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.web.internal.controller.generic.WorkspaceController;
import org.squashtest.tm.web.internal.helper.InternationalizableComparator;

@Controller
@RequestMapping("/requirement-workspace")
public class RequirementWorkspaceController extends WorkspaceController<RequirementLibrary> {
	
	private WorkspaceService<RequirementLibrary> workspaceService;


	@Override
	protected WorkspaceService<RequirementLibrary> getWorkspaceService() {
		return workspaceService;
	}

	@Override
	protected String getWorkspaceViewName() {
		return "page/requirement-workspace";
	}
	
	@Override
	protected void populateModel(Model model, Locale locale) {
		
		List<RequirementLibrary> libraries = workspaceService.findAllImportableLibraries();
		//List<RequirementCriticality> criticalities = sortCriticalities(locale);	//not needed yet
		List<RequirementCategory> categories = sortCategories();
		
		model.addAttribute("editableLibraries", libraries);
		model.addAttribute("categories", categories);
		
		
	}

	@ServiceReference(serviceBeanName="squashtest.tm.service.RequirementsWorkspaceService")
	public final void setWorkspaceService(WorkspaceService<RequirementLibrary> requirementsWorkspaceService) {
		this.workspaceService = requirementsWorkspaceService;
	}
	

	/**
	 * @see org.squashtest.tm.web.internal.controller.generic.WorkspaceController#getWorkspaceType()
	 */
	protected WorkspaceType getWorkspaceType() {
		return WorkspaceType.REQUIREMENT_WORKSPACE;
	}

	
	private List<RequirementCategory> sortCategories(){
		InternationalizableComparator comparator = new InternationalizableComparator(getI18nHelper());
		List<RequirementCategory> categories = Arrays.asList(RequirementCategory.values());
		Collections.sort(categories, comparator);
		return categories;
	}
	
}
