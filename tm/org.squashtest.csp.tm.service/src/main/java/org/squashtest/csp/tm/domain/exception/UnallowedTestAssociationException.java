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
package org.squashtest.csp.tm.domain.exception;

import org.squashtest.csp.tm.domain.testautomation.TestAutomationTest;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.tm.core.foundation.exception.ActionException;

/**
 * <p>Thrown when one tries to bind a {@link TestAutomationTest} to a {@link TestCase}, while the test automation feature is disabled 
 * for the project hosting it.</p>
 * 
 * @author bsiri
 *
 */
public class UnallowedTestAssociationException extends ActionException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private static final String MESSAGE_KEY = "testautomation.exceptions.unallowedassociation";
	
	
	@Override
	public String getI18nKey() {
		return MESSAGE_KEY;
	}
	
	
	
	
}
