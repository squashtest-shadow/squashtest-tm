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
package org.squashtest.tm.service.deletion;

import java.util.ArrayList;
import java.util.Collection;

public class OperationReport {

	Collection<Node> removedNodes = new ArrayList<Node>();
	Collection<NodeRenaming> nodeRenaming = new ArrayList<NodeRenaming>();
	Collection<NodeMovement> nodeMovement = new ArrayList<NodeMovement>();
	
	
	public Collection<Node> getRemovedNodes() {
		return removedNodes;
	}

	public Collection<NodeRenaming> getNodeRenaming() {
		return nodeRenaming;
	}

	public Collection<NodeMovement> getNodeMovement() {
		return nodeMovement;
	}

	public void mergeWith(OperationReport other){
		this.removedNodes.addAll(other.getRemovedNodes());
		this.nodeRenaming.addAll(other.getNodeRenaming());
		this.nodeMovement.addAll(other.getNodeMovement());
	}
	
	public void addRemovedNode(Node removed){
		removedNodes.add(removed);
	}	
	
	public void addRemovedNode(String nodetype, Long nodeId){
		addRemovedNode(new Node(nodeId, nodetype));
	}
	
	public void addRemovedNodes(Collection<Node> toRemove){
		removedNodes.addAll(toRemove);
	}
	
	public void addRemovedNodes(Collection<Long> ids, String nodeType){
		for (Long id : ids){
			addRemovedNode(new Node(id, nodeType));
		}
	}
	
	public void addNodeRenaming(NodeRenaming renaming){
		nodeRenaming.add(renaming);
	}
	
	public void addNodeRenaming(String nodetype, Long nodeid, String newName){
		addNodeRenaming(new NodeRenaming(new Node(nodeid, nodetype), newName));
	}
	
	public void addNodeRenamings(Collection<NodeRenaming> renamings){
		nodeRenaming.addAll(renamings);
	}
	
	public void addNodeMovement(NodeMovement movement){
		nodeMovement.add(movement);
	}
	
	public void addNodeMovements(Collection<NodeMovement> movements){
		nodeMovement.addAll(movements);
	}

}



