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
package org.squashtest.csp.tm.internal.repository.testautomation;

import java.util.Collection;

import org.squashtest.csp.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.csp.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.csp.tm.domain.testautomation.AutomatedTest;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;

public interface CustomAutomatedExecutionExtenderDao {
	
	/**
	 * Given a load of {@link AutomatedExecutionExtender}, returns detached entities (raw beans) of ALL of its dependencies, including full
	 * initialization of themselves, their {@link AutomatedSuite}, {@link AutomatedTest}, {@link TestAutomationProject} and {@link TestAutomationServer}.
	 * That may represent quite a load for the machine, especially considering that the entities passed in arguments are still present in memory at this point. 
	 * 
	 * @param toInit
	 * @return
	 */
	Collection<AutomatedExecutionExtender> getInitializedAndDetachedExtenders(Collection<AutomatedExecutionExtender> toInit);
}
