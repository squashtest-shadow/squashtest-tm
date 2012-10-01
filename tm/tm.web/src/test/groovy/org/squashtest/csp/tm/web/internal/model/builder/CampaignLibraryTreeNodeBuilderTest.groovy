/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.web.internal.model.builder


import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignFolder;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode.State;

import spock.lang.Specification;

class CampaignLibraryTreeNodeBuilderTest extends Specification {
	CampaignLibraryTreeNodeBuilder builder = new CampaignLibraryTreeNodeBuilder(Mock(PermissionEvaluationService))
	def "should build a tree node for a campaign folder"() {
		given:
		CampaignFolder node  = new CampaignFolder(name: "f")

		use (ReflectionCategory) {
			CampaignLibraryNode.set field: "id", of: node, to: 10L
		}

		when:
		def res = builder.setNode(node).build()

		then:
		res.title == node.name
		res.attr['resId'] == "${node.id}"
		res.attr['rel'] == "folder"
		res.attr['resType'] == "campaign-folders"
		res.state == State.leaf
	}
	def "should build a Campaign node"() {
		given:
		Campaign node  = new Campaign(name: "r")

		use (ReflectionCategory) {
			CampaignLibraryNode.set field: "id", of: node, to: 10L
		}

		when:
		def res = builder.setNode(node).build()

		then:
		res.title == node.name
		res.attr['resId'] == "${node.id}"
		res.attr['resType'] == "campaigns"
		res.attr['rel'] == "file"
		res.state == State.leaf
	}
	
	def "should build a folder with leaf state"(){
		given :
			CampaignFolder node = new CampaignFolder(name:"folder") 
			
		when :
			def res = builder.setNode(node).build()
		
		then :
			res.state == State.leaf
		
	}
	
	def "should build a folder with closed state"(){
		given :
			CampaignFolder node = new CampaignFolder(name:"folder") 
			node.addContent(new CampaignFolder());
		
		when :
			def res = builder.setNode(node).build()
		
		then :
			res.state == State.closed
		
	}
	
}
