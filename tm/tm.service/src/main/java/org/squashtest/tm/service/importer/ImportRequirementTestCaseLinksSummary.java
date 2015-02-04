/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.importer;

import java.util.Set;

public interface ImportRequirementTestCaseLinksSummary {
	/**
	 * 
	 * @return total Links read in the archive
	 */
	int getTotal();
	/**
	 * 
	 * @return total Links successfully imported
	 */
	int getSuccess();
	/**
	 * 
	 * @return total Links that could not be imported
	 */
	int getFailures();
	/**
	 * 
	 * @return row numbers where requirement was not found
	 */
	Set<Integer> getRequirementNotFound();
	/**
	 * 
	 * @return row numbers where test-case was not found
	 */
	Set<Integer> getTestCaseNotFound();
	/**
	 * 
	 * @return row numbers where the requirement had no version of the specified number
	 */
	Set<Integer> getVersionNotFound();
	/**
	 * 
	 * @return row numbers where the requirement version was obsolete and, therefore could not be linked to any test-case
	 */
	Set<Integer> getObsoletes();
	/**
	 * 
	 * @return row numbers where the user had no "link" access to the requirement
	 */
	Set<Integer> getRequirementAccessRejected();
	/**
	 * 
	 * @return row numbers where the user had no "link" access to the test-case
	 */
	Set<Integer> getTestCaseAccessRejected();
	/**
	 * adds the result of an import summary to this import summary 
	 */
	void add(ImportRequirementTestCaseLinksSummary summary);
	/**
	 * 
	 * @return row numbers where the test case was already linked to a version of the same Requirement
	 */
	Set<Integer> getLinkAlreadyExist();
	
	/**
	 * if the format of the file is bad, this method will let the client know.
	 * 
	 * @return
	 */
	Set<String> getMissingColumnHeaders();
	
	/**
	 * Tells whether the operation failed completely because of something really bad.
	 * 
	 */
	boolean isCriticalErrors();
	
}
