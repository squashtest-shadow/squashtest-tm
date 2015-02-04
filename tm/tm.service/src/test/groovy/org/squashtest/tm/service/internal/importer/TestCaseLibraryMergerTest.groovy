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
package org.squashtest.tm.service.internal.importer

import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.service.internal.importer.TestCaseLibraryMerger
import org.squashtest.tm.service.internal.importer.RequirementLibraryMerger.FolderMerger
import org.squashtest.tm.service.internal.importer.TestCaseLibraryMerger.DestinationManager
import org.squashtest.tm.service.internal.importer.TestCaseLibraryMerger.FolderPair
import org.squashtest.tm.service.internal.importer.TestCaseLibraryMerger.NodeMerger
import org.squashtest.tm.service.internal.importer.TestCaseLibraryMerger.TestCaseMerger
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;

import spock.lang.Specification



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


	def "a destination manager knows how to persist a test case in a folder"(){

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

		1 * service.addTestCaseToFolder(5l, mtc, null)
	}


	def "a destination manager knows how to persist a test case in a library"(){

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

		1 * service.addTestCaseToLibrary(1l, mtc, null)
	}

	def "a destination manager knows how to persist a folder in a folder"(){

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


	def "a destination manager knows how to persist a folder in a library"(){

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


	def "a destination manager knows how to apply its configuration to another manager"(){

		given :
		def context = new TestCaseLibraryMerger()
		def destManager = new DestinationManager()
		destManager.setMergingContext(context);


		and :
		def manager1 = new DestinationManager();
		def manager2 = new DestinationManager();

		and :
		def destFolder = Mock(TestCaseFolder)
		def destLib = Mock(TestCaseLibrary)

		when :
		destManager.setDestination(destLib)
		destManager.applyConfigurationTo(manager1)

		destManager.setDestination(destFolder)
		destManager.applyConfigurationTo(manager2)

		then :
		manager1.context == context
		manager2.context == context

		manager1.destLibrary==destLib
		manager1.destFolder == null


		manager2.destLibrary==null
		manager2.destFolder == destFolder

	}

	/* ***************************** TestCaseMerger test ********************************** */

	def "a test case merger should rename a test case if the name is already taken then persist"(){

		given :
		def context = new TestCaseLibraryMerger()
		def service = Mock(TestCaseLibraryNavigationService)
		context.setLibraryService(service)

		def parentLib = Mock(TestCaseLibrary)

		parentLib.getRootContent() >> generateNodeCollection(10)
		parentLib.getId() >> 2l

		and :
		def transTC = Mock(TestCase)
		transTC.getName() >> "node 2"

		and :
		def merger = new TestCaseMerger()
		merger.context=context
		merger.destLibrary=parentLib
		merger.setTransientTestCase transTC

		when :
		merger.merge()

		then :
		1 * service.addTestCaseToLibrary(2l, transTC, null)
		1 * transTC.setName("node 2-import1")
		context.summary.renamed == 1


	}

	def "a test case merger should persist a test case without renaming it"(){

		given :
		def context = new TestCaseLibraryMerger()
		def service = Mock(TestCaseLibraryNavigationService)
		context.setLibraryService(service)

		def parentLib = Mock(TestCaseLibrary)

		parentLib.getRootContent() >> generateNodeCollection(10)
		parentLib.getId() >> 2l

		and :
		def transTC = Mock(TestCase)
		transTC.getName() >> "node 12"

		and :
		def merger = new TestCaseMerger()
		merger.context=context
		merger.destLibrary=parentLib
		merger.toMerge= transTC

		when :
		merger.merge()

		then :
		1 * service.addTestCaseToLibrary(2l, transTC, null)
		0 * transTC.setName(_)
		context.summary.renamed == 0

	}

	/* **************************** FolderMerger test ************************ */

	def "a FolderMerger should persist a whole folder right away if there are no conflicting name"(){
		given :
		def context = new TestCaseLibraryMerger()
		def service = Mock(TestCaseLibraryNavigationService)
		context.setLibraryService(service)

		and :

		def parentLib = Mock(TestCaseLibrary)

		parentLib.getRootContent() >> generateNodeCollection(10)
		parentLib.getId() >> 2l

		and :
		def transF = Mock(TestCaseFolder)
		transF.getName() >> "node 12"

		and :
		def merger = new TestCaseLibraryMerger.FolderMerger()
		merger.context=context
		merger.destLibrary=parentLib
		merger.setTransientFolder transF

		when :
		merger.merge()

		then :
		1 * service.addFolderToLibrary(2l, transF)
		0 * transF.setName(_)
		context.summary.renamed == 0

	}

	def "a FolderMerger should take specific actions if the name is already taken"(){
		given :
		def context = new TestCaseLibraryMerger()
		def service = Mock(TestCaseLibraryNavigationService)
		context.setLibraryService(service)

		and :

		def parentLib = Mock(TestCaseLibrary)

		def rootContent = generateNodeCollection(10)

		parentLib.getRootContent() >> rootContent
		parentLib.getId() >> 2l

		and :
		def transF = Mock(TestCaseFolder)
		transF.getName() >> "node 11"

		and :
		def merger = new TestCaseLibraryMerger.FolderMerger()
		merger.context=context
		merger.destLibrary=parentLib
		merger.toMerge = transF

		and :
		def conflictingNode = Mock(TestCase)
		rootContent << conflictingNode


		when :
		merger.merge()

		then :
		2 * conflictingNode.getName() >> "node 11"
		conflictingNode.accept(merger)

	}

	def "a FolderMerger should rename then persist a transient folder if the conflicting node is a test case"(){

		given :
		def context = new TestCaseLibraryMerger()
		def service = Mock(TestCaseLibraryNavigationService)
		context.setLibraryService(service)

		and :

		def parentLib = Mock(TestCaseLibrary)

		def rootContent = generateNodeCollection(10)

		parentLib.getRootContent() >> rootContent
		parentLib.getId() >> 2l

		and :
		def transF = Mock(TestCaseFolder)
		transF.getName() >> "node 5"

		and :
		def merger = new TestCaseLibraryMerger.FolderMerger()
		merger.context=context
		merger.destLibrary=parentLib
		merger.toMerge = transF


		when :
		merger.visit(Mock(TestCase))

		then :
		context.summary.renamed == 1
		1 * service.addFolderToLibrary(2l, transF)
		1 * transF.setName("node 5-import1")

	}


	def "a FolderMerger should notify the context that it should keep looping over two more folders"(){
		given :
		def context = new TestCaseLibraryMerger()
		def service = Mock(TestCaseLibraryNavigationService)
		context.setLibraryService(service)

		and :

		def parentLib = Mock(TestCaseLibrary)

		def rootContent = generateNodeCollection(10)

		parentLib.getRootContent() >> rootContent
		parentLib.getId() >> 2l

		and :
		def transF = Mock(TestCaseFolder)
		transF.getName() >> "node 5"

		and :
		def merger = new TestCaseLibraryMerger.FolderMerger()
		merger.context=context
		merger.destLibrary=parentLib
		merger.toMerge = transF

		and :
		def persistedFolder = Mock(TestCaseFolder)

		when :
		merger.visit(persistedFolder)

		then :
		context.nonTreated.size()==1
		context.nonTreated[0].dest == persistedFolder
		context.nonTreated[0].src == transF


	}

	/* *************************** NodeMerger test **************************** */

	def "a NodeMerger should create, configure and run a TestCaseMerger when visiting a test case"(){

		given :
		def context = new TestCaseLibraryMerger()
		def parentLib = Mock(TestCaseLibrary)

		def merger = new NodeMerger()
		merger.context=context;
		merger.destLibrary = parentLib

		and :
		def tcMerger = Mock(TestCaseMerger)
		merger.tcMerger=tcMerger

		and :
		def transTC = Mock(TestCase)

		when :
		merger.visit(transTC)

		then :
		1 * tcMerger.setMergingContext(context)
		1 * tcMerger.setDestination(parentLib)
		1 * tcMerger.setTransientTestCase(transTC)
		1 * tcMerger.merge()

	}

	def "a NodeMerger should create, configure and run a FolderMerger when visiting a folder"(){

		given :
		def context = new TestCaseLibraryMerger()
		def parentLib = Mock(TestCaseLibrary)

		def merger = new TestCaseLibraryMerger.NodeMerger()
		merger.context=context;
		merger.destLibrary = parentLib

		and :
		def fMerger = Mock(TestCaseLibraryMerger.FolderMerger)
		merger.fMerger=fMerger

		and :
		def transFolder= Mock(TestCaseFolder)

		when :
		merger.visit(transFolder)

		then :
		1 * fMerger.setMergingContext(context)
		1 * fMerger.setDestination(parentLib)
		1 * fMerger.setTransientFolder(transFolder)
		1 * fMerger.merge()

	}

	/* ***************** test of the main class at last *********************** */

	def "should merge things into a library"(){

		given :

		def context = new TestCaseLibraryMerger()
		def merger = Mock(NodeMerger)
		context.merger=merger

		and :
		def destLib = Mock(TestCaseLibrary)
		def srcFold = Mock(TestCaseFolder)

		srcFold.getContent() >> generateNodeCollection(10)

		and :
		def nonTreatedDest = Mock(TestCaseFolder)
		def nonTreatedSource = Mock(TestCaseFolder)

		nonTreatedSource.getContent() >> generateNodeCollection(5)

		context.nonTreated.add(new FolderPair(nonTreatedDest, nonTreatedSource))

		when :
		context.mergeIntoLibrary(destLib, srcFold)


		then :

		15 * merger.visit(_)
	}


	def "constructor with argument"(){

		given :
		def service = Mock(TestCaseLibraryNavigationService)

		when :
		def merger = new TestCaseLibraryMerger(service)

		then :
		merger.service==service

	}


	/* **************************** utilities ********************************* */

	def generateNodeCollection = { it ->
		def result=[]

		it.times{
			def node = (Math.random()> 0.5) ? new TestCase() : new TestCaseFolder()
			node.setName("node $it")
			result << node
		}

		return result
	}




}
