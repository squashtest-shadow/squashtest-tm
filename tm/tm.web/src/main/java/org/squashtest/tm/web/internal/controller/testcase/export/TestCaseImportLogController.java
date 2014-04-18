/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

package org.squashtest.tm.web.internal.controller.testcase.export;

import java.io.File;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

/**
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/test-cases/import-logs")
public class TestCaseImportLogController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseImportLogController.class);

	@Inject
	private TestCaseImportLogHelper logHelper;

	// There are dots in `{timestamp}`. We need to parse using a regexp (`{:.+}`) because standard parser ditches file extensions.
	@RequestMapping(value = "/{timestamp:.+}", method = RequestMethod.GET)
	public FileSystemResource getExcelImportLog(@PathVariable String timestamp, WebRequest request,
			HttpServletResponse response) {
		File logFile = logHelper.fetchLogFile(request, timestamp);
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + logHelper.logFilename(timestamp) + ".xls");

		return new FileSystemResource(logFile);
	}
}