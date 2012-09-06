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
package org.squashtest.csp.tm.internal.repository;

import org.squashtest.csp.tm.domain.testautomation.AutomatedTest;
import org.squashtest.csp.tm.internal.repository.hibernate.NonUniqueEntityException;

public interface TestAutomationTestDao {
	
	/**
	 * Will persist a new {@link AutomatedTest}. Note : each server must have different characteristics, more exactly each combination of 
	 * attributes is unique. Therefore if the object to be persisted already exists in the database an exception will be raised instead.
	 * 
	 * @param newTest
	 * @throws NonUniqueEntityException if the given server happen to exist already. 
	 */
	void persist(AutomatedTest newTest);
	
	/**
	 * Will persist a TestAutomationTest if really new, or return the existing instance
	 * if not. An instance exists if : 
	 * 
	 * <ul>
	 * 	<li>argument's id is set and exists in base,</li>
	 * 	<li>argument's id is not set but matches one by content</li>
	 * </ul>
	 * In all cases it returns the persisted project : this returned instance should replace the one supplied as argument in the client code.
	 * 
	 * @param newTest
	 * @return a persistent version of that test.
	 */
	AutomatedTest uniquePersist(AutomatedTest newTest);
	
	/**
	 * 
	 *  
	 * @param id
	 * @return
	 */
	AutomatedTest findById(Long testId);
	
	
	/**
	 *	<p>Given a detached (or even attached) {@link AutomatedTest} example, will fetch a {@link AutomatedTest}
	 *	having the same characteristics. Null attributes will be discarded before the comparison. </p>
	 *
	 * @return a TestAutomation test if one was found, null if none was found.
	 * @throws NonUniqueEntityException if more than one match. Causes are either a not restrictive enough example... or a bug.
	 */	
	AutomatedTest findByExample(AutomatedTest example);
	
		
}
