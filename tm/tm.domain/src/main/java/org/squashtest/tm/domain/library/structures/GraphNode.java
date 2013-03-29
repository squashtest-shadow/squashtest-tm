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
package org.squashtest.tm.domain.library.structures;

import java.util.ArrayList;
import java.util.List;


public class GraphNode<T extends GraphNode<T>>{
	
	private final List<T> parents = new ArrayList<T>();
	private final List<T> children = new ArrayList<T>();
	
	

	private Long key;
	private LibraryGraph<T> graph;
	
	
	public GraphNode(){
		
	}

	public GraphNode(Long key){
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

	public Long getKey(){
		return key;
	}

	public void setKey(Long key){
		this.key=key;
	}

	public LibraryGraph<T> getGraph() {
		return graph;
	}


	public void setGraph(LibraryGraph<T> graph) {
		this.graph = graph;
	}
	

	
}