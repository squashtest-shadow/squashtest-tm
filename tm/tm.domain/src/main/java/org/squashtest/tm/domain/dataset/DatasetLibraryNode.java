/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
package org.squashtest.tm.domain.dataset;

import org.hibernate.annotations.*;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.customreport.*;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.tree.*;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * @author aguilhem
 */

@Entity
@Table(name = "DATASET_LIBRARY_NODE")
public class DatasetLibraryNode implements TreeLibraryNode {

	private static final String DLN_ID = "DLN_ID";

	@Id
	@Column(name = DLN_ID)
	@GeneratedValue(strategy=GenerationType.AUTO, generator="dataset_library_node_dln_id_seq")
	@SequenceGenerator(name="dataset_library_node_dln_id_seq", sequenceName="dataset_library_node_dln_id_seq", allocationSize = 1)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(insertable=false, updatable=false, name="ENTITY_TYPE")
	private DatasetTreeDefinition entityType;

	@Column(insertable=false, updatable=false, name="ENTITY_ID")
	private Long entityId;

	/**
	 * To prevent no named entity as we have in {@link Requirement} / {@link RequirementVersion}
	 * path hell, we decided to denormalize the name.
	 * So the entity name and node name should be the same, take care if you rename directly one of them !
	 * Use {@link CustomReportLibraryNode#renameNode(String)} method which take care of all constraints relative to node name.
	 */
	@Column
	@Size(max = Sizes.NAME_MAX)
	private String name;

	@JoinTable(name="DLN_RELATIONSHIP",
		joinColumns={@JoinColumn(name="DESCENDANT_ID", referencedColumnName= DLN_ID, insertable=false, updatable=false)},
		inverseJoinColumns={@JoinColumn(name="ANCESTOR_ID", referencedColumnName= DLN_ID, insertable=false, updatable=false)})
	@ManyToOne(fetch = FetchType.LAZY,targetEntity=DatasetLibraryNode.class)
	private TreeLibraryNode parent;

	@JoinTable(name="DLN_RELATIONSHIP",
		joinColumns={@JoinColumn(name="ANCESTOR_ID", referencedColumnName= DLN_ID)},
		inverseJoinColumns={@JoinColumn(name="DESCENDANT_ID", referencedColumnName= DLN_ID)})
	@OneToMany(cascade={ CascadeType.ALL },fetch = FetchType.LAZY,
		targetEntity=DatasetLibraryNode.class)
	@IndexColumn(name="CONTENT_ORDER")
	private List<TreeLibraryNode> children = new ArrayList<>();

	//for the @MetaValue we cannot use the Tree Entity Definition
	//as value must be a constant so constant names are in an interface
	@Any( metaColumn = @Column( name = "ENTITY_TYPE" ), fetch=FetchType.LAZY)
	@AnyMetaDef(
		idType = "long",
		metaType = "string",
		metaValues = {
			@MetaValue( value = DatasetNodeType.DATASET_NAME, targetEntity = GlobalDataset.class ),
			@MetaValue( value = DatasetNodeType.COMPOSITE_NAME, targetEntity = CompositeDataset.class ),
			@MetaValue( value = DatasetNodeType.FOLDER_NAME, targetEntity = DatasetFolder.class ),
			@MetaValue( value = DatasetNodeType.LIBRARY_NAME, targetEntity = DatasetLibrary.class ),
			@MetaValue( value = DatasetNodeType.TEMPLATE_NAME, targetEntity = DatasetTemplate.class )
		})
	@JoinColumn( name = "ENTITY_ID" )
	@Cascade(value=org.hibernate.annotations.CascadeType.ALL)
	private TreeEntity entity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DL_ID")
	private DatasetLibrary library;

	public DatasetLibraryNode() {
		super();
	}

	public DatasetLibraryNode(DatasetTreeDefinition entityType,
								   Long entityId, String name, DatasetLibrary library) {
		super();
		this.entityType = entityType;
		this.entityId = entityId;
		this.name = name;
		this.library = library;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public long getEntityId() {
		return entityId;
	}

	@Override
	public void setEntityType(TreeEntityDefinition entityType) {
		this.entityType = (DatasetTreeDefinition) entityType;
	}

	@Override
	public TreeEntity getEntity() {
		return entity;
	}

	@Override
	public void setEntity(TreeEntity treeEntity) {
		this.entity = treeEntity;
	}

	@Override
	public TreeEntityDefinition getEntityType() {
		return entityType;
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
	public GenericTreeLibrary getLibrary() {
		return library;
	}

	/**
	 * concrete class getter for @AclConstrainedObject
	 * @return
	 */
	@AclConstrainedObject
	public DatasetLibrary getDatasetLibrary() {
		return library;
	}

	public void setLibrary(DatasetLibrary library) {
		this.library = library;
	}

	@Override
	public void addChild(TreeLibraryNode treeLibraryNode) throws UnsupportedOperationException, IllegalArgumentException, NameAlreadyInUseException {
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

		if(this.childNameAlreadyUsed(newChildName)){
			TreeLibraryNode node = getContentNodeByName(newChildName);
			throw new DuplicateNameException(node.getEntityType().getTypeName(), newChildName);
		}
		this.getChildren().add(treeLibraryNode);
	}

	@Override
	public void removeChild(TreeLibraryNode treeLibraryNode) {
		children.remove(treeLibraryNode);
		//forcing hibernate to clean it's children list,
		//without that clean, suppression can fail because hibernate do not update correctly the RELATIONSHIP table
		//so the triggers fails to update CLOSURE table and the whole suppression fail on integrity violation constraint...
		children = new ArrayList<>(children);
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

	public boolean childNameAlreadyUsed(String newChildName) {
		for (TreeLibraryNode child : children) {
			if (child.getName().equals(newChildName)) {
				return true;
			}
		}
		return false;
	}

	private TreeLibraryNode getContentNodeByName (String name){
		for (TreeLibraryNode child : children) {
			if (child.getName().equals(name)) {
				return child;
			}
		}
		return null;
	}

	@Override
	public boolean hasContent() {
		if (!getEntityType().isContainer()) {
			return false;
		}
		return !children.isEmpty();
	}

	@Override
	public void renameNode(String newName) {
		if (getEntityType().equals(DatasetTreeDefinition.LIBRARY)) {
			throw new IllegalArgumentException("Cannot rename a library, rename the project instead");
		}
		if(nameAlreadyUsedBySibling(newName)){
			throw new DuplicateNameException(newName,this.getEntityType().getTypeName());
		}
		else {
			setName(newName);
			getEntity().setName(newName);
		}
	}

	private boolean nameAlreadyUsedBySibling(String newName) {
		List<String> siblingsNames = getSiblingsNames();
		return siblingsNames.contains(newName);
	}

	private List<String> getSiblingsNames() {
		List<TreeLibraryNode> siblings = getSiblings();
		List<String> siblingNames = new ArrayList<>();
		for (TreeLibraryNode sibling : siblings) {
			siblingNames.add(sibling.getName());
		}
		return siblingNames;
	}

	private List<TreeLibraryNode> getSiblings() {
		TreeLibraryNode parentNode = getParent();
		return parentNode.getChildren();
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void accept(TreeNodeVisitor visitor) {
		throw new UnsupportedOperationException("NO IMPLEMENTATION... YET...");
	}
}
