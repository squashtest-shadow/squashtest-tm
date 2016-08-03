/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.testcase.requirement;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for the management screen of Requirements linked to a given TestCase
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/test-cases/{testCaseId}/linked-requirements-manager")
public class LinkedRequirementsManagerController {
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showManager(@PathVariable long testCaseId) {
		ModelAndView mav = new ModelAndView("page/test-cases/show-linked-requirements-manager");
		mav.addObject("testCaseId", testCaseId);
		return mav;
	}
}
