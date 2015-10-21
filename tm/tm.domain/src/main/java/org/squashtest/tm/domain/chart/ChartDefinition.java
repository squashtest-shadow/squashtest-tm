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
package org.squashtest.tm.domain.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.customreport.TreeEntityVisitor;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.domain.users.User;

@Entity
@Table(name = "CHART_DEFINITION")
public class ChartDefinition implements TreeEntity{

	@Id
	@Column(name = "CHART_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_definition_chart_id_seq")
	@SequenceGenerator(name = "chart_definition_chart_id_seq", sequenceName = "chart_definition_chart_id_seq")
	private long id;

	@NotBlank
	@Size(min = 0, max = MAX_NAME_SIZE)
	private String name;

	@JoinColumn(name = "USER_ID")
	@ManyToOne
	private User owner;

	@Enumerated(EnumType.STRING)
	private Visibility visibility;

	@Enumerated(EnumType.STRING)
	@Column(name = "CHART_TYPE")
	private ChartType type;

	@Lob
	@Type(type = "org.hibernate.type.StringClobType")
	private String description;


	@ElementCollection
	@CollectionTable(name = "CHART_SCOPE", joinColumns = @JoinColumn(name = "CHART_ID") )
	@AttributeOverrides({
		@AttributeOverride(name = "type", column = @Column(name = "ENTITY_REFERENCE_TYPE") ),
		@AttributeOverride(name = "id", column = @Column(name = "ENTITY_REFERENCE_ID") )
	})
	private List<EntityReference> scope = new ArrayList<>();


	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "QUERY_ID", nullable = false)
	private ChartQuery query = new ChartQuery();



	public User getOwner() {
		return owner;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public ChartType getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public List<EntityReference> getScope() {
		return scope;
	}

	public List<Filter> getFilters() {
		return query.getFilters();
	}

	public List<AxisColumn> getAxis() {
		return query.getAxis();
	}

	public List<MeasureColumn> getMeasures() {
		return query.getMeasures();
	}

	public ChartQuery getQuery(){
		return query;
	}


	/**
	 * Returns which entities are covered by this chart, sorted by roles
	 *
	 * @return
	 */
	public Map<ColumnRole, Set<EntityType>> getInvolvedEntities(){
		return query.getInvolvedEntities();
	}


	@Override
	public Long getId() {
		return id;
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
	public TreeLibraryNode getTreeNode() {
		throw new UnsupportedOperationException("TO IMPLEMENT");
	}
	@Override
	public void accept(TreeEntityVisitor visitor) {
		visitor.visit(this);
	}

	public void setOwner(User user) {
		this.owner = user;

	}
}
