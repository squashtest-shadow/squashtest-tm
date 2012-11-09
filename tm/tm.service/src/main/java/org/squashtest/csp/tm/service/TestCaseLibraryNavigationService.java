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
package org.squashtest.csp.tm.service;

import java.io.InputStream;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.ExportTestCaseData;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.service.importer.ImportSummary;

/**
 * Service for navigation in a TestCase library use case.
 * 
 * @author Gregory Fouquet
 * 
 */
public interface TestCaseLibraryNavigationService extends
		LibraryNavigationService<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode>, TestCaseLibraryFinderService {

	void addTestCaseToLibrary(long libraryId, TestCase testCase);
	
	/**
	 * @deprecated use {@link TestCaseFinder#findById(long)} instead
	 * @param testCaseId
	 * @return
	 */
	@Deprecated
	TestCase findTestCase(long testCaseId);

	void addTestCaseToFolder(long folderId, TestCase testCase);

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
	List<ExportTestCaseData> findTestCasesToExportFromProject(List<Long> ids);

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
