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
package org.squashtest.csp.core.bugtracker.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnectorProvider;


/**
 * Factory of BugTrackerConnector objects. It delegates to {@link BugTrackerConnectorProvider} which should register to
 * this factory.
 *
 * @author Gregory Fouquet
 *
 */
@Component("squashtest.core.bugtracker.BugTrackerConnectorFactory")
public class BugTrackerConnectorFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerConnectorFactory.class);

	/**
	 * Registered providers mapped by connector kind.
	 */
	private Map<String, BugTrackerConnectorProvider> providerByKind = new ConcurrentHashMap<String, BugTrackerConnectorProvider>(
			5);

	public void setProviderByKind(Map<String, BugTrackerConnectorProvider> providerByKind) {
		this.providerByKind = providerByKind;
	}

	public BugTrackerConnector createConnector(BugTracker bugTracker) {
		String kind = bugTracker.getKind();

		LOGGER.debug("Creating Connector for bug tracker of kind {}", kind);
		BugTrackerConnectorProvider connector = getProviderForKind(kind);

		return connector.createConnector(bugTracker);
	}

	/**
	 * Returns the connector provider for the given kind.
	 *
	 * @param kind
	 * @return
	 * @throws UnknownConnectorKindException
	 *             if there is no registered connector provider for the given kind.
	 */
	private BugTrackerConnectorProvider getProviderForKind(String kind) throws UnknownConnectorKindException {
		BugTrackerConnectorProvider connector;

		connector = providerByKind.get(kind);

		if (connector == null) {
			throw new UnknownConnectorKindException(kind);
		}
		return connector;
	}

	/**
	 * Registers a new kind of connector provider, making it instantiable by this factory.
	 *
	 * @param provider
	 */
	public void registerProvider(BugTrackerConnectorProvider provider, Map serviceProperties) {
		String kind = provider.getBugTrackerKind();

		if (kind == null) {
			throw new NullArgumentException("provider.bugTrackerKind");
		}

		LOGGER.info("Registering Connector provider for bug trackers of kind '{}'", kind);

		providerByKind.put(kind, provider);
	}

	/**
	 * Unregisters a kind of provider, making it no longer instanciable by this factory.
	 *
	 * @param provider
	 */
	public void unregisterProvider(BugTrackerConnectorProvider provider, Map serviceProperties) {
		String kind = provider.getBugTrackerKind();

		LOGGER.info("Unregistering Connector provider for bug trackers of kind '{}'", kind);

		providerByKind.remove(kind);
	}
}
