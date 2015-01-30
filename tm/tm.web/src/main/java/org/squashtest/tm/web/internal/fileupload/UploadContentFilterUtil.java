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
package org.squashtest.tm.web.internal.fileupload;

import org.apache.commons.io.FilenameUtils;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEventMulticaster;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextListener;
import org.squashtest.tm.event.ConfigUpdateEvent;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.web.internal.controller.attachment.UploadedData;

public class UploadContentFilterUtil implements OsgiBundleApplicationContextListener<ConfigUpdateEvent> {


	private OsgiBundleApplicationContextEventMulticaster publisher;
	
	private ConfigurationService config; 
	
	public void setConfig(ConfigurationService config) {
		this.config = config;
	}
	public void setPublisher(OsgiBundleApplicationContextEventMulticaster publisher) {
		this.publisher = publisher;
	}

	private String[] allowed;

	private String whiteListKey;
	
	public void setWhiteListKey(String whiteListKey) {
		this.whiteListKey = whiteListKey;	
	}
	

	private void updateConfig(){
		String whiteList = config.findConfiguration(whiteListKey);
		allowed = whiteList.split(",");
	}

	public boolean isTypeAllowed(UploadedData upload) {

		String fileType = FilenameUtils.getExtension(upload.getName());

		for (String type : allowed) {
			if (type.trim().equalsIgnoreCase(fileType)) {
				return true;
			}
		}

		return false;
	}

	public void init(){
		publisher.addApplicationListener(this);
		updateConfig();
	}


	@Override
	public void onOsgiApplicationEvent(ConfigUpdateEvent event) {
		updateConfig();	
	}
}
