/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.csp.core.bugtracker.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.service.AdvancedBugtrackerConnectorAdapter;
import org.squashtest.csp.core.bugtracker.service.InternalBugtrackerConnector;
import org.squashtest.csp.core.bugtracker.service.SimpleBugtrackerConnectorAdapter;
import org.squashtest.csp.core.bugtracker.spi.AdvancedBugTrackerConnector;
import org.squashtest.csp.core.bugtracker.spi.AdvancedBugTrackerConnectorProvider;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnectorProvider;


/**
 * Factory of BugTrackerConnector objects. It delegates to {@link BugTrackerConnectorProvider} which should register to
 * this factory.
 *
 * @author Gregory Fouquet
 *
 */
public class BugTrackerConnectorFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerConnectorFactory.class);

	/**
	 * Registered providers mapped by connector kind.
	 */
	private Map<String, BugTrackerConnectorProvider> providerByKind = new ConcurrentHashMap<String, BugTrackerConnectorProvider>(2);
	private Map<String, AdvancedBugTrackerConnectorProvider> advProviderByKind = new ConcurrentHashMap<String, AdvancedBugTrackerConnectorProvider>(2);

	
	public void setProviderByKind(Map<String, BugTrackerConnectorProvider> providerByKind) {
		this.providerByKind = providerByKind;
	}
	
	public void setAdvProvidersByKind(Map<String, AdvancedBugTrackerConnectorProvider> advProviderByKind){
		this.advProviderByKind = advProviderByKind;
	}
	
	public Set<String> getProviderKinds(){
		Set<String> result = new HashSet<String>();
		result.addAll(providerByKind.keySet());
		result.addAll(advProviderByKind.keySet());
		return result;
	}
	
	
	public InternalBugtrackerConnector createConnector(BugTracker bugTracker) {
		
		String kind = bugTracker.getKind();
		InternalBugtrackerConnector  connector;
		
		LOGGER.debug("Creating Connector for bug tracker of kind {}", kind);
		
		if (isSimpleConnector(kind)){
			connector = createAndWrapSimpleConnector(bugTracker);
		}
		else if (isAdvancedConnector(kind)){
			connector = createAndWrapAdvancedConnector(bugTracker);
		}
		else{
			throw new UnknownConnectorKindException(kind);
		}
		
		return connector;
	}

	
	private boolean isSimpleConnector(String kind){
		return providerByKind.containsKey(kind);
	}
	
	
	private boolean isAdvancedConnector(String kind){
		return advProviderByKind.containsKey(kind);
	}
	
	private InternalBugtrackerConnector createAndWrapSimpleConnector(BugTracker bugTracker){
		BugTrackerConnectorProvider provider = providerByKind.get(bugTracker.getKind());
		BugTrackerConnector connector = provider.createConnector(bugTracker);
		return new SimpleBugtrackerConnectorAdapter(connector);
	}
	
	private InternalBugtrackerConnector createAndWrapAdvancedConnector(BugTracker bugTracker){
		AdvancedBugTrackerConnectorProvider provider = advProviderByKind.get(bugTracker.getKind());
		AdvancedBugTrackerConnector connector = provider.createConnector(bugTracker);
		return new AdvancedBugtrackerConnectorAdapter(connector);		
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
		
		if (provider == null){
			return;
		}
		
		String kind = provider.getBugTrackerKind();

		LOGGER.info("Unregistering Connector provider for bug trackers of kind '{}'", kind);

		providerByKind.remove(kind);
	}
	
	/**
	 * Registers a new kind of connector provider, making it instantiable by this factory.
	 *
	 * @param provider
	 */
	public void registerAdvancedProvider(AdvancedBugTrackerConnectorProvider provider, Map serviceProperties) {
		String kind = provider.getBugTrackerKind();

		if (kind == null) {
			throw new NullArgumentException("provider.bugTrackerKind");
		}

		LOGGER.info("Registering Connector provider for bug trackers of kind '{}'", kind);

		advProviderByKind.put(kind, provider);
	}

	/**
	 * Unregisters a kind of provider, making it no longer instanciable by this factory.
	 *
	 * @param provider
	 */
	public void unregisterAdvancedProvider(AdvancedBugTrackerConnectorProvider provider, Map serviceProperties) {
		
		if (provider == null){
			return;
		}
		
		String kind = provider.getBugTrackerKind();

		LOGGER.info("Unregistering Connector provider for bug trackers of kind '{}'", kind);

		advProviderByKind.remove(kind);
	}
}
