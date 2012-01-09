/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.service.importer;

import java.io.InputStream;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.library.structures.StringPathMap;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
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
	
	@Inject
	private ExcelTestCaseParser parser;
	
	
	public ImportSummary importExcelTestCases(InputStream archiveStream, Long libraryId){

		ArchiveReader reader = factory.createReader(archiveStream);
		
		/* phase 1 : convert the content of the archive into Squash entities */
		
		HierarchyCreator creator = new HierarchyCreator();
		creator.setArchiveReader(reader);
		creator.setParser(parser);
		
		creator.create();
		
		TestCaseFolder root = creator.getNodes();
		ImportSummaryImpl summary = creator.getSummary();
		
		/* phase 2 : merge with the actual database content */
		
		return null;
	}
	
	
	/* ************ private workers ************************* */
	
	
	private static class HierarchyCreator{
		
		
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
	
}
