/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.web.internal.controller.campaign;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.service.CampaignLibraryNavigationService;
import org.squashtest.csp.tm.service.RequirementLibraryNavigationService;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;

@Controller
@RequestMapping("/campaign-libraries/{libraryId}")
public class CampaignLibraryModificationController {
	private CampaignLibraryNavigationService campaignLibraryNavigationService;

	@ServiceReference
	public void setTestCaseLibraryNavigationService(CampaignLibraryNavigationService campaignLibraryNavigationService) {
		this.campaignLibraryNavigationService = campaignLibraryNavigationService;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public final ModelAndView showCampaignLibrary(@PathVariable long libraryId) {
		
		CampaignLibrary lib = campaignLibraryNavigationService.findLibrary(libraryId);
		
		ModelAndView mav = new ModelAndView("fragment/library/show-libraries-details");
		
		mav.addObject("library", lib);
		
		return mav;
	}
}
