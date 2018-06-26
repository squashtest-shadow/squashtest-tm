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

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.customreport.CustomReportTreeEntityVisitor;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.tree.TreeEntity;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * @author aguilhem
 */
@Entity
public class DatasetFolder implements TreeEntity<DatasetTreeEntityVisitor> {

	@Id
	@Column(name = "DF_ID")
	@GeneratedValue(strategy=GenerationType.AUTO, generator="dataset_folder_df_id_seq")
	@SequenceGenerator(name="dataset_folder_df_id_seq", sequenceName="dataset_folder_df_id_seq", allocationSize = 1)
	private Long id;

	@NotBlank
	@Size(max = Sizes.NAME_MAX)
	@Column
	private String name;

	@Column
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String description;

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROJECT_ID")
	private Project project;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void accept(DatasetTreeEntityVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public void setProject(Project project) {
		this.project = project;
	}

	@Override
	public TreeEntity createCopy() {
		DatasetFolder copy = new DatasetFolder();
		copy.setName(this.getName());
		copy.setDescription(this.getDescription());
		copy.setProject(this.getProject());
		return copy;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
