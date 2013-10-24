/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.service.security.acls.jdbc

import javax.inject.Inject;

import org.spockframework.util.NotThreadSafe;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.service.DbunitServiceSpecification;
import org.unitils.dbunit.annotation.DataSet;
import org.squashtest.tm.domain.project.Project;

import spock.lang.Unroll
import spock.unitils.UnitilsSupport;


/*
 * The dataset in this test is the following :
 * 
 * project : {
 *  id:14
 * }
 * 
 * team{
 * 	id:38
 *  clearance: project manager  
 * }
 * 
 * user{
 * 	id:39
 * 	team:none
 * 	clearance:none
 * 	remarks : totally unrelated.
 * }
 * 
 * user{
 * 	id : 34
	team : none
 *  clearance :  tester 
 * }
 * 
 * user{
 *  id : 35
 *  team : none
 *  clearance : project manager
 * }
 * 
 * user{
 * 	id:36
 *  team:38
 *  clearance : none 
 *  team clearance : project manager
 * }
 * 
 * user{
 *  id:37
 *  team : 38
 *  clearance : project manager
 *  team clearance : project manager
 * }
 * 	
 * 
 * 
 */

@NotThreadSafe
@UnitilsSupport
@Transactional
class DerivedPermissionsManagerIt extends DbunitServiceSpecification {

	private static final String PROJECT_CLASSNAME = Project.class.getName()
	private static final ObjectIdentity PROJECT_IDENTITY = new ObjectIdentityImpl(PROJECT_CLASSNAME, 14l)

	static final long TEAM_ID = 38l
	
	@Inject
	private DerivedPermissionsManager manager

	
	
	@Unroll("should decide that it should #yesorno handle object of class #oclass")
	def "should decide that it should handle this or that"(){
		
		expect :
			ObjectIdentity id = new ObjectIdentityImpl(type, 0l)
			response == manager.isSortOfProject(id)
			
		where :
		
		type 						|		response		| yesorno
		Project.class.getName()		|		true			| ""
		'some.other.class'			|		false			| "not"
	}
	
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "should find the members of the team"(){
		
		given :
			def id = TEAM_ID	
		
		when :
			def ids = manager.findMembers(id)
		
		
		then :
			ids as Set == [36l, 37l] as Set
		
		
	}
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "should find out that the said team is actually a user and returns it"(){
		
		given : 
			def id = 36l
		
		when :
			def ids = manager.findMembers(id)
		
		
		then :
			ids == [36l]
		
	}
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "should find all the users that participate to a project, either directly or via a team"(){
		
		when :
			def usermanagers = manager.findUsers(PROJECT_IDENTITY)
		
		then :
			usermanagers as Set == [34l, 35l, 36l, 37l] as Set
	}
	
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "should remove project manager authorities from the given users"(){
		
		when :
			manager.removeProjectManagerAuthorities([35l, 37l])
			def remainingAuthorizedUsers = 
				executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY 
							where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """)
			
			
		then :
			remainingAuthorizedUsers == [36l]
	}
	
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "should grant project manager authority to user"(){
		
		when :
			manager.grantProjectManagerAuthorities([34l])
			def authorizedUsers = 
				executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY 
								where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """)
		
		then :
			authorizedUsers.contains 34l
	}
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "should filter out users that don't manage anything"(){
		
		given :
			def ids = [34l, 35l, 36l, 37l]
		
		when :
			def res = manager.retainUsersManagingAnything(ids)
		
		then :
			res as Set == [35l, 36l, 37l] as Set
	}
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "should update the authorities of a new project manager on the project"(){
		
		given :
			executeSQL(""" update ACL_RESPONSIBILITY_SCOPE_ENTRY 
						set ACL_GROUP_ID=5 where PARTY_ID=34 """)

		when :
			manager.recomputeDerivedPermissions(34l)
			
			def authorizedUsers =
			executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
								where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """)
					
		then :
			authorizedUsers as Set == [34l, 35l, 36l, 37l] as Set
		
	}
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "should update the authorities of a new team member"(){
		
		given :
			executeSQL(""" insert into CORE_TEAM_MEMBER values(38, 34) """)

		when :
			manager.recomputeDerivedPermissions(34l)
			
			def authorizedUsers =
			executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
								where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """)
		
		then :
			authorizedUsers as Set == [34l, 35l, 36l, 37l] as Set
		
	}
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "should remove the authorities of a demoted project manager"(){
		
		given :
			executeSQL(""" update ACL_RESPONSIBILITY_SCOPE_ENTRY 
						set ACL_GROUP_ID=2 where PARTY_ID=35 """)

		when :
			manager.recomputeDerivedPermissions(35l)
			
			def authorizedUsers =
			executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
								where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """)
		
		then :
			authorizedUsers as Set == [36l, 37l] as Set
		
	}
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "should remove the authorities of a member removed of a team of project leaders and who doesn't manage anything else"(){
		
		given :
			executeSQL("""delete from CORE_TEAM_MEMBER where USER_ID=36 """)

		when :
			manager.recomputeDerivedPermissions(36l)
			
			def authorizedUsers =
			executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
								where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """)
		
		then :
			authorizedUsers as Set == [35l, 37l] as Set
		
	}
	
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "should maintain the authorities of a removed project leader because he still belongs to the team of project leaders"(){
		
		given :
			executeSQL("""delete from ACL_RESPONSIBILITY_SCOPE_ENTRY where PARTY_ID=37 """)

		when :
			manager.recomputeDerivedPermissions(37l)
			
			def authorizedUsers =
			executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
								where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """)
		
		then :
			authorizedUsers as Set == [35l, 36l, 37l] as Set
		
	}
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "should maintain the authorities of a guy removed from the team of project leader because he still has direct manager clearances"(){
		
		given :
			executeSQL(""" delete from CORE_TEAM_MEMBER where USER_ID=37 """)

		when :
			manager.recomputeDerivedPermissions(37l)
			
			def authorizedUsers =
			executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
								where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """)
		
		then :
			authorizedUsers as Set == [35l, 36l, 37l] as Set
		
	}
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "once the team of project leaders is removed, one user is demoted but not the other one"(){
		
		given :
			executeSQL(""" delete from ACL_RESPONSIBILITY_SCOPE_ENTRY where  PARTY_ID=""" +TEAM_ID)

		when :
			manager.recomputeDerivedPermissions(TEAM_ID)
			
			def authorizedUsers =
			executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
								where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """)
		
		then :
			authorizedUsers as Set == [35l,  37l] as Set
		
	}
	
	@DataSet("DerivedPermissionsManagerIt.setup.xml")
	def "once the team of project leaders is demoted, one user is demoted but not the other one"(){
		
		given :
			executeSQL(""" update ACL_RESPONSIBILITY_SCOPE_ENTRY 
						set ACL_GROUP_ID=2 where PARTY_ID=""" +TEAM_ID)

		when :
			manager.recomputeDerivedPermissions(TEAM_ID)
			
			def authorizedUsers =
			executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
								where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """)
		
		then :
			authorizedUsers as Set == [35l,  37l] as Set
		
	}
	
}
