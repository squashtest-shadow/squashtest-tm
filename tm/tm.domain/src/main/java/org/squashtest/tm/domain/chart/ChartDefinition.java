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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.users.User;

@Entity
@Table(name = "CHART_DEFINITION")
public class ChartDefinition {

	@Id
	@Column(name = "CHART_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_def_id_seq")
	@SequenceGenerator(name = "chart_def_id_seq", sequenceName = "chart_def_id_seq")
	private long Id;

	@NotBlank
	@Size(min = 0, max = 30)
	private String name;

	@JoinColumn(name = "USER_ID")
	@ManyToOne
	private User owner;

	@Enumerated(EnumType.STRING)
	private Visibility visibility;

	@Enumerated(EnumType.STRING)
	private ChartType type;


	@Lob
	@Type(type = "org.hibernate.type.StringClobType")
	private String description;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "CHART_FILTER", joinColumns = @JoinColumn(name = "CHART_ID") , inverseJoinColumns = @JoinColumn(name = "FILTER_ID") )
	private List<Filter> filters;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "CHART_AXIS", joinColumns = @JoinColumn(name = "CHART_ID") , inverseJoinColumns = @JoinColumn(name = "AXIS_ID") )
	@OrderColumn(name = "RANK")
	private List<AxisColumn> axis;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "CHART_MEASURE", joinColumns = @JoinColumn(name = "CHART_ID") , inverseJoinColumns = @JoinColumn(name = "MEASURE_ID") )
	@OrderColumn(name = "RANK")
	private List<MeasureColumn> measure;


}
