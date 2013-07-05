/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.service.project;

import java.util.List;

import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.project.AdministrableProject;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.security.acls.PermissionGroup;
/**
 * Holder for non dynamically generated find methods for both Project and ProjectTemplate
 * @author mpagnon
 *
 */
public interface CustomGenericProjectFinder {
	/**
	 * Returns a TestAutomationServer instance. Either it is a persisted instance that the tm project was bound to
	 * lastly (through a ta project), either it will be the default server configuration.
	 * 
	 */
	TestAutomationServer getLastBoundServerOrDefault(long projectId);
	
	AdministrableProject findAdministrableProjectById(long projectId);	
	
	List<TestAutomationProject> findBoundTestAutomationProjects(long projectId);

	List<PartyProjectPermissionsBean> findPartyPermissionsBeansByProject(long projectId);
	
	PagedCollectionHolder<List<PartyProjectPermissionsBean>> findPartyPermissionsBeanByProject(PagingAndSorting sorting, Filtering filtering, long projectId);
	
	List<PermissionGroup> findAllPossiblePermission();

	List<Party> findPartyWithoutPermissionByProject(long projectId);
}
