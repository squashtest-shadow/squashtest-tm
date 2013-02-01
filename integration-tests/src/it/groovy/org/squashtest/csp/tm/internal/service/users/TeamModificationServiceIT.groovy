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

package org.squashtest.csp.tm.internal.service.users;

import static org.junit.Assert.*

import javax.inject.Inject

import org.apache.poi.hssf.record.formula.functions.T
import org.springframework.transaction.annotation.Transactional

import org.squashtest.csp.tm.domain.NamedReference
import org.squashtest.csp.tm.domain.users.Team;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.domain.users.UsersGroup;
import org.squashtest.csp.tm.internal.service.DbunitServiceSpecification
import org.squashtest.csp.tm.internal.service.customField.NameAlreadyInUseException;
import org.squashtest.csp.tm.service.users.TeamModificationService;
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.annotation.ExpectedDataSet;

import spock.unitils.UnitilsSupport

/**
 * @author mpagnon
 *
 */
@UnitilsSupport
@Transactional
class TeamModificationServiceIT extends DbunitServiceSpecification {

	@Inject
	TeamModificationService service

	def "should persist a team"(){
		given : 
		Team team = new Team()
		team.name = "team1"
		
		when:
		service.persist(team)

		then:
		 def result = findAll("Team")
		 result.any ({it.name == "team1"})
	}
	
	@DataSet("TeamModificationServiceIT.should delete team with acls.xml")
	def"should delete a team along with it's acls"(){
		given:
		def teamId = 10L
		when:
		service.deleteTeam(teamId)
		then:
		!found(Team.class, 10L)
		!found("ACL_RESPONSIBILITY_SCOPE_ENTRY", "ID", 240L)		
	}
	
	@DataSet("TeamModificationServiceIT.should not persist team homonyme.xml")
	def"should not persist team homonyme"(){
		given : 
		Team team = new Team()
		team.name = "team1"
		when:
		service.persist(team)
		then:
		thrown(NameAlreadyInUseException)
	}
	
	
	@DataSet("TeamModificationServiceIT.should delete team but no user.xml")
	def"should delete a team but no user"(){
		given:
		def teamId = 10L
		when:
		service.deleteTeam(teamId)
		then:
		!found(Team.class, 10L)
		found(User.class, 20L)
		found(User.class, 30L)
		found("ACL_RESPONSIBILITY_SCOPE_ENTRY", "ID", 241L)
		found("ACL_RESPONSIBILITY_SCOPE_ENTRY", "ID", 242L)
	}
	
	@DataSet("TeamModificationServiceIT.should delete team having a core group.xml")
	def"should delete team having a core group"(){
		given:
		def teamId = 10L
		when:
		service.deleteTeam(teamId)
		then:
		!found(Team.class, 10L)
		
	}
}
