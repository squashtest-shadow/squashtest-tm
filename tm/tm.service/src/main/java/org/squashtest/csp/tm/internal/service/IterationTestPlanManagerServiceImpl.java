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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.domain.IdentifiersOrderComparator;
import org.squashtest.csp.core.security.acls.model.ObjectAclService;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.infrastructure.strategy.LibrarySelectionStrategy;
import org.squashtest.csp.tm.internal.repository.ItemTestPlanDao;
import org.squashtest.csp.tm.internal.repository.IterationDao;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.tm.internal.repository.UserDao;
import org.squashtest.csp.tm.service.IterationTestPlanManagerService;
import org.squashtest.csp.tm.service.ProjectFilterModificationService;

@Service("squashtest.tm.service.IterationTestPlanManagerService")
@Transactional
public class IterationTestPlanManagerServiceImpl implements IterationTestPlanManagerService {

	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;

	@Inject
	private IterationDao iterationDao;

	@Inject
	private ItemTestPlanDao itemTestPlanDao;

	@Inject
	private ObjectAclService aclService;

	@Inject
	private UserDao userDao;
	
	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private LibraryNodeDao<TestCaseLibraryNode> testCaseLibraryNodeDao;

	@Inject
	private ProjectFilterModificationService projectFilterModificationService;

	private ObjectIdentityRetrievalStrategy objIdRetrievalStrategy;

	@Inject
	@Qualifier("squashtest.tm.service.TestCaseLibrarySelectionStrategy")
	private LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy;

	@ServiceReference
	public void setObjectAclService(ObjectAclService aclService) {
		this.aclService = aclService;
	}

	@ServiceReference
	public void setObjectIdentityRetrievalStrategy(ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy) {
		this.objIdRetrievalStrategy = objectIdentityRetrievalStrategy;
	}

	// FIXME : security
	@Override
	public Iteration findIteration(long iterationId) {
		return iterationDao.findById(iterationId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestCaseLibrary> findLinkableTestCaseLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : testCaseLibraryDao
				.findAll();

	}

	/*
	 * security note here : well what if we add test cases for which the user have no permissions on ? think of
	 * something better.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.csp.tm.service.IterationTestPlanManagerService#addTestCasesToIteration(java.util.List, long)
	 */

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'LINK') "
			+ "or hasRole('ROLE_ADMIN')")
	public void addTestCasesToIteration(final List<Long> objectsIds, long iterationId) {

		Iteration iteration = iterationDao.findById(iterationId);

		addTestPlanItemsToIteration(objectsIds, iteration);
	}

	@Override
	@PreAuthorize("hasPermission(#iteration, 'LINK') " + "or hasRole('ROLE_ADMIN')")
	public List<IterationTestPlanItem> addTestPlanItemsToIteration(final List<Long> testNodesIds, Iteration iteration) {

		// nodes are returned unsorted
		List<TestCaseLibraryNode> nodes = testCaseLibraryNodeDao.findAllByIds(testNodesIds);

		// now we resort them according to the order in which the testcaseids were given
		IdentifiersOrderComparator comparator = new IdentifiersOrderComparator(testNodesIds);
		Collections.sort(nodes, comparator);

		List<TestCase> testCases = new TestCaseNodeWalker().walk(nodes);

		List<IterationTestPlanItem> testPlan = new LinkedList<IterationTestPlanItem>();

		for (TestCase testCase : testCases) {
			IterationTestPlanItem itp = new IterationTestPlanItem(testCase);
			testPlan.add(itp);
		}
		addTestPlanToIteration(testPlan, iteration.getId());
		return testPlan;
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'LINK') "
			+ "or hasRole('ROLE_ADMIN')")
	public void addTestPlanToIteration(List<IterationTestPlanItem> testPlan, long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		for (IterationTestPlanItem itp : testPlan) {
			itemTestPlanDao.persist(itp);
			iteration.addTestPlan(itp);
		}

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'LINK') "
			+ "or hasRole('ROLE_ADMIN')")
	public boolean removeTestPlansFromIteration(List<Long> testPlanIds, long iterationId) {
		boolean unauthorizedDeletion = false;
		Iteration it = iterationDao.findById(iterationId);

		unauthorizedDeletion = removeTestPlansFromIterationObj(testPlanIds, it);

		return unauthorizedDeletion;
	}

	@Override
	@PreAuthorize("hasPermission(#iteration, 'LINK') " + "or hasRole('ROLE_ADMIN')")
	public boolean removeTestPlansFromIterationObj(List<Long> testPlanIds, Iteration iteration) {
		boolean unauthorizedDeletion = false;

		for (Long id : testPlanIds) {
			IterationTestPlanItem itp = iteration.getTestPlan(id);
			// We do not allow deletion if there are execution
			if (itp.getExecutions().isEmpty()) {
				iteration.removeTestPlan(itp);
				itemTestPlanDao.remove(itp);
			} else {
				unauthorizedDeletion = true;
			}
		}

		return unauthorizedDeletion;
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'LINK') "
			+ "or hasRole('ROLE_ADMIN')")
	public boolean removeTestPlanFromIteration(Long testPlanId, long iterationId) {
		boolean unauthorizedDeletion = false;
		Iteration it = iterationDao.findById(iterationId);

		IterationTestPlanItem itp = it.getTestPlan(testPlanId);
		// We do not allow deletion if there are execution
		if (itp.getExecutions().isEmpty()) {
			it.removeTestPlan(itp);
			itemTestPlanDao.remove(itp);
		} else {
			unauthorizedDeletion = true;
		}

		return unauthorizedDeletion;

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public List<TestCase> findPlannedTestCases(Long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		return iteration.getPlannedTestCase();
	}

	// FIXME : security
	@Override
	public void updateTestCaseLastExecutedByAndOn(IterationTestPlanItem givenTestPlan, Date lastExecutedOn,
			String lastExecutedBy) {
		givenTestPlan.setLastExecutedBy(lastExecutedBy);
		givenTestPlan.setLastExecutedOn(lastExecutedOn);
		givenTestPlan.setUser(userDao.findUserByLogin(lastExecutedBy));

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IterationTestPlanItem>> findTestPlan(long iterationId, CollectionSorting filter) {
		List<IterationTestPlanItem> testPlan = iterationDao.findTestPlanFiltered(iterationId, filter);
		long count = iterationDao.countTestPlans(iterationId);
		return new FilteredCollectionHolder<List<IterationTestPlanItem>>(count, testPlan);
	}

	@Override
	public List<User> findAssignableUserForTestPlan(long iterationId) {

		Iteration iteration = iterationDao.findById(iterationId);

		List<ObjectIdentity> entityRefs = new ArrayList<ObjectIdentity>();

		ObjectIdentity oid = objIdRetrievalStrategy.getObjectIdentity(iteration);
		entityRefs.add(oid);

		List<String> loginList = aclService.findUsersWithExecutePermission(entityRefs);
		List<User> usersList = userDao.findUsersByLoginList(loginList);

		return usersList;

	}

	// FIXME : security execute
	@Override
	public void assignUserToTestPlanItem(Long testPlanId, long iterationId, Long userId) {
		Iteration iteration = iterationDao.findById(iterationId);
		User user = (userId == 0) ? null : userDao.findById(userId);

		IterationTestPlanItem itp = iteration.getTestPlan(testPlanId);
		if (!itp.isTestCaseDeleted()) {
			itp.setUser(user);
		}
	}

	// FIXME : security execute
	@Override
	public void assignUserToTestPlanItems(List<Long> testPlanIds, long iterationId, Long userId) {
		Iteration iteration = iterationDao.findById(iterationId);

		User user = (userId == 0) ? null : userDao.findById(userId);

		for (Long testPlan : testPlanIds) {
			IterationTestPlanItem itp = iteration.getTestPlan(testPlan);
			if (!itp.isTestCaseDeleted()) {
				itp.setUser(user);
			}
		}
	}

	// FIXME : security
	@Override
	public IterationTestPlanItem findTestPlanItemByTestCaseId(long iterationId, long testCaseId) {
		Iteration iteration = iterationDao.findById(iterationId);
		return iteration.getTestPlanForTestCaseId(testCaseId);
	}

	// FIXME : security
	@Override
	public IterationTestPlanItem findTestPlanItem(Long iterationId, Long itemTestPlanId) {
		Iteration iteration = iterationDao.findById(iterationId);
		return iteration.getTestPlan(itemTestPlanId);
	}
	
	@Override
	public List<ExecutionStatus> getExecutionStatusList(){
		
		List<ExecutionStatus> statusList = new LinkedList<ExecutionStatus>();
		statusList.addAll(ExecutionStatus.getCanonicalStatusSet());
		return statusList;
	}
	
	@Override
	public void assignExecutionStatusToTestPlanItem(Long testPlanId, long iterationId, String statusName){
		
		IterationTestPlanItem testPlanItem = findTestPlanItem(iterationId,testPlanId);
		testPlanItem.setExecutionStatus(ExecutionStatus.valueOf(statusName));
	}
}
