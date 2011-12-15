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
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

@Component
public class HandlerMaxUploadSizeException extends
		AbstractHandlerExceptionResolver {


	
	public HandlerMaxUploadSizeException() {
		super();
	}
	

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		if (exceptionIsHandled(ex)) {	
			
			response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
			
			MaxUploadSizeExceededException mex = (MaxUploadSizeExceededException) ex; // NOSONAR Type was checked earlier
			
			
			if (clientAcceptsMIME(request, MimeType.APPLICATION_JSON)){
				return handleAsJson(mex);
			}
			else if (clientAcceptsMIME(request, MimeType.TEXT_PLAIN)){
				return handleAsText(mex);
			}
			//special delivery for IE
			else if (clientAcceptsMIME(request, MimeType.ANYTHING)){
				return handleAsText(mex);
			}
		}

		return null;
	}
	
	
	private ModelAndView handleAsJson(MaxUploadSizeExceededException mex){
		MaxUploadSizeErrorModel error = new MaxUploadSizeErrorModel(mex);
		return new ModelAndView(new MappingJacksonJsonView(), "maxUploadError", error);		
	}


	private ModelAndView handleAsText(MaxUploadSizeExceededException mex){
		
		String error = "{ \"maxSize\" : " + mex.getMaxUploadSize() +"}";
		
		AbstractView view = new AbstractView() {
					
			@Override
			protected void renderMergedOutputModel(Map<String, Object> model,HttpServletRequest request, HttpServletResponse response)
					throws Exception {
					response.getOutputStream().write(((String)model.get("actionValidationError")).getBytes());
			}
		};
		
		return new ModelAndView(view,"actionValidationError",error);
				
	}
	

	
	private boolean exceptionIsHandled(Exception ex) {
		return ex instanceof MaxUploadSizeExceededException;
	}

	
	@SuppressWarnings("unchecked")
	private boolean clientAcceptsMIME(HttpServletRequest request, MimeType type) {
		Enumeration<String> e = request.getHeaders("Accept");

		while (e.hasMoreElements()) {
			String header = e.nextElement();
			if (StringUtils.containsIgnoreCase(StringUtils.trimToEmpty(header), type.requestHeaderValue())) {
				return true;
			}
		}
		return false;
	}
}
