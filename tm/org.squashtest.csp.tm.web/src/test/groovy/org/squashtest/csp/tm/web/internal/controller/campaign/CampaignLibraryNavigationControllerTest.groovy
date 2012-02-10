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
package org.squashtest.csp.tm.web.internal.controller.campaign;

import static org.junit.Assert.*

import javax.inject.Provider

import org.apache.poi.hssf.record.formula.functions.T
import org.squashtest.csp.core.service.security.PermissionEvaluationService
import org.squashtest.csp.tm.domain.campaign.Iteration
import org.squashtest.csp.tm.service.CampaignLibraryNavigationService
import org.squashtest.csp.tm.web.internal.model.builder.CampaignLibraryTreeNodeBuilder
import org.squashtest.csp.tm.web.internal.model.builder.DriveNodeBuilder
import org.squashtest.csp.tm.web.internal.model.builder.IterationNodeBuilder
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode

class CampaignLibraryNavigationControllerTest extends spock.lang.Specification {
	CampaignLibraryNavigationController controller = new CampaignLibraryNavigationController()
	CampaignLibraryNavigationService service = Mock()
	Provider driveNodeBuilder = Mock()
	Provider iterationNodeBuilder = Mock()
	Provider campaignLibraryTreeNodeBuilder = Mock()

	def setup() {
		controller.setCampaignLibraryNavigationService(service);
		controller.driveNodeBuilder = driveNodeBuilder
		controller.iterationNodeBuilder = iterationNodeBuilder
		controller.campaignLibraryTreeNodeBuilder = campaignLibraryTreeNodeBuilder

		driveNodeBuilder.get() >> new DriveNodeBuilder(Mock(PermissionEvaluationService))
		iterationNodeBuilder.get() >> new IterationNodeBuilder(Mock(PermissionEvaluationService))
		campaignLibraryTreeNodeBuilder.get() >> new CampaignLibraryTreeNodeBuilder(Mock(PermissionEvaluationService))
	}

	def "should add iteration to campaign content and return iteration node model"() {
		given:
		Iteration iter = new Iteration(id:1l)
		service.findIteration(_) >> iter;

		when:
		JsTreeNode res = controller.addNewIterationToCampaign(iter, 10)

		then:
		1 * service.addIterationToCampaign(iter, 10)
		res != null
	}

	def "should return iteration nodes of campaign"() {
		given:
		Iteration iter = Mock()
		service.findIterationsByCampaignId(10) >> [iter]

		when:
		def res = controller.getCampaignIterationsTreeModel(10)

		then:
		res.size() == 1
	}
}
