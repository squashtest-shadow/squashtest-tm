/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.service.importer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.squashtest.csp.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNodeVisitor;
import org.squashtest.csp.tm.service.RequirementLibraryNavigationService;
import org.squashtest.csp.tm.service.importer.ImportSummary;

/**
 * <p>The idea behind the implementation of this class is the following :</p> 
 * 
 * parameters :
 * <ul>
 * 	<li>the receiving persistent container : D <li>
 * 	<li>the detached container containing the transient entities we want to persist : S</li>
 * </ul>
 * 
 * <ul>
 * 	<li>for each n in S :</li>
 * 		<ul>
 * 			<li>if n is a Requirement : </li>
 * 			<ul>
 * 				<li>if name(n) already exists in names(D) :</li>
 * 				<ul>
 * 					<li>rename n with a available name in D</li>
 * 					<li>add a warning regarding this operation</li>
 * 				</ul>
 * 				<li>in any case, persist n into D</li>
 * 			</ul>
 * 			<li>if n is a folder :</li>
 * 			<ul>
 * 				<li>if name(n) already exists in names(D) :</li>
 * 				<ul>
 * 					<li>fetch node p in D where name(p) = name(n)</li>
 * 					<li>if p is a Requirement :</li>
 * 					<ul>
 * 						<li>rename n with an available name in D</li>
 * 						<li>add a warning regarding this operation</li>
 * 						<li>persist n into D</li>
 * 					</ul>
 * 					<li>if p is a Folder</li>
 * 					<ul>
 * 						<li>call this function recursively using D <- p and S <- n </li>
 * 					</ul>
 * 				</ul>
 * 			</ul>
 * 		</ul>
 * </ul>
 * 
 * Regarding the summary, may increment failures and warning, but not total requirements nor success. 
 * 
 * @author bsiri
 *
 */

/*
 * Node : the use of visitors and the distinct interfaces between libraries and folders made the following implementation unnecessarily complex.
 */

class RequirementLibraryMerger {

	
	private RequirementLibraryNavigationService service;
	
	private ImportSummaryImpl summary = new ImportSummaryImpl();
	
	private LinkedList<FolderPair> nonTreated = new LinkedList<FolderPair>();
		
	
	public RequirementLibraryMerger(){
		super();
	}
	
	public RequirementLibraryMerger(RequirementLibraryNavigationService service){
		this();
		this.service=service;
	}
	
	

	public void setLibraryService(RequirementLibraryNavigationService service2){
		this.service = service2;
	}
	
	public ImportSummary getSummary(){
		return summary;
	}
	
	private NodeMerger merger = new NodeMerger();
	
	
	/**
	 * the Library is the root of the hierarchy, and that's where we're importing our data. the data that couldn't be added to the root of the library
	 * (mostly duplicate folders) will be treated in additional loops (see #mergerIntoFolder)
	 * 
	 * @param library
	 * @param root
	 * @param organizedPseudoReqNodes 
	 */
	public void mergeIntoLibrary(RequirementLibrary library, RequirementFolder root, Map<RequirementFolder, List<PseudoRequirement>> organizedPseudoReqNodes){
		
		//phase 1 : add the content of the root of the library
		merger.setMergingContext(this);
		merger.setDestination(library);
		
		for (RequirementLibraryNode node : root.getContent()){
			node.accept(merger);
		}
		
		//phase 2 : if some source folder already exists, then no need to persist it, but we must merge its content instead with the content of the 
		//corresponding persistent entity.
		
		//important : do not replace the while loop with a for or foreach : 
		//nonTreated may/should be modified during treatment 
		FolderPair pair;
		
		while(! nonTreated.isEmpty()){
			
			pair = nonTreated.removeFirst();
			
			merger.setDestination(pair.dest);
			
			for (RequirementLibraryNode node : pair.src.getContent()){
				node.accept(merger);
			}
			
		}
		
	}

	
	/* ******************************** private classes ************************************ */
	
	private static class FolderPair {
		RequirementFolder dest;
		RequirementFolder src;
		
		public FolderPair(RequirementFolder dest, RequirementFolder src){
			this.dest=dest;
			this.src=src;
		}
		
	}
	

	/*
	 * This class is an adapter to help with the API differences between Libraries and Folders  
	 */
	
	private static class DestinationManager {
		
		protected RequirementLibraryMerger context;
		
		protected RequirementLibrary destLibrary ;
		protected RequirementFolder destFolder;

		
		public void setMergingContext(RequirementLibraryMerger merger){
			this.context=merger;
		}
		
		public void setDestination(RequirementLibrary library){
			this.destLibrary=library;
			this.destFolder=null;
		}
		
		public void setDestination(RequirementFolder folder){
			this.destFolder=folder;
			this.destLibrary=null;
		}
		
		
		protected Set<RequirementLibraryNode> getDestinationContent(){
			if (destLibrary!=null){
				return destLibrary.getRootContent();
			}else{
				return destFolder.getContent();
			}
		}
		
		
		protected void persistRequirement(NewRequirementVersionDto req){
			if (destLibrary!=null){
				context.service.addRequirementToRequirementLibrary(destLibrary.getId(), req);
			}else{
				context.service.addRequirementToRequirementFolder(destFolder.getId(), req);
			}			
		}
		
		protected void persistFolder(RequirementFolder folder){
			if (destLibrary!=null){
				context.service.addFolderToLibrary(destLibrary.getId(), folder);
			}else{
				context.service.addFolderToFolder(destFolder.getId(), folder);
			}					
		}
		
		protected void applyConfigurationTo(DestinationManager otherManager){
			otherManager.setMergingContext(context);
			
			if (destLibrary!=null){
				otherManager.setDestination(destLibrary);
			}else{
				otherManager.setDestination(destFolder);
			}	
		}
		
		
	}
	
	
	
	
	private static class NodeMerger extends DestinationManager implements RequirementLibraryNodeVisitor{

		private RequirementMerger reqMerger= new RequirementMerger();
		private FolderMerger fMerger = new FolderMerger();

		@Override
		public void visit(Requirement visited) {
			applyConfigurationTo(reqMerger);
			reqMerger.setTransientRequirement(visited);
			
			reqMerger.merge();
			
		}
		
		@Override
		public void visit(RequirementFolder visited) {
			applyConfigurationTo(fMerger);
			fMerger.setTransientFolder(visited);		
			
			fMerger.merge();
		}
		
		
	}
	
	
	private static class RequirementMerger extends DestinationManager{
		
		private Requirement toMerge;
		
		public void setTransientRequirement(Requirement req){
			toMerge=req;
		}

	
		public void merge(){
			Collection<String> names = collectNames(getDestinationContent());
			
			if (names.contains(toMerge.getName())){
				String newName = generateUniqueName(names, toMerge.getName());
				toMerge.setName(newName);
				context.summary.incrRenamed();				
			}
			NewRequirementVersionDto newRequirementVersionDto = createVersionDto(toMerge);
			persistRequirement(newRequirementVersionDto);
			
		}


		private NewRequirementVersionDto createVersionDto(Requirement toMerge2) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

	
	private static class FolderMerger extends DestinationManager implements RequirementLibraryNodeVisitor{
		
		private RequirementFolder toMerge;


		public void setTransientFolder(RequirementFolder visited){
			this.toMerge=visited;
		}
		
		public void merge(){
			
			Collection<String> names = collectNames(getDestinationContent());
			
			if (names.contains(toMerge.getName())){
				RequirementLibraryNode conflictingNode = getByName(getDestinationContent(), toMerge.getName());	
				conflictingNode.accept(this);
			}
			else{
				persistFolder(toMerge);
			}
					
		}
		
		//in the case of a conflict with an existing requirement we have to rename the transient folder then persist it
		@Override
		public void visit(Requirement persisted) {
			Collection<String> allNames = collectNames(getDestinationContent());
			
			String newName = generateUniqueName(allNames, toMerge.getName());
			toMerge.setName(newName);
			
			context.summary.incrRenamed();
			
			persistFolder(toMerge);
			
		}
		
		
		//in the case of a conflict with an existing folder it's fine : we don't have to persist it.
		//However we must handle the transient content and merge them in turn : we notify the context that it must now merge it.
		@Override
		public void visit(RequirementFolder persisted) {
			FolderPair pair = new FolderPair(persisted, toMerge);
			context.nonTreated.add(pair);
		}

		

	}
	 
		
	/* ******************************** util functions ************************************* */
	
	
	private static Collection<String> collectNames(Set<RequirementLibraryNode> set){
		List<String> res = new LinkedList<String>();
		
		for (RequirementLibraryNode node : set){
			res.add(node.getName());
		}
		
		return res;
	}
	
	
	/* 
	 * mostly copy pasta from AbstractLibraryNavigationService#generateUniqueCopyNumber(List<String>)
	 *  
	 */
	private static String generateUniqueName(Collection<String> pickedNames, String baseName){
		
		int higherIndex = 0;
		//we want to match one or more digits following the first instance of substring -Import
		Pattern pattern = Pattern.compile(baseName+"-import(\\d+)");
	
		for (String copyName : pickedNames) {
			
			Matcher matcher = pattern.matcher(copyName);
			
			if (matcher.find()){
								
				String index = matcher.group(1);

				if (higherIndex < Integer.parseInt(index)) {
					higherIndex = Integer.parseInt(index);
				}			
			}

		}
		
		int newIndex = higherIndex + 1;
		return baseName+"-import"+newIndex;
	}
	
	private static RequirementLibraryNode getByName(Set<RequirementLibraryNode> set, String needle){
		for (RequirementLibraryNode node : set ){
			if (node.getName().equals(needle)){
				return node;
			}
		}
		throw new RuntimeException("that method should never have been called if not preceeded by a preventive call to "+
				"collectName().contains() or if this preventive call returned false - something is wrong with your code dude ");
		
	}
	
	
}
