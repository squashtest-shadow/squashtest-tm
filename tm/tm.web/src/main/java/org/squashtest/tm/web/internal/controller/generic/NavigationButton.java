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
package org.squashtest.tm.web.internal.controller.generic;

public class NavigationButton {

	private String id;
	private String tooltip;
	private String url;
	private String imageOffUrl;
	private String imageOnUrl;
	private String label;

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setImageOffUrl(String imageOffUrl) {
		this.imageOffUrl = imageOffUrl;
	}

	public String getImageOffUrl() {
		return imageOffUrl;
	}

	public void setImageOnUrl(String imageOnUrl) {
		this.imageOnUrl = imageOnUrl;
	}

	public String getImageOnUrl() {
		return imageOnUrl;
	}
}
