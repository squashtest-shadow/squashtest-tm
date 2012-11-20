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

package org.squashtest.csp.tm.internal.repository.hibernate

import javax.inject.Inject;

import org.squashtest.csp.tm.domain.project.GenericProject;
import org.squashtest.csp.tm.internal.repository.GenericProjectDao;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.unitils.dbunit.annotation.DataSet;
import static org.squashtest.tm.core.foundation.collection.SortOrder.*;

import spock.lang.Unroll;
import spock.unitils.UnitilsSupport;

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
		start | pageSize | sortAttr | sortOrder  | expected 
		0     | 4        | "id"     | ASCENDING  | ["ONE", "TWO", "THREE", "FOUR"]
		0     | 4        | "name"   | DESCENDING | ["TWO", "THREE", "ONE", "FOUR"]
		0     | 2        | "id"     | ASCENDING  | ["ONE", "TWO"]
		2     | 4        | "id"     | ASCENDING  | ["THREE", "FOUR"]
		
	}

	@DataSet("GenericProjectDaoIT.xml")
	def "should count existing projects" () {
		expect:
		dao.countGenericProjects() == 4L
	}
}
