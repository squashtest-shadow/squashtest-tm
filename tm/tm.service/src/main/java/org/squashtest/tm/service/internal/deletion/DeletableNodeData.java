/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.deletion;

import org.squashtest.tm.domain.library.structures.NodeData;

public class DeletableNodeData extends NodeData<Long>{

	private  Boolean isDeletable = true;
	private  String nodeName;
	
	@Override
	//TODO : find the correct generic type for TreeNodeData to avoid the brutal cast here
	public void updateWith(NodeData<Long> newData) {
		this.isDeletable=((DeletableNodeData)newData).isDeletable();
	}
	
	public Boolean isDeletable(){
		return isDeletable;
	}
	
	public void setDeletable( Boolean isDeletable){
		this.isDeletable = isDeletable;
	}
	
	public void setNodeName(String name){
		nodeName=name;
	}
	
	public String getNodeName(){
		return nodeName;
	}
	
	public DeletableNodeData(Long key){
		super(key);
		isDeletable=true;
	}
	
	public DeletableNodeData(Long key, Boolean deletable){
		super(key);
		this.isDeletable=deletable;
	}
	
	public DeletableNodeData(Long key, Boolean deletable, String name){
		super(key);
		this.isDeletable=deletable;
		nodeName=name;
	}	
	
	
}
