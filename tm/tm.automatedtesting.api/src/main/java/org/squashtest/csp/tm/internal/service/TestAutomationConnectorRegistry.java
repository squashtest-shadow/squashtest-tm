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
package org.squashtest.csp.tm.internal.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import squashtm.automatest.api.TestAutomationConnector;


@Component
public class TestAutomationConnectorRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnectorRegistry.class);

	/**
	 * Registered providers mapped by connector kind.
	 */
	private Map<String, TestAutomationConnector> availableConnectors = new ConcurrentHashMap<String, TestAutomationConnector>(
			5);



	public TestAutomationConnector getConnectorForKind(String kind){
		TestAutomationConnector connector = availableConnectors.get(kind);
		if (connector==null){
			throw new IllegalArgumentException("TestAutomationConnector : unknown kind '"+kind+"'");
		}
		return connector;
	}

	/**
	 * Registers a new kind of connector connector.
	 *
	 * @param provider
	 */
	public void registerConnector(TestAutomationConnector provider, Map serviceProperties) {
		String kind = provider.getConnectorKind();

		if (kind == null) {
			throw new IllegalArgumentException("TestAutomationConnector : kind is undefined");
		}

		LOGGER.info("Registering Connector provider for test automation platforms of kind '{}'", kind);

		availableConnectors.put(kind, provider);
	}

	/**
	 * Unregisters a kind of provider, making it no longer addressable by this registry
	 *
	 * @param provider
	 */
	public void unregisterProvider(TestAutomationConnector provider, Map serviceProperties) {
		String kind = provider.getConnectorKind();

		LOGGER.info("Unregistering Connector provider for test automation platforms of kind '{}'", kind);

		availableConnectors.remove(kind);
	}
	
}
