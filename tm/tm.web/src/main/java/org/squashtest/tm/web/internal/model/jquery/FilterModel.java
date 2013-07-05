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
package org.squashtest.tm.web.internal.model.jquery;

import java.util.ArrayList;
import java.util.List;

/*
 * Note : we set the NOSONAR flag on the setters for Array-type properties otherwise it rings because we don't clone them.
 * We can reasonably ignore those warnings because that class is meant to be serialized from/to json. Of course, that assumption holds
 * as long as no one uses that class for another purpose.
 * 
 * 
 * @author bsiri
 */

public class FilterModel {
	private List<Object[]> projectData = new ArrayList<Object[]>();
	private Boolean enabled;
	
	public Object[] getProjectData() {
		return projectData.toArray();
	}
	public void setProjectData(Object[][] projectData) {
		this.projectData = new ArrayList<Object[]>(projectData.length);
		for(Object[] project : projectData) {
			this.projectData.add(project);
		}
	}
	public Boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean activated) {
		this.enabled = activated;
	}
	public void addProject(long id, String name) {
		projectData.add(new Object[] {id, name, false});
	}
	
}
