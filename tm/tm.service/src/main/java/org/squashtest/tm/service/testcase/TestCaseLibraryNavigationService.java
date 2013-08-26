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
package org.squashtest.tm.service.testcase;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.ExportTestCaseData;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.library.LibraryNavigationService;

/**
 * Service for navigation in a TestCase library use case.
 * 
 * @author Gregory Fouquet
 * 
 */
public interface TestCaseLibraryNavigationService extends
		LibraryNavigationService<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode>, TestCaseLibraryFinderService {

	/**
	 * Adds a TestCase to the root of the library. The custom fields will be created with their default value.
	 * 
	 * @param libraryId
	 * @param testCase
	 */
	void addTestCaseToLibrary(long libraryId, TestCase testCase);
	
	
	/**
	 * Adds a TestCase to the root of the Library, and its initial custom field values. The initial custom field values
	 * are passed as a Map<Long, String>, that maps the id of the {@link CustomField} to the values of the corresponding {@link CustomFieldValue}.
	 * Read that last sentence again. 
	 * 
	 * @param libraryId
	 * @param testCase
	 * @param customFieldValues
	 */
	void addTestCaseToLibrary(long libraryId, TestCase testCase, Map<Long, String> customFieldValues);
	

	
	/**
	 * Adds a TestCase to a folder. The custom fields will be created with their default value.
	 * 
	 * @param libraryId
	 * @param testCase
	 */
	void addTestCaseToFolder(long folderId, TestCase testCase);
	
	
	/**
	 * Adds a TestCase to a folder, and its initial custom field values. The initial custom field values
	 * are passed as a Map<Long, String>, that maps the id of the {@link CustomField} to the values of the corresponding {@link CustomFieldValue}.
	 * Read that last sentence again. 
	 * 
	 * @param libraryId
	 * @param testCase
	 * @param customFieldValues
	 */
	void addTestCaseToFolder(long folderId, TestCase testCase, Map<Long, String> customFieldValues);
	
	
	/**
	 * @deprecated use {@link TestCaseFinder#findById(long)} instead
	 * @param testCaseId
	 * @return
	 */
	@Deprecated
	TestCase findTestCase(long testCaseId);

	
	/**
	 * Accepts a stream to a .zip file containing regular folders or excel files and nothing else. Will convert the test
	 * cases from excel to squash.
	 * 
	 * @param archiveStream
	 * @param libraryId
	 *            the identifier of the library we are importing test cases into.
	 * @param encoding
	 *            the encoding
	 * @return a summary of the operations.
	 */
	ImportSummary importExcelTestCase(InputStream archiveStream, long libraryId, String encoding);

	/**
	 * Will find all test cases found in the given projects and return their information as a list of
	 * {@linkplain ExportTestCaseData}
	 * 
	 * @param libraryIds
	 *            ids of {@linkplain Project}
	 * @return a list of {@linkplain ExportTestCaseData}
	 */
	List<ExportTestCaseData> findTestCasesToExportFromLibrary(List<Long> ids);

	/**
	 * Will find all test cases of the given ids and contained in folders of the given ids, and return their
	 * information as a list of {@linkplain ExportTestCaseData}
	 * 
	 * @param nodesIds
	 *            ids of {@linkplain TestCaseLibraryNode}
	 * @return a list of {@linkplain ExportTestCaseData}
	 */
	List<ExportTestCaseData> findTestCasesToExportFromNodes(@NotNull List<Long> nodesIds);
	

}
