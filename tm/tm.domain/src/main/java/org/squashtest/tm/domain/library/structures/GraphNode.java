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

import java.util.ArrayList;
import java.util.List;


public class GraphNode<IDENT, T extends GraphNode<IDENT, T>>{
	
	protected final List<T> parents = new ArrayList<T>();
	protected final List<T> children = new ArrayList<T>();
	
	

	protected IDENT key;
	protected LibraryGraph<IDENT, T> graph;
	
	
	public GraphNode(){
		
	}

	public GraphNode(IDENT key){
		this.key=key;
	}


	public List<T> getParents() {
		return parents;
	}


	public List<T> getChildren() {
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


	public void setGraph(LibraryGraph<IDENT, T> graph) {
		this.graph = graph;
	}

	
}