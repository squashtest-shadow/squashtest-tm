/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_PARTY")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Party {
	
	private final static String TYPE = "PARTY";
	
	@Id
	@GeneratedValue
	@Column(name = "PARTY_ID")
	protected Long id;
	
	@OneToOne
	@JoinTable(name = "CORE_GROUP_MEMBER", joinColumns = @JoinColumn(name = "PARTY_ID"), inverseJoinColumns = @JoinColumn(name = "GROUP_ID"))
	private UsersGroup group;
	
	public UsersGroup getGroup() {
		return group;
	}

	public void setGroup(UsersGroup group) {
		this.group = group;
	}
	public Long getId() {
		return id;
	}

	public String getName(){
		return "";
	}
	
	public String getType(){
		return TYPE;
	}
	
	abstract void accept(PartyVisitor visitor);
	
	
}
