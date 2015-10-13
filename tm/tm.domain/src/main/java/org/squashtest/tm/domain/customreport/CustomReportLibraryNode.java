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
package org.squashtest.tm.domain.customreport;

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
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.MetaValue;
import org.hibernate.annotations.Table;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeEntityDefinition;
import org.squashtest.tm.domain.tree.TreeLibrary;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.domain.tree.TreeNodeVisitor;
import org.squashtest.tm.exception.NameAlreadyInUseException;

@Entity
@Table(appliesTo="CUSTOM_REPORT_LIBRARY_NODE")
public class CustomReportLibraryNode  implements TreeLibraryNode {
	

	@Id
	@Column(name = "CRLN_ID")
	@GeneratedValue(strategy=GenerationType.AUTO, generator="custom_report_library_node_crln_id_seq")
	@SequenceGenerator(name="custom_report_library_node_crln_id_seq", sequenceName="custom_report_library_node_crln_id_seq")
	private Long id;
	
	@Enumerated(EnumType.STRING)
	@Column(insertable=false, updatable=false)
	private CustomReportTreeDefinition entityType;
	
	@Column(insertable=false, updatable=false)
	private Long entityId;
	
	/**
	 * To prevent no named entity as we have in {@link Requirement} / {@link RequirementVersion}
	 * path hell, we decided to denormalize the name. 
	 * So the entity name and node name should be the same, take care if you rename one of them !
	 */
	@Column
	private String name;
	
	@JoinTable(name="CRLN_RELATIONSHIP",
			joinColumns={@JoinColumn(name="DESCENDANT_ID", referencedColumnName="CRLN_ID", insertable=false, updatable=false)},
			inverseJoinColumns={@JoinColumn(name="ANCESTOR_ID", referencedColumnName="CRLN_ID", insertable=false, updatable=false)})
	@ManyToOne(fetch = FetchType.LAZY,targetEntity=CustomReportLibraryNode.class)
	private TreeLibraryNode parent;
	
	@JoinTable(name="CRLN_RELATIONSHIP",
			joinColumns={@JoinColumn(name="ANCESTOR_ID", referencedColumnName="CRLN_ID")},
			inverseJoinColumns={@JoinColumn(name="DESCENDANT_ID", referencedColumnName="CRLN_ID")})
	@OneToMany(fetch = FetchType.LAZY,
			targetEntity=CustomReportLibraryNode.class)
	@IndexColumn(name="CONTENT_ORDER")
	private List<TreeLibraryNode> children;
	
	//for the @MetaValue we cannot use the Tree Entity Definition
	//as value must be a constant so constant names are in an interface
	@Any( metaColumn = @Column( name = "ENTITY_TYPE" ), fetch=FetchType.LAZY)
	@AnyMetaDef( 
	    idType = "long", 
	    metaType = "string", 
	    metaValues = {
	        @MetaValue( value = CustomReportNodeType.CHART_NAME, targetEntity = ChartDefinition.class ),
	        @MetaValue( value = CustomReportNodeType.FOLDER_NAME, targetEntity = CustomReportFolder.class ),
	        @MetaValue( value = CustomReportNodeType.LIBRARY_NAME, targetEntity = CustomReportLibrary.class )
	    })
	@JoinColumn( name = "ENTITY_ID" )
	@Cascade(value=org.hibernate.annotations.CascadeType.ALL)
	private TreeEntity entity;
	
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

	@Override
	public TreeLibraryNode getParent() {
		return parent;
	}
	
	@Override
	public void setParent(TreeLibraryNode parent) {
		this.parent = parent;
	}
	
	@Override
	public List<TreeLibraryNode> getChildren() {
		return children;
	}

	@Override
	public TreeLibrary getLibrary() {
		return library;
	}

	@Override
	public void setLibrary(TreeLibrary library) {
		this.library = library;
	}
	
	@Override
	public void accept(TreeNodeVisitor visitor) {
		throw new UnsupportedOperationException("NO IMPLEMENTATION... YET...");
	}
	
	@Override
	public long getEntityId() {
		return entityId;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name=name;
	}

	@Override
	public TreeEntityDefinition getEntityType() {
		return entityType;
	}

	/**
	 * See private attribute entity in this class.
	 */
	@Override
	public TreeEntity getEntity() {
		return entity;
	}

	@Override
	public void setEntity(TreeEntity treeEntity) {
		this.entity = treeEntity;
	}

	@Override
	public void addChild(TreeLibraryNode treeLibraryNode) {
		if (treeLibraryNode == null) {
			throw new IllegalArgumentException("Cannot add a null child to a library node");
		}
		if (treeLibraryNode.getEntity()==null) {
			throw new IllegalArgumentException("Cannot add a library node representing a null entity");
		}
		if (!this.getEntityType().isContainer()) {
			throw new UnsupportedOperationException("This type of library node doesn't accept childs");
		}

		treeLibraryNode.isCoherentWithEntity();
		
		String newChildName = treeLibraryNode.getName();
		
		if(this.nameAlreadyUsed(treeLibraryNode.getName())){
			throw new NameAlreadyInUseException(newChildName,this.getEntityType().getTypeName());
		}
		this.getChildren().add(treeLibraryNode);
	}

	@Override
	public void isCoherentWithEntity() {
		String nodeName = getName();
		String entityName = getEntity().getName();
		if (!nodeName.equals(entityName)) {
			String message = "Cannot add a library node of with name %s to represent an entity with diffrent name %s";
			throw new IllegalArgumentException(String.format(message, nodeName, entityName));
		}
	}

	private boolean nameAlreadyUsed(String newChildName) {
		for (TreeLibraryNode child : children) {
			if (child.getName().equals(newChildName)) {
				return true;
			}
		}
		return false;
	}

}
