/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Identified;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Created by jlor on 09/05/2017.
 */
@Entity
public class RequirementVersionLinkType implements Identified {

	@Id
	@Column(name = "REQUIREMENT_VERSION_LINK_TYPE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "requirement_version_link_type_link_type_id_seq")
	@SequenceGenerator(name = "requirement_version_link_type_link_type_id_seq", sequenceName = "requirement_version_link_type_link_type_id_seq", allocationSize = 1)
	private Long id;

	@Column(name = "ROLE_1")
	@Size(max = 50)
	@NotBlank
	private String role1 = "";

	@Column(name = "ROLE_2")
	@Size(max = 50)
	@NotBlank
	private String role2 = "";

	@Column(name="IS_DEFAULT")
	private boolean isDefault;

	public RequirementVersionLinkType() {
		this.id = 1l;
	}

	@Override
	public Long getId() {
		return id;
	}

	public String getRole1() {
		return role1;
	}

	public String getRole2() {
		return role2;
	}

	public void setRole1(String role1) {
		this.role1 = role1;
	}

	public void setRole2(String role2) {
		this.role2 = role2;
	}
}