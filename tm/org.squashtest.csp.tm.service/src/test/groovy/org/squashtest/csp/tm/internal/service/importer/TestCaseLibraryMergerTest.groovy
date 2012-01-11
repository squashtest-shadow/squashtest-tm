package org.squashtest.csp.tm.internal.service.importer

import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;

import spock.lang.Specification;



class TestCaseLibraryMergerTest extends Specification {

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
	
	
	def "a merger should find the destination content regardless of which is the destination"(){
		given :
			def superMerger = Mock(TestCaseLibraryMerger)
			def subMerger = new TestCaseLibraryMerger.Merger(superMerger)
			
		and :
			def expectationFolder = ["folder", "result"]
			def expectationLibrary = ["library", "expected"]
			
		and :
			def folder = Mock(TestCaseFolder)	
			folder.getContent() >> expectationFolder
			
			def library = Mock(TestCaseLibrary)
			library.getRootContent() >> expectationLibrary
		
		when :
			subMerger.setDestination(library)
			def resLib = subMerger.getDestinationContent()
			
			subMerger.setDestination(folder)
			def resFolder = subMerger.getDestinationContent()
		
		then :
			resLib.containsAll(expectationLibrary)
			resFolder.containsAll(expectationFolder)
			
	}
	
	
	def "a merger should persist a test case in the correct destination"(){
		
		given :
			def superMerger = new TestCaseLibraryMerger()
			def subMerger = new TestCaseLibraryMerger.Merger(superMerger)
		
		and :
		
			def folder = Mock(TestCaseFolder)
			folder.getId() >> 5l
			
			subMerger.setDestination(folder)
	
			def mtc = Mock(TestCase)
				
		and :	
			
			def service = Mock(TestCaseLibraryNavigationService)
			
			superMerger.setLibraryService(service)
			
	
			
		when :
			subMerger.persistTestCase(mtc)
			
		then :
			1 * service.addTestCaseToFolder(5l, mtc)
			
		
	}
	
}
