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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;

@Entity
public class CustomReportFolder implements TreeEntity {

	@Id
	@Column(name = "CRF_ID")
	@GeneratedValue(strategy=GenerationType.AUTO, generator="custom_report_folder_crf_id_seq")
	@SequenceGenerator(name="custom_report_folder_crf_id_seq", sequenceName="custom_report_folder_crf_id_seq")
	private Long id;
	
	@NotBlank
	@Size(min = 0, max = MAX_NAME_SIZE)
	@Column
	private String name;
	
	@Column
	@Lob
	@Type(type = "org.hibernate.type.StringClobType")
	private String description;
	
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


	public void setId(Long id) {
		this.id = id;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public TreeLibraryNode getTreeNode() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void accept(TreeEntityVisitor visitor) {
		visitor.visit(this);
	}

}
