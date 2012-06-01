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
package org.squashtest.csp.tm.web.internal.controller.generic;

import java.security.Principal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.web.internal.interceptor.OpenedEntities;

@Controller
@RequestMapping(value = "/opened-entity")
public class ObjectAccessController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectAccessController.class);

	@RequestMapping(value = "/test-cases/{id}", method = RequestMethod.DELETE)
	public void leaveTestCase(@PathVariable("id") Long id, HttpServletRequest request) {
		Principal user = request.getUserPrincipal();
		HttpSession session = request.getSession();
		if (session != null) {
			ServletContext context = request.getSession().getServletContext();
			if (context != null) {
				LOGGER.debug("context = "+context);
				LOGGER.debug("leave Test case #" + id);
				if (user != null) {
					LOGGER.debug(""+user.getName());
					OpenedEntities openedEnities = (OpenedEntities) context.getAttribute(TestCase.class.getSimpleName());
					if(openedEnities != null){
						openedEnities.removeView(user.getName(), id);
					}
				}
			}
		}
		
	}

}
