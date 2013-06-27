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
package org.squashtest.tm.web.internal.model.viewmapper;

import java.util.NoSuchElementException;




/**
 * @param <KEY>
 */
public interface DatatableMapper<KEY> {

	/**
	 * Returns the name of the attribute indexed by KEY in the mapper.
	 * 
	 * @param key
	 * @return
	 * @throws NoSuchElementException if no result was found for the given key
	 */
	String attrAt(KEY key) throws NoSuchElementException;	
	
	
	/**
	 * Returns the path of that attribute. Contrary to {@link #attrAt(Object)}, you will also know of which class it belongs :
	 * the returned data is &lt;simpleclassname&gt;.&lt;attributename&gt;. Example : the name of a TestCase will be returned 
	 * as "TestCase.name".
	 * 
	 * @param key
	 * @return
	 * @throws NoSuchElementException if no result was found for the given key
	 */
	String pathAt(KEY key) throws NoSuchElementException;
	
	
	/**
	 * will register an attribute named 'attributeName' of a class 'ownerType', of which the type is 'attributeType' and that will be refered to as 'key'.
	 * 
	 * @param ownerType
	 * @param attributeName
	 * @param attributeType
	 * @param key
	 * @return this
	 */
	public DatatableMapper<KEY> mapAttribute(Class<?> ownerType, String attributeName, Class<?> attributeType, KEY key);

}
