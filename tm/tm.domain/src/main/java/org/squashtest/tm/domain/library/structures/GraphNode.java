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
package org.squashtest.tm.domain.library.structures;

import java.util.HashSet;
import java.util.Set;


public class GraphNode<IDENT, T extends GraphNode<IDENT, T>>{
	
	protected final Set<T> parents = new HashSet<T>();
	protected final Set<T> children = new HashSet<T>();
	
	

	protected IDENT key;
	protected LibraryGraph<IDENT, T> graph;
	
	
	public GraphNode(){
		
	}

	public GraphNode(IDENT key){
		this.key=key;
	}


	public Set<T> getParents() {
		return parents;
	}


	public Set<T> getChildren() {
		return children;
	}
	
	public void addParent(T parent){
		if (parent!=null){ parents.add(parent);}
	}
	
	public void addChild(T child){
		if (child!=null){ children.add(child);}
	}

	public IDENT getKey(){
		return key;
	}

	public void setKey(IDENT key){
		this.key=key;
	}

	public LibraryGraph<IDENT, T> getGraph() {
		return graph;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		T other = (T) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}
	
	

	
}