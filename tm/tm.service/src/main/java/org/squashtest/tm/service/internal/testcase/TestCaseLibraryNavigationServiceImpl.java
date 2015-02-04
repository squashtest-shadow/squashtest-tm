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
package org.squashtest.tm.service.internal.testcase;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.domain.testcase.ExportTestCaseData;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNodeVisitor;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.InconsistentInfoListItemException;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.batchexport.TestCaseExcelExporterService;
import org.squashtest.tm.service.internal.batchimport.TestCaseExcelBatchImporter;
import org.squashtest.tm.service.internal.importer.TestCaseImporter;
import org.squashtest.tm.service.internal.library.AbstractLibraryNavigationService;
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy;
import org.squashtest.tm.service.internal.library.NodeDeletionHandler;
import org.squashtest.tm.service.internal.library.PasteStrategy;
import org.squashtest.tm.service.internal.library.PathService;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.LibraryDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryNodeDao;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.statistics.testcase.TestCaseStatisticsBundle;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.testcase.TestCaseStatisticsService;

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
	private TestCaseLibraryNodeDao testCaseLibraryNodeDao;

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
	private Provider<PasteStrategy<TestCaseFolder, TestCaseLibraryNode>> pasteToTestCaseFolderStrategyProvider;
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToTestCaseLibraryStrategy")
	private Provider<PasteStrategy<TestCaseLibrary, TestCaseLibraryNode>> pasteToTestCaseLibraryStrategyProvider;

	@Inject
	private TestCaseStatisticsService statisticsService;

	@Inject
	private TestCaseCallTreeFinder calltreeService;

	@Inject
	private TestCaseExcelExporterService excelService;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private TestCaseExcelBatchImporter batchImporter;

	@Inject
	private PathService pathService;

	@Inject
	private InfoListItemFinderService infoListItemService;

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
	protected final TestCaseLibraryNodeDao getLibraryNodeDao() {
		return testCaseLibraryNodeDao;
	}

	@Override
	protected PasteStrategy<TestCaseFolder, TestCaseLibraryNode> getPasteToFolderStrategy() {
		return pasteToTestCaseFolderStrategyProvider.get();
	}

	@Override
	protected PasteStrategy<TestCaseLibrary, TestCaseLibraryNode> getPasteToLibraryStrategy() {
		return pasteToTestCaseLibraryStrategyProvider.get();
	}

	@Override
	@Transactional(readOnly = true)
	public String getPathAsString(long entityId) {
		return pathService.buildTestCasePath(entityId);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.squashtest.tm.service.testcase.TestCaseLibraryFinderService#getPathsAsString(java.util.List)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<String> getPathsAsString(List<Long> ids) {
		return pathService.buildTestCasesPaths(ids);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.squashtest.tm.service.testcase.TestCaseLibraryFinderService#findNodesByPath(java.util.List)
	 */

	@Override
	@Transactional(readOnly = true)
	public List<TestCaseLibraryNode> findNodesByPath(List<String> paths) {
		return getLibraryNodeDao().findNodesByPath(paths);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.squashtest.tm.service.testcase.TestCaseLibraryFinderService#findNodeIdsByPath(java.util.List)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Long> findNodeIdsByPath(List<String> paths) {
		return getLibraryNodeDao().findNodeIdsByPath(paths);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.squashtest.tm.service.testcase.TestCaseLibraryFinderService#findNodeIdByPath(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Long findNodeIdByPath(String path) {
		return getLibraryNodeDao().findNodeIdByPath(path);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.squashtest.tm.service.testcase.TestCaseLibraryFinderService#findNodeByPath(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public TestCaseLibraryNode findNodeByPath(String path) {
		return getLibraryNodeDao().findNodeByPath(path);
	}



	@Override
	@PreAuthorize("hasPermission(#destinationId, 'org.squashtest.tm.domain.testcase.TestCaseLibrary' , 'CREATE' )"
			+ "or hasRole('ROLE_ADMIN')")
	public void addFolderToLibrary(long destinationId, TestCaseFolder newFolder) {

		TestCaseLibrary container = getLibraryDao().findById(destinationId);
		container.addContent(newFolder);

		// fix the nature and type for the possible nested test cases inside that folder
		new NatureTypeChainFixer().fix(newFolder);

		// now proceed
		getFolderDao().persist(newFolder);

		// and then create the custom field values, as a better fix for [Issue 2061]
		new CustomFieldValuesFixer().fix(newFolder);
	}

	@Override
	@PreAuthorize("hasPermission(#destinationId, 'org.squashtest.tm.domain.testcase.TestCaseFolder' , 'CREATE' )"
			+ "or hasRole('ROLE_ADMIN')")
	public final void addFolderToFolder(long destinationId, TestCaseFolder newFolder) {

		TestCaseFolder container = getFolderDao().findById(destinationId);
		container.addContent(newFolder);

		// fix the nature and type for the possible nested test cases inside that folder
		new NatureTypeChainFixer().fix(newFolder);

		// now proceed
		getFolderDao().persist(newFolder);

		// and then create the custom field values, as a better fix for [Issue 2061]
		new CustomFieldValuesFixer().fix(newFolder);
	}


	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.testcase.TestCaseLibrary' , 'CREATE' )"
			+ "or hasRole('ROLE_ADMIN')")
	public void addTestCaseToLibrary(long libraryId, TestCase testCase, Integer position) {

		TestCaseLibrary library = testCaseLibraryDao.findById(libraryId);

		if (!library.isContentNameAvailable(testCase.getName())) {
			throw new DuplicateNameException(testCase.getName(), testCase.getName());
		}
		else {
			if (position != null){
				library.addContent(testCase,position);
			}
			else{
				library.addContent(testCase);
			}
			replaceInfoListReferences(testCase);
			testCaseDao.safePersist(testCase);
			createCustomFieldValuesForTestCase(testCase);

		}
	}



	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.testcase.TestCaseLibrary' , 'CREATE' )"
			+ "or hasRole('ROLE_ADMIN')")
	public void addTestCaseToLibrary(long libraryId, TestCase testCase, Map<Long, RawValue> customFieldValues, Integer position) {
		addTestCaseToLibrary(libraryId, testCase, position);
		initCustomFieldValues(testCase, customFieldValues);
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.testcase.TestCaseFolder' , 'CREATE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void addTestCaseToFolder(long folderId, TestCase testCase, Integer position) {
		TestCaseFolder folder = testCaseFolderDao.findById(folderId);

		if (!folder.isContentNameAvailable(testCase.getName())) {
			throw new DuplicateNameException(testCase.getName(), testCase.getName());
		} else {
			if (position != null){
				folder.addContent(testCase, position);
			}
			else{
				folder.addContent(testCase);
			}
			replaceInfoListReferences(testCase);
			testCaseDao.safePersist(testCase);
			createCustomFieldValuesForTestCase(testCase);
		}
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.testcase.TestCaseFolder' , 'CREATE') "
			+ "or hasRole('ROLE_ADMIN')")
	public void addTestCaseToFolder(long folderId, TestCase testCase, Map<Long, RawValue> customFieldValues, Integer position) {
		addTestCaseToFolder(folderId, testCase, position);
		initCustomFieldValues(testCase, customFieldValues);
	}

	@Override
	public Long mkdirs(String folderpath) {

		String path = folderpath.replaceFirst("^/", "").replaceFirst("/$", "");

		StringBuffer buffer = new StringBuffer();
		String[] split = path.split("(?<!\\\\)/");
		List<String> paths = new ArrayList<String>(split.length - 1);

		// build all the paths on the way.
		buffer.append("/" + split[0]);
		for (int i = 1; i < split.length; i++) {
			buffer.append("/" + split[i]);
			paths.add(buffer.toString());
		}

		// find the folder ids, if exist
		List<Long> foundIds = findNodeIdsByPath(paths);

		int nullIdx = foundIds.indexOf(null);
		TestCaseFolder foldertree = null;

		switch (nullIdx) {
		case -1:
			return foundIds.get(foundIds.size() - 1); // all folders do exist, simply return the last element;

		case 0:
			Long libraryId = projectDao.findByName(split[0].replaceAll("\\\\\\/", "/")).getTestCaseLibrary().getId();
			foldertree = mkTransFolders(1, split);
			addFolderToLibrary(libraryId, foldertree);
			break;

		default:
			Long parentFolder = foundIds.get(nullIdx - 1);
			foldertree = mkTransFolders(nullIdx + 1, split);
			addFolderToFolder(parentFolder, foldertree);
			break;
		}

		TestCaseFolder lastFolder = foldertree;
		do {
			if (lastFolder.hasContent()) {
				lastFolder = (TestCaseFolder) lastFolder.getContent().get(0);
			}
		} while (lastFolder.hasContent());

		return lastFolder.getId();

	}

	private TestCaseFolder mkTransFolders(int startIndex, String[] names) {

		TestCaseFolder baseFolder = null;
		TestCaseFolder currentParent = null;
		TestCaseFolder currentChild = null;

		for (int i = startIndex; i < names.length; i++) {
			currentChild = new TestCaseFolder();
			currentChild.setName(names[i].replaceAll("\\\\\\/", "/")); // unescapes escaped slashes '\/' to slashes '/'
			currentChild.setDescription("");

			// if this is the first round in the loop we must initialize some variables
			if (baseFolder == null) {
				baseFolder = currentChild;
				currentParent = currentChild;
			} else {
				currentParent.addContent(currentChild);
			}

			currentParent = currentChild;

		}

		return baseFolder;

	}

	/*
	 * CUF : this is a very quick fix for [Issue 2061], TODO : remove the lines that are related to this issue and
	 * replace
	 * it with another solution mentioned in the ticket
	 * same for requirement import
	 */
	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.testcase.TestCaseLibrary', 'IMPORT') or hasRole('ROLE_ADMIN')")
	public ImportSummary importZipTestCase(InputStream archiveStream, long libraryId, String encoding) {

		ImportSummary summary = testCaseImporter.importExcelTestCases(archiveStream, libraryId, encoding);

		return summary;
	}

	@Override
	public ImportLog simulateImportExcelTestCase(File excelFile) {
		return batchImporter.simulateImport(excelFile);
	}

	@Override
	public ImportLog performImportExcelTestCase(File excelFile) {
		return batchImporter.performImport(excelFile);
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
	@Transactional(readOnly = true)
	public List<ExportTestCaseData> findTestCasesToExport(List<Long> libraryIds, List<Long> nodeIds,
			boolean includeCalledTests) {

		Collection<Long> allIds = findTestCaseIdsFromSelection(libraryIds, nodeIds, includeCalledTests);
		allIds = securityFilterIds(allIds, "org.squashtest.tm.domain.testcase.TestCase", "EXPORT");

		List<ExportTestCaseData> testCases = testCaseDao.findTestCaseToExportFromNodes(new ArrayList<Long>(allIds));
		return (List<ExportTestCaseData>) setFullFolderPath(testCases);

	}

	@Override
	@Transactional(readOnly = true)
	public File exportTestCaseAsExcel(List<Long> libraryIds, List<Long> nodeIds, boolean includeCalledTests, boolean keepRteFormat) {

		Collection<Long> allIds = findTestCaseIdsFromSelection(libraryIds, nodeIds, includeCalledTests);
		allIds = securityFilterIds(allIds, "org.squashtest.tm.domain.testcase.TestCase", "EXPORT");

		return excelService.exportAsExcel(new ArrayList<Long>(allIds), keepRteFormat);
	}

	@Override
	public TestCaseStatisticsBundle getStatisticsForSelection(Collection<Long> libraryIds, Collection<Long> nodeIds) {

		Collection<Long> tcIds = findTestCaseIdsFromSelection(libraryIds, nodeIds);

		return statisticsService.gatherTestCaseStatisticsBundle(tcIds);
	}

	@Override
	public Collection<Long> findTestCaseIdsFromSelection(Collection<Long> libraryIds, Collection<Long> nodeIds) {
		return findTestCaseIdsFromSelection(libraryIds, nodeIds, false);
	}

	@Override
	public Collection<Long> findTestCaseIdsFromSelection(Collection<Long> libraryIds, Collection<Long> nodeIds,
			boolean includeCalledTests) {

		// get all the test cases
		Collection<Long> tcIds = new ArrayList<Long>();

		if (!libraryIds.isEmpty()) {
			tcIds.addAll(testCaseDao.findAllTestCaseIdsByLibraries(libraryIds));
		}
		if (!nodeIds.isEmpty()) {
			tcIds.addAll(testCaseDao.findAllTestCaseIdsByNodeIds(nodeIds));
		}
		if (includeCalledTests) {
			tcIds.addAll(calltreeService.getTestCaseCallTree(tcIds));
		}

		// filter out duplicates
		Set<Long> set = new HashSet<Long>(tcIds);
		tcIds = new ArrayList<Long>(set);

		// sec check
		tcIds = securityFilterIds(tcIds, "org.squashtest.tm.domain.testcase.TestCase", "READ");

		return tcIds;

	}

	private Collection<Long> securityFilterIds(Collection<Long> original, String entityType, String permission) {
		Collection<Long> effective = new ArrayList<Long>();
		for (Long id : original) {
			if (permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", permission, id, entityType)) {
				effective.add(id);
			}
		}
		return effective;
	}

	@Override
	public List<String> getParentNodesAsStringList(Long nodeId) {
		List<Long> ids = testCaseLibraryNodeDao.getParentsIds(nodeId);

		TestCase tc = testCaseDao.findById(nodeId);
		Long librabryId = tc.getLibrary().getId();

		List<String> parents = new ArrayList<String>();

		parents.add("#TestCaseLibrary-" + librabryId);

		if (ids.size() > 1) {
			for (int i = 0; i < ids.size() - 1; i++) {
				parents.add("#TestCaseFolder-" + String.valueOf(ids.get(i)));
			}
		}

		return parents;
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.testcase.TestCaseFolder', 'READ') or hasRole('ROLE_ADMIN')")
	public List<String> findNamesInFolderStartingWith(long folderId, String nameStart) {
		return testCaseFolderDao.findNamesInFolderStartingWith(folderId, nameStart);
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.testcase.TestCaseLibrary', 'READ') or hasRole('ROLE_ADMIN')")
	public List<String> findNamesInLibraryStartingWith(long libraryId, String nameStart) {
		return testCaseFolderDao.findNamesInLibraryStartingWith(libraryId, nameStart);
	}


	@Override
	public int countSiblingsOfNode(long nodeId) {
		return testCaseLibraryNodeDao.countSiblingsOfNode(nodeId);
	}

	// ******************** more private code *******************


	private void createCustomFieldValuesForTestCase(TestCase testCase) {
		createCustomFieldValues(testCase);

		// also create the custom field values for the steps if any
		if (!testCase.getSteps().isEmpty()) {
			createCustomFieldValues(testCase.getActionSteps());
		}
	}

	private void replaceInfoListReferences(TestCase testCase){

		InfoListItem nature = testCase.getNature();

		// if no nature set -> use the default item configured for the project
		if (nature == null){
			testCase.setNature( testCase.getProject().getTestCaseNatures().getDefaultItem());
		}
		else{
			// validate the code
			String natureCode = nature.getCode();
			if (! infoListItemService.isNatureConsistent(testCase.getProject().getId(), natureCode)){
				throw new InconsistentInfoListItemException("nature", natureCode);
			}

			// in case the item used here is merely a reference we need to replace it with
			// a persistent instance
			if (nature instanceof ListItemReference){
				testCase.setNature(infoListItemService.findReference((ListItemReference)nature));
			}
		}


		// now unroll the same procedure with the type
		InfoListItem type = testCase.getType();

		if (type == null){
			testCase.setType( testCase.getProject().getTestCaseTypes().getDefaultItem());
		}
		else{
			String typeCode = type.getCode();
			if (! infoListItemService.isTypeConsistent(testCase.getProject().getId(), typeCode)){
				throw new InconsistentInfoListItemException("type", typeCode);
			}
			if (type instanceof ListItemReference){
				testCase.setType(infoListItemService.findReference((ListItemReference)type));
			}
		}
	}


	private class NatureTypeChainFixer implements TestCaseLibraryNodeVisitor{

		private void fix(TestCaseFolder folder){
			for (TestCaseLibraryNode node : folder.getContent()){
				node.accept(this);
			}
		}

		@Override
		public void visit(TestCase visited) {
			replaceInfoListReferences(visited);
		}

		@Override
		public void visit(TestCaseFolder visited) {
			fix(visited);
		}
	}

	private class CustomFieldValuesFixer implements TestCaseLibraryNodeVisitor{

		private void fix(TestCaseFolder folder){
			for (TestCaseLibraryNode node : folder.getContent()){
				node.accept(this);
			}
		}

		@Override
		public void visit(TestCase visited) {
			createCustomFieldValuesForTestCase(visited);
		}

		@Override
		public void visit(TestCaseFolder visited) {
			fix(visited);
		}

	}

}
