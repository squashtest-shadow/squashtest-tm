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
package org.squashtest.csp.tm.service;

import java.util.List;

import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestCaseSearchCriteria;

public interface SearchService {

	List<CampaignLibraryNode> findCampaignByName(String aName, boolean groupByProject);

	@Deprecated //since 12/02/17, remove it if you see this comment and if task 384-04 is done.
	List<TestCaseLibraryNode> findTestCaseByName(String aName, boolean groupByProject);
	
	List<TestCaseLibraryNode> findTestCase(TestCaseSearchCriteria criteria);

	List<RequirementLibraryNode> findAllBySearchCriteria(RequirementSearchCriteria criteria);

	List<RequirementLibraryNode> findAllBySearchCriteriaOrderByProject(RequirementSearchCriteria criteria);

	List<TestCase> findTestCaseByRequirement(RequirementSearchCriteria criteria, boolean isProjectOrdered);
}
