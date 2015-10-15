/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.customreport;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;

@Controller
public class CustomReportController {
	public static final Logger LOGGER = LoggerFactory.getLogger(CustomReportController.class);

	@Inject
	private CustomReportLibraryNodeService customReportLibraryNodeService;
	
	
	
	
	//---- SHOW DETAIL METHODS -----
	
	@RequestMapping(value="custom-report-library/{id}", method=RequestMethod.GET)
	public @ResponseBody CustomReportLibrary getLibraryDetails(@PathVariable Long id){
		return customReportLibraryNodeService.findCustomReportLibraryById(id);
	}
	
	@RequestMapping(value="custom-report-folder/{id}", method=RequestMethod.GET)
	public @ResponseBody CustomReportFolder getFolderDetails(@PathVariable Long id){
		return customReportLibraryNodeService.findCustomReportFolderById(id);
	}
}
