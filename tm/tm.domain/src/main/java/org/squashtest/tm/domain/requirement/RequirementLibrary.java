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
package org.squashtest.tm.domain.requirement;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.squashtest.tm.domain.library.NodeContainerVisitor;
import org.squashtest.tm.domain.project.GenericLibrary;
import org.squashtest.tm.domain.project.GenericProject;

@SuppressWarnings("rawtypes")
@Entity
public class RequirementLibrary extends GenericLibrary<RequirementLibraryNode>  {

	private static final String CLASS_NAME = "org.squashtest.tm.domain.requirement.RequirementLibrary";
	private static final String SIMPLE_CLASS_NAME = "RequirementLibrary";

	@Id
	@GeneratedValue
	@Column(name = "RL_ID")
	private Long id;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "REQUIREMENT_LIBRARY_CONTENT", joinColumns = @JoinColumn(name = "LIBRARY_ID"), inverseJoinColumns = @JoinColumn(name = "CONTENT_ID"))
	private final Set<RequirementLibraryNode> rootContent = new HashSet<RequirementLibraryNode>();

	@OneToOne(mappedBy = "requirementLibrary")
	private GenericProject project;	
	
	@ElementCollection
	@CollectionTable(name = "REQUIREMENT_LIBRARY_PLUGINS", joinColumns = @JoinColumn(name = "LIBRARY_ID"))
	@Enumerated(EnumType.STRING)
	@Column(name = "PLUGIN_ID")
	private Set<String> enabledPlugins = new HashSet<String>(5);
	
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public Set<RequirementLibraryNode> getContent() {
		return rootContent;

	}
	
	public Set<RequirementLibraryNode> getRootContent() {
		return rootContent;

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
	public void removeContent(RequirementLibraryNode node) {
		rootContent.remove(node);

	}
	
	
	// ***************************** PluginReferencer section ****************************
	
	@Override
	public Set<String> getEnabledPlugins() {
		return enabledPlugins;
	}
	
	@Override
	public void enablePlugin(String pluginId) {
		enabledPlugins.add(pluginId);
	}
	
	@Override
	public void disablePlugin(String pluginId) {
		enabledPlugins.remove(pluginId);
	}
	
	@Override
	public boolean isPluginEnabled(String pluginId) {
		return (enabledPlugins.contains(pluginId));
	}

	

	/* ***************************** SelfClassAware section ******************************* */

	@Override
	public String getClassSimpleName() {
		return RequirementLibrary.SIMPLE_CLASS_NAME;
	}

	@Override
	public String getClassName() {
		return RequirementLibrary.CLASS_NAME;
	}

	@Override
	public boolean hasContent() {
		return (rootContent.size() > 0);
	}
	
	@Override
	public void accept(NodeContainerVisitor visitor) {
		visitor.visit(this);
		
	}

}
