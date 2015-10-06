/**
 * This file is part of the Squashtest platform.
 * Copyright (C) 2010 - 2015 Henix, henix.fr
 * <p/>
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * <p/>
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * this software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.fileupload;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationListener;
import org.squashtest.tm.event.ConfigUpdateEvent;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.web.internal.controller.attachment.UploadedData;

/**
 * TODO NOSGI fait que ça compile à l'arrache
 */
public class UploadContentFilterUtil implements ApplicationListener<ConfigUpdateEvent> {


//	private OsgiBundleApplicationContextEventMulticaster publisher;

    private ConfigurationService config;

    public void setConfig(ConfigurationService config) {
        this.config = config;
    }
//	public void setPublisher(OsgiBundleApplicationContextEventMulticaster publisher) {
//		this.publisher = publisher;
//	}

    private String[] allowed;

    private String whiteListKey;

    public void setWhiteListKey(String whiteListKey) {
        this.whiteListKey = whiteListKey;
    }


    private void updateConfig() {
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

    public void init() {
//		publisher.addApplicationListener(this);
        updateConfig();
    }


    //	@Override
    public void onOsgiApplicationEvent(ConfigUpdateEvent event) {
        updateConfig();
    }

    @Override
    public void onApplicationEvent(ConfigUpdateEvent event) {

    }
}
