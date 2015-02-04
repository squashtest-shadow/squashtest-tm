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
package org.squashtest.tm.service.internal.importer

import static org.squashtest.tm.domain.requirement.RequirementStatus.*

import org.squashtest.tm.domain.requirement.RequirementFolder
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.service.internal.importer.PseudoRequirement
import org.squashtest.tm.service.internal.importer.RequirementLibraryMerger
import org.squashtest.tm.service.internal.importer.RequirementMerger
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;

import spock.lang.Specification

/**
 * @author Gregory
 *
 */
class RequirementMergerTest extends Specification {
	RequirementMerger merger = new RequirementMerger()
	RequirementLibraryNavigationService service = Mock()
	RequirementLibraryMerger context = new RequirementLibraryMerger(service)
		
	def setup() {
		merger.context = context
		merger.destFolder = Mock(RequirementFolder)
		merger.destFolder.content >> []
		merger.destFolder.id >> 0
		merger.destFolder.isContentNameAvailable(_) >> true
	}
	
	def "should merge status of requirement"() {
		given:
		PseudoRequirement pseudo = new PseudoRequirement("foo", 10)
		pseudo.pseudoRequirementVersions[0].status = RequirementStatus.APPROVED

		def check = {
			println it;
			it.status == RequirementStatus.APPROVED
		}

		when:
		merger.merge([pseudo])
		
		then:
		1 * context.service.addRequirementToRequirementFolder(0, { it.status == RequirementStatus.APPROVED })
	}
}
