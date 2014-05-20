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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.internal.repository.hibernate.NonUniqueEntityException;




public interface TestAutomationServerDao {

	/**
	 * Will persist a new {@link TestAutomationServer}.
	 * 
	 * @param server
	 * @throws NonUniqueEntityException if the given server happen to exist already.
	 */
	void persist(TestAutomationServer server);


	List<TestAutomationServer> findAllOrderedByName();

	long countAll();

	List<TestAutomationServer> findPagedServers(PagingAndSorting  pas);

	boolean hasBoundProjects(long serverId);

	/**
	 * 
	 * 
	 * @param id
	 * @return
	 */
	TestAutomationServer findById(Long id);


	TestAutomationServer findByName(String serverName);

	/**
	 * return all the projects that the given server hosts.
	 * 
	 * @param serverId
	 * @return
	 */
	List<TestAutomationProject> findAllHostedProjects(long serverId);


	void deleteServer(long serverId);

}
