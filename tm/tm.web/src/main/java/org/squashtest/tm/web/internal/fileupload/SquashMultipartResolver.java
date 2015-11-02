/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.fileupload;

import org.springframework.context.ApplicationListener;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.squashtest.tm.event.ConfigUpdateEvent;
import org.squashtest.tm.service.configuration.ConfigurationService;

import javax.annotation.PostConstruct;

public class SquashMultipartResolver extends CommonsMultipartResolver implements ApplicationListener<ConfigUpdateEvent> {

	private ConfigurationService config;

	/**
	 * Defaults to UPLOAD_SIZE_LIMIT
	 */
	private String maxUploadSizeKey = ConfigurationService.Properties.UPLOAD_SIZE_LIMIT;

	public SquashMultipartResolver() {
		super();
		this.setDefaultEncoding("UTF-8");
	}

	@PostConstruct
	public void init() {
		updateConfig();
	}

	public void setConfig(ConfigurationService config) {
		this.config = config;
	}

	/**
	 * Sets property name of max upload size which shall be fetched with configuration service.
	 * @param maxUploadSizeKey
	 */
	public void setMaxUploadSizeKey(String maxUploadSizeKey) {
		this.maxUploadSizeKey = maxUploadSizeKey;

	}

	private void updateConfig() {
		String uploadLimit = config.findConfiguration(maxUploadSizeKey);
		setMaxUploadSize(Long.valueOf(uploadLimit));
	}

	@Override
	public void onApplicationEvent(ConfigUpdateEvent event) {
		updateConfig();
	}
}
