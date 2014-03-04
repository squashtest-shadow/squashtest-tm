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
package org.squashtest.tm.service.internal.repository;

import java.util.List;
import java.util.NoSuchElementException;

import org.squashtest.tm.domain.library.LibraryNode;

public interface LibraryNodeDao<NODE extends LibraryNode> extends EntityDao<NODE>{

	/**
	 * Returns the path of the given entity. The path is the concatenation of the ancestor names, sorted by ancestry. It does not begin with /&ltproject-name&gt; 
	 * 
	 * @param entityId
	 * @return
	 */
	List<String> getParentsName(long entityId);
	
	/**
	 * Returns the ids path. The path is a list of ids sorted by ancestry: first = elder, last = younger.
	 * The list contains only ids of library nodes.
	 * 
	 * @param entityId
	 * @return ids of all entity parents sorted from elder to younger.
	 */
	List<Long> getParentsIds(long entityId);
	
	
	
	/**
	 * <p>Given a list of ids of library NODE, return the path of those nodes.  The path starts with /&lt;projectname&gt;. 
	 * The path is slash-separated '/'. If one of the elements in the path uses a '/', it will be escaped as '\/'.</p>
	 * 
	 * <p>The order of the result is consistent with the order of the input. If an element could not be found 
	 * (an invalid id for instance), the corresponding path in the result is NULL.</p>
	 * 
	 * @param ids
	 * @return
	 */
	List<String> getPathsAsString(List<Long> ids);
	
	
	
	
	
	/**
	 * <p>Given a list of paths of library NODE, return the ids of those nodes.  The path starts with /&lt;projectname&gt;. 
	 * Like in {@link #getPathsAsString(List)} a path is slash-separated '/', but this time names containing a '/' don't need 
	 * escaping (you may use escaped or unescaped names as will). </p>
	 * 
	 * <p>The order of the result is consistent with the order of the input. If an element could not be found 
	 * (an invalid path for instance), the corresponding id in the result is NULL.</p>
	 * 
	 * @param path
	 * @return
	 */
	List<Long> findNodeIdsByPath(List<String> path);
	
	
	/**
	 * Same as {@link #findNodeIdsByPath(List)}, for one test case only. Throws {@link NoSuchElementException} if 
	 * not found.
	 *  
	 * @param path
	 * @return
	 */
	long findNodeIdByPath(String path);
	
	
	
	/**
	 * Same than above, but returns the entities instead.
	 * 
	 * @param path
	 * @return
	 */
	List<NODE> findNodesByPath(List<String> path); 
	
	
	/**
	 * Same than above, but for one path only. Throws {@link NoSuchElementException} if 
	 * not found.
	 * 
	 * @param path
	 * @return
	 */
	NODE findNodesByPath(String path);
	
}
