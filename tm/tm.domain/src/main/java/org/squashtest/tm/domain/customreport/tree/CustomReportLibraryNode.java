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
package org.squashtest.tm.domain.customreport.tree;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;

import org.squashtest.tm.domain.tree.TreeEntityDefinition;
import org.squashtest.tm.domain.tree.TreeLibrary;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.domain.tree.TreeNodeVisitor;

@Entity
public class CustomReportLibraryNode implements TreeLibraryNode {

	@Id
	@Column(name = "CRLN_ID")
	@GeneratedValue(strategy=GenerationType.AUTO, generator="custom_rpport_library_node_crln_id_seq")
	@SequenceGenerator(name="custom_rpport_library_node_crln_id_seq", sequenceName="custom_rpport_library_node_crln_id_seq")
	private Long id;
	
	@Enumerated(EnumType.STRING)
	@Column
	private CustomReportTreeDefinition entityType;
	
	@Column
	private Long entityId;
	
	@Column
	private String name;
	
	@JoinTable(name="CRLN_RELATIONSHIP", 
			joinColumns={@JoinColumn(name="DESCENDANT_ID", referencedColumnName="CRLN_ID")},
			inverseJoinColumns={@JoinColumn(name="ANCESTOR_ID", referencedColumnName="CRLN_ID")})
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY,targetEntity=CustomReportLibraryNode.class)
	private TreeLibraryNode parent;
	
//	@JoinTable(name="CRLN_RELATIONSHIP", 
//			joinColumns={@JoinColumn(name="ANCESTOR_ID", referencedColumnName="CRLN_ID")},
//			inverseJoinColumns={@JoinColumn(name="DESCENDANT_ID", referencedColumnName="CRLN_ID")})
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY,
			targetEntity=CustomReportLibraryNode.class,
			mappedBy = "parent")
	@OrderColumn(name = "CONTENT_ORDER")
	private List<TreeLibraryNode> children;
	
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, 
			targetEntity=CustomReportLibrary.class)
	@JoinColumn(name = "CRL_ID")
	private TreeLibrary library;
	
	public CustomReportLibraryNode() {
		super();
	}
	

	public CustomReportLibraryNode(CustomReportTreeDefinition entityType,
			Long entityId, String name, TreeLibrary library) {
		super();
		this.entityType = entityType;
		this.entityId = entityId;
		this.name = name;
		this.library = library;
	}


	@Override
	public Long getId() {
		return id;
	}

	public TreeLibraryNode getParent() {
		return parent;
	}

	@Override
	public List<TreeLibraryNode> getChildrens() {
		return children;
	}

	@Override
	public TreeLibrary getLibrary() {
		return library;
	}

	public void setLibrary(TreeLibrary library) {
		this.library = library;
	}
	
	@Override
	public void accept(TreeNodeVisitor visitor) {
		throw new UnsupportedOperationException("NO IMPLEMENTATION... YET...");
	}
	
	@Override
	public long getEntityId() {
		throw new UnsupportedOperationException("NO IMPLEMENTATION... YET...");
	}

	@Override
	public String getEntityName() {
		return name;
	}

	@Override
	public TreeEntityDefinition getEntityType() {
		throw new UnsupportedOperationException("NO IMPLEMENTATION... YET...");
	}
}
