/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.csp.core.bugtracker.internal.service

import org.squashtest.csp.core.bugtracker.core.BugTrackerConnectorFactory
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.csp.core.bugtracker.service.BugTrackerContext
import org.squashtest.csp.core.bugtracker.service.BugTrackerContextHolder
import org.squashtest.csp.core.bugtracker.service.BugTrackersServiceImpl
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class BugTrackersServiceImplTest extends Specification {
	BugTrackersServiceImpl service = new BugTrackersServiceImpl()
	BugTrackerContextHolder contextHolder = Mock()

	def setup() {
		service.contextHolder = contextHolder
	}

	def "should tell no bugtracker is defined"() {
		given:
		service.bugTracker = BugTracker.NOT_DEFINED

		when:
		def defined = service.isBugTrackerDefined()

		then:
		!defined
	}

	def "should tell a bugtracker is defined"() {
		given:
		service.bugTracker = Mock(BugTracker)

		when:
		def defined = service.isBugTrackerDefined()

		then:
		defined
	}

	def "should tell credentials are needed"() {
		given:
		service.bugTracker = Mock(BugTracker)

		and:
		BugTrackerContext context = Mock()
		contextHolder.context >> context
		context.hasCredentials() >> false

		when:
		def needsCredentials = service.isCredentialsNeeded()

		then:
		needsCredentials
	}

	def "should tell credentials are not needed"() {
		given:
		service.bugTracker = Mock(BugTracker)

		and:
		BugTrackerContext context = Mock()
		contextHolder.context >> context
		context.hasCredentials() >> true

		when:
		def needsCredentials = service.isCredentialsNeeded()

		then:
		!needsCredentials
	}
	def "should store credentials in context"() {
		given:
		service.bugTracker = Mock(BugTracker)

		and :
		BugTrackerConnector connector= Mock();

		and:
		BugTrackerContext context = Mock()
		contextHolder.context >> context

		and:
		BugTrackerConnectorFactory factory = Mock();
		factory.createConnector (_) >> connector;
		service.bugTrackerConnectorFactory = factory;


		when:
		service.setCredentials("foo", "bar")

		then:
		1 * context.setCredentials(!null)
	}

	def "should not store credentials in context when they are not valid"() {
		given:
		service.bugTracker = Mock(BugTracker)

		and:
		BugTrackerContext context = Mock()
		contextHolder.context >> context

		and: "credentials are always invalid"
		BugTrackerConnector connector= Mock();
		connector.checkCredentials (_)  >> {throw new BugTrackerRemoteException("toto", null);}

		and:
		BugTrackerConnectorFactory factory = Mock();
		factory.createConnector (_) >> connector;
		service.bugTrackerConnectorFactory = factory;

		when:
		service.setCredentials("foo", "bar")

		then:
		thrown BugTrackerRemoteException
		//		1 * context.setCredentials(null)
	}
}
