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

import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;

/**
 * Creates a {@link BugTracker} using a {@link Properties} set. If no properties are available, this factory returns
 * {@link BugTracker#NOT_DEFINED}
 *
 * @author Gregory Fouquet
 *
 */
public class PropertiesBugTrackerFactoryBean implements FactoryBean<BugTracker>, InitializingBean {
	private static final String BUG_TRACKER_URL_KEY = "squashtest.bugtracker.url";
	private static final String BUG_TRACKER_KIND_KEY = "squashtest.bugtracker.kind";

	private Properties bugTrackerProperties;
	private BugTracker bugTracker = BugTracker.NOT_DEFINED;

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

	public void setBugTrackerProperties(Properties bugTrackerProperties) {
		this.bugTrackerProperties = bugTrackerProperties;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (bugTrackerProperties != null) {
			String kind = bugTrackerProperties.getProperty(BUG_TRACKER_KIND_KEY);
			String url = bugTrackerProperties.getProperty(BUG_TRACKER_URL_KEY);

			BugTracker bt = new BugTracker(url, kind);
			bugTracker = bt;
		}
	}

}
