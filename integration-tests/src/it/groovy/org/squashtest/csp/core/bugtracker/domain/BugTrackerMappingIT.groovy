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
package org.squashtest.csp.core.bugtracker.domain

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.hibernate.mapping.HibernateMappingSpecification

/**
 * @author Gregory Fouquet
 *
 */
@Transactional
class BugTrackerMappingIT extends HibernateMappingSpecification {
	def "[Issue 3928] should persist a bugtracker"() {
		given:
		BugTracker bt = new BugTracker(url: "http://foo/bar", name: "foo", kind: "bar")

		when:
		currentSession.persist(bt)
		currentSession.flush()
		currentSession.evict(bt)

		then:
		currentSession.get(BugTracker, bt.id) != null
	}

}
