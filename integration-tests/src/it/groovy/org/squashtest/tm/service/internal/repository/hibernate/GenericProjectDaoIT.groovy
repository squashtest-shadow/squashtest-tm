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
package org.squashtest.tm.service.internal.repository.hibernate

import static org.squashtest.tm.core.foundation.collection.SortOrder.*

import javax.inject.Inject

import org.squashtest.tm.core.foundation.collection.DefaultSorting
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.service.internal.repository.GenericProjectDao
import org.unitils.dbunit.annotation.DataSet

import spock.lang.Unroll
import spock.unitils.UnitilsSupport

/**
 * @author Gregory Fouquet
 *
 */
@UnitilsSupport
class GenericProjectDaoIT extends DbunitDaoSpecification {
	@Inject GenericProjectDao dao

	@Unroll
	@DataSet("GenericProjectDaoIT.xml")
	def "should return a list of existing project" () {
		given:
		PagingAndSorting paging = Mock()
		paging.firstItemIndex >> start
		paging.pageSize >> pageSize
		paging.sortedAttribute >> sortAttr
		paging.sortOrder >> sortOrder

		when:
		List<GenericProject> list = dao.findAll(paging)

		then:
		list*.name == expected

		where:
		start | pageSize | sortAttr | sortOrder		| expected
		0     | 4        | "id"     | ASCENDING		| ["ONE", "TWO", "THREE", "FOUR"]
		0     | 4        | "name"   | DESCENDING	| ["twobis", "TWO", "THREE", "ONE"]
		0     | 2        | "id"     | ASCENDING		| ["ONE", "TWO"]
		2     | 4        | "id"     | ASCENDING		| ["THREE", "FOUR", "twobis"]

	}

	@DataSet("GenericProjectDaoIT.xml")
	def "should find project by id ordered by name"(){
		given :
		def ids = [100001L, 100002L, 100003L, 100004L, 100005L]
		when :
		List<GenericProject> result = dao.findAllByIds(ids, new DefaultSorting("name"))
		then:
		result*.name == ["FOUR", "ONE", "THREE", "TWO", "twobis"]
	}

	@DataSet("GenericProjectDaoIT.xml")
	def "should count existing projects" () {
		expect:
		dao.countGenericProjects() == 5
	}

	@Unroll
	@DataSet("GenericProjectDaoIT.xml")
	def "should count #count projects for name #name" () {
		expect:
		dao.countByName(name) == count

		where:
		name       | count
		"whatever" | 0
		"ONE"      | 1
	}

	@DataSet("GenericProjectDaoIT.xml")
	def "should coerce template into a project" () {
		when:
		GenericProject res = dao.coerceTemplateIntoProject(100004)

		then:
		res instanceof Project
	}


	@DataSet("GenericProjectDaoIT.xml")
	@Unroll("asserting that project of id #pId is a template is #res")
	def "should tells that the given project is a project template"(){
		expect :
		res == dao.isProjectTemplate(pId)
		where :
		pId	| res
		100001L	| false
		100004L	| true
	}

	@DataSet("GenericProjectDaoIT.server.xml")
	def "should find a project's server"(){
		given: def pId = 100001L
		when :
		def res = dao.findTestAutomationServer(pId)
		then :
		res != null
		res.id == 1000011L
	}

	@DataSet("GenericProjectDaoIT.taprojects.xml")
	def "should find a project's taprojects jobNames"(){
		given: def pId = 100001L
		when :
		Collection<String> res = dao.findBoundTestAutomationProjectJobNames(pId)
		then :
		res.containsAll(["job-1", "job-2"])
	}



}
