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
package squashtm.testautomation.internal.service;

import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.domain.TestAutomationServer;
import squashtm.testautomation.service.TestAutomationFinderService;

/**
 * That private interface contains methods that cannot be secured because the current ACL system doesn't encompass 
 * the TestAutomationX entities. That service should always be called through a front service being itself secured.
 * 
 * That interface won't be published as OSGI service.
 * 
 * @author bsiri
 *
 */
public interface InsecureTestAutomationManagementService extends TestAutomationFinderService{

	
	
	/**
	 * <p>
	 * 	Will persist the supplied remote TestAutomationProject. The argument must be attached to a {@link TestAutomationServer} 
	 * 	and return it using {@link TestAutomationProject#getServer()}.
	 * </p> 
	 * 
	 *  <p>
	 *  The service will first try to reattach the server and project instance to existing instance in the database 
	 *  (See org.squashtest.csp.tm.domain.testautomation package info for more about this), then persist the unknown 
	 *  entities.
	 * </p>
	 * 
	 * <p>
	 * 	That method returns the persisted instance of TestAutomationProject, that should be used in place of the one supplied in arguments 
	 * 	from now on by the client code. That method is idempotent : an attached entity won't be peristed again.
	 * </p>
	 * 
	 * @param remoteProject
	 */
	TestAutomationProject persistOrAttach(TestAutomationProject newProject); 
	
	
	
	/**
	 * That method returns the default server configuration. It is classified as insecure because 
	 * a TestAutomationServer isn't  a valid ACL entity, and yet it contains a password in clear (omg).
	 *  
	 * @return the default instance of TestAutomationServer.
	 */
	TestAutomationServer getDefaultServer();	
	
	
	
	
}
