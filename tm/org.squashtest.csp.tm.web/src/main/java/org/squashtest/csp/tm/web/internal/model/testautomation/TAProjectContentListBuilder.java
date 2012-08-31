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
package org.squashtest.csp.tm.web.internal.model.testautomation;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.squashtest.csp.tm.web.internal.model.testautomation.TestAutomationProjectContentList.TANode;
import org.squashtest.csp.tm.web.internal.model.testautomation.TestAutomationProjectContentList.TAProject;

import squashtm.testautomation.domain.TestAutomationTest;
import squashtm.testautomation.model.TestAutomationProjectContent;

public class TAProjectContentListBuilder {

	public TestAutomationProjectContentList build(Collection<TestAutomationProjectContent> projectContents){
	
		Collection<TAProject> contentList = new LinkedList<TAProject>();
		
		for (TestAutomationProjectContent content : projectContents){
			
			TAProject newProject = build(content); 
				contentList.add(newProject);
		}
		
		TestAutomationProjectContentList list = new TestAutomationProjectContentList();
		
		list.setProjects(contentList);
		
		return list;
		
	}
	
	private TAProject build(TestAutomationProjectContent content){
		
		TAProject newProject = new TAProject();
		
		newProject.setProjectId(content.getProject().getId());
		
		newProject.setProjectName(content.getProject().getName());
		
		Collection<TANode> nodes = new LinkedList<TANode>();
		
		for (TestAutomationTest test : content.getTests()){
			mergeWith(nodes, test);
		}
		
		newProject.setChildren(nodes);
		
		return newProject;
		
	}
	
	private void mergeWith(Collection<TANode> nodes, TestAutomationTest test){
		
		String[] pathArray = test.getName().trim().split("\\/");
		List<String> path = Arrays.asList(pathArray); 
		
		if (path.isEmpty()){
			return;
		}
		
		TANode root = new TANode();
		root.setChildren(nodes);
		
		TANode parent = root;
		TANode current = null;
		
		ListIterator<String> iterator = path.listIterator();
		
		while(iterator.hasNext()){
			String name = iterator.next();
			current = parent.findChild(name);
			if (current==null){
				current = createNode(name);
				parent.getChildren().add(current);
			}
			parent = current;
		}
		
		current.setFile(true);
				
	}
	
	
	protected TANode createNode(String name){
		TANode newNode = new TANode();
		newNode.setName(name);
		return newNode;
	}
	
	
	
}
