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
package org.squashtest.csp.tm.web.internal.handler;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import org.squashtest.csp.tm.domain.ActionException;


@Component
public class HandlerActionExceptionResolver extends   AbstractHandlerExceptionResolver {
	private static final String JSON_MIME_TYPE = "application/json";
	private static final String TEXT_MIME_TYPE = "text/plain";
	
	public HandlerActionExceptionResolver(){
		super();
	}
	

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		
		if (exceptionIsHandled(ex)){
		
			
			if (clientAcceptsMIME(request, JSON_MIME_TYPE)) {
				response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);	
				ActionValidationErrorModel error = new ActionValidationErrorModel(ex);
	
				return new ModelAndView(new MappingJacksonJsonView(), "actionValidationError", error);
			}
			
			else if (clientAcceptsMIME(request, TEXT_MIME_TYPE)){
				response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
				
				String error = ex.getClass().getSimpleName()+":"+ex.getMessage();
				
				AbstractView view = new AbstractView() {
					
					@Override
					protected void renderMergedOutputModel(Map<String, Object> model,
							HttpServletRequest request, HttpServletResponse response)
							throws Exception {
						for (Object obj : model.values()){
							response.getOutputStream().write(obj.toString().getBytes());
							response.getOutputStream().write('\n');
						}
						
					}
				};
				
				return new ModelAndView(view,"actionValidationError",error);
				
			}
		}

		return null;
	}


	
	private boolean exceptionIsHandled(Exception ex) {
		return ex instanceof ActionException;
	}

	
	@SuppressWarnings("unchecked")
	private boolean clientAcceptsMIME(HttpServletRequest request, String mimeType) {
		Enumeration<String> e = request.getHeaders("Accept");

		while (e.hasMoreElements()) {
			String header = e.nextElement();
			if (StringUtils.containsIgnoreCase(StringUtils.trimToEmpty(header), mimeType)) {
				return true;
			}
		}
		return false;
	}
}
