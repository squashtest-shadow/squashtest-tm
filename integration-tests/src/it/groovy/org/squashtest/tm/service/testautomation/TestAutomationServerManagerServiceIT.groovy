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
package org.squashtest.tm.service.testautomation

import javax.inject.Inject

import org.hibernate.exception.ConstraintViolationException
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.service.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet
import org.squashtest.tm.service.internal.customfield.DefaultEditionStatusStrategy
import org.squashtest.tm.service.testautomation.TestAutomationProjectManagerService

import spock.lang.Specification
import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
public class TestAutomationServerManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	private TestAutomationServerManagerService service

	@DataSet("TestAutomationServerManagerServiceIT.not bound.xml")
	def "should delete a test automation server" (){
		given :
		def serverId = 1L
		when :
		service.deleteServer(serverId)
		then:
		!found(TestAutomationServer.class, serverId)
	}

}
