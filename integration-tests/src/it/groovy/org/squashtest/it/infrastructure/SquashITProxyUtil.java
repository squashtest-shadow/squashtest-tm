/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.it.infrastructure;

public interface SquashITProxyUtil {

	/**
	 * will invoke a *public* method by name on a given proxy with the given parameters.
	 * No safety check, be ready to be thrown an exception, ie if the method is not public. And many more.
	 * 
	 * Important : if methodArgs accept Something[]... as an argument, please pass it as an Array.
	 * 
	 */
	void invoke(Object proxy, String methodName, Object... methodArgs);
	
	/**
	 * returns whether the argument is a proxy handled by the util or not
	 * 
	 */
	boolean isProxySupported(Object potentialProxy);
	
	/**
	 * returns the instance backing the proxy.
	 * 
	 * @param proxy
	 * @return
	 */
	Object getTarget(Object proxy);
	
}
