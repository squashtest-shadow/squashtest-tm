/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.domain.testcase;

import java.util.Collection;
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

import org.squashtest.tm.domain.library.NodeContainerVisitor;
import org.squashtest.tm.domain.project.GenericLibrary;
import org.squashtest.tm.domain.project.GenericProject;

@Entity
public class TestCaseLibrary extends GenericLibrary<TestCaseLibraryNode> {

	private static final String CLASS_NAME = "org.squashtest.tm.domain.testcase.TestCaseLibrary";
	private static final String SIMPLE_CLASS_NAME = "TestCaseLibrary";

	@Id
	@GeneratedValue
	@Column(name = "TCL_ID")
	private Long id;

	@OneToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "TEST_CASE_LIBRARY_CONTENT", joinColumns = @JoinColumn(name = "LIBRARY_ID"), inverseJoinColumns = @JoinColumn(name = "CONTENT_ID"))
	private final Set<TestCaseLibraryNode> rootContent = new HashSet<TestCaseLibraryNode>();

	@OneToOne(mappedBy = "testCaseLibrary")
	private GenericProject project;
		
	@OneToMany(cascade = { CascadeType.ALL })
	private Set<TestCaseLibraryPluginBinding> enabledPlugins = new HashSet<TestCaseLibraryPluginBinding>(5);

	public Set<TestCaseLibraryNode> getRootContent() {
		return rootContent;
	}
	@Override
	public Set<TestCaseLibraryNode> getContent(){
		return getRootContent();
	}
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public GenericProject getProject() {
		return project;
	}

	@Override
	public void notifyAssociatedWithProject(GenericProject p) {
		this.project = p;
	}

	@Override
	public void removeContent(TestCaseLibraryNode node) {
		rootContent.remove(node);

	}
	
	// ***************************** PluginReferencer section ****************************
	
	@Override
	public Set<String> getEnabledPlugins() {
		Set<String> pluginIds = new HashSet<String>(enabledPlugins.size());
		for (TestCaseLibraryPluginBinding binding : enabledPlugins){
			pluginIds.add(binding.getPluginId());
		}
		return pluginIds;
	}

	
	@Override
	public Set<TestCaseLibraryPluginBinding> getAllPluginBindings() {
		return enabledPlugins;
	}	
	
	@Override
	public void enablePlugin(String pluginId) {
		if (! isPluginEnabled(pluginId)){
			TestCaseLibraryPluginBinding newBinding = new TestCaseLibraryPluginBinding(pluginId);
			enabledPlugins.add(newBinding);
		}
	}
	
	@Override
	public void disablePlugin(String pluginId) {
		TestCaseLibraryPluginBinding binding = getPluginBinding(pluginId);
		if (binding != null){
			enabledPlugins.remove(binding);
		}
	}
	
	@Override
	public TestCaseLibraryPluginBinding getPluginBinding(String pluginId) {
		for (TestCaseLibraryPluginBinding binding : enabledPlugins){
			if (binding.getPluginId().equals(pluginId)){
				return binding;
			}
		}
		return null;
	}
	
	@Override
	public boolean isPluginEnabled(String pluginId) {
		return (getPluginBinding(pluginId) != null);
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
	
	@Override
	public void accept(NodeContainerVisitor visitor) {
		visitor.visit(this);
		
	}
	@Override
	public Collection<TestCaseLibraryNode> getOrderedContent() {
		return rootContent;
	}

}
