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
package org.squashtest.tm.domain.library;

import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;

/**
 * Look, enough of that visitor logic b*******
 * 
 * @author bsiri
 */
public class WhichNodeVisitor implements NodeContainerVisitor, NodeVisitor {

	
	public static enum NodeType{
		CAMPAIGN_LIBRARY,
		REQUIREMENT_LIBRARY,
		TEST_CASE_LIBRARY,
		CAMPAIGN_FOLDER,
		REQUIREMENT_FOLDER,
		TEST_CASE_FOLDER,
		TEST_CASE,
		REQUIREMENT,
		CAMPAIGN,
		ITERATION,
		TEST_SUITE,
		UNDEFINED;
	}
	
		
	private NodeType nodeType = NodeType.UNDEFINED; 

	public <X extends NodeContainer<?>> NodeType getTypeOf(X container ){
		nodeType = NodeType.UNDEFINED;
		container.accept(this);
		return nodeType;
	}
	
	public <X extends TreeNode> NodeType getTypeOf(X node){
		nodeType = NodeType.UNDEFINED;
		node.accept(this);
		return nodeType;
	}
	
	@Override
	public void visit(CampaignLibrary campaignLibrary) {
		nodeType = NodeType.CAMPAIGN_LIBRARY;
	}

	@Override
	public void visit(RequirementLibrary requirementLibrary) {
		nodeType = NodeType.REQUIREMENT_LIBRARY;
	}

	@Override
	public void visit(TestCaseLibrary testCaseLibrary) {
		nodeType = NodeType.TEST_CASE_LIBRARY;
	}

	@Override
	public void visit(CampaignFolder campaignFolder) {
		nodeType = NodeType.CAMPAIGN_FOLDER;
	}

	@Override
	public void visit(RequirementFolder requirementFolder) {
		nodeType = NodeType.REQUIREMENT_FOLDER;
	}

	@Override
	public void visit(TestCaseFolder testCaseFolder) {
		nodeType = NodeType.TEST_CASE_FOLDER;
	}

	@Override
	public void visit(Campaign campaign) {
		nodeType = NodeType.CAMPAIGN;
	}

	@Override
	public void visit(Iteration iteration) {
		nodeType = NodeType.ITERATION;
	}

	@Override
	public void visit(Requirement requirement) {
		nodeType = NodeType.REQUIREMENT;
	}

	@Override
	public void visit(TestSuite testSuite) {
		nodeType = NodeType.TEST_SUITE;
	}

	@Override
	public void visit(TestCase testCase) {
		nodeType = NodeType.TEST_CASE;
	}

	
	
}
