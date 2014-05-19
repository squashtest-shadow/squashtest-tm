/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.testautomation;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.internal.repository.TestAutomationProjectDao;
import org.squashtest.tm.service.internal.repository.TestAutomationServerDao;
import org.squashtest.tm.service.testautomation.TestAutomationServerManagerService;

@Transactional
@Service("squashtest.tm.service.TestAutomationServerManagementService")
public class TestAutomationServerManagerServiceImpl implements TestAutomationServerManagerService {

	@Inject
	private TestAutomationServerDao serverDao;

	@Inject
	private TestAutomationProjectDao projectDao;


	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void persist(TestAutomationServer server) {
		serverDao.persist(server);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public boolean hasBoundProjects(long serverId) {
		return serverDao.hasBoundProjects(serverId);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public boolean hasExecutedTests(long serverId) {
		Collection<TestAutomationProject> projects = serverDao.findAllHostedProjects(serverId);
		return projectDao.haveExecutedTests(projects);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void deleteServer(long serverId) {
		Collection<TestAutomationProject> projects = serverDao.findAllHostedProjects(serverId);

		projectDao.deleteProjects(projects);
		serverDao.deleteServer(serverId);

	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TM_PROJECT_MANAGER')")
	public List<TestAutomationServer> findAllOrderedByName() {
		return serverDao.findAllOrderedByName();
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TM_PROJECT_MANAGER')")
	public PagedCollectionHolder<List<TestAutomationServer>> findSortedTestAutomationServers(PagingAndSorting pagingNsorting) {

		List<TestAutomationServer> sortedServers = serverDao.findPagedServers(pagingNsorting);
		long count = serverDao.countAll();

		return new PagingBackedPagedCollectionHolder<List<TestAutomationServer>>(pagingNsorting, count, sortedServers);
	}

	@Override
	public void changeURL(long serverId, URL url){
		TestAutomationServer server = serverDao.findById(serverId);
		server.setBaseURL(url);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void changeName(long serverId, String newName) {
		TestAutomationServer server = serverDao.findById(serverId);
		server.setName(newName);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void changeLogin(long serverId, String login) {
		TestAutomationServer server = serverDao.findById(serverId);
		server.setLogin(login);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void changePassword(long serverId, String password) {
		TestAutomationServer server = serverDao.findById(serverId);
		server.setPassword(password);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void changeDescription(long serverId, String description) {
		TestAutomationServer server = serverDao.findById(serverId);
		server.setDescription(description);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void changeIsManualSlaveSelection(long serverId, boolean manualSlaveSelection) {
		TestAutomationServer server = serverDao.findById(serverId);
		server.setManualSlaveSelection(manualSlaveSelection);
	}

}
