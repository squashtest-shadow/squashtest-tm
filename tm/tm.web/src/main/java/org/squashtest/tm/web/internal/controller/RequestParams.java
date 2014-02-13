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
package org.squashtest.tm.web.internal.controller;

/**
 * Constants to be used as request param names.
 * 
 */
public final class RequestParams {

	private RequestParams() {
		super();
	}
	/**
	 * request DataTable datas
	 */
	public static final String S_ECHO_PARAM = "sEcho";

	/**
	 * multiple folder ids post param
	 */
	public static final String FOLDER_IDS = "folderIds[]";

	/**
	 * multiple ids post param
	 */
	public static final String IDS = "ids[]";

	/**
	 * Dry-run some app-state-modifying request
	 */
	public static final String DRY_RUN = "dry-run";
	
	

}
