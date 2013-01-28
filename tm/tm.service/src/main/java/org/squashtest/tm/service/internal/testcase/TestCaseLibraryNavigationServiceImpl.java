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
package org.squashtest.tm.service.internal.testcase;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.domain.testcase.ActionStepCollector;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.ExportTestCaseData;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.internal.importer.TestCaseImporter;
import org.squashtest.tm.service.internal.library.AbstractLibraryNavigationService;
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy;
import org.squashtest.tm.service.internal.library.NodeDeletionHandler;
import org.squashtest.tm.service.internal.library.PasteStrategy;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.LibraryDao;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;

@Service("squashtest.tm.service.TestCaseLibraryNavigationService")
@Transactional
public class TestCaseLibraryNavigationServiceImpl extends
		AbstractLibraryNavigationService<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode> implements
		TestCaseLibraryNavigationService {
	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;
	@Inject
	private TestCaseFolderDao testCaseFolderDao;
	@Inject
	private TestCaseDao testCaseDao;
	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private LibraryNodeDao<TestCaseLibraryNode> testCaseLibraryNodeDao;

	@Inject
	private TestCaseImporter testCaseImporter;
	@Inject
	private TestCaseNodeDeletionHandler deletionHandler;
	@Inject
	private ProjectFilterModificationService projectFilterModificationService;
	@Inject
	@Qualifier("squashtest.tm.service.TestCaseLibrarySelectionStrategy")
	private LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy;
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToTestCaseFolderStrategy")
	private PasteStrategy<TestCaseFolder, TestCaseLibraryNode> pasteToTestCaseFolderStrategy;
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToTestCaseLibraryStrategy")
	private PasteStrategy<TestCaseLibrary, TestCaseLibraryNode> pasteToTestCaseLibraryStrategy;
	
	@Override
	protected NodeDeletionHandler<TestCaseLibraryNode, TestCaseFolder> getDeletionHandler() {
		return deletionHandler;
	}

	@Override
	protected LibraryDao<TestCaseLibrary, TestCaseLibraryNode> getLibraryDao() {
		return testCaseLibraryDao;
	}

	@Override
	protected FolderDao<TestCaseFolder, TestCaseLibraryNode> getFolderDao() {
		return testCaseFolderDao;
	}

	@Override
	protected final LibraryNodeDao<TestCaseLibraryNode> getLibraryNodeDao() {
		return testCaseLibraryNodeDao;
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ') or hasRole('ROLE_ADMIN')")
	public TestCase findTestCase(long testCaseId) {
		return testCaseDao.findById(testCaseId);
	}

	@Override
	protected PasteStrategy<TestCaseFolder, TestCaseLibraryNode> getPasteToFolderStrategy() {
		return pasteToTestCaseFolderStrategy;
	}

	@Override
	protected PasteStrategy<TestCaseLibrary, TestCaseLibraryNode> getPasteToLibraryStrategy() {
		return pasteToTestCaseLibraryStrategy;
	}

	@Override
	public String getPathAsString(long entityId) {
		// get
		TestCaseLibraryNode node = getLibraryNodeDao().findById(entityId);

		// check
		checkPermission(new SecurityCheckableObject(node, "READ"));

		// proceed
		List<String> names = getLibraryNodeDao().getParentsName(entityId);

		return "/" + node.getProject().getName() + "/" + formatPath(names);

	}

	private String formatPath(List<String> names) {
		StringBuilder builder = new StringBuilder();
		for (String name : names) {
			builder.append("/").append(name);
		}
		return builder.toString();
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.testcase.TestCaseLibrary' , 'CREATE' )"
			+ "or hasRole('ROLE_ADMIN')")
	public void addTestCaseToLibrary(long libraryId, TestCase testCase) {
		
		TestCaseLibrary library = testCaseLibraryDao.findById(libraryId);

		if (!library.isContentNameAvailable(testCase.getName())) {
			throw new DuplicateNameException(testCase.getName(), testCase.getName());
		} 
		else {
			library.addContent(testCase);
			testCaseDao.safePersist(testCase);
			createCustomFieldValues(testCase);
			
			//also create the custom field values for the steps if any
			if (! testCase.getSteps().isEmpty()){
				createCustomFieldValues(testCase.getActionSteps());
			}
			
		}
	}
	
	
	

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.testcase.TestCaseLibrary' , 'CREATE' )"
			+ "or hasRole('ROLE_ADMIN')")
	public void addTestCaseToLibrary(long libraryId, TestCase testCase,	Map<Long, String> customFieldValues) {
		addTestCaseToLibrary(libraryId, testCase);
		initCustomFieldValues(testCase, customFieldValues);		
	}
	
	
	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.testcase.TestCaseFolder' , 'CREATE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void addTestCaseToFolder(long folderId, TestCase testCase) {
		TestCaseFolder folder = testCaseFolderDao.findById(folderId);

		if (!folder.isContentNameAvailable(testCase.getName())) {
			throw new DuplicateNameException(testCase.getName(), testCase.getName());
		} 
		else {
			folder.addContent(testCase);
			testCaseDao.safePersist(testCase);
			createCustomFieldValues(testCase);
			
			//also create the custom field values for the steps if any
			if (! testCase.getSteps().isEmpty()){
				createCustomFieldValues(testCase.getActionSteps());
			}
		}
	}
	
	
	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.testcase.TestCaseFolder' , 'CREATE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void addTestCaseToFolder(long folderId, TestCase testCase,Map<Long, String> customFieldValues) {
		addTestCaseToFolder(folderId, testCase);
		initCustomFieldValues(testCase, customFieldValues);
	}
	
	

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.testcase.TestCaseLibrary', 'IMPORT') or hasRole('ROLE_ADMIN')")
	public ImportSummary importExcelTestCase(InputStream archiveStream, long libraryId, String encoding) {

		return testCaseImporter.importExcelTestCases(archiveStream, libraryId, encoding);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'LINK') or hasRole('ROLE_ADMIN')")
	public List<TestCaseLibrary> findLinkableTestCaseLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : testCaseLibraryDao
				.findAll();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExportTestCaseData> findTestCasesToExportFromProject(List<Long> ids) {
		List<ExportTestCaseData> testCases = testCaseDao.findTestCaseToExportFromProject(ids);
		return (List<ExportTestCaseData>) setFullFolderPath(testCases);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExportTestCaseData> findTestCasesToExportFromNodes(List<Long> nodesIds) {
		List<ExportTestCaseData> testCases = testCaseDao.findTestCaseToExportFromNodes(nodesIds);
		return (List<ExportTestCaseData>) setFullFolderPath(testCases);
	}

}
