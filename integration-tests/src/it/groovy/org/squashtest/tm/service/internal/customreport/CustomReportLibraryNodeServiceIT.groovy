/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.customreport

import javax.inject.Inject

import org.hibernate.SessionFactory
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryDao;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao;
import org.squashtest.tm.service.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@DataSet("CustomReportLibraryNodeServiceIT.sandbox.xml")
class CustomReportLibraryNodeServiceIT extends DbunitServiceSpecification {

	@Inject
	CustomReportLibraryNodeService service;
	
	@Inject
	CustomReportLibraryNodeDao crlnDao;
	
	@Inject
	CustomReportLibraryDao crlDao;
	
	def "should add new folder to library"() {
		given :
		def parent = crlnDao.findById(-1L);
		def library = crlDao.findById(-1L);
		
		CustomReportFolder folder = new CustomReportFolder();
		folder.setName("newFolder");
		
		when:
		def res = service.createNewCustomReportLibraryNode(-1L,folder);
		def resId = res.getId();
		getSession().flush();
		getSession().clear();
		def newChildAfterPersist = crlnDao.findById(resId);
		def parentNode = newChildAfterPersist.getParent();

		then:
		res.id != null;
		library != null;
		parentNode.id == parent.id;
	}
	
	def "should find descendants for nodes in one library"() {
		given :
		
		when:
		def res = service.createNewCustomReportLibraryNode(-1L,folder);
		def resId = res.getId();
		getSession().flush();
		getSession().clear();
		def newChildAfterPersist = crlnDao.findById(resId);
		def parentNode = newChildAfterPersist.getParent();

		then:
		res.id != null;
		library != null;
		parentNode.id == parent.id;
		
		where:
		parentIds 	|| 	childrenIds
//		-1L			||	[-2L,-3L,-3L,-4L,-5L,-7L,-10L,-20L,-30L]
		[-20L]		||	[-40L]
	}
	
}
