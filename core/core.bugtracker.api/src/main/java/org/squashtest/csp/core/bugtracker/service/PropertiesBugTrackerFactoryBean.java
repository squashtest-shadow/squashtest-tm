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
package org.squashtest.csp.core.bugtracker.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;

/**
 * Creates a {@link BugTracker}. If any of the properties are not set or set to 'none', it will create
 * an undefined bugtracker. If the name of the bugtracker is not defined, the name will default to 'default'.
 * 
 * @author Gregory Fouquet
 * 
 */
public class PropertiesBugTrackerFactoryBean implements FactoryBean<BugTracker>, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesBugTrackerFactoryBean.class);

	private static final String BUG_TRACKER_UNDEFINED_KIND = "none";
	private static final String BUG_TRACKER_UNDEFINED_URL = "none";
	private static final String BUG_TRACKER_UNSPECIFIED_NAME_DEFAULT = "default";
	

	private BugTracker bugTracker = BugTracker.NOT_DEFINED;

	private String kind;
	private String url;
	private String name;

	@Override
	public BugTracker getObject() throws Exception {
		return bugTracker;
	}

	@Override
	public Class<?> getObjectType() {
		return BugTracker.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		fixWrongProperties();

		if (isNullBugTracker(kind, url)) {
			bugTracker = BugTracker.NOT_DEFINED;
		} else {
			BugTracker bt = new BugTracker(url, kind, name);
			bugTracker = bt;
		}

		LOGGER.warn("Squash will try to connect to a '" + kind + "' kinded bugtracker at url '" + url + "\' named '"+name+'\'');
	}

	/**
	 * 
	 */
	private void fixWrongProperties() {
		if (isPropertyUnset(kind, BUG_TRACKER_UNDEFINED_KIND)) {
			LOGGER.warn("Bug tracker kind was not properly set, '" + BUG_TRACKER_UNDEFINED_KIND
					+ "' will be used instead");
			kind = BUG_TRACKER_UNDEFINED_KIND;
		}
		if (isPropertyUnset(url, BUG_TRACKER_UNDEFINED_URL)) {
			LOGGER.warn("Bug tracker url was not properly set, '" + BUG_TRACKER_UNDEFINED_URL
					+ "' will be used instead");
			url = BUG_TRACKER_UNDEFINED_URL;
		}
		if (isPropertyUnset(name, BUG_TRACKER_UNSPECIFIED_NAME_DEFAULT)){
			LOGGER.warn("Bug tracker name was not properly set, '" + BUG_TRACKER_UNSPECIFIED_NAME_DEFAULT
					+ "' will be used instead");
			name = BUG_TRACKER_UNSPECIFIED_NAME_DEFAULT;
		}

	}
	
	private boolean isPropertyUnset(String property, String defaultSetting){
		return (
					StringUtils.isBlank(property) ||
					property.equals(defaultSetting)
				);
		
	}

	private boolean isNullBugTracker(String kind, String url) {
		return kind.equals(BUG_TRACKER_UNDEFINED_KIND) || url.equals(BUG_TRACKER_UNDEFINED_URL);
	}

	/**
	 * @return the kind
	 */
	public String getKind() {
		return kind;
	}

	/**
	 * @param kind
	 *            the kind to set
	 */
	public void setKind(String kind) {
		this.kind = kind;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	public void setName(String name){
		this.name=name;
	}
	
	public String getName(){
		return name;
	}

}
