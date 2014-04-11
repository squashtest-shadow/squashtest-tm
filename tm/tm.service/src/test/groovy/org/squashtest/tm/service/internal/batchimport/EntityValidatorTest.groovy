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
package org.squashtest.tm.service.internal.batchimport

import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus;
import static ImportStatus.*

import spock.lang.Specification
import spock.lang.Unroll;

class EntityValidatorTest extends Specification {

	EntityValidator validator;
	Model model;
	
	static def longstring 		=	"IMAHMODERATELYLONGSTRINGOVERFIFTYCHARACTERSSOIFAILTESTSFORTESTCASEREFERENCES"
	
	static def toolongstring 	= 	"BOUHAHAHAHAHAHAAHAHAIAMAVERYLONGSTRINGOVERTWOHUNDREDANDFIFTYFIVECHARACTERSSOIFAILMANYTESTSOFSIZELIMITINLUCINGNAMESANDCUFSANDALLYOUNAMEIT"+
								  	"BOUHAHAHAHAHAHAAHAHAIAMAVERYLONGSTRINGOVERTWOHUNDREDANDFIFTYFIVECHARACTERSSOIFAILMANYTESTSOFSIZELIMITINLUCINGNAMESANDCUFSANDALLYOUNAMEIT"
	
	def setup(){
		validator = new EntityValidator()
		model = Mock()
		validator.model = model
	}
	
	
	
	
	def "should say that a test case is good for the service"(){
		
		given :
			TestCaseTarget target = new TestCaseTarget("/project/test-case")
			TestCase testCase = new TestCase(name:"test-case")
		
		and :
			model.getProjectStatus("project") >> new TargetStatus(Existence.EXISTS, 10l)
		
		when :
			LogTrain train = validator.basicTestCaseChecks(target, testCase)
		
		
		then :
			train.hasCriticalErrors() == false
			train.entries == []
	}
	
	
	
	
	@Unroll("should say nay because #humanmsg")
	def "should say nay for various reasons"(){
		
		given :
			model.getProjectStatus(_) >> { 
				return (it[0] == "project") ? 
					new TargetStatus(Existence.EXISTS, 10l) : 
					new TargetStatus(Existence.NOT_EXISTS, null) 
			}
		
		when :
			LogTrain train = validator.basicTestCaseChecks(target, testCase)
		
		
		then :
		
			train.entries.each { println it.i18nError }
			
			train.entries.size() == 1
			
			def pb = train.entries[0]
			pb.status == status
			pb.i18nError == msg
			
		where :
			testCase										|	target							|	 status	|	msg								|	humanmsg
			tc(name:"test-case")							| 	tar("project/test-case")		|	FAILURE	|	Messages.ERROR_MALFORMED_PATH	|	"malformed path"
			tc(name:"")										|	tar("/project/whatever")		|	FAILURE	|	Messages.ERROR_FIELD_MANDATORY	|	"name is empty"
			tc(name:"test-case")							|	tar("/unknown/test-case")		|	FAILURE	|	Messages.ERROR_PROJECT_NOT_EXIST|	"project doesn't exists"
			tc(name:toolongstring)							|	tar("/project/"+toolongstring)	|	WARNING	|	Messages.ERROR_MAX_SIZE			|	"name is too long"
			tc(name:"test-case", reference:longstring)		|	tar("/project/test-case")		|	WARNING	|	Messages.ERROR_MAX_SIZE			|	"ref is too long"
			
	}
	
	

	
	def tc(args){
		return new TestCase(args)
	}
	
	def tar(arg){
		return new TestCaseTarget(arg)
	}
}
