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
package org.squashtest.csp.core.security.acls.jdbc

import javax.inject.Inject;

import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.squashtest.test.unitils.dbunit.datasetloadstrategy.DeleteInsertLoadStrategy;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.datasetloadstrategy.impl.CleanInsertLoadStrategy;
import org.unitils.dbunit.datasetloadstrategy.impl.InsertLoadStrategy;

import spock.lang.Specification;
import spock.unitils.UnitilsSupport;

@ContextConfiguration(["classpath:service/dependencies-scan-context.xml", "classpath:unitils-datasource-context.xml", "classpath*:META-INF/**/bundle-context.xml", "classpath*:META-INF/**/repository-context.xml", "classpath*:META-INF/**/dynamicdao-context.xml", "classpath*:META-INF/**/dynamicmanager-context.xml"])
@TransactionConfiguration(transactionManager = "squashtest.tm.hibernate.TransactionManager")
@UnitilsSupport
class LookupStrategyConfigIT extends Specification {
	@Inject BasicLookupStrategy lookupStrategy

	@DataSet(value="LookupStrategyConfigIT.should read acl on an object identity for a user.xml", loadStrategy=DeleteInsertLoadStrategy)
	def "should read acl on an object identity for a user"() {
		given:
		ObjectIdentity oid = Mock()
		oid.identifier >> -1000
		oid.type >> "newspaper.content.Photo"

		and:
		PrincipalSid sid = Mock()
		sid.principal >> "jimmy.olsen"

		when:
		def acls = lookupStrategy.readAclsById([oid], [sid])
		println acls

		then:
		acls.size() == 1
	}
	@DataSet(value="LookupStrategyConfigIT.should read no acl on an object identity for a user.xml", loadStrategy=DeleteInsertLoadStrategy)
	def "should read no acl on an object identity for a user with no permissions"() {
		given:
		ObjectIdentity oid = Mock()
		oid.identifier >> -1000
		oid.type >> "newspaper.content.Article"

		and:
		PrincipalSid sid = Mock()
		sid.principal >> "jonah.jameson"

		when:
		def acls = lookupStrategy.readAclsById([oid], [sid])
		println acls

		then:
		acls.size() == 0
	}

	@DataSet(value="LookupStrategyConfigIT.should read no acl for deactivated user.xml", loadStrategy=DeleteInsertLoadStrategy)
	def "should read no acl on an object identity for a deactivated user"() {
		given:
		ObjectIdentity oid = Mock()
		oid.identifier >> -1000
		oid.type >> "newspaper.content.Photo"

		and:
		PrincipalSid sid = Mock()
		sid.principal >> "jimmy.olsen"

		when:
		def acls = lookupStrategy.readAclsById([oid], [sid])
		println acls

		then:
		acls.size() == 0
	}
}
