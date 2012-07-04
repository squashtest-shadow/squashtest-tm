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
package org.squashtest.csp.tm.domain.testcase;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.squashtest.csp.core.security.annotation.AclConstrainedObject;
import org.squashtest.csp.tm.domain.library.Library;
import org.squashtest.csp.tm.domain.project.GenericLibrary;
import org.squashtest.csp.tm.domain.project.Project;

@Entity
public class TestCaseLibrary extends GenericLibrary<TestCaseLibraryNode> implements Library<TestCaseLibraryNode> {

	private static final String CLASS_NAME = "org.squashtest.csp.tm.domain.testcase.TestCaseLibrary";
	private static final String SIMPLE_CLASS_NAME = "TestCaseLibrary";

	@Id
	@GeneratedValue
	@Column(name = "TCL_ID")
	private Long id;

	@OneToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "TEST_CASE_LIBRARY_CONTENT", joinColumns = @JoinColumn(name = "LIBRARY_ID"), inverseJoinColumns = @JoinColumn(name = "CONTENT_ID"))
	private final Set<TestCaseLibraryNode> rootContent = new HashSet<TestCaseLibraryNode>();

	@OneToOne(mappedBy = "testCaseLibrary")
	private Project project;

	@Override
	public Set<TestCaseLibraryNode> getRootContent() {
		return rootContent;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public void notifyAssociatedWithProject(Project p) {
		this.project = p;
	}

	@Override
	public void removeRootContent(TestCaseLibraryNode node) {
		rootContent.remove(node);

	}

	/* ***************************** SelfClassAware section ******************************* */

	@Override
	public String getClassSimpleName() {
		return TestCaseLibrary.SIMPLE_CLASS_NAME;
	}

	@Override
	public String getClassName() {
		return TestCaseLibrary.CLASS_NAME;
	}

	@Override
	public boolean hasContent() {
		return (rootContent.size() > 0);
	}

	

}
