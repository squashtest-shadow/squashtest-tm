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

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.SessionFactory;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
import org.squashtest.tm.service.internal.batchimport.Model.StepType;
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService;
import org.hibernate.Query;
import spock.lang.Specification;
import org.hibernate.Session;
import static org.squashtest.tm.service.internal.batchimport.Model.Existence.*;


public class ModelTest extends Specification{

	SessionFactory factory;
	CustomFieldDao cufDao;
	TestCaseLibraryFinderService finderService;
	
	Model model;
	
	def setup(){
		
		factory = Mock(SessionFactory)
		cufDao = Mock(CustomFieldDao)
		finderService = Mock(TestCaseLibraryFinderService)
		
		model = new Model()
		model.sessionFactory = factory
		model.cufDao = cufDao
		model.finderService = finderService
		
	}
	
	
	// ****************** utils **********************
	
	// note : implicitly this test also tests TestCaseTargets.equals(_)
	def "should eliminate redundant values"(){
		
		expect :
			model.uniqueList(targets) == filtered
		
		where :
			targets = createTargets("/project/a", "/project/b", "/project/a", "/project/c")
			filtered = createTargets("/project/a", "/project/b", "/project/c")
	}
	
	def "should collect projects names from targets"(){
		
		expect :
			model.collectProjects(targets) == ["project a", "project b"]
		
		where :
			targets = createTargets("/project a/test1", "/project a/test2", "/project b/test 3")
				
	}

	def "should collect the paths of a bunch of targets"(){
		
		expect :
			model.collectPaths(targets) == ["/project/a", "/project/B"]
		 
		where :
			targets = createTargets("/project/a", "/project/B")
	}
	
	// ************* core mechanics ******************

	
	
	def "should init the model with some test cases"(){
		given :
			def targets = createTargets("/project/folder/test1", "/project/<nonexistant>/test2", "/project/folder/test3")
		
		and :
			finderService.findNodeIdsByPath(_) >> [10l, null, 20l]
			
		when :
			model.initTestCases(targets)
		
			TargetStatus status1 = model.testCaseStatusByTarget[targets[0]]
			TargetStatus status2 = model.testCaseStatusByTarget[targets[1]]
			TargetStatus status3 = model.testCaseStatusByTarget[targets[2]]
			
		then :
			status1.status == EXISTS
			status2.status == NOT_EXISTS
			status3.status == EXISTS
			
			status1.id == 10l
			status2.id == null
			status3.id == 20l
		
			 
	}
	
	
	def "should init the test steps"(){
		
		given :
			def targets = createTargets("/project/notok", "/project/ok", "/project/tobedeleted")
			
		and :
			model.testCaseStatusByTarget[targets[0]] = new TargetStatus(NOT_EXISTS, null)
			model.testCaseStatusByTarget[targets[1]] = new TargetStatus(EXISTS, 10l)
			model.testCaseStatusByTarget[targets[2]] = new TargetStatus(TO_BE_DELETED, 20l)
		and :
			mockSessionQuery(["ACTION", "CALL", "ACTION"])
				
		when :
			model.initTestSteps(targets)
		
		
		then :
			model.testCaseStepsByTarget[targets[0]] == []
			model.testCaseStepsByTarget[targets[1]] == [StepType.ACTION, StepType.CALL, StepType.ACTION]
			model.testCaseStepsByTarget[targets[2]] == []
		
			
	}
	
	
	def "should init the projects"(){
		
		given :
			def targets = createTargets("/project a/test1", "/sprololo/testB", "/project a/test2")
		
		and :
			Project p = mockProject("project a", 10l)
			mockSessionQuery([p])
			
		and : 
			def tccufs = [Mock(CustomField), Mock(CustomField), Mock(CustomField)]
			def stcufs = [Mock(CustomField)]
			cufDao.findAllBoundCustomFields(10l, BindableEntity.TEST_CASE) >> tccufs
			cufDao.findAllBoundCustomFields(10l, BindableEntity.TEST_STEP) >> stcufs
		
		when :
			model.initProjects(targets)
		
			TargetStatus status1 = model.projectStatusByName["project a"] 
			TargetStatus status2 = model.projectStatusByName["sprololo"]
			
		then :
			status1.status == EXISTS
			status1.id == 10l
			
			status2.status == NOT_EXISTS
			status2.id == null
		
			model.tcCufsPerProjectname["project a"] == tccufs
			model.stepCufsPerProjectname["project a"] == stcufs
	}
	
	
	// ************************** accessors ****************************
	
	// cannot test the case where the entry doesn't exist yet in the map
	// because it would trigger init() on that target
	// which cannot easily be mocked.
	def "should get the status of a test case"(){
		
		given :
			def target = new TestCaseTarget("/project/bob")
			def status = new TargetStatus(TO_BE_CREATED)
			model.testCaseStatusByTarget[target] = status		
		
		when :
			def res = model.getStatus(target) 
		
		then :
			res == status
		
	} 
	
	def "should change the status of a TestCase to 'EXISTS'"(){
		given :
			def target = new TestCaseTarget("/project/mike")
			model.testCaseStatusByTarget[target] == new TargetStatus(TO_BE_CREATED)
		when :
			model.setExists(target, 10l)
		
		then :
			model.getStatus(target).status == EXISTS
	}
	
	def "should change the status of a TestCase to 'TO_BE_CREATED'"(){
		given :
			def target = new TestCaseTarget("/project/mike")
			model.testCaseStatusByTarget[target] == new TargetStatus(NOT_EXISTS)
		when :
			model.setToBeCreated(target)
		
		then :
			model.getStatus(target).status == TO_BE_CREATED
	}
	
	def "should flag a test case to TO_BE_DELETED and remove preemptively the step informations"(){
		given :
			def target = new TestCaseTarget("/project/robert")
			model.testCaseStatusByTarget[target] = new TargetStatus(EXISTS, 12l)
			model.testCaseStepsByTarget[target] = [StepType.ACTION, StepType.ACTION, StepType.ACTION]
		when :
			model.setToBeDeleted(target)
		
		then :
			model.getStatus(target).status == TO_BE_DELETED
			model.testCaseStepsByTarget[target] == []
			
	}
	

		
	def "should flag a test case as deleted and remove the step informations"(){
		given :
			def target = new TestCaseTarget("/project/daryl")
			model.testCaseStatusByTarget[target] = new TargetStatus(TO_BE_DELETED)
			model.testCaseStepsByTarget[target] = [StepType.ACTION, StepType.ACTION, StepType.ACTION]
		when :
			model.setDeleted(target)
		
		then :
			model.getStatus(target).status == NOT_EXISTS
			model.testCaseStepsByTarget[target] == []
			
	}
	
	
	def "should tell that a step exists"(){
		
		given :
			def tc = new TestCaseTarget("/project/bob")
			def st  = new TestStepTarget(tc, 1)
			
			model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
			model.testCaseStepsByTarget[tc] = [StepType.ACTION, StepType.ACTION, StepType.ACTION]
		
			
		when :
			def res = model.stepExists(st)
		
		then :
			res == true
		
	} 
	
	def "should tell that a step do not exists (out of bound)"(){
		
		given :
			def tc = new TestCaseTarget("/project/bob")
			def st  = new TestStepTarget(tc, 10)
			
			model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
			model.testCaseStepsByTarget[tc] = [StepType.ACTION, StepType.ACTION, StepType.ACTION]
		
			
		when :
			def res = model.stepExists(st)
		
		then :
			res == false
		
	}
	
	
	def "should tell that a step do not exists (null)"(){
		
		given :
			def tc = new TestCaseTarget("/project/bob")
			def st  = new TestStepTarget(tc, null)
			
			model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
			model.testCaseStepsByTarget[tc] = [StepType.ACTION, StepType.ACTION, StepType.ACTION]
		
			
		when :
			def res = model.stepExists(st)
		
		then :
			res == false
		
	}
	
	
	def "should add a test step of a given type to the model at the specified index"(){
		given :
			def tc = new TestCaseTarget("/project/bob")
			def st  = new TestStepTarget(tc, 1)
			
			model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
			model.testCaseStepsByTarget[tc] = [StepType.ACTION, StepType.ACTION, StepType.ACTION]
		
			
		when :
			model.add(st, StepType.CALL)
		
		then :
			model.testCaseStepsByTarget[tc] == [StepType.ACTION, StepType.CALL, StepType.ACTION, StepType.ACTION]
	}
	
	
	def "should add a test step of a given type to the model at the end because of null index"(){
		
		given :
			def tc = new TestCaseTarget("/project/bob")
			def st  = new TestStepTarget(tc, null)
			
			model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
			model.testCaseStepsByTarget[tc] = [StepType.ACTION, StepType.ACTION, StepType.ACTION]
		
			
		when :
			model.add(st, StepType.CALL)
		
		then :
			model.testCaseStepsByTarget[tc] == [StepType.ACTION, StepType.ACTION, StepType.ACTION, StepType.CALL]
	}
	
	
	def "should add a test step of a given type to the model at the end because of out of upper bound index"(){
		
		given :
			def tc = new TestCaseTarget("/project/bob")
			def st  = new TestStepTarget(tc, 18)
			
			model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
			model.testCaseStepsByTarget[tc] = [StepType.ACTION, StepType.ACTION, StepType.ACTION]
		
			
		when :
			model.add(st, StepType.CALL)
		
		then :
			model.testCaseStepsByTarget[tc] == [StepType.ACTION, StepType.ACTION, StepType.ACTION, StepType.CALL]
	}
	
	
	def "should remove a step if exists"(){
		
		given :
			def tc = new TestCaseTarget("/project/bob")
			def st  = new TestStepTarget(tc, 1)
			
			model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
			model.testCaseStepsByTarget[tc] = [StepType.ACTION, StepType.CALL, StepType.ACTION]
		
		when :
			model.remove(st)
		
		then :
			model.testCaseStepsByTarget[tc] == [StepType.ACTION, StepType.ACTION]
		
	}
	
	def "should fail to remove a step if not exists"(){
		
		given :
			def tc = new TestCaseTarget("/project/bob")
			def st  = new TestStepTarget(tc, 17)
			
			model.testCaseStatusByTarget[tc] = new TargetStatus(EXISTS, 10l)
			model.testCaseStepsByTarget[tc] = [StepType.ACTION, StepType.CALL, StepType.ACTION]
		
		when :
			model.remove(st)
		
		then :
			thrown IllegalArgumentException
		
	}
	
	
	// **************************** utils ******************************
	
	def mockProject(name, id){
		Project proj = Mock(Project)
		proj.getName() >> name
		proj.getId() >> id
		return proj
	}
	
	def mockSessionQuery(queryResult){
		Query q = Mock(Query)
		Session s = Mock(Session)
		
		q.list() >> queryResult
		s.getNamedQuery(_) >> q
		factory.getCurrentSession() >> s
	}
	
	def createTargets(String... paths){
		return paths.collect{ new TestCaseTarget(it) }
	}
}
