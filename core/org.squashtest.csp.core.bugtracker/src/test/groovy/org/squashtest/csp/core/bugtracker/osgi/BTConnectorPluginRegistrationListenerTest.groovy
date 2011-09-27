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
package org.squashtest.csp.core.bugtracker.osgi

import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory;
import org.squashtest.csp.core.bugtracker.internal.osgi.BTConnectorPluginRegistrationListener;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnectorProvider;

import spock.lang.Specification;

class BTConnectorPluginRegistrationListenerTest extends Specification {
	BTConnectorPluginRegistrationListener registerer = new BTConnectorPluginRegistrationListener()
	BugTrackerConnectorFactory factory = Mock()

	def setup() {
		registerer.bugTrackerConnectorFactory = factory
	}

	def "should register connector provider to factory"() {
		given:
		BugTrackerConnectorProvider service = Mock()

		when:
		registerer.registered service, null

		then:
		1 * factory.registerProvider(service)
	}

	def "should not register a service which is not a connector provider"() {
		given:
		Object service = new Object()

		when:
		registerer.registered service, null

		then:
		0 * factory.registerProvider(service)
	}

	def "should unregister connector provider to factory"() {
		given:
		BugTrackerConnectorProvider service = Mock()

		when:
		registerer.unregistered service, null

		then:
		1 * factory.unregisterProvider(service)
	}

	def "should not unregister a service which is not a connector provider"() {
		given:
		Object service = new Object()

		when:
		registerer.unregistered service, null

		then:
		0 * factory.unregisterProvider(service)
	}
}
