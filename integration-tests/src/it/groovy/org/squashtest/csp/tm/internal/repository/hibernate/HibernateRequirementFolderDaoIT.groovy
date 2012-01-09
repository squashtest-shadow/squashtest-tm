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

package org.squashtest.csp.tm.internal.repository.hibernate

import java.util.List;
import java.util.ResourceBundle.SingleFormatControl;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.internal.repository.RequirementFolderDao;
import org.squashtest.csp.tools.unittest.assertions.CollectionAssertions;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;

import javax.inject.Inject;

@UnitilsSupport
class HibernateRequirementFolderDaoIT extends DbunitDaoSpecification {
	@Inject
	RequirementFolderDao folderDao

	@Inject RequirementDao requirementDao
	
	@DataSet("HibernateRequirementFolderDaoIT.should not retrieve deleted content.xml")
	def "should not retrieve deleted content"(){
		when :
		List content = folderDao.findAllContentById(1);

		then :
		(content.collect { it.name }).containsAll(["req 1"])
		content.size() == 1
	}

	@DataSet("HibernateRequirementFolderDaoIT.should not retrieve deleted folder.xml")
	def "should not retrieve deleted folder"(){
		when :
		def res = folderDao.findById(1)

		then :
		res == null
	}
}
