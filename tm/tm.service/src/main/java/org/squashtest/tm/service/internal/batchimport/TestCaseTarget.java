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
package org.squashtest.tm.service.internal.batchimport;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.Target;

public class TestCaseTarget extends Target{

	private String path;
	private Integer order;
	
	public TestCaseTarget(){
		super();
	}
	
	public TestCaseTarget(String path){
		super();
		this.path = path;
	}
	
	public TestCaseTarget(String path, Integer order){
		super();
		this.path = path;
		this.order = order;
	}
	
	@Override
	public EntityType getType() {
		return EntityType.TEST_CASE;
	}
	
	

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	@Override
	// GENERATED:START
	public int hashCode() {
		final int prime = 31;
		int result = 47;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}
	// GENERATED:END

	@Override
	// GENERATED:START
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestCaseTarget other = (TestCaseTarget) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
	// GENERATED:END


	@Override
	public String toString(){
		return path;
	}
	
	@Override
	public boolean isWellFormed() {
		return Utils.isPathWellFormed(path);
	}
	
	@Override
	public String getProject() {
		return Utils.extractProjectName(path);
	}
	
	public String getName(){
		return Utils.extractTestCaseName(path);
	}
	
	
	/**
	 * note : return the names of each folders, including the project, of this test case. Assumes that the path is well formed.
	 * 
	 * @return
	 */
	public String getFolder(){
		
		String[] names =  Utils.splitPath(path);
		String[] shortened = Arrays.copyOf(names, names.length-1);
		
		return StringUtils.join(shortened, '/');
	}
	
	public boolean isRootTestCase(){
		String[] names =  Utils.splitPath(path);
		return names.length==2;	//that is, composed of a project and a test case name only.
	}
}
