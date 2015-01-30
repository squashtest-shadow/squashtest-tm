package org.squashtest.tm.web.internal.fileupload;

import org.springframework.osgi.context.event.OsgiBundleApplicationContextEventMulticaster;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextListener;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.squashtest.tm.event.ConfigUpdateEvent;
import org.squashtest.tm.service.configuration.ConfigurationService;

public class SquashMultipartResolver extends CommonsMultipartResolver implements OsgiBundleApplicationContextListener<ConfigUpdateEvent> {


	private OsgiBundleApplicationContextEventMulticaster publisher;
	
	private ConfigurationService config; 
	
	private String maxUploadSizeKey;
	
	public void init(){
		publisher.addApplicationListener(this);
		updateConfig();
	}

	public void setPublisher(OsgiBundleApplicationContextEventMulticaster publisher) {
		this.publisher = publisher;
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
	public void onOsgiApplicationEvent(ConfigUpdateEvent event) {
		updateConfig();	
	}
	
}
