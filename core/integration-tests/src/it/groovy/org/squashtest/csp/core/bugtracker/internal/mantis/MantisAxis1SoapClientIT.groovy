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
package org.squashtest.csp.core.bugtracker.internal.mantis

import org.squashtest.csp.core.bugtracker.core.BugTracker;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;

import spock.lang.Specification;
/**
 * Integration tests for {@link MantisAxis1SoapClient}. Also contains some Mantis API exloration tests 
 */
class MantisAxis1SoapClientIT extends Specification {
	ResourceBundle properties = ResourceBundle.getBundle('mantis')
	BugTracker bugTracker= new BugTracker(properties.getString('url'), 'mantis')
	AuthenticationCredentials credentials = new AuthenticationCredentials(properties.getString('username'), properties.getString('password'))

	def "should connect to remote service"() {
		given:
		AuthenticationCredentials credentials = new AuthenticationCredentials(null, null)

		when:
		MantisAxis1SoapClient client = new MantisAxis1SoapClient(bugTracker)
		def version = client.service.mc_version()

		then:
		version != null
	}

	def "should fetch severities"() {
		given:

		when:
		MantisAxis1SoapClient client = new MantisAxis1SoapClient(bugTracker)
		def severities = client.getSeverities(credentials).collect { it.name }

		def expectedSeverities = [
			'feature',
			'trivial',
			'text',
			'tweak',
			'minor',
			'major',
			'crash',
			'block'
		]

		then:
		severities.size() == expectedSeverities.size()
		severities.containsAll expectedSeverities
	}

	def "should fetch project statuses"() {
		given:

		when:
		MantisAxis1SoapClient client = new MantisAxis1SoapClient(bugTracker)
		def res = client.service.mc_enum_project_status(credentials.username, credentials.password).collect { it.name }

		def expected = [
			'development',
			'stable',
			'release',
			'obsolete'
		]

		then:
		res.size() == expected.size()
		res.containsAll expected
	}

	def "should fetch project view states"() {
		given:

		when:
		MantisAxis1SoapClient client = new MantisAxis1SoapClient(bugTracker)
		def res = client.service.mc_enum_project_view_states(credentials.username, credentials.password).collect { it.name }

		def expected = [
			'public',
			'private'
		]

		then:
		res.size() == expected.size()
		res.containsAll expected
	}

	def "should fetch project id from its name"() {
		given:

		when:
		MantisAxis1SoapClient client = new MantisAxis1SoapClient(bugTracker)
		def res = client.service.mc_project_get_id_from_name(credentials.username, credentials.password, 'foo')

		then:
		res
	}

	def "should fetch issues for  project"() {
		given:

		when:
		MantisAxis1SoapClient client = new MantisAxis1SoapClient(bugTracker)
		def projectId = client.service.mc_project_get_id_from_name(credentials.username, credentials.password, 'foo')
		def res = client.service.mc_project_get_issue_headers(credentials.username, credentials.password, projectId, 0, 0)

		then:
		res.size() != 0
	}

	def "should create port and fetch severities 50 times"() {
		given:

		when:
		def start = System.nanoTime();
		for (int i = 0; i < 100; i++) {
			MantisAxis1SoapClient client = new MantisAxis1SoapClient(bugTracker)
			client.getSeverities(credentials)
		}
		def end = System.nanoTime();


		then:
		println end - start
	}
	def "should fetch severities 50 times"() {
		given:

		when:
		def start = System.nanoTime();
		MantisAxis1SoapClient client = new MantisAxis1SoapClient(bugTracker)
		for (int i = 0; i < 100; i++) {
			client.getSeverities(credentials)
		}
		def end = System.nanoTime();


		then:
		println end - start
	}
}
