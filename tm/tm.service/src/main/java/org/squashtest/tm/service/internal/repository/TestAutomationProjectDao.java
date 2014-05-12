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

import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.internal.repository.hibernate.NonUniqueEntityException;

public interface TestAutomationProjectDao {
	
	/**
	 * Will persist a new {@link TestAutomationProject}. Note : each server must have different characteristics, more exactly each combination of 
	 * attributes is unique. Therefore if the object to be persisted already exists in the database an exception will be raised instead.
	 * 
	 * @param newProject
	 * @throws NonUniqueEntityException if the given server happen to exist already. 
	 */
	void persist(TestAutomationProject newProject);
	
	/**
	 * Will persist a TestAutomationProject if really new, or return the existing instance
	 * if not. An instance exists if : 
	 * 
	 * <ul>
	 * 	<li>argument's id is set and exists in base,</li>
	 * 	<li>argument's id is not set but matches one by content</li>
	 * </ul>
	 * In all cases it returns the persisted project : this returned instance should replace the one supplied as argument in the client code.
	 * 
	 * @param newProject
	 * @return a persistent version of that project.
	 */
	TestAutomationProject uniquePersist(TestAutomationProject newProject);
	
	/**
	 * 
	 *  
	 * @param id
	 * @return
	 */
	TestAutomationProject findById(Long projectId);
	
	
	/**
	 *	<p>Given a detached (or even attached) {@link TestAutomationProject} example, will fetch a {@link TestAutomationProject}
	 *	having the same characteristics. Null attributes will be discarded before the comparison. </p>
	 *
	 * @return a TestAutomation project if one was found, null if none was found.
	 * @throws NonUniqueEntityException if more than one match. Causes are either a not restrictive enough example... or a bug.
	 */	
	TestAutomationProject findByExample(TestAutomationProject example);
	
		
}
