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
package org.squashtest.tm.web.internal.controller.reqtc;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.service.RequirementLibraryNavigationService;
import org.squashtest.csp.tm.service.importer.ImportRequirementTestCaseLinksSummary;

/**
 * Controller which processes requests related to links between Requirement and Test-Case
 * 
 * @author mpagnon
 * 
 */
@Controller
@RequestMapping(value = "/req-tc")
public class RequirementTestCaseController {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementTestCaseController.class);
	@Inject
	private RequirementLibraryNavigationService requirementLibraryNavigationService;
	
	@RequestMapping(value="/import-links/upload", method = RequestMethod.POST,  params = "upload-ticket")
	public ModelAndView importArchive(@RequestParam("archive") MultipartFile archive) throws IOException{
		LOGGER.debug("Start upload links requirement/test-cases");
		InputStream stream = archive.getInputStream();
		ImportRequirementTestCaseLinksSummary summary =  requirementLibraryNavigationService.importLinksExcel(stream);
		ModelAndView mav =  new ModelAndView("fragment/import/import-links-summary");
		mav.addObject("summary", summary);
		return mav;
		
	}
	
}
