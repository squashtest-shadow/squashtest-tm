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
package org.squashtest.tm.service.requirement

import com.google.common.base.Optional
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.domain.requirement.*
import org.squashtest.tm.exception.requirement.link.AlreadyLinkedRequirementVersionException
import org.squashtest.tm.exception.requirement.link.LinkedRequirementVersionException
import org.squashtest.tm.exception.requirement.link.SameRequirementLinkedRequirementVersionException
import org.squashtest.tm.exception.requirement.link.UnlinkableLinkedRequirementVersionException
import org.squashtest.tm.service.internal.milestone.ActiveMilestoneHolderImpl
import org.squashtest.tm.service.internal.repository.LibraryNodeDao
import org.squashtest.tm.service.internal.repository.RequirementVersionDao
import org.squashtest.tm.service.internal.repository.RequirementVersionLinkDao
import org.squashtest.tm.service.internal.repository.RequirementVersionLinkTypeDao
import org.squashtest.tm.service.internal.requirement.LinkedRequirementVersionManagerServiceImpl
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder
import spock.lang.Specification

/**
 * Created by jlor on 07/11/2017.
 */
class LinkedRequirementVersionManagerServiceImplTest extends Specification {

	LinkedRequirementVersionManagerService service =
		new LinkedRequirementVersionManagerServiceImpl()

	RequirementVersionLinkDao reqVersionLinkDao = Mock()

	LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao = Mock()

	ActiveMilestoneHolder activeMilestoneHolder = Mock()

	RequirementVersionDao reqVersionDao = Mock()

	RequirementVersionLinkTypeDao reqVersionLinkTypeDao = Mock()

	def setup() {
		service.reqVersionLinkDao = reqVersionLinkDao
		service.requirementLibraryNodeDao = requirementLibraryNodeDao
		service.activeMilestoneHolder = activeMilestoneHolder
		service.reqVersionDao = reqVersionDao
		service.reqVersionLinkTypeDao = reqVersionLinkTypeDao
	}

	def "#findAllByRequirementVersion"() {

		given: "Input parameters"
			long requirementId = 432L
			PagingAndSorting pas = Mock()

		and: "Mock dao data"
			RequirementVersionLink link1 = Mock()
			LinkedRequirementVersion lrv1 = Mock()
			link1.getRelatedLinkedRequirementVersion() >> lrv1

			RequirementVersionLink link2 = Mock()
			LinkedRequirementVersion lrv2 = Mock()
			link2.getRelatedLinkedRequirementVersion() >> lrv2

			RequirementVersionLink link3 = Mock()
			LinkedRequirementVersion lrv3 = Mock()
			link3.getRelatedLinkedRequirementVersion() >> lrv3

			List<RequirementVersionLink> linksList = [link1, link2, link3] as List

		and: "Mock dao method"
			reqVersionLinkDao.findAllByReqVersionId(requirementId, pas) >> linksList

		and: "Expected result data"
			List<LinkedRequirementVersion> expectedList = [lrv1, lrv2, lrv3] as List

		when:
			PagedCollectionHolder result = service.findAllByRequirementVersion(432, pas)

		then:
			result.getTotalNumberOfItems() == expectedList.size()
			result.getPagedItems().containsAll(expectedList)
	}

	def "#removeLinkedRequirementVersionsFromRequirementVersion"() {

		given: "Input data"
			long reqVerId = 432L
			List<Long> reqVerIdsToUnlink = [987L, 654L, 321] as List

		when:
			service.removeLinkedRequirementVersionsFromRequirementVersion(reqVerId, reqVerIdsToUnlink)

		then:
			1*reqVersionLinkDao.deleteAllLinks(reqVerId, reqVerIdsToUnlink)

	}

	/*def "#addLinkedReqVersionsToReqVersion"() {

		given: "Input data"
			long reqVerId = 321L
			List<Long> reqVerIdsToLink = [123L, 456L, 789L, 951L]

		and: "Mock dao data"

			RequirementVersion mainRv = Mock()

			RequirementVersion rv1 = Mock()
			Requirement req1 = new Requirement(rv1)

			RequirementVersion rv2 = Mock()
			Requirement req2 = new Requirement(rv2)

			RequirementVersion rv3 = Mock()
			Requirement req3 = new Requirement(rv3)

			RequirementVersion rv4 = Mock()
			Requirement req4 = new Requirement(rv4)

			List<LibraryNodeDao> reqList = [req1, req2, req3, req4] as List

			RequirementVersionLinkType defaultType = Mock()

		and: "Mock exceptions data"
			AlreadyLinkedRequirementVersionException ex1 = Mock()
			SameRequirementLinkedRequirementVersionException ex2 = Mock()
			UnlinkableLinkedRequirementVersionException ex3 = Mock()

		and: "Mock dao methods"
			requirementLibraryNodeDao.findAllByIds(reqVerIdsToLink) >> reqList
			reqVersionDao.findOne(reqVerId) >> mainRv
			reqVersionLinkTypeDao.getDefaultRequirementVersionLinkType() >> defaultType

		and: "Mock internal service methods"
			service.checkIfLinkAlreadyExists(mainRv, rv2) >> { throw ex1 }
			service.checkIfSameRequirement(mainRv, rv3) >> { throw ex2 }
			service.checkIfVersionsAreLinkable(mainRv, rv4) >> { throw ex3 }

		and: "Mock service method"
			activeMilestoneHolder.getActiveMilestone() >> Optional.fromNullable(null)

		and: "Expected result data"
			List<LinkedRequirementVersionException> expectedList = [ex1, ex2, ex3] as List

		when:
			result = service.addLinkedReqVersionsToReqVersion(reqVerId, reqVerIdsToLink)

		then:
			1*reqVersionLinkDao.addLink()
			result.size() == expectedList.size()
			result.containsAll(expectedList)

	}*/
}
