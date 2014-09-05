/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.core.api.repository;

import javax.inject.Inject;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.service.internal.repository.hibernate.DbunitDaoSpecification;
import org.squashtest.tm.api.repository.SqlQueryRunner;
import org.unitils.dbunit.annotation.DataSet;

import spock.lang.Specification;
import spock.unitils.UnitilsSupport;

/**
 * @author Gregory Fouquet
 *
 */
@UnitilsSupport
@ContextConfiguration(["classpath:repository/dependencies-scan-context.xml", "classpath:unitils-datasource-context.xml", "classpath*:META-INF/**/repository-context.xml"])
@TransactionConfiguration(transactionManager = "squashtest.tm.hibernate.TransactionManager", defaultRollback = true)
@Transactional(isolation=Isolation.READ_UNCOMMITTED)
class SqlQueryRunnerIT extends Specification {
	@Inject SqlQueryRunner runner

	@DataSet("SqlQueryRunnerIT.should select all active core users.xml")
	def "should select all active core user logins"() {
		when:
		def res = runner.executeSelect("select LOGIN from CORE_USER where ACTIVE <> 0")

		then:
		res == [
			"daniel.bryan",
			"chris.jericho"
		]
	}

	@DataSet("SqlQueryRunnerIT.should select all active core users.xml")
	def "should select all active core user logins and names"() {
		when:
		def res = runner.executeSelect("select LOGIN, LAST_NAME from CORE_USER where ACTIVE <> 0")

		then:
		res == [
			["daniel.bryan", "bryan"],
			["chris.jericho", "jericho"]
		]
	}

	@DataSet("SqlQueryRunnerIT.should select all active core users.xml")
	def "should select all active core user aliased logins and names"() {
		when:
		def res = runner.executeSelect('select LOGIN "logname", LAST_NAME "name" from CORE_USER where ACTIVE <> 0')

		then:
		res == [
			["daniel.bryan", "bryan"],
			["chris.jericho", "jericho"]
		]
	}

	@DataSet("SqlQueryRunnerIT.should select all active core users.xml")
	def "should select single inactive core user"() {
		when:
		def res = runner.executeUniqueSelect("select LOGIN from CORE_USER where ACTIVE = 0")

		then:
		res == "shawn.michaels"
	}

	@DataSet("SqlQueryRunnerIT.should select all active core users.xml")
	def "should select core user by last_name named parameter"() {
		when:
		def res = runner.executeSelect("select LOGIN from CORE_USER where LAST_NAME = :name", [name: "bryan"])

		then:
		res == ["daniel.bryan"]
	}

	@DataSet("SqlQueryRunnerIT.should select all active core users.xml")
	def "should select core user by last_name named list parameter"() {
		when:
		def res = runner.executeSelect("select LOGIN from CORE_USER where LAST_NAME in ( :names )", [names: ["bryan", "jericho"]])

		then:
		res == ["daniel.bryan", "chris.jericho"]
	}

	@DataSet("SqlQueryRunnerIT.should select all active core users.xml")
	def "should select unique core user by last_name named parameter"() {
		when:
		def res = runner.executeUniqueSelect("select LOGIN from CORE_USER where LAST_NAME = :name", [name: "bryan"])

		then:
		res == "daniel.bryan"
	}
}
