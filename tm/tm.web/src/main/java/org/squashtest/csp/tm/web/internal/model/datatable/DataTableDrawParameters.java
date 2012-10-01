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
package org.squashtest.csp.tm.web.internal.model.datatable;

/**
 * Parameters of the draw request sent by a datatable.
 *
 * @author Gregory Fouquet
 *
 */
// NOSONAR names have to match JSON structure  
public class DataTableDrawParameters {
	private int iDisplayStart;
	private int iDisplayLength;
	private String sEcho;
	private int iSortCol_0;
	private String sSortDir_0;



	public int getiDisplayStart() { // NOSONAR names have to match JSON structure
		return iDisplayStart;
	}

	public void setiDisplayStart(int iDisplayStart) { // NOSONAR names have to match JSON structure
		this.iDisplayStart = iDisplayStart;
	}

	public int getiDisplayLength() {
		return iDisplayLength;
	}

	public void setiDisplayLength(int iDisplayLength) { // NOSONAR names have to match JSON structure
		this.iDisplayLength = iDisplayLength;
	}

	public String getsEcho() { // NOSONAR names have to match JSON structure
		return sEcho;
	}

	public void setsEcho(String sEcho) { // NOSONAR names have to match JSON structure
		this.sEcho = sEcho;
	}

	public int getiSortCol_0() { // NOSONAR names have to match JSON structure
		return iSortCol_0;
	}

	public void setiSortCol_0(int iSortCol_0) { // NOSONAR names have to match JSON structure
		this.iSortCol_0 = iSortCol_0;
	}

	public String getsSortDir_0() { // NOSONAR names have to match JSON structure
		return sSortDir_0;
	}

	public void setsSortDir_0(String sSortDir_0) { // NOSONAR names have to match JSON structure
		this.sSortDir_0 = sSortDir_0;
	}
}
