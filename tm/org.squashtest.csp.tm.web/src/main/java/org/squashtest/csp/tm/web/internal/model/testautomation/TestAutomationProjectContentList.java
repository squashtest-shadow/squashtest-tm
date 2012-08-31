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

import java.util.Collection;
import java.util.LinkedList;


public class TestAutomationProjectContentList {

	private Collection<TAProject> projects = new LinkedList<TestAutomationProjectContentList.TAProject>();

	
	public Collection<TAProject> getProjects() {
		return projects;
	}


	public void setProjects(Collection<TAProject> projects) {
		this.projects = projects;
	}

		
	
	
	// **************** private stuffs ******************
	



	static class TAProject {

		private long projectId;
		
		private String projectName;
		
		private Collection<TANode> children = new LinkedList<TANode>();

		public long getProjectId() {
			return projectId;
		}

		public String getProjectName() {
			return projectName;
		}

		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}

		public Collection<TANode> getChildren() {
			return children;
		}

		public void setChildren(Collection<TANode> children) {
			this.children = children;
		}

		public void setProjectId(long projectId) {
			this.projectId = projectId;
		}



	}
	
	
	static class TANode {

		private String name;
		
		private boolean isFile = false;
		
		private Collection<TANode> children = new LinkedList<TANode>();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isFile() {
			return isFile;
		}

		public void setFile(boolean isFile) {
			this.isFile = isFile;
		}



		public Collection<TANode> getChildren() {
			return children;
		}

		public void setChildren(Collection<TANode> children) {
			this.children = children;
		}

		public TANode findChild(String name){
			for (TANode node : children){
				if (node.name.equals(name)){
					return node;
				}
			}
			return null;
		}
		
		
	}


	
}
