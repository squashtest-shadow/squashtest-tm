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
package org.squashtest.tm.service.internal.campaign;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultColumnFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.DelegatePagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.MultiSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.domain.IdentifiersOrderComparator;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.security.UserContextHolder;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.campaign.CustomIterationModificationService;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy;
import org.squashtest.tm.service.internal.repository.DatasetDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.internal.testcase.TestCaseNodeWalker;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.security.acls.model.ObjectAclService;
import org.squashtest.tm.service.user.UserAccountService;

@Service("squashtest.tm.service.IterationTestPlanManagerService")
@Transactional
public class IterationTestPlanManagerServiceImpl implements IterationTestPlanManagerService {


	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;

	@Inject
	private IterationDao iterationDao;

	@Inject
	private IterationTestPlanDao iterationTestPlanDao;

	@Inject
	private ObjectAclService aclService;

	@Inject
	private UserDao userDao;

	@Inject
	private IndexationService indexationService;

	@Inject
	private UserAccountService userService;

	@Inject
	private DatasetDao datasetDao;

	@Inject
	private CampaignNodeDeletionHandler deletionHandler;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private LibraryNodeDao<TestCaseLibraryNode> testCaseLibraryNodeDao;

	@Inject
	private ProjectFilterModificationService projectFilterModificationService;

	@Inject
	@Qualifier("squashtest.core.security.ObjectIdentityRetrievalStrategy")
	private ObjectIdentityRetrievalStrategy objIdRetrievalStrategy;

	@Inject
	@Qualifier("squashtest.tm.service.TestCaseLibrarySelectionStrategy")
	private LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy;

	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	public List<TestCaseLibrary> findLinkableTestCaseLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : testCaseLibraryDao
				.findAll();

	}

	/**
	 *
	 * @see org.squashtest.tm.service.campaign.CustomIterationModificationService#findAssignedTestPlan(long, Paging)
	 */
	@Override
	public PagedCollectionHolder<List<IndexedIterationTestPlanItem>> findAssignedTestPlan(long iterationId,
			PagingAndMultiSorting sorting, ColumnFiltering columnFiltering) {

		// configure the filter, in case the test plan must be restricted to what the user can see.
		Filtering userFiltering = DefaultFiltering.NO_FILTERING;

		try {
			PermissionsUtils.checkPermission(permissionEvaluationService, Arrays.asList(iterationId),
					"READ_UNASSIGNED", Iteration.class.getName());
		} catch (AccessDeniedException ade) {
			String userLogin = userService.findCurrentUser().getLogin();
			userFiltering = new DefaultFiltering("User.login", userLogin);
		}

		List<IndexedIterationTestPlanItem> indexedItems = iterationDao.findIndexedTestPlan(iterationId, sorting,
				userFiltering, columnFiltering);
		long testPlanSize = iterationDao.countTestPlans(iterationId, userFiltering, columnFiltering);

		return new PagingBackedPagedCollectionHolder<List<IndexedIterationTestPlanItem>>(sorting, testPlanSize,
				indexedItems);
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
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public void addTestCasesToIteration(final List<Long> objectsIds, long iterationId) {

		Iteration iteration = iterationDao.findById(iterationId);

		addTestPlanItemsToIteration(objectsIds, iteration);
	}

	@Override
	@PreAuthorize("hasPermission(#iteration, 'LINK') " + OR_HAS_ROLE_ADMIN)
	public List<IterationTestPlanItem> addTestPlanItemsToIteration(final List<Long> testNodesIds, Iteration iteration) {

		// nodes are returned unsorted
		List<TestCaseLibraryNode> nodes = testCaseLibraryNodeDao.findAllByIds(testNodesIds);

		// now we resort them according to the order in which the testcaseids were given
		IdentifiersOrderComparator comparator = new IdentifiersOrderComparator(testNodesIds);
		Collections.sort(nodes, comparator);

		List<TestCase> testCases = new TestCaseNodeWalker().walk(nodes);

		List<IterationTestPlanItem> testPlan = new LinkedList<IterationTestPlanItem>();

		for (TestCase testCase : testCases) {
			addTestCaseToTestPlan(testCase, testPlan);
		}
		addTestPlanToIteration(testPlan, iteration.getId());
		return testPlan;
	}

	private void addTestCaseToTestPlan(TestCase testCase, List<IterationTestPlanItem> testPlan) {
		List<Dataset> datasets = datasetDao.findOwnDatasetsByTestCase(testCase.getId());

		if (datasets != null && !datasets.isEmpty()) {
			testPlan.addAll(IterationTestPlanItem.createTestPlanItems(testCase, datasets));
		} else {
			// TODO somewhat useless, above "if" branch could handle both cases
			testPlan.add(IterationTestPlanItem.createUnparameterizedTestPlanItem(testCase));
		}

		indexationService.reindexTestCase(testCase.getId());
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public void addTestPlanToIteration(List<IterationTestPlanItem> testPlan, long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		for (IterationTestPlanItem itp : testPlan) {
			iterationTestPlanDao.persist(itp);
			iteration.addTestPlan(itp);
		}
	}

	/**
	 * @see CustomIterationModificationService#changeTestPlanPosition(long, int, List)
	 */
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public void changeTestPlanPosition(long iterationId, int newPosition, List<Long> itemIds) {
		Iteration iteration = iterationDao.findById(iterationId);
		List<IterationTestPlanItem> items = iterationTestPlanDao.findAllByIds(itemIds);

		iteration.moveTestPlans(newPosition, items);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public void reorderTestPlan(long iterationId, MultiSorting newSorting) {

		Paging noPaging = Pagings.NO_PAGING;
		PagingAndMultiSorting sorting = new DelegatePagingAndMultiSorting(noPaging, newSorting);
		Filtering filtering = DefaultFiltering.NO_FILTERING;
		ColumnFiltering columnFiltering = DefaultColumnFiltering.NO_FILTERING;

		List<IterationTestPlanItem> items = iterationDao.findTestPlan(iterationId, sorting, filtering, columnFiltering);

		Iteration iteration = iterationDao.findById(iterationId);

		iteration.getTestPlans().clear();
		iteration.getTestPlans().addAll(items);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public boolean removeTestPlansFromIteration(List<Long> testPlanIds, long iterationId) {
		boolean unauthorizedDeletion = false;
		Iteration it = iterationDao.findById(iterationId);

		unauthorizedDeletion = removeTestPlansFromIterationObj(testPlanIds, it);

		return unauthorizedDeletion;
	}

	@Override
	@PreAuthorize("hasPermission(#iteration, 'LINK') " + OR_HAS_ROLE_ADMIN)
	public boolean removeTestPlansFromIterationObj(List<Long> testPlanItemsIds, Iteration iteration) {
		boolean unauthorizedDeletion = false;

		List<IterationTestPlanItem> items = iterationTestPlanDao.findAllByIds(testPlanItemsIds);

		for (IterationTestPlanItem item : items) {
			// We do not allow deletion if there are execution and the user does not have sufficient rights
			// so we keep track of whether at lease one item couldn't be deleted
			unauthorizedDeletion = unauthorizedDeletion || removeTestPlanItemIfOkWithExecsAndRights(iteration, item);
		}

		return unauthorizedDeletion;
	}

	private boolean removeTestPlanItemIfOkWithExecsAndRights(Iteration iteration, IterationTestPlanItem item) {
		boolean unauhorized = false;

		if (item.getExecutions().isEmpty()) {
			doRemoveTestPlanItemFromIteration(iteration, item);
		} else {
			try {
				PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(item,
						"EXTENDED_DELETE"));
				doRemoveTestPlanItemFromIteration(iteration, item);
			} catch (AccessDeniedException exception) {
				unauhorized = true;
			}

		}
		return unauhorized;
	}

	private void doRemoveTestPlanItemFromIteration(Iteration iteration, IterationTestPlanItem item) {
		TestCase testCase = item.getReferencedTestCase();

		iteration.removeItemFromTestPlan(item);

		deletionHandler.deleteIterationTestPlanItem(item);

		// unless the test case was deleted, we need to re-index its statistics
		if (testCase != null) {
			indexationService.reindexTestCase(testCase.getId());
		}
	}

	@Override
	@PreAuthorize("hasPermission(#testPlanItemId, 'org.squashtest.tm.domain.campaign.IterationTestPlanItem', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public boolean removeTestPlanFromIteration(long testPlanItemId) {
		boolean unauthorizedDeletion = false;
		IterationTestPlanItem item = iterationTestPlanDao.findById(testPlanItemId);
		Iteration iteration = item.getIteration();

		// We do not allow deletion if there are execution and the user isn't authorized to do that
		unauthorizedDeletion = removeTestPlanItemIfOkWithExecsAndRights(iteration, item);

		return unauthorizedDeletion;

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	public List<TestCase> findPlannedTestCases(Long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		return iteration.getPlannedTestCase();
	}

	@Override
	@PreAuthorize("hasPermission(#item, 'EXECUTE') " + OR_HAS_ROLE_ADMIN + "or hasRole('ROLE_TA_API_CLIENT')")
	public void updateMetadata(IterationTestPlanItem item) {
		Execution execution = item.getLatestExecution();
		if (execution != null) {
			item.setLastExecutedBy(execution.getLastExecutedBy());
			item.setLastExecutedOn(execution.getLastExecutedOn());
			item.setUser(userDao.findUserByLogin(execution.getLastExecutedBy()));
		}
	}

	/**
	 * Instead of using data from an actual execution (like {@link #updateMetadata(IterationTestPlanItem)}, we update
	 * those data according to given parameters.
	 *
	 * @param item: the iteration test plan item to update the metadatas of
	 * @param user : the user that will be set as last executor and assigne
	 * @param date : the date to set lastExecutedOn property
	 **/
	private void arbitraryUpdateMetadata(IterationTestPlanItem item, User user, Date date) {

		item.setLastExecutedBy(user.getLogin());
		item.setLastExecutedOn(date);
		item.setUser(user);
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<IndexedIterationTestPlanItem>> findTestPlan(long iterationId,
			PagingAndSorting filter) {
		List<IndexedIterationTestPlanItem> testPlan = iterationDao.findIndexedTestPlan(iterationId, filter,
				DefaultFiltering.NO_FILTERING, DefaultColumnFiltering.NO_FILTERING);
		long count = iterationDao.countTestPlans(iterationId, DefaultFiltering.NO_FILTERING);
		return new PagingBackedPagedCollectionHolder<List<IndexedIterationTestPlanItem>>(filter, count, testPlan);
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

	/**
	 *
	 * @see org.squashtest.tm.service.campaign.IterationTestPlanManagerService#assignUserToTestPlanItem(long, long)
	 */
	@Override
	@PreAuthorize("hasPermission(#testPlanItemId, 'org.squashtest.tm.domain.campaign.IterationTestPlanItem', 'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public void assignUserToTestPlanItem(long testPlanItemId, long userId) {
		User user = (userId == 0) ? null : userDao.findById(userId);

		IterationTestPlanItem itp = iterationTestPlanDao.findById(testPlanItemId);
		if (!itp.isTestCaseDeleted()) {
			itp.setUser(user);
		}
	}

	/**
	 * @see org.squashtest.tm.service.campaign.IterationTestPlanManagerService#assignUserToTestPlanItems(java.util.List,
	 *      long)
	 */
	@Override
	public void assignUserToTestPlanItems(List<Long> testPlanIds, long userId) {
		//check permission
		PermissionsUtils.checkPermission(permissionEvaluationService, testPlanIds, "WRITE", IterationTestPlanItem.class.getName());
		List<IterationTestPlanItem> items = iterationTestPlanDao.findAllByIds(testPlanIds);

		User user = (userId == 0) ? null : userDao.findById(userId);

		for (IterationTestPlanItem item : items) {
			if (!item.isTestCaseDeleted()) {
				item.setUser(user);
			}
		}
	}

	/**
	 * @deprecated method used only in Integration tests should be removed
	 * @param iterationId
	 * @param testCaseId
	 * @return
	 */
	@Deprecated
	@Override
	public IterationTestPlanItem findTestPlanItemByTestCaseId(long iterationId, long testCaseId) {
		Iteration iteration = iterationDao.findById(iterationId);
		for (IterationTestPlanItem item : iteration.getTestPlans()) {
			if (!item.isTestCaseDeleted() && item.getReferencedTestCase().getId() == testCaseId) {
				return item;
			}
		}
		return null;
	}

	@Override
	@PreAuthorize("hasPermission(#itemTestPlanId, 'org.squashtest.tm.domain.campaign.IterationTestPlanItem', 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	public IterationTestPlanItem findTestPlanItem(long itemTestPlanId) {
		return iterationTestPlanDao.findById(itemTestPlanId);
	}

	@Override
	public List<ExecutionStatus> getExecutionStatusList() {

		List<ExecutionStatus> statusList = new LinkedList<ExecutionStatus>();
		statusList.addAll(ExecutionStatus.getCanonicalStatusSet());
		return statusList;
	}


	/**
	 * {@link IterationTestPlanManagerService}{@link #forceExecutionStatus(List, String)}
	 * @return
	 */
	@Override
	public List<IterationTestPlanItem> forceExecutionStatus(List<Long> testPlanIds, String statusName) {
		PermissionsUtils.checkPermission(permissionEvaluationService, testPlanIds, "WRITE", IterationTestPlanItem.class.getName());
		List<IterationTestPlanItem> testPlanItems = iterationTestPlanDao.findAllByIds(testPlanIds);
		ExecutionStatus status = ExecutionStatus.valueOf(statusName);
		String login = UserContextHolder.getUsername();
		User user = userDao.findUserByLogin(login);
		Date date = new Date();
		for(IterationTestPlanItem item : testPlanItems){
			item.setExecutionStatus(status);
			arbitraryUpdateMetadata(item, user, date);
		}
		return testPlanItems;

	}


	/**
	 * Creates a fragment of test plan, containing either :
	 * <ul>
	 * <li>a unique item when the test case is not parameterized</li>
	 * <li>one item per dataset when the test case is parameterized</li>
	 * </ul>
	 *
	 * <strong>Note :</strong> The returned test plan fragment is in a transient state.
	 *
	 * @param referenced
	 * @param assignee
	 * @return
	 */
	private Collection<IterationTestPlanItem> createTestPlanFragment(TestCase testCase) {
		List<IterationTestPlanItem> fragment = new ArrayList<IterationTestPlanItem>();
		addTestCaseToTestPlan(testCase, fragment);
		return fragment;
	}

	/**
	 * @see org.squashtest.tm.service.campaign.IterationTestPlanManagerService#createTestPlanFragment(org.squashtest.tm.domain.testcase.TestCase,
	 *      org.squashtest.tm.domain.users.User)
	 */
	@Override
	public Collection<IterationTestPlanItem> createTestPlanFragment(TestCase testCase, User assignee) {
		Collection<IterationTestPlanItem> fragment = createTestPlanFragment(testCase);

		for (IterationTestPlanItem item : fragment) {
			item.setUser(assignee);
		}

		return fragment;
	}

	@Override
	@PreAuthorize("hasPermission(#itemId, 'org.squashtest.tm.domain.campaign.IterationTestPlanItem', 'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public void changeDataset(long itemId, Long datasetId) {
		IterationTestPlanItem item = iterationTestPlanDao.findById(itemId);

		if (datasetId == null){
			item.setReferencedDataset(null);
		}
		else if (! item.isTestCaseDeleted()){
			TestCase tc = item.getReferencedTestCase();
			Dataset ds = datasetDao.findById(datasetId);
			if (! ds.getTestCase().equals(tc)){
				throw new IllegalArgumentException("dataset [id:'"+ds.getId()+"', name:'"+ds.getName()+
						"'] doesn't belong to test case [id:'"+tc.getId()+"', name:'"+tc.getName()+"']");
			}
			item.setReferencedDataset(ds);
		}

	}
}
