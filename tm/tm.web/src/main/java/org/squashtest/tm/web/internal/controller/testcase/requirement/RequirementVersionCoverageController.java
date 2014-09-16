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
package org.squashtest.tm.web.internal.controller.testcase.requirement;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.service.importer.ImportRequirementTestCaseLinksSummary;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;

/**
 * Controller which processes requests related to links between Requirement and Test-Case
 * 
 * @author mpagnon
 * 
 */
@Controller
@RequestMapping(value = "/requirement-version-coverage")
public class RequirementVersionCoverageController {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementVersionCoverageController.class);
	@Inject
	private RequirementLibraryNavigationService requirementLibraryNavigationService;
	@Inject
	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;
	
	@RequestMapping(value="/upload", method = RequestMethod.POST,  params = "upload-ticket")
	public ModelAndView importArchive(@RequestParam("file") MultipartFile file) throws IOException{
		LOGGER.debug("Start upload links requirement/test-cases");
		InputStream stream = file.getInputStream();
		ImportRequirementTestCaseLinksSummary summary =  requirementLibraryNavigationService.importLinksExcel(stream);
		ModelAndView mav =  new ModelAndView("fragment/import/import-links-summary");
		mav.addObject("summary", summary);
		return mav;
		
	}
	
	@RequestMapping(value = "{requirementVersionCoverageId}", method = RequestMethod.DELETE)
	public @ResponseBody
	void removeVerifiedRequirementVersionFromTestCase(@PathVariable long requirementVersionId,
			@PathVariable long testCaseId) {
		verifiedRequirementsManagerService.removeVerifiedRequirementVersionFromTestCase(requirementVersionId, testCaseId);

	}
	
}
