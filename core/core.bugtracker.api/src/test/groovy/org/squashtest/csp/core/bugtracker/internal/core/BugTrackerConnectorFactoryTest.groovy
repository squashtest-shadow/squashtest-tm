/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.csp.core.bugtracker.internal.core

import org.apache.commons.lang.NullArgumentException;
import org.junit.Ignore;
import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory;
import org.squashtest.csp.core.bugtracker.core.UnknownConnectorKindException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.service.SimpleBugtrackerConnectorAdapter;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnectorProvider;

import spock.lang.Specification;


class BugTrackerConnectorFactoryTest extends Specification {
	BugTrackerConnectorFactory factory = new BugTrackerConnectorFactory();

	def "should create a connector of said kind"() {
		given: "a bugtracker definition"
		BugTracker bt = new BugTracker("http://bt", "foo", "", true)

		and: "a connector provider"
		BugTrackerConnector connector = Mock()

		BugTrackerConnectorProvider provider = Mock()
		provider.bugTrackerKind >> "foo"
		provider.createConnector(_) >> connector

		when:
		factory.registerProvider (provider, null)
		def res = factory.createConnector(bt)

		then:
		res instanceof SimpleBugtrackerConnectorAdapter
	}


	def "should not register null kinded providers"() {
		given: "a null kinded provider"
		BugTrackerConnectorProvider provider = Mock()

		when:
		factory.registerProvider (provider, null)

		then:
		thrown(NullArgumentException)
	}

	def "should refuse to create a connector of unknown kind"() {
		given: "a bugtracker definition"
		BugTracker bt = new BugTracker("http://foo", "foo", "", true)

		when:
		def res = factory.createConnector(bt)

		then:
		thrown(UnknownConnectorKindException)
	}

	def "should refuse to create a connector of unregistered kind"() {
		given: "a bugtracker definition"
		BugTracker bt = new BugTracker("http://foo", "foo", "", true)

		and: "a registered connector provider"
		BugTrackerConnector connector = Mock()

		BugTrackerConnectorProvider provider = Mock()
		provider.bugTrackerKind >> "foo"
		provider.createConnector(_) >> connector
		factory.registerProvider (provider, null)

		when:
		factory.unregisterProvider (provider,null)
		def res = factory.createConnector(bt)

		then:
		thrown(UnknownConnectorKindException)
	}
}
