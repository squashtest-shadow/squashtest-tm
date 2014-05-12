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
package org.squashtest.tm.service.internal.campaign;

import org.springframework.transaction.support.TransactionSynchronization;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.service.internal.testautomation.InsecureTestAutomationManagementService;
/**
 * 
 * This class has been created to avoid [Issue 1531]
 * 
 * @see CustomIterationModificationServiceImpl#createAndStartAutomatedSuite(java.util.List)
 * @author mpagnon
 *
 */
public class AutomatedRunTransactionHandler implements TransactionSynchronization {

	
	private AutomatedSuite suite;
	private InsecureTestAutomationManagementService testAutomationService;

	public AutomatedRunTransactionHandler(AutomatedSuite suite, InsecureTestAutomationManagementService testAutomationService){
		this.suite = suite;
		this.testAutomationService = testAutomationService;
	}
	@Override
	public void suspend() {
		//NOPE

	}

	@Override
	public void resume() {
		//NOPE

	}

	@Override
	public void flush() {
		//NOPE

	}

	@Override
	public void beforeCommit(boolean readOnly) {
		//NOPE

	}

	@Override
	public void beforeCompletion() {
		//NOPE

	}

	@Override
	public void afterCommit() {
		testAutomationService.startAutomatedSuite(suite);
	}

	@Override
	public void afterCompletion(int status) {
		//NOPE
	}

}
