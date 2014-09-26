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

import java.net.URL;
import java.util.List;

import org.hibernate.Session;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.internal.repository.hibernate.NonUniqueEntityException;

public interface TestAutomationServerDao {

	/**
	 * Will persist a new {@link TestAutomationServer}.
	 * 
	 * @param server
	 *            : the server to persist
	 * @throws NonUniqueEntityException
	 *             if the given server happen to exist already.
	 */
	void persist(TestAutomationServer server);

	/**
	 * Will find all occurrences of {@link TestAutomationServer} in the database ordered by their name.
	 * 
	 * @return : all {@link TestAutomationServer} ordered by their name
	 */
	List<TestAutomationServer> findAllOrderedByName();

	/**
	 * Will count all occurrences of {@link TestAutomationServer} in the database
	 * 
	 * @return the number of {@link TestAutomationServer} in the database
	 */
	long countAll();

	List<TestAutomationServer> findPagedServers(PagingAndSorting pas);

	/**
	 * Checks if the {@link TestAutomationServer} is bound to at least one {@link TestAutomationProject}
	 * 
	 * @param serverId
	 *            : the id of the concernedTestAutomationServer
	 * @return : true if the TestAutomationServer is bound to a TA-project
	 */
	boolean hasBoundProjects(long serverId);

	/**
	 * Simple find entity by id.
	 * 
	 * @param id
	 *            : the id of the entity to find
	 * @return the entity matching the given id or <code>null</code>
	 */
	TestAutomationServer findById(Long id);

	/**
	 * Find the {@linkplain TestAutomationServer} by it's name.
	 * 
	 * @param serverName
	 *            : the name of the entity to find
	 * @return : the entity matching the given name (must be only one or database is corrupted) or <code>null</code>.
	 */
	TestAutomationServer findByName(String serverName);

	/**
	 * 
	 * Find the {@linkplain TestAutomationServer} using its URL and the login used to log on it. There is indeed a
	 * unique constraint on it (one can log on a given server only once with a given account).
	 * 
	 * @param url
	 * @param login
	 * @return
	 */
	TestAutomationServer findByUrlAndLogin(URL url, String login);

	/**
	 * Will delete the given {@linkplain TestAutomationServer} and dereference it from TM {@linkplain Project}s.
	 * <p>
	 * <b style="color:red">Warning :</b> When using this method there is a risk that your Hibernate beans are not up to
	 * date. Use {@link Session#clear()} and {@link Session#refresh(Object)} to make sure your they are.
	 * </p>
	 * 
	 * @param serverId
	 *            the id of the {@linkplain TestAutomationServer} to delete.
	 */
	void deleteServer(long serverId);

}
