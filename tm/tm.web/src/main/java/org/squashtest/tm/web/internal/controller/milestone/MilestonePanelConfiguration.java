/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.milestone;

import java.util.HashMap;
import java.util.Map;

public class MilestonePanelConfiguration {

	private Map<String, Object> basic = new HashMap<>();

	private Map<String, Object> urls = new HashMap<>();

	private Map<String, Object> permissions = new HashMap<>();


	public Map<String, Object> getBasic() {
		return basic;
	}

	public void setBasic(Map<String, Object> basic) {
		this.basic = basic;
	}

	public Map<String, Object> getUrls() {
		return urls;
	}

	public void setUrls(Map<String, Object> urls) {
		this.urls = urls;
	}

	public Map<String, Object> getPermissions() {
		return permissions;
	}

	public void setPermissions(Map<String, Object> permissions) {
		this.permissions = permissions;
	}

	public void addBasic(String attribute, Object value){
		this.basic.put(attribute, value);
	}

	public void addUrls(String attribute, Object value){
		this.urls.put(attribute, value);
	}

	public void addPermissions(String attribute, Object value){
		this.permissions.put(attribute, value);
	}


}
