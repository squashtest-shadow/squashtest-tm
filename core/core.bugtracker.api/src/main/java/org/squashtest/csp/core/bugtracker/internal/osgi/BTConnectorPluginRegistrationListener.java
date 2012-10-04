/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.core.bugtracker.internal.osgi;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.osgi.service.exporter.OsgiServiceRegistrationListener;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnectorProvider;


//@Component("squashtest.core.bugtracker.BTConnectorPluginRegistrationListener")
@Deprecated
public class BTConnectorPluginRegistrationListener implements OsgiServiceRegistrationListener {
	@Inject
	private BugTrackerConnectorFactory bugTrackerConnectorFactory;

	public void setBugTrackerConnectorFactory(
			BugTrackerConnectorFactory bugTrackerConnectorFactory) {
		this.bugTrackerConnectorFactory = bugTrackerConnectorFactory;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void registered(Object service, Map serviceProperties) {
		if (shouldPropagateEvent(service)) {
			bugTrackerConnectorFactory.registerProvider((BugTrackerConnectorProvider) service, serviceProperties);
		}

	}

	private boolean shouldPropagateEvent(Object service) {
		return service instanceof BugTrackerConnectorProvider;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void unregistered(Object service, Map serviceProperties) {
		if (shouldPropagateEvent(service)) {
			bugTrackerConnectorFactory.unregisterProvider((BugTrackerConnectorProvider) service, serviceProperties);
		}

	}
}
