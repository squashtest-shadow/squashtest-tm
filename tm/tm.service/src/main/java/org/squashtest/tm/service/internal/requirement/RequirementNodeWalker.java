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
package org.squashtest.tm.service.internal.requirement;

import java.util.LinkedList;
import java.util.List;

import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementLibraryNodeVisitor;


/**
 * 
 *  That class will descend a RequirementLibraryNode hierarchy and add them in a particular order.
 * 
 *  Currently : will walk depth-first and wont add duplicate entries.
 *  
 *  NOT THREAD SAFE. Get a new instance every-time you need a walk.
 */

/*
 * TODO : 
 * 		1) define Folder and LibraryNode as visitable so that we can turn that class into generic.
 * 		2) let the user choose the walking and adding strategy if need be some day.
 */
public class RequirementNodeWalker implements RequirementLibraryNodeVisitor{


	List<Requirement> outputList ;
			
	public RequirementNodeWalker(){
		outputList = new LinkedList<Requirement>();
	}
	
	public List<Requirement> walk(List<RequirementLibraryNode> nodes){
		
		for (RequirementLibraryNode node : nodes){
			node.accept(this);
		}
		
		return outputList;
	}

	
	

	@Override
	public void visit(RequirementFolder requirementFolder) {
		for (RequirementLibraryNode node : requirementFolder.getContent()){
			node.accept(this);
		}
	}

	@Override
	public void visit(Requirement requirement) {
		
		if (! outputList.contains(requirement)){
			outputList.add(requirement);
		}
	}

	
}
