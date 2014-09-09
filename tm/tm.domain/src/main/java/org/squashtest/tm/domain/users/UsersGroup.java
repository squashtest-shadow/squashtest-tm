/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.domain.users;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_GROUP")
public class UsersGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "core_group_id_seq")
	@SequenceGenerator(name = "core_group_id_seq", sequenceName = "core_group_id_seq")
	private Long id;
	private String qualifiedName;
	private transient String simpleName;

	public UsersGroup() {
		if (qualifiedName != null) {
			this.calculateSimpleName();
		}
	}

	public String getSimpleName() {
		this.calculateSimpleName();
		return simpleName;
	}

	public void calculateSimpleName() {
		String theName = qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
		this.simpleName = theName;
	}

	public UsersGroup(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public String getQualifiedName() {
		return qualifiedName;

	}

	public void setQualifiedName(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public Long getId() {
		return id;
	}

}
