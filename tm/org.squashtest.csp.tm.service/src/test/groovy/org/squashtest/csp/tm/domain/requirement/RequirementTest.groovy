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
package org.squashtest.csp.tm.domain.requirement

import static org.squashtest.csp.tm.domain.requirement.RequirementStatus.*

import org.squashtest.csp.tm.domain.IllegalRequirementModificationException
import org.squashtest.csp.tm.domain.testcase.TestCase

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class RequirementTest extends Specification {

	Requirement requirement;
	
	//params : 
	@Shared methods = [ 
		[ "setName", {it.getName()}, "toto" ], 
		[ "setDescription", {it.getDescription()}, "successful test" ], 
		[ "addVerifyingTestCase", {def list = it.getVerifyingTestCase() as List; return list.get(0)}, new TestCase(name:"tc", description:"desc tc")],
		[ "setReference", {it.getReference()}, "blahblah"], 
		[ "setCriticality", {it.getCriticality()}, RequirementCriticality.MAJOR]
	]
	
	
	@Unroll
	def "should allow modification for status WORK_IN_PROGRESS"(){
		
		given :
			Requirement requirement = new Requirement("test req", "this is a test req")
			requirement.setStatus(WORK_IN_PROGRESS)
		
		when :
			Requirement.metaClass.invokeMethod(requirement, method[0], method[2])
		
		then :	
			method[2] == method[1].call(requirement)
			
		where :
			method << methods
			
	}
	
	@Unroll
	def "should allow modification for status UNDER_REVIEW"(){
		
		given :
			Requirement requirement = new Requirement("test req", "this is a test req")
			requirement.setStatus(UNDER_REVIEW)
		
		when :
			Requirement.metaClass.invokeMethod(requirement, method[0], method[2])
		
		then :
			method[2] == method[1].call(requirement)
			
		where :
			method << methods
			
	}
	
	
	@Unroll
	def "should not allow modification for status APPROVED"(){
		
		given :
			Requirement requirement = new Requirement("test req", "this is a test req")
			requirement.setStatus(UNDER_REVIEW)//needed because of the workflow
			requirement.setStatus(APPROVED)
		
		when :
			Requirement.metaClass.invokeMethod(requirement, method[0], method[2])
		
		then :
			thrown(IllegalRequirementModificationException)
			
		where :
			method << methods
			
	}

	
	@Unroll
	def "should not allow modification for status OBSOLETE"(){
		
		given :
			Requirement requirement = new Requirement("test req", "this is a test req")
			requirement.setStatus(OBSOLETE)
		
		when :
			Requirement.metaClass.invokeMethod(requirement, method[0], method[2])
		
		then :
			thrown(IllegalRequirementModificationException)
			
		where :
			method << methods
			
	}
	
	
	
	@Unroll("should allow removal of a test case for #status ")
	def "should allow removal of a test case"(){
		
		given : 
			def tc = new TestCase(name:"tc", description:"tc")
			Requirement requirement = prepareRequirement(status, tc)
		when :
			requirement.removeVerifyingTestCase(tc)
		then :
			requirement.getVerifyingTestCase().size()==0;
			
		where :
			status << [ WORK_IN_PROGRESS, UNDER_REVIEW ]		
		
	}
	
	@Unroll("should not allow removal of a test case for #status ")
	def "should not allow removal of a test case"(){
		
		given :
			def tc = new TestCase(name:"tc", description:"tc")
			Requirement requirement = prepareRequirement(status, tc)
		when :
			requirement.removeVerifyingTestCase(tc)
		then :
			thrown(IllegalRequirementModificationException)
			
		where :
			status << [ APPROVED, OBSOLETE]
		
	}
	
	
	
	@Unroll("should allow status change when current status is #status")
	def "should allow status change"(){
		given : 
			def req = prepareRequirement(status)
		when :
			req.setStatus(status)
		then :
			notThrown(IllegalRequirementModificationException)
		where :
			status << [WORK_IN_PROGRESS, UNDER_REVIEW, APPROVED]
	}
	
	def "should not allow status change for OBSOLETE"(){
		given :
			def req = prepareRequirement(OBSOLETE)
		when :
			req.setStatus(OBSOLETE)
		then :
			thrown(IllegalRequirementModificationException)
	}
	
	
	@Unroll("the following workflow transition for #status are legal")
	def "check workflow legal"(){
		
		when :
			def arrayResult = [];
			for (tester in availableStatuses){
				def req = prepareRequirement(status)
				req.setStatus(tester)
				arrayResult << req.getStatus()
			}
			
		then :
			arrayResult  == availableStatuses
			
		where :
			status				|	availableStatuses
			WORK_IN_PROGRESS  	|	[ OBSOLETE, WORK_IN_PROGRESS, UNDER_REVIEW ] 
			UNDER_REVIEW		|	[ OBSOLETE, UNDER_REVIEW, APPROVED, WORK_IN_PROGRESS ]
			APPROVED			|	[ OBSOLETE, APPROVED, UNDER_REVIEW, WORK_IN_PROGRESS ] 
			
	}
	
	@Unroll("the following workflow transition for #status are not legal")
	def "check workflow illegal"(){
		
		when :
			def arrayResult = [];
			for (tester in illegalStatuses){
				def req = prepareRequirement(status)
				try{
					req.setStatus(tester)
				}catch(IllegalRequirementModificationException){
					arrayResult << tester
				}
			}
			
		then :
			arrayResult  == illegalStatuses
			
		where :
			status				|	illegalStatuses
			WORK_IN_PROGRESS  	|	[ APPROVED ]
			UNDER_REVIEW		|	[  ]
			APPROVED			|	[  ]
			OBSOLETE			|	[ WORK_IN_PROGRESS, UNDER_REVIEW, APPROVED, OBSOLETE ]
			
	}
	
	
	/*
	@Unroll("check legal workflow transitions for #status")
	def "check workflow"(){
		given :
			def req = prepareRequirement(status)
		when :
			req
		
		then :
		
		
	}*/

	
	//that (naive) method builds requirements with initial status that could bypass the workflow.
	private Requirement prepareRequirement(RequirementStatus status){
		def req = new Requirement(name:"req", description:"this is a req");
		
		for (iterStatus in RequirementStatus.values()){
			req.status = iterStatus;
			if (iterStatus == status) break;
		}
		
		return req;
	}
	
	//same
	private Requirement prepareRequirement(RequirementStatus status, TestCase testCase){
		def req = new Requirement(name:"req", description:"this is a req");
			req.addVerifyingTestCase(testCase)
		
		for (iterStatus in RequirementStatus.values()){
			req.status = iterStatus;
			if (iterStatus == status) break;
		}
		
		return req;
	}

}
