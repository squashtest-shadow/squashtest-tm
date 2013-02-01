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
package org.squashtest.csp.tm.internal.service.users

import org.apache.poi.hssf.record.formula.functions.T
import org.squashtest.csp.core.security.acls.model.ObjectAclService
import org.squashtest.csp.tm.domain.users.Team
import org.squashtest.csp.tm.internal.repository.TeamDao
import org.squashtest.csp.tm.internal.service.customField.NameAlreadyInUseException
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory

import spock.lang.Specification


class CustomTeamModificationServiceImplTest extends Specification {

CustomTeamModificationServiceImpl service = new CustomTeamModificationServiceImpl()
TeamDao teamDao = Mock()
ObjectAclService aclService = Mock()
	
	def setup(){
		service.teamDao = teamDao
		service.aclService = aclService
	}

	def "should persist a new team"(){
		given : Team team = new Team()
		when: service.persist(team)
		then : 1* teamDao.persist(team)
	}
	
	def "should not persist team because name already in use"(){
		given : Team team = new Team()
		team.setName("team1")
		Team team2 = Mock()
		teamDao.findAllByName("team1")>> [team2]
		
		when : service.persist(team)
		
		then:
		0* teamDao.persist(team)
		thrown(NameAlreadyInUseException)
	}
	
	def "should delete a team and delete acls"(){
		given : Team team = new Team()
		use(ReflectionCategory){
			Team.setMetaClass(field:"id", of:team, to :1L)
		}
		teamDao.findById(1L)>> team
		when: service.deleteTeam(1L)
		then : 
		1* aclService.removeAllResponsibilitiesForParty(1L)
		1* teamDao.delete(team)
	}
	
}
