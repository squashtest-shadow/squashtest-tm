package org.squashtest.csp.tm.internal.service.importer


import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.internal.utils.archive.Entry;
import org.squashtest.csp.tm.internal.utils.archive.impl.ZipReader.ZipReaderEntry;

import spock.lang.Specification;


class TestCaseImporter_HierarchyCreatorTest extends Specification {

	def "should find an existing folder"(){
		
		given :
			def importer = new TestCaseImporter.HierarchyCreator();
			
		and :
			def folder = Mock(TestCaseFolder)
			importer.pathMap.put("/toto/folder", folder);
			
		and :
			def entry = Mock(Entry)
			entry.getName() >> "/toto/folder"
					
		when :
			def result = importer.findOrCreateFolder(entry)
		
		
		then :
			result == folder
		
	}
	
	def "should recursively create missing parent folders"(){
		
		given :
			def importer = new TestCaseImporter.HierarchyCreator();
			
			
		and :
			def entry = new ZipReaderEntry(null, "/melvin/van/peebles", true);
		
		when :
			importer.findOrCreateFolder(entry)
			
		then :
			def peebles = importer.pathMap.getMappedElement("/melvin/van/peebles")
			peebles instanceof TestCaseFolder
			peebles.getName() == "peebles"
			
			def van = importer.pathMap.getMappedElement("/melvin/van")
			van instanceof TestCaseFolder
			van.getName() == "van"
			van.getContent() == [peebles] as Set
			
			def melvin = importer.pathMap.getMappedElement("/melvin")
			melvin instanceof TestCaseFolder
			melvin.getName() == "melvin"
			melvin.getContent() == [van] as Set
			
			def root = importer.pathMap.getMappedElement("/")
			root.getContent() == [melvin] as Set
		
	}
	
	def "should create a test case"(){
		
		given :		
			def importer = new TestCaseImporter.HierarchyCreator();
			def parser = Mock(ExcelTestCaseParser)
			importer.setParser(parser)
			
		and :
			def mTc = new TestCase()
			parser.parseFile(_,_) >> mTc
		
		
		and :
			def entry = new ZipReaderEntry(Mock(ZipInputStream), "/melvin/van/peebles", false);
			
		and : 
			def parent = new TestCaseFolder()
			importer.pathMap.put("/melvin/van", parent)
			
		and : 
			
			
		
		when :
			importer.createTestCase(entry)
		
		then :
			def tc = importer.pathMap.getMappedElement("/melvin/van/peebles")
			tc instanceof TestCase
			tc.getName() == "peebles"
			
			parent.getContent() == [tc] as Set
	}
		
}
