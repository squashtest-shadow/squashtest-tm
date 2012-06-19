/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.core.bugtracker.domain;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class BTProject implements Identifiable<BTProject>{
	
	private final String id;
	private final String name;
	
	private List<Priority> priorities = new LinkedList<Priority>();
	private List<Version> versions = new LinkedList<Version>();
	private List<User> users = new LinkedList<User>();
	private List<Category> categories = new LinkedList<Category>();

	public BTProject(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	public void setVersions(List<Version> versions){
		this.versions=versions;
	}
	
	public List<Version> getVersions(){
		return versions;
	}
	
	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
	
	public List<Priority> getPriorities(){
		return priorities;
	}
	
	public void setPriorities(List<Priority> priorities){
		this.priorities=priorities;
	}
	

	public void addVersion(Version version){
		versions.add(version);
	}
	
	public void addAllVersions(Collection<Version> versions){
		this.versions.addAll(versions);
	}
	
	public Version findVersionByName(String versionName){
		for (Version version : versions){
			if (version.getName().equals(versionName)){
				return version;
			}
		}
		return null;
	}
	
	public Version findVersionById(String versionId){
		for (Version version : versions){
			if (version.getId().equals(versionId)){
				return version;
			}
		}
		return null;	
	}
	
	public void addAllCategories(Collection<Category> categories){
		this.categories.addAll(categories);
	}
	
	public Category findCategoryByName(String categoryName){
		for (Category category : categories){
			if (category.getName().equals(categoryName)){
				return category;
			}
		}
		return null;		
	}
	
	public Category findCategoryById(String categoryId){
		for (Category category : categories){
			if (category.getId().equals(categoryId)){
				return category;
			}
		}
		return null;		
	}
	
	public void addallPriorities(Collection<Priority> priorities){
		this.priorities.addAll(priorities);
	}
	
	public Priority findPriorityByName(String priorityName){
		for (Priority priority : priorities){
			if (priority.getName().equals(priorityName)){
				return priority;
			}
		}
		return null;			
	}
	
	public Priority findPriorityById(String priorityId){
		for (Priority priority : priorities){
			if (priority.getId().equals(priorityId)){
				return priority;
			}
		}
		return null;			
	}
	
	
	
	public void setUsers(List<User> users){
		this.users=users;
	}
	
	public List<User> getUsers(){
		return users;
	}
	
	public void addUser(User user){
		this.users.add(user);
	}
	
	public void addAllUsers(Collection<User> users){
		this.users.addAll(users);
	}
	
	public User findUserByName(String userName){
		for (User user : users){
			if (user.getName().equals(userName)){
				return user;
			}
		}
		return null;
	}
	
	public User findUserById(String userId){
		for (User user : users){
			if (user.getId().equals(userId)){
				return user;
			}
		}
		return null;
	}
	
	
	/**
	 * is hopefully never a dummy
	 * 
	 */
	@Override
	public boolean isDummy(){
		return false;
	}
	

	/**
	 *  returns true if the user list is empty or if it contains only {@link User}.NO_USER 
	 * 
	 * @return
	 */
	public boolean canAssignUsers(){
		return ! (
				(users.isEmpty()) ||
				(users.size() == 1 && users.get(0).isDummy()) 
		);
	}
}
