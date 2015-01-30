package org.squashtest.tm.event;

import org.springframework.context.ApplicationEvent;

public class ConfigUpdateEvent extends ApplicationEvent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConfigUpdateEvent(Object source) {
		super(source);
	}

}
