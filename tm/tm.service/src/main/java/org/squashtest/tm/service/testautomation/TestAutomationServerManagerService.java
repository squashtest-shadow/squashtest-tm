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
package org.squashtest.tm.service.testautomation;

import java.net.URL;
import java.util.List;

import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.internal.repository.hibernate.NonUniqueEntityException;

public interface TestAutomationServerManagerService {

	// *********************** entity management *******************

	TestAutomationServer findById(long serverId);

	/**
	 * Will persist a new {@link TestAutomationServer}.
	 * 
	 * @param server
	 *            : the server to persist
	 * @throws NonUniqueEntityException
	 *             if the given server happen to exist already.
	 */
	void persist(TestAutomationServer server);

	boolean hasBoundProjects(long serverId);

	boolean hasExecutedTests(long serverId);

	void deleteServer(long serverId);

	List<TestAutomationServer> findAllOrderedByName();

	PagedCollectionHolder<List<TestAutomationServer>> findSortedTestAutomationServers(PagingAndSorting pagingNsorting);

	// *********************** Properties mutators ****************************

	void changeURL(long serverId, URL url);

	void changeName(long serverId, String newName);

	void changeLogin(long serverId, String login);

	void changePassword(long serverId, String password);

	void changeDescription(long serverId, String description);

	void changeManualSlaveSelection(long serverId, boolean hasSlaves);

}
