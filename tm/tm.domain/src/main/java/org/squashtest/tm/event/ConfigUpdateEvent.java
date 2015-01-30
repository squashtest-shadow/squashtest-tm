package org.squashtest.tm.event;

import org.osgi.framework.Bundle;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEvent;

public class ConfigUpdateEvent extends OsgiBundleApplicationContextEvent{

	public ConfigUpdateEvent(ApplicationContext source, Bundle bundle) {
		super(source, bundle);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


}
