/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.spring

import org.jooq.DSLContext
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

import static org.squashtest.tm.jooq.domain.tables.Project.PROJECT

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class JooqConfigurationIT extends DbunitServiceSpecification {

	@Inject
	DSLContext dslContext

	@DataSet("JooqConfigurationIT.xml")
	def "should execute jooq query"() {
		when:
		def records = dslContext.select(PROJECT.PROJECT_ID).from(PROJECT).where(PROJECT.PROJECT_ID.eq(-1L)).fetch()

		then:
		records.size().equals(1)
	}

	@DataSet("JooqConfigurationIT.xml")
	def "should generate request with upper case on table names"() {
		when:
		def request = dslContext.select(PROJECT.PROJECT_ID).from(PROJECT).where(PROJECT.PROJECT_ID.eq(-1L)).getSQL();

		then:
		request.contains("PROJECT")
		!request.contains("project")
		!request.contains("Project")
	}


}
