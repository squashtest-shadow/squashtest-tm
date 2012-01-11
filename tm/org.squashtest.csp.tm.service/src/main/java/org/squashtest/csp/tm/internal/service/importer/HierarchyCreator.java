package org.squashtest.csp.tm.internal.service.importer;

import org.squashtest.csp.tm.domain.library.structures.StringPathMap;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.internal.utils.archive.ArchiveReader;
import org.squashtest.csp.tm.internal.utils.archive.Entry;

class HierarchyCreator{
	
	
	private ArchiveReader reader;
	private ExcelTestCaseParser parser;
	
	private StringPathMap<TestCaseLibraryNode> pathMap = new StringPathMap<TestCaseLibraryNode>();
	
	
	private ImportSummaryImpl summary = new ImportSummaryImpl();
	private TestCaseFolder root;
	
	
	public HierarchyCreator(){
		root = new TestCaseFolder();
		root.setName("/");
		
		pathMap.put("/", root);
	}

	
	public void setArchiveReader(ArchiveReader reader){
		this.reader = reader;
	}
	
	public void setParser(ExcelTestCaseParser parser){
		this.parser = parser;
	}
	
	public ImportSummaryImpl getSummary(){
		return summary;
	}
	
	
	public TestCaseFolder getNodes(){
		return root;
	}
	
	public void create(){
		
		while(reader.hasNext()){
			
			Entry entry = reader.next();

			
			if (entry.isDirectory()){					
				findOrCreateFolder(entry);					
			}else{					
				createTestCase(entry);					
			}
			
		}
	}
	
	/**
	 * will chain-create folders if path elements do not exist. Will also store the path in a map
	 * for faster reference later.
	 * 
	 * @param path
	 */
	private TestCaseFolder findOrCreateFolder(Entry entry){
		TestCaseFolder isFound = (TestCaseFolder)pathMap.getMappedElement(entry.getName());
		
		if (isFound != null){
			
			return isFound;
			
		}else{
			TestCaseFolder parent = findOrCreateFolder(entry.getParent());
			
			TestCaseFolder newFolder = new TestCaseFolder();
			newFolder.setName(entry.getShortName());
			parent.addContent(newFolder);
			
			pathMap.put(entry.getName(), newFolder);
			
			return newFolder;
		}
	}
	
	
	/**
	 * will chain-create folders if the parents does not exit, create the test case, and store the path in 
	 * a map for faster reference later.
	 * @param entry
	 */
	private void createTestCase(Entry entry){
		try{
			//create the test case
			TestCase testCase = parser.parseFile(entry.getStream(), summary);
			testCase.setName(entry.getShortName());
			
			//find or create the parent folder
			TestCaseFolder parent = findOrCreateFolder(entry.getParent());
			
			parent.addContent(testCase);
			
			pathMap.put(entry.getName(), testCase);
			
		}catch(SheetCorruptedException ex){
			summary.incrFailures();
		}
		
	}
	
	
	
}