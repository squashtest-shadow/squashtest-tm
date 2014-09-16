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
package org.squashtest.tm.service.audit

import org.squashtest.tm.core.foundation.collection.Paging
import org.squashtest.tm.domain.event.RequirementCreation
import org.squashtest.tm.service.internal.audit.RequirementAuditTrailServiceImpl
import org.squashtest.tm.service.internal.repository.RequirementAuditEventDao

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class RequirementAuditTrailServiceImplTest extends Specification {
	RequirementAuditTrailServiceImpl service = new RequirementAuditTrailServiceImpl()
	RequirementAuditEventDao dao = Mock()
	
	def setup() {
		service.auditEventDao = dao
	}
	
	def "should return an correctly paged resultset"() {
		given:
		Paging paging = Mock() 
		paging.firstItemIndex >> 1
		paging.pageSize >> 2
		
		and:
		def events = [new RequirementCreation(), new RequirementCreation()] 
		dao.findAllByRequirementVersionIdOrderedByDate(10L, paging) >> events
		
		and:
		dao.countByRequirementVersionId(10L) >> 20L
		
		when:
		def res = service.findAllByRequirementVersionIdOrderedByDate(10L, paging)
		
		then:
		res.items == events
		res.firstItemIndex == 1
		res.totalNumberOfItems == 20L
		
	}
	
}
