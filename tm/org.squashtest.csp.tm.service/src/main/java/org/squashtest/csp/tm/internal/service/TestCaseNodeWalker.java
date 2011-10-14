package org.squashtest.csp.tm.internal.service;

import java.util.LinkedList;
import java.util.List;

import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNodeVisitor;


/**
 * 
 *  That class will descend a TestCaseLibraryNode hierarchy and add them in a particular order.
 * 
 *  Currently : will walk depth-first and wont add duplicate entries.
 *  
 *  NOT THREAD SAFE. Get a new instance everytime you need a walk.
 */

/*
 * Todo : 
 * 		1) define Folder and LibraryNode as visitable so that we can turn that class into generic.
 * 		2) let the user choose the walking and adding strategy if need be some day.
 */
public class TestCaseNodeWalker implements TestCaseLibraryNodeVisitor{


	List<TestCase> outputList ;
			
	public TestCaseNodeWalker(){
		outputList = new LinkedList<TestCase>();
	}
	
	public List<TestCase> walk(List<TestCaseLibraryNode> inputList){
		
		for (TestCaseLibraryNode node : inputList){
			node.accept(this);
		}
		
		return outputList;
	}

	
	@Override
	public void visit(TestCase testCase){
		if (! outputList.contains(testCase)){
			outputList.add(testCase);
		}
	}
	
	@Override
	public void visit(TestCaseFolder testCaseFolder){
		for (TestCaseLibraryNode node : testCaseFolder.getContent()){
			node.accept(this);
		}
	}

	
}
