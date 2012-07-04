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
package org.squashtest.csp.tm.domain.campaign;

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

import org.apache.commons.lang.NullArgumentException;
import org.hibernate.annotations.Where;
import org.squashtest.csp.core.security.annotation.AclConstrainedObject;
import org.squashtest.csp.tm.domain.library.Library;
import org.squashtest.csp.tm.domain.project.GenericLibrary;
import org.squashtest.csp.tm.domain.project.Project;

@Entity
public class CampaignLibrary extends GenericLibrary<CampaignLibraryNode> implements Library<CampaignLibraryNode> {

	private static final String CLASS_NAME = "org.squashtest.csp.tm.domain.campaign.CampaignLibrary";
	private static final String SIMPLE_CLASS_NAME = "CampaignLibrary";

	@Id
	@GeneratedValue
	@Column(name = "CL_ID")
	private Long id;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "CAMPAIGN_LIBRARY_CONTENT", joinColumns = @JoinColumn(name = "LIBRARY_ID"), inverseJoinColumns = @JoinColumn(name = "CONTENT_ID"))
	@Where(clause = "DELETED_ON IS NULL")
	private final Set<CampaignLibraryNode> rootContent = new HashSet<CampaignLibraryNode>();

	@OneToOne(mappedBy = "campaignLibrary")
	private Project project;

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public Set<CampaignLibraryNode> getRootContent() {
		return rootContent;
	}

	@Override
	public void removeRootContent(CampaignLibraryNode node) {
		if (node == null) {
			throw new NullArgumentException("CampaignLibrary : cannot remove null node");
		}
		rootContent.remove(node);
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public void notifyAssociatedWithProject(Project p) {
		this.project = p;
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

	
}
