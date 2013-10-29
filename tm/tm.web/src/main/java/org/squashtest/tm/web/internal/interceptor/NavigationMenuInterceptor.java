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
/**
 * 
 */
package org.squashtest.tm.web.internal.interceptor;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.squashtest.tm.web.internal.application.ApplicationPluginManager;
import org.squashtest.tm.web.internal.controller.generic.NavigationButton;

/**
 * @author mpagnon
 *
 */
public class NavigationMenuInterceptor implements WebRequestInterceptor {
	
	@Inject
	private ApplicationPluginManager applicationPluginsManager;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NavigationMenuInterceptor.class);
	/**
	 * @see org.springframework.web.context.request.WebRequestInterceptor#preHandle(org.springframework.web.context.request.WebRequest)
	 */
	@Override
	public void preHandle(WebRequest request) throws Exception {
	
	}

	/**
	 * @see org.springframework.web.context.request.WebRequestInterceptor#postHandle(org.springframework.web.context.request.WebRequest, org.springframework.ui.ModelMap)
	 */
	@Override
	public void postHandle(WebRequest request, ModelMap model) throws Exception {
		LOGGER.debug("Post handle the request to add navigation buttons to the model");
		List<NavigationButton> navigationButtons = applicationPluginsManager.getNavigationButtons();
		model.addAttribute("navigationButtons", navigationButtons);
	}

	/**
	 * @see org.springframework.web.context.request.WebRequestInterceptor#afterCompletion(org.springframework.web.context.request.WebRequest, java.lang.Exception)
	 */
	@Override
	public void afterCompletion(WebRequest request, Exception ex) throws Exception {
	}

}
