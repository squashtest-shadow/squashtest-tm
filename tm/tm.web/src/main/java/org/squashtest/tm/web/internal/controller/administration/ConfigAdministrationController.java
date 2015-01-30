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
package org.squashtest.tm.web.internal.controller.administration;

import javax.inject.Inject;

import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEventMulticaster;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.event.ConfigUpdateEvent;
import org.squashtest.tm.service.configuration.ConfigurationService;

@Controller
@RequestMapping("administration/config")
public class ConfigAdministrationController implements ApplicationContextAware, BundleContextAware {

	private static final String WHITE_LIST = "uploadfilter.fileExtensions.whitelist";
	private static final String UPLOAD_SIZE_LIMIT = "uploadfilter.upload.sizeLimitInBytes";
	private static final String IMPORT_SIZE_LIMIT = "uploadfilter.upload.import.sizeLimitInBytes";
	@Inject
	private ConfigurationService configService;
	
	/**
	 * bundle context needed to create osgi event
	 */
	private BundleContext bundleCtx;
	/**
	 * application context needed to create osgi event
	 */
	private ApplicationContext applicationCtx;
	
	/**
	 * publisher of the osgi event.
	 */
	@Inject
	private OsgiBundleApplicationContextEventMulticaster publisher;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public void changeConfig(@RequestParam(WHITE_LIST) String whiteList,
			@RequestParam(UPLOAD_SIZE_LIMIT) String uploadSizeLimit,
			@RequestParam(IMPORT_SIZE_LIMIT) String importSizeLimit){
		
		configService.updateConfiguration(WHITE_LIST, whiteList);
		configService.updateConfiguration(UPLOAD_SIZE_LIMIT, uploadSizeLimit);
		configService.updateConfiguration(IMPORT_SIZE_LIMIT, importSizeLimit);
		ConfigUpdateEvent event = new ConfigUpdateEvent(applicationCtx, bundleCtx.getBundle());
		publisher.multicastEvent(event);
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		bundleCtx = bundleContext;
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		applicationCtx = applicationContext;
		
	}

	
	
}
