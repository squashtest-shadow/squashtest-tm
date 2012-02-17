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
package org.squashtest.csp.tm.internal.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.domain.project.ProjectResource;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestCaseSearchCriteria;
import org.squashtest.csp.tm.internal.repository.CampaignDao;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.service.ProjectFilterModificationService;
import org.squashtest.csp.tm.service.SearchService;

@Service("squashtest.tm.service.SearchService")
@Transactional
@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
public class SearchServiceImpl implements SearchService {

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private RequirementDao requirementDao;

	@Inject
	private ProjectFilterModificationService projectFilterModificationService;


	/*
	 * TODO : the user project filter is applied in a straight forward manner in the following methods. The relevant code should be moved
	 * in an aspect, because the need for filtering might appear in other parts of the app in the future.
	 *
	 * See task (TODO : write the task)
	 *
	 * (non-Javadoc)
	 * @see org.squashtest.csp.tm.service.SearchService#findAllBySearchCriteria(org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria)
	 */


	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<CampaignLibraryNode> findCampaignByName(String aName, boolean groupByProject) {
		List<CampaignLibraryNode> list = campaignDao.findAllByNameContaining(aName, groupByProject);
		return applyProjectFilter(list);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestCaseLibraryNode> findTestCaseByName(String aName, boolean groupByProject) {
		List<TestCaseLibraryNode> list = testCaseDao.findAllByNameContaining(aName, groupByProject);
		return applyProjectFilter(list);
	}
	
	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestCaseLibraryNode> findTestCase(TestCaseSearchCriteria criteria){
		List<TestCaseLibraryNode> list = testCaseDao.findBySearchCriteria(criteria);
		return applyProjectFilter(list);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<RequirementLibraryNode> findAllBySearchCriteria(RequirementSearchCriteria criteria) {
		List<RequirementLibraryNode> list = requirementDao.findAllBySearchCriteria(criteria);
		return applyProjectFilter(list);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<RequirementLibraryNode> findAllBySearchCriteriaOrderByProject(RequirementSearchCriteria criteria) {
		List<RequirementLibraryNode>  list =requirementDao.findAllBySearchCriteriaOrderByProject(criteria);
		return applyProjectFilter(list);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestCase> findTestCaseByRequirement(RequirementSearchCriteria criteria,
			boolean isProjectOrdered) {
		List<TestCase> list = testCaseDao.findAllByRequirement(criteria, isProjectOrdered);
		// get calling test cases
		findCallingTestCase(list);
		return applyProjectFilter(list);
	}

	/***
	 * This method complete a given test case list with all calling test cases.
	 *
	 * @param originalList
	 *            the original test case list you want to complete
	 * @return the completed test case list
	 */
	private List<TestCase> findCallingTestCase(List<TestCase> originalList) {
		// Initiate the filtered list with tc to add
		List<TestCase> testCaseToAddList = new ArrayList<TestCase>();
		// browse the original tc list
		for (TestCase currentTestCase : originalList) {
			// find calling test cases
			List<TestCase>  callingTestCasesList = testCaseDao.findAllCallingTestCases(currentTestCase.getId(), null);
			for (TestCase callingTestcase : callingTestCasesList) {
				// add the new tc, avoid duplicates
				if (!originalList.contains(callingTestcase)) {
					testCaseToAddList.add(callingTestcase);
				}
			}
		}
		// Merge the lists
		originalList.addAll(testCaseToAddList);
		return originalList;
	}

	protected  <X extends ProjectResource> List<X> applyProjectFilter(List<X> initialList){
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		if (! pf.getActivated()){
			return initialList;
		}else{
			List<X> filtered = new LinkedList<X>();
			for (X object : initialList){
				if (pf.isProjectSelected(object.getProject())){
					filtered.add(object);
				}
			}
			return filtered;
		}

	}

}
