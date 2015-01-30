package org.squashtest.tm.web.internal.fileupload;

import org.springframework.context.ApplicationListener;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.squashtest.tm.event.ConfigUpdateEvent;
import org.squashtest.tm.service.configuration.ConfigurationService;

public class SquashMultipartResolver extends CommonsMultipartResolver implements ApplicationListener<ConfigUpdateEvent>{

	
	private ConfigurationService config; 
	
	private String maxUploadSizeKey;
	
	public void init(){
		updateConfig();
	}

	public void setConfig(ConfigurationService config) {
		this.config = config;
	}

	public void setmaxUploadSizeKey(String maxUploadSizeKey) {
		this.maxUploadSizeKey = maxUploadSizeKey;

	}
	
	private void updateConfig(){
		String uploadLimit = config.findConfiguration(maxUploadSizeKey);
		setMaxUploadSize(Long.valueOf(uploadLimit));
	}

	@Override
	public void onApplicationEvent(ConfigUpdateEvent event) {
		updateConfig();
		
	}
	
	
}
