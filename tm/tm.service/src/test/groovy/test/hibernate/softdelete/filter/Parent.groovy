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
package test.hibernate.softdelete.filter


import java.util.ArrayList;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterJoinTable;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Where;
import org.hibernate.type.CalendarType;

// filter def required, can be defined in some package-info
@FilterDef(name="deletedEntity", defaultCondition="deleted = 'false'")

@Entity
class Parent {
	@Id @GeneratedValue
	Long id;
	
	@OneToMany(cascade=[CascadeType.PERSIST])
	@JoinTable(name="parent_child", joinColumns=@JoinColumn(name="parent_id"), inverseJoinColumns=@JoinColumn(name="child_id"))
	// the filter shoul be applied to this collection's elements
	@Filter(name="deletedEntity")
	List<Child> children = new ArrayList<Child>()
	
	boolean deleted;
}
