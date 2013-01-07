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
package org.squashtest.tm.web.internal.exceptionresolver;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import org.squashtest.csp.tm.domain.DomainException;


@Component
public class HandlerDomainExceptionResolver extends
		AbstractHandlerExceptionResolver {
	
	@Inject
	private MessageSource messageSource;

	
	public HandlerDomainExceptionResolver() {
		super();
	}
	

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		
		if (exceptionIsHandled(ex) && clientAcceptsJson(request)) {
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

			DomainException dex = (DomainException) ex; // NOSONAR Type was checked earlier
			List<FieldValidationErrorModel> errors = buildFieldValidationErrors(dex, request.getLocale());

			return new ModelAndView(new MappingJacksonJsonView(), "fieldValidationErrors", errors);
		}

		return null;
	}


	private List<FieldValidationErrorModel> buildFieldValidationErrors(DomainException dex, Locale locale ) {
		List<FieldValidationErrorModel> ves = new ArrayList<FieldValidationErrorModel>();
		String message = messageSource.getMessage(dex.getI18nKey(), dex.getI18nParams(), locale);
		ves.add(new FieldValidationErrorModel(dex.getObjectName(), dex.getField(), message));

		return ves;
	}

	
	private boolean exceptionIsHandled(Exception ex) {
		return ex instanceof DomainException;
	}

	
	@SuppressWarnings("unchecked")
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
