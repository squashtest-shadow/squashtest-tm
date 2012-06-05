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
package org.squashtest.csp.tm.web.internal.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.squashtest.csp.core.domain.Identified;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.testcase.TestCase;
/**
 * 
 * @author mpagnon
 *
 */
public class RequirementViewInterceptor extends ObjectViewsInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementViewInterceptor.class); 	

	@Override
	public void preHandle(WebRequest request) throws Exception {
		
	}

	@Override
	public void postHandle(WebRequest request, ModelMap model) throws Exception {
		Identified identified = (Identified) model.get("requirement");
        boolean otherViewers = super.addViewerToEntity(Requirement.class.getSimpleName(), identified, request.getRemoteUser());
        model.addAttribute("otherViewers", otherViewers);
	}

	@Override
	public void afterCompletion(WebRequest request, Exception ex) throws Exception {
		// TODO Auto-generated method stub
	}

}
