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

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.runner.RunWith;
import org.spockframework.runtime.Sputnik;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.service.DbunitServiceSpecification;
import org.unitils.dbunit.annotation.DataSet;
import static org.squashtest.tm.service.internal.batchimport.Model.Existence.*

import spock.unitils.UnitilsSupport;

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class ModelIT  extends DbunitServiceSpecification {

	@Inject
	Provider<Model> modelProvider
	
	
	Model model
	
	def setup(){
		model = modelProvider.get()
	}
	
	
	@DataSet("batchimport.sandbox.xml")
	def "should init the requested target, some targets already exists and some do not"(){
		
		given : 
			def targets = createTestCaseTargets("/Test Project-1/test 3", "/autre project/folder/TEST B", "/bob/nonexistant", "/Test Project-1/dossier 2/0 test case / with slash")
			
		when :
			model.initTestCases(targets)
		
		
		then :
			model.getStatus(targets[0]).status == EXISTS
			model.getStatus(targets[1]).status == EXISTS
			model.getStatus(targets[2]).status == NOT_EXISTS
			model.getStatus(targets[3]).status == EXISTS
			
			model.getStatus(targets[0]).id == 245l
			model.getStatus(targets[1]).id == 248l
			model.getStatus(targets[2]).id == null
			model.getStatus(targets[3]).id == 244l
				
	}
	
	
	def createTestCaseTargets(String... paths){
		return paths.collect{ return new TestCaseTarget(it) }
	}
	
}
