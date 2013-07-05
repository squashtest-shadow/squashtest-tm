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
package org.squashtest.tm.plugin.testautomation.jenkins.beans;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class TestList {
	
	private Suite[] suites;

	public Suite[] getSuites() {
		return suites;
	}

	public void setSuites(Suite[] suites) {	//NOSONAR no, this array is not stored directly
		this.suites = Arrays.copyOf(suites, suites.length);
	}
	
	public Collection<String> collectAllTestNames(){
		
		Collection<String> names = new LinkedList<String>();
		
		for (Suite suite : suites){
			for (Case c : suite.getCases()){
				names.add(suite.getNameAsPath()+"/"+c.getName());
			}
		}
		
		return names;
	}
}
