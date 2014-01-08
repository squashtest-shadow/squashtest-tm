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
package org.squashtest.tm.domain.project;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;


/**
 * Abstract entity representing a parameterized association between a library and a plugin. The model for this entity 
 * is definitely not satisfactory : for instance it should own the reference to the bound plugin among others. The 
 * defect in the model are mostly motivated by technical constraints.
 * 
 * @author bsiri
 *
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class LibraryPluginBinding  {

	@Id
	@GeneratedValue
	@Column(name = "PLUGIN_BINDING_ID")
	private long id;
	
	@ElementCollection
	@CollectionTable(name = "LIBRARY_PLUGIN_BINDING_PROPERTY", joinColumns = @JoinColumn(name = "PLUGIN_BINDING_ID"))
	@MapKeyColumn(name = "PLUGIN_BINDING_KEY")
	@Column(name = "PLUGIN_BINDING_VALUE")
	private Map<String, String> properties = new HashMap<String, String>(2);


	public long getId() {
		return id;
	}
	
	public abstract String getPluginId();
	
	public abstract void setPluginId(String pluginId);
}
