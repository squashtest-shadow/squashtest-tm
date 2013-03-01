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
package org.squashtest.tm.service.internal.testautomation.service;

import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.testautomation.TestAutomationFinderService;


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
	 *  (See org.squashtest.tm.domain.testautomation package info for more about this), then persist the unknown 
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
	 * <p>Pretty much the same than {@link #persistOrAttach(TestAutomationProject)}. The argument must be attached to a persisted and 
	 * session-bound {@link TestAutomationProject}.</p>
	 * 
	 * <p>Same principles and same results apply here</p>
	 * 
	 * 
	 * @param newTest
	 * @return a persisted TestAutomationTest, that should be used by the client code from now on.
	 */
	AutomatedTest persistOrAttach(AutomatedTest newTest);
	
	
	TestAutomationProject findProjectById(long projectId);
	
	
	AutomatedTest findTestById(long testId);
	
	
	/**
	 * That method returns the default server configuration. It is classified as insecure because 
	 * a TestAutomationServer isn't  a valid ACL entity, and yet it contains a password in clear (omg).
	 *  
	 * @return the default instance of TestAutomationServer.
	 */
	TestAutomationServer getDefaultServer();	
	
	
	
	/**
	 * Will (attempt to) retrieve the result url for all the executions that belongs to a given automated suite, 
	 * and of which tests belongs to a given project.
	 * 
	 * @param project
	 * @param suite
	 */
	void fetchAllResultURL(TestAutomationProject project, AutomatedSuite suite);
	

	

}
