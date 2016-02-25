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
package org.squashtest.tm.domain.requirement;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Persister;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.resource.Resource;
import org.squashtest.tm.infrastructure.hibernate.ReadOnlyCollectionPersister;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Auditable
@Indexed
public abstract class RequirementLibraryNode<RESOURCE extends Resource> implements LibraryNode {
	@Id
	@Column(name = "RLN_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "requirement_library_node_rln_id_seq")
	@SequenceGenerator(name = "requirement_library_node_rln_id_seq", sequenceName = "requirement_library_node_rln_id_seq")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "PROJECT_ID")
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Project project;

	@Override
	public Project getProject() {
		return project;
	}
	
	
	/**
	 * <p>This is not a business attribute and should not be used in services. This mapping 
	 * exists solely to make hql queries on it. It allows for fast retrieval of the 
	 * name (of a folder, or of the newest version of a requirement).</p> 
	 * 
	 *	<p>Technical note : although the mapping is one to one is amusing to see that 
	 * a persister usually meant for collections (ie one to many) works fine nonetheless </p>
	 */
/*	@OneToOne(fetch=FetchType.LAZY)
	@JoinTable(name="RLN_RESOURCE",
	joinColumns=@JoinColumn(name="RLN_ID", insertable=false, updatable=false ),
	inverseJoinColumns = @JoinColumn(name="RES_ID"))
	@EntityPer
	@Immutable
	private Resource mainResource;
	
	
	public Resource getMainResource(){
		return mainResource;
	}*/
	
	/**
	 * This basically corresponds to :
	 * <ul>
	 * 	<li>if this node is a folder -&gt; return resource.name</li>
	 * 	<li>if this node is a requirement -&gt; return the name of its latest version</li>
	 *  </ul>
	 * 
	 * and waiting for the day we can end this bullshit
	 * @return
	 */


	/**
	 * Notifies this object it is now a resource of the given project.
	 *
	 * @param project
	 */
	@Override
	public void notifyAssociatedWithProject(Project project) {
		this.project = project;

	}

	public RequirementLibraryNode() {
		super();
	}

	public RequirementLibraryNode(String name, String description) {
		setName(name);
		setDescription(description);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	@AclConstrainedObject
	public Library<?> getLibrary() {
		return getProject().getRequirementLibrary();
	}

	@Override
	public AttachmentList getAttachmentList() {
		return getResource().getAttachmentList();
	}

	/**
	 * Implementors should ask the visitor to visit this object.
	 *
	 * @param visitor
	 */
	public abstract void accept(RequirementLibraryNodeVisitor visitor);

	public abstract RESOURCE getResource();
}
