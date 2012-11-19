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
package org.squashtest.csp.tm.internal.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.campaign.CampaignFolder;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.domain.project.ProjectResource;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestCaseSearchCriteria;
import org.squashtest.csp.tm.internal.repository.CampaignDao;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.service.CampaignLibraryNavigationService;
import org.squashtest.csp.tm.service.ProjectFilterModificationService;
import org.squashtest.csp.tm.service.RequirementLibraryNavigationService;
import org.squashtest.csp.tm.service.SearchService;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;

@Service("squashtest.tm.service.SearchService")
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

	@Inject
	private TestCaseDao testCaseDao;
	@Inject
	private TestCaseLibraryNavigationService testCaseLibraryNavigationService;
	@Inject
	private CampaignDao campaignDao;
	@Inject
	private CampaignLibraryNavigationService campaignLibraryNavigationService;
	@Inject
	private RequirementDao requirementDao;
	@Inject
	private RequirementLibraryNavigationService requirementLibraryNavigationService;
	@Inject
	private ProjectFilterModificationService projectFilterModificationService;

	/*
	 * TODO : the user project filter is applied in a straight forward manner in the following methods. The relevant
	 * code should be moved in an aspect, because the need for filtering might appear in other parts of the app in the
	 * future.
	 * 
	 * See task (TODO : write the task)
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.squashtest.csp.tm.service.SearchService#findAllBySearchCriteria(org.squashtest.csp.tm.domain.requirement.
	 * RequirementSearchCriteria)
	 */
	private static final String FILTRED_READ_OR_ADMIN = "hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')";

	@Override
	@PostFilter(FILTRED_READ_OR_ADMIN)
	public List<CampaignLibraryNode> findCampaignByName(String aName, boolean groupByProject) {
		List<CampaignLibraryNode> list = campaignDao.findAllByNameContaining(aName, groupByProject);
		return applyProjectFilter(list);
	}

	@Override
	@PostFilter(FILTRED_READ_OR_ADMIN)
	public List<TestCaseLibraryNode> findTestCaseByName(String aName, boolean groupByProject) {
		List<TestCaseLibraryNode> list = testCaseDao.findAllByNameContaining(aName, groupByProject);
		return applyProjectFilter(list);
	}

	@Override
	@PostFilter(FILTRED_READ_OR_ADMIN)
	public List<TestCaseLibraryNode> findTestCase(TestCaseSearchCriteria criteria) {
		List<TestCaseLibraryNode> list = testCaseDao.findBySearchCriteria(criteria);
		return applyProjectFilter(list);
	}

	@Override
	@PostFilter(FILTRED_READ_OR_ADMIN)
	public List<RequirementLibraryNode> findAllBySearchCriteria(RequirementSearchCriteria criteria) {
		List<RequirementLibraryNode> list = requirementDao.findAllBySearchCriteria(criteria);
		return applyProjectFilter(list);
	}

	@Override
	@PostFilter(FILTRED_READ_OR_ADMIN)
	public List<RequirementLibraryNode> findAllBySearchCriteriaOrderByProject(RequirementSearchCriteria criteria) {
		List<RequirementLibraryNode> list = requirementDao.findAllBySearchCriteriaOrderByProject(criteria);
		return applyProjectFilter(list);
	}

	@Override
	@PostFilter(FILTRED_READ_OR_ADMIN)
	public List<TestCase> findTestCaseByRequirement(RequirementSearchCriteria criteria, boolean isProjectOrdered) {
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
			List<TestCase> callingTestCasesList = testCaseDao.findAllCallingTestCases(currentTestCase.getId(), null);
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

	protected <PR extends ProjectResource<?>> List<PR> applyProjectFilter(List<PR> initialList) {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		if (!pf.getActivated()) {
			return initialList;
		} else {
			return applyFilter(initialList, pf);
		}

	}

	private <PR extends ProjectResource<?>> List<PR> applyFilter(List<PR> unfilteredResources, ProjectFilter filter) {
		List<PR> filtered = new ArrayList<PR>(unfilteredResources.size());

		for (PR resource : unfilteredResources) {
			if (filter.isProjectSelected(resource.getProject())) {
				filtered.add(resource);
			}
		}
		return filtered;
	}

	// ------------------------------------------------------------------------------------------TODO mutualize
	// duplicated code
	@Override
	public List<String> findBreadCrumbForRequirement(String className, Long nodeId, String rejex) {
		RequirementLibraryNode node = null;
		if (className.endsWith("Folder")) {
			node = requirementLibraryNavigationService.findFolder(nodeId);
		} else {
			node = requirementLibraryNavigationService.findRequirement(nodeId);
		}

		return findBreadCrumbOfRequirementNode(node, requirementLibraryNavigationService, rejex);
	}

	private List<String> findBreadCrumbOfRequirementNode(RequirementLibraryNode node,
			RequirementLibraryNavigationService libraryNavigationService, String rejex) {
		List<String> result = new ArrayList<String>();
		result.add(node.getClass().getSimpleName() + rejex + node.getId());
		RequirementFolder parent = libraryNavigationService.findParentIfExists(node);
		fillBreadCrumbListUntillLibraryForRequirement(node, libraryNavigationService, rejex, result, parent);

		return result;
	}

	private void fillBreadCrumbListUntillLibraryForRequirement(RequirementLibraryNode node,
			RequirementLibraryNavigationService libraryNavigationService, String rejex, List<String> result,
			RequirementFolder parent) {
		RequirementLibraryNode root = node;
		RequirementFolder ancestor = parent;
		while (ancestor != null) {
			result.add(ancestor.getClass().getSimpleName() + rejex + ancestor.getId());
			root = ancestor;
			ancestor = libraryNavigationService.findParentIfExists(root);
		}
		RequirementLibrary library = libraryNavigationService.findLibraryOfRootNodeIfExist(root);
		result.add(library.getClassSimpleName() + rejex + library.getId());
	}

	@Override
	public List<String> findBreadCrumbForTestCase(String className, Long nodeId, String rejex) {
		TestCaseLibraryNode node = null;
		if (className.endsWith("Folder")) {
			node = testCaseLibraryNavigationService.findFolder(nodeId);
		} else {
			node = testCaseLibraryNavigationService.findTestCase(nodeId);
		}

		return findBreadCrumbOfTestCaseNode(node, testCaseLibraryNavigationService, rejex);
	}

	private List<String> findBreadCrumbOfTestCaseNode(TestCaseLibraryNode node,
			TestCaseLibraryNavigationService libraryNavigationService, String rejex) {
		List<String> result = new ArrayList<String>();
		result.add(node.getClass().getSimpleName() + rejex + node.getId());
		TestCaseFolder parent = libraryNavigationService.findParentIfExists(node);
		fillBreadCrumbListUntillLibraryForTestCase(node, libraryNavigationService, rejex, result, parent);

		return result;
	}

	private void fillBreadCrumbListUntillLibraryForTestCase(TestCaseLibraryNode node,
			TestCaseLibraryNavigationService libraryNavigationService, String rejex, List<String> result,
			TestCaseFolder parent) {
		TestCaseLibraryNode root = node;
		TestCaseFolder ancestor = parent;

		while (ancestor != null) {
			result.add(ancestor.getClass().getSimpleName() + rejex + ancestor.getId());
			root = ancestor;
			ancestor = libraryNavigationService.findParentIfExists(root);
		}
		TestCaseLibrary library = libraryNavigationService.findLibraryOfRootNodeIfExist(root);
		result.add(library.getClassSimpleName() + rejex + library.getId());
	}

	@Override
	public List<String> findBreadCrumbForCampaign(String className, Long id, String rejex) {
		List<String> result = null;
		if (!"Iteration".equals(className) && !"TestSuite".equals(className)) {
			CampaignLibraryNode node = null;
			if (className.endsWith("Folder")) {
				node = campaignLibraryNavigationService.findFolder(id);
			} else {
				node = campaignLibraryNavigationService.findCampaign(id);
			}
			result = findBreadCrumbOfCampaignNode(node, campaignLibraryNavigationService, rejex);
		}

		// TODO complete for iteration or test suite search
		return result;
	}

	private List<String> findBreadCrumbOfCampaignNode(CampaignLibraryNode node,
			CampaignLibraryNavigationService libraryNavigationService, String rejex) {
		List<String> result = new ArrayList<String>();
		result.add(node.getClass().getSimpleName() + rejex + node.getId());
		CampaignFolder parent = libraryNavigationService.findParentIfExists(node);
		fillBreadCrumbListUntillLibraryForCampaign(node, libraryNavigationService, rejex, result, parent);

		return result;
	}

	private void fillBreadCrumbListUntillLibraryForCampaign(CampaignLibraryNode node,
			CampaignLibraryNavigationService libraryNavigationService, String rejex, List<String> result,
			CampaignFolder parent) {
		CampaignLibraryNode root = node;
		CampaignFolder ancestor = parent;

		while (ancestor != null) {
			result.add(ancestor.getClass().getSimpleName() + rejex + ancestor.getId());
			root = ancestor;
			ancestor = libraryNavigationService.findParentIfExists(root);
		}
		CampaignLibrary library = libraryNavigationService.findLibraryOfRootNodeIfExist(root);
		result.add(library.getClassSimpleName() + rejex + library.getId());
	}
	// -----------------------------------------------------------------------------end TODO
}
