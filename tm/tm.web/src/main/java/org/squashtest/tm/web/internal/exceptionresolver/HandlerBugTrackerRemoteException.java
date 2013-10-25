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
package org.squashtest.tm.web.internal.exceptionresolver;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;

// TODO Append something after Exception in class name
@Component
public class HandlerBugTrackerRemoteException extends AbstractHandlerExceptionResolver {


	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		if (exceptionIsHandled(ex) && clientAcceptsJson(request)) {
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

			BugTrackerRemoteException remoteException = (BugTrackerRemoteException) ex; // NOSONAR Type was previously
																						// checked
			List<FieldValidationErrorModel> errors = buildFieldValidationErrors(remoteException);

			return new ModelAndView(new MappingJacksonJsonView(), "fieldValidationErrors", errors);
		}

		return null;
	}

	private List<FieldValidationErrorModel> buildFieldValidationErrors(BugTrackerRemoteException remoteException) {
		List<FieldValidationErrorModel> ves = new ArrayList<FieldValidationErrorModel>();

		ves.add(new FieldValidationErrorModel("", "bugtracker", remoteException.getMessage()));

		return ves;
	}

	private boolean exceptionIsHandled(Exception ex) {
		return ex instanceof BugTrackerRemoteException;
	}

	private boolean clientAcceptsJson(HttpServletRequest request) {
		Enumeration<String> e = request.getHeaders("Accept");

		while (e.hasMoreElements()) {
			String header = e.nextElement();
			if (StringUtils.containsIgnoreCase(StringUtils.trimToEmpty(header), MimeType.APPLICATION_JSON.requestHeaderValue())) {
				return true;
			}
		}
		return false;
	}

}
