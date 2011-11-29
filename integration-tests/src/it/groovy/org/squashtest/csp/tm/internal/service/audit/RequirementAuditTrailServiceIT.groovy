
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

package org.squashtest.csp.tm.internal.service.audit;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.infrastructure.collection.Paging;
import org.squashtest.csp.tm.domain.event.RequirementAuditEvent;
import org.squashtest.csp.tm.internal.service.DbunitServiceSpecification;
import org.squashtest.csp.tm.service.audit.RequirementAuditTrailService;
import org.unitils.dbunit.annotation.DataSet;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;

import spock.unitils.UnitilsSupport;

/**
 * @author Gregory Fouquet
 *
 */
@NotThreadSafe
@UnitilsSupport
@Transactional
class RequirementAuditTrailServiceIT extends DbunitServiceSpecification {
	@Inject RequirementAuditTrailService service
	
	@DataSet("RequirementAuditTrailServiceIT.should fetch lists of events.xml")
	def "should fetch list of event for a requirement sorted by date"(){
		given :
		def requirementId=1L
			
		and:
		Paging paging = Mock()
		paging.getFirstItemIndex() >> 1
		paging.getPageSize() >> 2
		
		when :
		PagedCollectionHolder paged = service.findAllByRequirementIdOrderedByDate(requirementId, paging);
		
		then :
		paged.items.collect { it.id } == [14L, 12L]
		paged.firstItemIndex == 1
		paged.totalNumberOfItems == 4
	}

}
