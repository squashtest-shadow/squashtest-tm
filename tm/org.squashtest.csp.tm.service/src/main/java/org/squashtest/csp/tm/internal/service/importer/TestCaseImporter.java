package org.squashtest.csp.tm.internal.service.importer;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.library.NodeReference;
import org.squashtest.csp.tm.domain.library.structures.StringPathMap;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.tm.internal.utils.archive.ArchiveReader;
import org.squashtest.csp.tm.internal.utils.archive.ArchiveReaderFactory;
import org.squashtest.csp.tm.internal.utils.archive.Entry;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.squashtest.csp.tm.service.importer.ImportSummary;


@Component
public class TestCaseImporter {

	@Inject
	private TestCaseLibraryNavigationService service;
	
	@Inject
	private ArchiveReaderFactory factory;
	
	
	public ImportSummary importExcelTestCases(InputStream archiveStream, Long libraryId){

		ArchiveReader reader = factory.createReader(archiveStream);
		
		//convert the archive content to detached Squash entities
		
		HierarchyCreator creator = new HierarchyCreator();
		creator.setArchiveReader(reader);
		creator.create();
		
		TestCaseFolder root = creator.getResult();
		ImportSummaryImpl summary = creator.getSummary();
		
		//second TODO merge with the actual database content
		return null;
	}
	
	
	/* ************ private workers ************************* */
	
	
	private static class HierarchyCreator{
		
		
		private ArchiveReader reader;
		private StringPathMap<TestCaseLibraryNode> pathMap = new StringPathMap<TestCaseLibraryNode>();
		
		
		private ImportSummaryImpl summary = new ImportSummaryImpl();
		private TestCaseFolder root;
		
		
		public HierarchyCreator(){
			root = new TestCaseFolder();
			root.setName("/");
			
			pathMap.put("/", root);
		}
		
		public HierarchyCreator(ArchiveReader reader){
			super();
			this.reader=reader;
		}
		
		public void setArchiveReader(ArchiveReader reader){
			this.reader = reader;
		}
		
		public ImportSummaryImpl getSummary(){
			return summary;
		}
		
		public TestCaseFolder getResult(){
			return root;
		}
		
		public void create(){
			
			while(reader.hasNext()){
				
				Entry entry = reader.next();

				
				if (entry.isDirectory()){
					createFolder(entry);
				}else{
					createTestCase(entry);
				}
				
			}
		}
		
		/**
		 * will chain-create folders if path elements do not exist.
		 * 
		 * @param path
		 */
		private void createFolder(Entry entry){
			TestCaseLibraryNode isFound = pathMap.getMappedElement(entry.getName());
			
			if (isFound != null){
				return;
			}else{
				Entry parentEntry = entry.getParent();
				
				//create the parent recursively if needed. Of course the root MUST be found at some point.
				createFolder(parentEntry);
				
				TestCaseFolder parent = (TestCaseFolder) pathMap.getMappedElement(parentEntry.getName());
				
				TestCaseFolder newFolder = new TestCaseFolder();
				newFolder.setName(entry.getName());
				parent.addContent(newFolder);
				
				pathMap.put(entry.getName(), newFolder);
			}
		}
		
		
		private void createTestCase(Entry entry){
			
		}
		
		
		
	}
	
}
