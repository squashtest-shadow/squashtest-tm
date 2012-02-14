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

import java.util.List;

import javax.inject.Inject;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.Paging;
import org.squashtest.csp.core.infrastructure.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.campaign.TestSuiteStatistics;
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;
import org.squashtest.csp.tm.service.CustomTestSuiteModificationService;

@Service("CustomTestSuiteModificationService")
public class CustomTestSuiteModificationServiceImpl implements
		CustomTestSuiteModificationService {
	
	@Inject
	private TestSuiteDao testSuiteDao;
	
	private PermissionEvaluationService permissionService;

	@Inject
	private CampaignNodeDeletionHandler campaignNodeDeletionHandler;
	
	@ServiceReference
	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite','WRITE') or hasRole('ROLE_ADMIN')")		
	public void rename(long suiteId, String newName)
			throws DuplicateNameException {
		TestSuite suite = testSuiteDao.findById(suiteId);
		suite.rename(newName);
	}
	
	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite','WRITE') or hasRole('ROLE_ADMIN')")		
	public void bindTestPlan(long suiteId, List<Long> itemTestPlanIds) {
		//that implementation relies on how the TestSuite will do the job (regarding the checks on whether the itps belong to the 
		//same iteration of not
		TestSuite suite = testSuiteDao.findById(suiteId);
		suite.addTestPlanById(itemTestPlanIds);
	}
	
	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite','WRITE') or hasRole('ROLE_ADMIN')")		
	public TestSuite findById(long suiteId) {
		TestSuite suite = testSuiteDao.findById(suiteId);
		return suite;
	}
	
	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite','READ') or hasRole('ROLE_ADMIN')")
	public PagedCollectionHolder<List<IterationTestPlanItem>> findTestSuiteTestPlan(long suiteId, Paging paging) {
		
		List<IterationTestPlanItem> testPlan = testSuiteDao.findTestPlanPaged(suiteId, paging);
		
		long count = testSuiteDao.countTestPlans(suiteId);
		
		return new PagingBackedPagedCollectionHolder<List<IterationTestPlanItem>> (paging, count, testPlan);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.csp.tm.domain.campaign.TestSuite','WRITE') or hasRole('ROLE_ADMIN')")		
	public TestSuiteStatistics findTestSuiteStatistics(long suiteId){
		TestSuiteStatistics stats = testSuiteDao.getTestSuiteStatistics(suiteId);
		return stats;
	}
	
	@Override
	public List<Long> remove(List<Long> suitesIds) {
		// fetch
		List<TestSuite> testSuites = testSuiteDao.findAllByIdList(suitesIds);
		// check
		checkPermissionsForAll(testSuites, "WRITE");
		// proceed
		List<Long> deletedIds = campaignNodeDeletionHandler.deleteSuites(testSuites);
		return deletedIds;
	}

	/* ************************* private stuffs ************************* */

	/* **that class just performs the same, using a domainObject directly */
	private class SecurityCheckableObject {
		private final Object domainObject;
		private final String permission;

		private SecurityCheckableObject(Object domainObject, String permission) {
			this.domainObject = domainObject;
			this.permission = permission;
		}

		public String getPermission() {
			return permission;
		}

		public Object getObject() {
			return domainObject;
		}

	}

	private void checkPermission(SecurityCheckableObject... checkableObjects) {
		for (SecurityCheckableObject object : checkableObjects) {
			if (!permissionService
					.hasRoleOrPermissionOnObject("ROLE_ADMIN", object.getPermission(), object.getObject())) {
				throw new AccessDeniedException("Access is denied");
			}
		}
	}


	/* ************************* private stuffs ************************* */

	private void checkPermissionsForAll(List<TestSuite> testSuites, String permission) {
		for (TestSuite testSuite : testSuites) {
			checkPermission(new SecurityCheckableObject(testSuite, permission));
		}

	}


}
