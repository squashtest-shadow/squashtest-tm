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
package org.squashtest.csp.tm.internal.service;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.CannotMoveNodeException;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.library.Folder;
import org.squashtest.csp.tm.domain.library.Library;
import org.squashtest.csp.tm.domain.library.LibraryNode;
import org.squashtest.csp.tm.internal.repository.FolderDao;
import org.squashtest.csp.tm.internal.repository.LibraryDao;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.squashtest.csp.tm.service.LibraryNavigationService;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

/**
 * Generic implementation of a library navigation service.
 * 
 * @author Gregory Fouquet
 * 
 * @param <LIBRARY>
 * @param <FOLDER>
 * @param <NODE>
 */



/*
 * Security Implementation note :  
 * 
 * this is sad but we can't use the annotations here. We would need the actual type 
 * of the entities we need to check instead of the generics. So we'll call the PermissionEvaluationService explicitly
 * once we've fetched the entities ourselves.
 * 
 * 
 * @author bsiri
 */

/*
 * Note : about methods moving entities from source to destinations : 
 * 
 * Basically such operations need to be performed in a precise order, that is : 1) remove the entity from the source collection and 
 * 2) insert it in the new one.
 *  
 * However Hibernate performs batch updates in the wrong order, ie it inserts new data before deleting the former ones, 
 * thus violating many unique constraints DB side. So we explicitly flush the session between the removal and the insertion. 
 * 
 * 
 * @author bsiri
 */

/*
 * Note regarding type safety when calling checkPermission(SecurityCheckableObject...) : see bug 
 * at http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6227971 
 * 
 * @author bsiri
 */


@Transactional
public abstract class AbstractLibraryNavigationService<LIBRARY extends Library<NODE>, FOLDER extends Folder<NODE>, NODE extends LibraryNode>
implements LibraryNavigationService<LIBRARY, FOLDER, NODE> {
	

	private PermissionEvaluationService permissionService;
	

	@ServiceReference
	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}

	protected abstract FolderDao<FOLDER, NODE> getFolderDao();
	protected abstract LibraryDao<LIBRARY, NODE> getLibraryDao();
	protected abstract LibraryNodeDao<NODE> getLibraryNodeDao();
	protected abstract NodeDeletionHandler<NODE, FOLDER> getDeletionHandler();
	

	public AbstractLibraryNavigationService() {
		super();
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public final List<NODE> findLibraryRootContent(long libraryId) {
		return getLibraryDao().findAllRootContentById(libraryId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public final List<NODE> findFolderContent(long folderId) {
		return getFolderDao().findAllContentById(folderId);
	}

	@Override
	public final LIBRARY findLibrary(long libraryId) {
		//fetch
		LIBRARY library = getLibraryDao().findById(libraryId);
		//check
		checkPermission(new SecurityCheckableObject(library, "READ"));
		//proceed
		return library;
	}
	
	
	@Override
	public final FOLDER findFolder(long folderId) {
		//fetch
		FOLDER folder = getFolderDao().findById(folderId);
		//check
		checkPermission(new SecurityCheckableObject(folder, "READ"));
		//proceed
		return getFolderDao().findById(folderId);
	}

	


	@Deprecated
	@SuppressWarnings("unchecked")
	@Override
	public final void renameFolder(long folderId, String newName) {
		//fetch
		FOLDER folder = getFolderDao().findById(folderId);
		//check
		checkPermission(new SecurityCheckableObject(folder, "WRITE"));
		
		//proceed
		LIBRARY library = getLibraryDao().findByRootContent((NODE) folder);

		if (library != null) {
			if (!library.isContentNameAvailable(newName)) {
				throw new DuplicateNameException(folder.getName(), newName);
			}
		} else {
			FOLDER parentFolder = getFolderDao().findByContent((NODE) folder);

			if (parentFolder != null && !parentFolder.isContentNameAvailable(newName)) {
				throw new DuplicateNameException(folder.getName(), newName);
			}
		}

		folder.setName(newName);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final void addFolderToLibrary(long destinationId, FOLDER newFolder) {
		//fetch
		LIBRARY container = getLibraryDao().findById(destinationId);
		//check
		checkPermission(new SecurityCheckableObject(container, "WRITE"));
		
		//proceed
		container.addRootContent((NODE) newFolder);
		getFolderDao().persist(newFolder);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final void addFolderToFolder(long destinationId, FOLDER newFolder) {
		//fetch
		FOLDER container = getFolderDao().findById(destinationId);
		//check
		checkPermission(new SecurityCheckableObject(container, "WRITE"));
		
		container.addContent((NODE) newFolder);
		getFolderDao().persist(newFolder);

	}
	

	/* ********************** move operations *************************** */
	
	private void removeFromLibrary(LIBRARY library, Long[] targetIds){
		try{
			for (Long id : targetIds){
				NODE node = getLibraryNodeDao().findById(id);
				library.removeRootContent(node);
			}
		}catch(DuplicateNameException dne){
			throw new CannotMoveNodeException();
		}
	}
	
	private void addNodesToLibrary(LIBRARY library, Long[] targetIds){
		try{
			for (Long id : targetIds){
				NODE node = getLibraryNodeDao().findById(id);
				library.addRootContent(node);
			}	
		}catch(DuplicateNameException dne){
			throw new CannotMoveNodeException();
		}
	}	
	
	private void removeFromFolder(FOLDER folder, Long[] targetIds){
		for (Long id : targetIds){
			NODE node = getLibraryNodeDao().findById(id);
			folder.removeContent(node);
		}
	}
	
	private void addNodesToFolder(FOLDER folder, Long[] targetIds){
		for (Long id : targetIds){
			NODE node = getLibraryNodeDao().findById(id);
			folder.addContent(node);
		}	
	}
		
	
	
	
	
	@Override
	public void modeNodesToFolder(long destinationId, Long[] targetIds){

		if (targetIds.length==0){
			return;
		}
		
		//fetch
		FOLDER destinationFolder = getFolderDao().findById(destinationId);
		
		
		//security check		
		NODE item1 = getLibraryNodeDao().findById(targetIds[0]);		
		LIBRARY parentLib = getLibraryDao().findByRootContent(item1);
		FOLDER parentFolder = getFolderDao().findByContent(item1);
		
		Object parentObject = (parentLib!=null) ? parentLib : parentFolder;
		
		for (Long id : targetIds){
			NODE node = getLibraryNodeDao().findById(id);
			checkPermission(new SecurityCheckableObject(destinationFolder, "WRITE"),
			new SecurityCheckableObject(parentObject, "WRITE"),
			new SecurityCheckableObject(node, "READ"));					
		}		
			
		//proceed
		if (parentLib!=null){
			removeFromLibrary(parentLib, targetIds);
		}
		else{
			removeFromFolder(parentFolder, targetIds);
		}
		
		getFolderDao().flush();
		
		addNodesToFolder(destinationFolder, targetIds);

	}
	
	

	@Override
	public void moveNodesToLibrary(long destinationId, Long[] targetIds){

		if (targetIds.length==0){
			return;
		}
		
		//fetch
		LIBRARY destinationLibrary = getLibraryDao().findById(destinationId);
		
		
		//security check		
		NODE item1 = getLibraryNodeDao().findById(targetIds[0]);		
		LIBRARY parentLib = getLibraryDao().findByRootContent(item1);
		FOLDER parentFolder = getFolderDao().findByContent(item1);
		
		Object parentObject = (parentLib!=null) ? parentLib : parentFolder;
		
		for (Long id : targetIds){
			NODE node = getLibraryNodeDao().findById(id);
			checkPermission(new SecurityCheckableObject(destinationLibrary, "WRITE"),
			new SecurityCheckableObject(parentObject, "WRITE"),
			new SecurityCheckableObject(node, "READ"));					
		}		
			
		//proceed
		if (parentLib!=null){
			removeFromLibrary(parentLib, targetIds);
		}
		else{
			removeFromFolder(parentFolder, targetIds);
		}
		
		getFolderDao().flush();
		
		addNodesToLibrary(destinationLibrary, targetIds);		
	}
	

/* ********************************* copy operations ****************************** */

	
	@Override
	public List<NODE> copyNodesToFolder(long destinationId, Long[] targetId){
		
		//fetch
		FOLDER destinationFolder = getFolderDao().findById(destinationId);
		
		//check. Note : we wont recursively check for the whole hierarchy as it's supposed to have the same 
		//identity holder
		for (Long id : targetId){
			NODE node = getLibraryNodeDao().findById(id);
			checkPermission(new SecurityCheckableObject(destinationFolder, "WRITE"), 
							new SecurityCheckableObject(node, "READ"));					
		}
		
		
		//proceed
		List<NODE> nodeList= new LinkedList<NODE>();
		
		for (Long id : targetId){
			NODE node = getLibraryNodeDao().findById(id);
			
			String tempName = node.getName();
			String newName;
			
			if (!destinationFolder.isContentNameAvailable(tempName)){
				List<String> copiesNames = getFolderDao().findNamesInFolderStartingWith(destinationId, tempName);
				int newCopy = generateUniqueCopyNumber(copiesNames);
				newName = tempName + "-Copie" + newCopy;
			}
			else{
				newName = tempName;
			}
		
			NODE newNode = (NODE)node.createCopy(); 	//well either a cast either make all our librarynode interface complex generics Enum style.
			newNode.setName(newName);				
		
			getLibraryNodeDao().persist(newNode);
			destinationFolder.addContent(newNode);
			nodeList.add(newNode);
			
			
		}
		
		return nodeList;
		
	}
	

	@Override
	public List<NODE> copyNodesToLibrary(long destinationId, Long[] targetId){
		
		//fetch
		LIBRARY destinationLibrary = getLibraryDao().findById(destinationId);
		
		//check. Note : we wont recursively check for the whole hierarchy as it's supposed to have the same 
		//identity holder
		for (Long id : targetId){
			NODE node = getLibraryNodeDao().findById(id);
			checkPermission(new SecurityCheckableObject(destinationLibrary, "WRITE"),
					new SecurityCheckableObject(node, "READ"));				
		}
		
		
		//proceed
		List<NODE> nodeList= new LinkedList<NODE>();
		
		for (Long id : targetId){
			NODE node = getLibraryNodeDao().findById(id);
		
			String tempName = node.getName();
			String newName;
			
			if (!destinationLibrary.isContentNameAvailable(tempName)){
				List<String> copiesNames = getFolderDao().findNamesInLibraryStartingWith(destinationId, tempName );
				int newCopy = generateUniqueCopyNumber(copiesNames);
				newName = tempName + "-Copie" + newCopy;
			}
			else{
				newName=tempName;
			}

			NODE newNode = (NODE)node.createCopy(); 	//well either a cast either make all our librarynode interface complex generics Enum style.						
			newNode.setName(newName);			
			
			getLibraryNodeDao().persist(newNode);
			destinationLibrary.addRootContent(newNode);
			nodeList.add(newNode);
			
			
		}
		
		return nodeList;
		
	}	
	
	
	
	public int generateUniqueCopyNumber(List<String> copiesNames){
		
		int lastCopy = 0;
		//we want to match one or more digits following the first instance of substring -Copie
		Pattern pattern = Pattern.compile("-Copie(\\d+)");
	
		for (String copyName : copiesNames) {
			
			Matcher matcher = pattern.matcher(copyName);
			
			if (matcher.find()){
								
				String copyNum = matcher.group(1);

				if (lastCopy < Integer.parseInt(copyNum)) {
					lastCopy = Integer.parseInt(copyNum);
				}			
			}

		}
		
		int newCopy = lastCopy + 1;
		return newCopy;
	}
	
	public FOLDER createCopyFolder(long folderId){
		FOLDER original = getFolderDao().findById(folderId);
		FOLDER clone = (FOLDER) original.createCopy();
		return clone;
	}
	

	
    /* ***************************** deletion operations *************************** */
	
	@Override
	public List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds) {
		return getDeletionHandler().simulateDeletion(targetIds);
	}

	@Override
	public List<Long> deleteNodes(List<Long> targetIds) {
		
		//check. Note : we wont recursively check for the whole hierarchy as it's supposed to have the same 
		//identity holder
		for (Long id : targetIds){
			NODE node = getLibraryNodeDao().findById(id);
			checkPermission(new SecurityCheckableObject(node, "WRITE"));				
		}		
		
		return getDeletionHandler().deleteNodes(targetIds);
	}
	

	
	
	
	/* ************************* private stuffs ************************* */
	
	/* **** manual security checks **** */

	
	/* that class is just a wrapper that associate an id, a kind of node, and a permission. */
	private class SecurityCheckableItem{
		private static final String FOLDER = "folder";
		private static final String LIBRARY = "library";
		
		private final long domainObjectId;
		private String domainObjectKind; // which should be one of the two above
		private final String permission;
		
		public SecurityCheckableItem(long domainObjectId,
				String domainObjectKind, String permission) {
			super();
			this.domainObjectId = domainObjectId;
			setKind(domainObjectKind);
			this.domainObjectKind = domainObjectKind;
			this.permission = permission;
		}
		
		private void setKind(String kind){
			if (! (kind.equals(SecurityCheckableItem.FOLDER)) 
					|| kind.equals(SecurityCheckableItem.LIBRARY)){
				throw new RuntimeException("(dev note : AbstracLibraryNavigationService : manual security checks aren't correctly configured");			
			}
			domainObjectKind=kind;
		}
		
		public long getId() {
			return domainObjectId;
		}
		public String getKind() {
			return domainObjectKind;
		}
		public String getPermission() {
			return permission;
		}
		
		
	}
	
	/* **that class just performs the same, using a domainObject directly */
	private class SecurityCheckableObject{
		private final Object domainObject;
		private final String permission;
		private SecurityCheckableObject(Object domainObject, String permission){
			this.domainObject=domainObject;
			this.permission = permission;
		}
		public String getPermission(){
			return permission;
		}
		public Object getObject(){
			return domainObject;
		}
		
	}
	
	
	/*
	 * given a list of SecurityCheckableItem, that method will throw an AccessDeniedException if at least one of them
	 * doesn't pass the security check. It's basically a logical AND between all the required conditions.
	 * 
	 * In case of success the method returns nothing and the calling method can proceed normally, if it fails an exception
	 * is raised and will join the natural workflow of an AccessDeniedException.
	 * 
	 * 
	 */
	private void checkPermission(SecurityCheckableItem... securityCheckableItems) throws AccessDeniedException {
		
		for (SecurityCheckableItem item : securityCheckableItems){
			
			Object domainObject;
			
			if (item.getKind().equals(SecurityCheckableItem.FOLDER)){
				domainObject = getFolderDao().findById(item.getId());
			}
			else {
				domainObject = getLibraryDao().findById(item.getId());
			}
			
			if (! permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", item.getPermission() , domainObject)){
				throw new AccessDeniedException("Access is denied");
			}
		}
	}
	
	private void checkPermission(SecurityCheckableObject... checkableObjects){
		for (SecurityCheckableObject object : checkableObjects){
			if (! permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", object.getPermission() , object.getObject())){
				throw new AccessDeniedException("Access is denied");
			} 
		}
	}
	

}