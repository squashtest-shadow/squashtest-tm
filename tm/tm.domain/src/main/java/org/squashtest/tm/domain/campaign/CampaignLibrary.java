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
package org.squashtest.tm.domain.campaign;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.lang.NullArgumentException;
import org.squashtest.tm.domain.library.NodeContainerVisitor;
import org.squashtest.tm.domain.project.GenericLibrary;
import org.squashtest.tm.domain.project.GenericProject;

@Entity
public class CampaignLibrary extends GenericLibrary<CampaignLibraryNode> {

	private static final String CLASS_NAME = "org.squashtest.tm.domain.campaign.CampaignLibrary";
	private static final String SIMPLE_CLASS_NAME = "CampaignLibrary";

	@Id
	@GeneratedValue
	@Column(name = "CL_ID")
	private Long id;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "CAMPAIGN_LIBRARY_CONTENT", joinColumns = @JoinColumn(name = "LIBRARY_ID"), inverseJoinColumns = @JoinColumn(name = "CONTENT_ID"))
	private final Set<CampaignLibraryNode> rootContent = new HashSet<CampaignLibraryNode>();

	@OneToOne(mappedBy = "campaignLibrary")
	private GenericProject project;
	
	@ElementCollection
	@CollectionTable(name = "CAMPAIGN_LIBRARY_PLUGINS", joinColumns = @JoinColumn(name = "LIBRARY_ID"))
	@Column(name = "PLUGIN_ID")
	private Set<String> enabledPlugins = new HashSet<String>(5);
	
	
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Long getId() {
		return id;
	}

	public Set<CampaignLibraryNode> getRootContent() {
		return rootContent;
	}
	
	@Override
	public Set<CampaignLibraryNode> getContent(){
		return getRootContent();
	}

	@Override
	public void removeContent(CampaignLibraryNode node) {
		if (node == null) {
			throw new NullArgumentException("CampaignLibrary : cannot remove null node");
		}
		rootContent.remove(node);
	}

	@Override
	public GenericProject getProject() {
		return project;
	}

	@Override
	public void notifyAssociatedWithProject(GenericProject p) {
		this.project = p;
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
		return CampaignLibrary.SIMPLE_CLASS_NAME;
	}

	@Override
	public String getClassName() {
		return CampaignLibrary.CLASS_NAME;
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
	public Collection<CampaignLibraryNode> getOrderedContent() {
		return rootContent;
	}
}
