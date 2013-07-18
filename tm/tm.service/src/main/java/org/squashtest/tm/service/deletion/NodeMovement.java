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

import java.util.List;

public final class NodeMovement{
	public Node newParent;
	public List<Node> movedNodes;
	public NodeMovement(Node newParent, List<Node> movedNodes){
		this.newParent = newParent;
		this.movedNodes = movedNodes;
	}
	public NodeMovement() {
		super();
	}
	public Node getNewParent() {
		return newParent;
	}
	public void setNewParent(Node newParent) {
		this.newParent = newParent;
	}
	public List<Node> getMovedNodes() {
		return movedNodes;
	}
	public void setMovedNodes(List<Node> movedNodes) {
		this.movedNodes = movedNodes;
	}
	
}
