package org.squashtest.csp.tm.internal.service.importer

import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.internal.service.importer.TestCaseLibraryMerger.DestinationManager;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;

import spock.lang.Specification;



class TestCaseLibraryMergerTest extends Specification {

	/* ************ static methods ************************ */
	
	def "should collect the names of a collection of nodes"(){
		
		given :
			def nodes = []
			
			10.times{ nodes << new TestCase(name:"tc named $it") } 
		
		and :
			def expected = []
			
			10.times{ expected << "tc named $it"}
		
		when :
		
			def res = TestCaseLibraryMerger.collectNames(nodes)
			
		
		then :
			res == expected
		
		
	}
	
	def "should generate a unique name"(){
		
		given :
			def names = ["test case", "folder", "chuck testa", "chuck testa-import5"]
		
		
		when :
			def newName = TestCaseLibraryMerger.generateUniqueName(names, "chuck testa")
		
			
		then :	
			newName == "chuck testa-import6"
		
	}
	
	
	def "should find a TCLN by name"(){
		given :
			def nodes = []			
			10.times{ nodes << new TestCase(name:"tc named $it") }
		
		and : 
			def needle = new TestCase(name:"needle")
			nodes << needle
		
		when :
			def res = TestCaseLibraryMerger.getByName(nodes, "needle")
			
		
		then :	
			res == needle
		
	}
	
	
	def "should rant because the needle was not found"(){
		given :
			def nodes = []
			10.times{ nodes << new TestCase(name:"tc named $it") }
			
		when :
			TestCaseLibraryMerger.getByName(nodes, "needle")
		
		then :
			thrown RuntimeException
		
	}
	
	
	/* ***************************** DestinationManager test ********************************** */
	
	def "a destination manager should find the destination content regardless of which is the destination"(){
		given :
			def context = Mock(TestCaseLibraryMerger)
			def destManager = new DestinationManager();
			destManager.setMergingContext(context);
			
		and :
			def expectationFolder = ["folder", "result"]
			def expectationLibrary = ["library", "expected"]
			
		and :
			def folder = Mock(TestCaseFolder)	
			folder.getContent() >> expectationFolder
			
			def library = Mock(TestCaseLibrary)
			library.getRootContent() >> expectationLibrary
		
		when :
			destManager.setDestination(library)
			def resLib = destManager.getDestinationContent()
			
			destManager.setDestination(folder)
			def resFolder = destManager.getDestinationContent()
		
		then :
			resLib.containsAll(expectationLibrary)
			resFolder.containsAll(expectationFolder)
			
	}
	
	
	def "a destination manager know how to persist a test case in a folder"(){
		
		given :
		
			def context = new TestCaseLibraryMerger()
			def destManager = new DestinationManager()
			destManager.setMergingContext(context);
		
		and :
		
			def folder = Mock(TestCaseFolder)
			folder.getId() >> 5l
			
			destManager.setDestination(folder)
	
			def mtc = Mock(TestCase)
				
		and :	
			
			def service = Mock(TestCaseLibraryNavigationService)			
			context.setLibraryService(service)
			
		when :
		
			destManager.persistTestCase(mtc)
			
		then :
		
			1 * service.addTestCaseToFolder(5l, mtc)
	}
	
	
	def "a destination manager know how to persist a test case in a library"(){
		
		given :
		
			def context = new TestCaseLibraryMerger()
			def destManager = new DestinationManager()
			destManager.setMergingContext(context);
		
		and :
		
			def library = Mock(TestCaseLibrary)
			library.getId() >> 1l
			
			destManager.setDestination(library)
	
			def mtc = Mock(TestCase)
				
		and :
			
			def service = Mock(TestCaseLibraryNavigationService)
			context.setLibraryService(service)		
	
			
		when :
		
			destManager.persistTestCase(mtc)
			
		then :
		
			1 * service.addTestCaseToLibrary(1l, mtc)
	}
	
	def "a destination manager know how to persist a folder in a folder"(){
		
		given :
		
			def context = new TestCaseLibraryMerger()
			def destManager = new DestinationManager()
			destManager.setMergingContext(context);
		
		and :
		
			def folder = Mock(TestCaseFolder)
			folder.getId() >> 5l
			
			destManager.setDestination(folder)
	
			def mf = Mock(TestCaseFolder)
				
		and :
			
			def service = Mock(TestCaseLibraryNavigationService)
			context.setLibraryService(service)
			
		when :
		
			destManager.persistFolder(mf)
			
		then :
		
			1 * service.addFolderToFolder(5l, mf)
	}
	
	
	def "a destination manager know how to persist a folder in a library"(){
		
		given :
		
			def context = new TestCaseLibraryMerger()
			def destManager = new DestinationManager()
			destManager.setMergingContext(context);
		
		and :
		
			def library = Mock(TestCaseLibrary)
			library.getId() >> 1l
			
			destManager.setDestination(library)
	
			def mf = Mock(TestCaseFolder)
				
		and :
			
			def service = Mock(TestCaseLibraryNavigationService)
			context.setLibraryService(service)
	
			
		when :
		
			destManager.persistFolder(mf)
			
		then :
		
			1 * service.addFolderToLibrary(1l, mf)
	}
	
}
