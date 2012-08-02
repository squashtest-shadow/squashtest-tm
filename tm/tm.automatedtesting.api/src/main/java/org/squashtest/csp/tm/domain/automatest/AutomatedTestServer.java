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
package org.squashtest.csp.tm.domain.automatest;

import java.net.URL;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


@Entity
public class AutomatedTestServer {

	private static final String ID_PREFIX = "TM-SUITE_";
	
	@Id
	@GeneratedValue
	@Column(name = "SERVER_ID")
	private Long id;

	@Column
	private URL baseURL;
	
	
	public String getId() {
		return ID_PREFIX+id;
	}


	public URL getBaseURL() {
		return baseURL;
	}


	public void setBaseURL(URL baseURL) {
		this.baseURL = baseURL;
	}



	
	
}
