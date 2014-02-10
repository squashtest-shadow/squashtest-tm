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
package org.squashtest.tm.hibernate.mapping.users
 
import org.apache.poi.hssf.record.formula.functions.T
import org.squashtest.tm.domain.users.Team
import org.squashtest.tm.hibernate.mapping.HibernateMappingSpecification
import org.squashtest.csp.tools.unittest.hibernate.HibernateOperationCategory

/**
  * @author mpagnon
  */
class TeamMappingIT extends HibernateMappingSpecification {
    def "should persist and retrieve a team"() {
        given:
        def team = new Team();
        team.name = "avengers"
		team.description ="code1"

        when:
        persistFixture team
        def res = use (HibernateOperationCategory) {
            sessionFactory.doInSession { it.get(Team, team.id) }
        }

        then:
        res != null
    }


}

