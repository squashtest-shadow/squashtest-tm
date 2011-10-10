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
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.security.acls.model.ObjectAclService;
import org.squashtest.csp.core.service.security.UserContextService;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
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
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestCaseFolderDao;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.tm.internal.repository.UserDao;
import org.squashtest.csp.tm.service.IterationModificationService;
import org.squashtest.csp.tm.service.IterationTestPlanManagerService;
import org.squashtest.csp.tm.service.ProjectFilterModificationService;

@Service("squashtest.tm.service.IterationTestPlanManagerService")
@Transactional
public class IterationTestPlanManagerServiceImpl implements IterationTestPlanManagerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(IterationTestPlanManagerServiceImpl.class);

	@Inject
	private IterationModificationService delegateService;

	@Inject
	private UserContextService userContextService;

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;

	@Inject
	private IterationDao iterationDao;

	@Inject
	private ItemTestPlanDao itemTestPlanDao;

	@Inject
	private TestCaseFolderDao folderDao;

	@Inject
	private ObjectAclService aclService;

	@Inject
	private UserDao userDao;

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
	 * security note here : well what if we add test cases for which the user have no permissions on ? think of something
	 * better.
	 *
	 * (non-Javadoc)
	 * @see org.squashtest.csp.tm.service.IterationTestPlanManagerService#addTestCasesToIteration(java.util.List, long)
	 */

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void addTestCasesToIteration(List<Long> objectsIds, long iterationId) {
		List<TestCase> tcs  = new ArrayList<TestCase>();

		for (Long tcId : objectsIds) {
			if (tcId > 0){
				TestCase testCase = testCaseDao.findById(tcId);
				tcs.add(testCase);
			}
			else{
				tcs.addAll(addFolderContent(Math.abs(tcId)));
			}
		}

		if (!tcs.isEmpty()) {

			Iteration it = iterationDao.findById(iterationId);

			for (TestCase testCase : tcs) {
				//do not persist new ItemTestPlan's if that is not necessary. Let's check first
				//if the test case was already planned.
				if (! it.isTestCasePlanned(testCase)){
					IterationTestPlanItem itp = new IterationTestPlanItem(testCase);
					itemTestPlanDao.persist(itp);
					it.addTestPlan(itp);
				}
			}
		}
	}

	// TODO : if still using recursive algorithms, linearize them.
	private List<TestCase> addFolderContent(long folderId){
		List<TestCase> resultList = new ArrayList<TestCase>();
		List<Long> folderIds = new ArrayList<Long>();
		folderIds.add(0, 1L);
		int i = 0;

		List<TestCaseLibraryNode> folderContent = folderDao.findAllContentById(folderId);

		do {
			folderIds.remove(i);
			for (TestCaseLibraryNode node : folderContent) {
				if (node.getClassSimpleName() == "TestCaseFolder"){
					folderIds.add(node.getId());
				}
				else{
					resultList.add((TestCase) node);
				}
			}
			if (!folderIds.isEmpty()){
				folderContent = folderDao.findAllContentById(folderIds.get(i));
			}
		}while (!folderIds.isEmpty());

		return resultList;
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public boolean removeTestPlansFromIteration(List<Long> testPlanIds, long iterationId) {
		boolean unauthorizedDeletion = false;
		Iteration it = iterationDao.findById(iterationId);
		for (Long id : testPlanIds) {
			IterationTestPlanItem itp = it.getTestPlan(id);
			// We do not allow deletion if there are execution
			if (itp.getExecutions().isEmpty()) {
				it.removeTestPlan(itp);
				itemTestPlanDao.remove(itp);
			} else {
				unauthorizedDeletion = true;
			}
		}

		return unauthorizedDeletion;
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.csp.tm.domain.campaign.Iteration', 'WRITE') "
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
	public void updateTestCaseLastExecutedByAndOn(IterationTestPlanItem givenTestPlan, Date lastExecutedOn, String lastExecutedBy) {
		givenTestPlan.setLastExecutedBy(lastExecutedBy);
		givenTestPlan.setLastExecutedOn(lastExecutedOn);
		givenTestPlan.setUser(userDao.findUserByLogin(lastExecutedBy));

	}

	// FIXME : security
	@Override
	public FilteredCollectionHolder<List<IterationTestPlanItem>> findTestPlan(long iterationId, CollectionSorting filter) {
		return delegateService.findIterationTestPlan(iterationId, filter);
	}

	@Override
	public List<User> findAssignableUserForTestPlan(long iterationId) {

		Iteration iteration = iterationDao.findById(iterationId);

		List<ObjectIdentity> entityRefs = new ArrayList<ObjectIdentity>();

		ObjectIdentity oid = objIdRetrievalStrategy.getObjectIdentity(iteration);
		entityRefs.add(oid);


		List<String> loginList = aclService.findUsersWithWritePermission(entityRefs);
		List<User> usersList = userDao.findUsersByLoginList(loginList);

		return usersList;


	}

	@Override
	public void assignUserToTestPlanItem(Long testPlanId, long iterationId,
			Long userId) {
		Iteration iteration = iterationDao.findById(iterationId);
		User user = (userId==0) ? null : userDao.findById(userId);
		
		IterationTestPlanItem itp = iteration.getTestPlan(testPlanId);
		if (! itp.isTestCaseDeleted()){
			itp.setUser(user);
		}
	}

	@Override
	public void assignUserToTestPlanItems(List<Long> testPlanIds,
			long iterationId, Long userId) {
		Iteration iteration = iterationDao.findById(iterationId);

		User user = (userId==0) ? null : userDao.findById(userId);

		for (Long testPlan : testPlanIds) {
			IterationTestPlanItem itp = iteration.getTestPlan(testPlan);
			if (! itp.isTestCaseDeleted()){
				itp.setUser(user);
			}
		}


	}

	@Override
	public IterationTestPlanItem findTestPlanItemByTestCaseId(long iterationId, long testCaseId) {
		Iteration iteration = iterationDao.findById(iterationId);
		IterationTestPlanItem itp = iteration.getTestPlanForTestCaseId(testCaseId);
		return itp;
	}

	@Override
	public IterationTestPlanItem findTestPlanItem(Long iterationId, Long itemTestPlanId) {
		Iteration iteration = iterationDao.findById(iterationId);
		return iteration.getTestPlan(itemTestPlanId);
	}


}
