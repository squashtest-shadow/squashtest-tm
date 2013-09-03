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
package org.squashtest.tm.web.internal.model.datatable;

import java.util.HashMap;
import java.util.Map;

/**
 * Parameters of the draw request sent by a datatable.
 * 
 * @author Gregory Fouquet
 * 
 */
// IGNOREVIOLATIONS:START 
public class DataTableDrawParameters {

	// the identifier of that particular ajax request
	private String sEcho;

	// table columns definition
	private Map<Integer, Object> mDataProp = new HashMap<Integer, Object>();

	// paging information
	private int iDisplayStart;
	private int iDisplayLength;

	// filtering information
	private String sSearch;

	// sorting informations
	private Map<Integer, Object> iSortCol = new HashMap<Integer, Object>();
	private Map<Integer, Object> sSortDir = new HashMap<Integer, Object>();
	private int iSortingCols;

	public int getiDisplayStart() {
		return iDisplayStart;
	}

	public void setiDisplayStart(int iDisplayStart) {
		this.iDisplayStart = iDisplayStart;
	}

	public int getiDisplayLength() {
		return iDisplayLength;
	}

	public void setiDisplayLength(int iDisplayLength) {
		this.iDisplayLength = iDisplayLength;
	}

	public String getsEcho() {
		return sEcho;
	}

	public void setsEcho(String sEcho) {
		this.sEcho = sEcho;
	}

	/**
	 * use #getsSortedAttribute_0() instead
	 */
	public int getiSortCol_0() {
		// Buggy 36984c995ed1 assumed iSortCol was a String. As I'm clueless, I'm extra-cautious
		Object sort0 = iSortCol.get(0);
		
		if (sort0 instanceof Number) {
			return ((Number) sort0).intValue();
		}
		if (sort0 instanceof String) {
			return Integer.valueOf((String) sort0);
		}
		throw new IllegalStateException("Value '" + sort0 + "' should either be a Number or a String but we got a "
				+ sort0.getClass().getName() + " instead. Maybe datatable config is wrong ?");
	}

	public void setiSortCol_0(int iSortCol_0) {
		this.iSortCol.put(0, iSortCol_0);
	}

	// legacy method
	public String getsSortDir_0() {
		return (String) sSortDir.get(0);
	}

	// legacy method
	public Object getsSortedAttribute_0() {
		Object o = mDataProp.get(getiSortCol_0());
		if (o == null) {
			o = getiSortCol_0();
		}
		return o;
	}

	// legacy method
	public void setsSortDir_0(String sSortDir_0) {
		this.sSortDir.put(0, sSortDir_0);
	}

	public String getsSearch() {
		return sSearch;
	}

	public void setsSearch(String sSearch) {
		this.sSearch = sSearch;
	}

	public Map<Integer, Object> getmDataProp() {
		return mDataProp;
	}

	public Object getmDataProp(Integer index) {
		return mDataProp.get(index);
	}

	public Map<Integer, Object> getiSortCol() {
		return iSortCol;
	}

	public Integer getiSortCol(Integer index) {
		return Integer.valueOf((String) iSortCol.get(index));
	}

	public Map<Integer, Object> getsSortDir() {
		return sSortDir;
	}

	public String getsSortDir(Integer index) {
		return (String) sSortDir.get(index);
	}

	public int getiSortingCols() {
		return iSortingCols;
	}

	public void setiSortingCols(int iSortingCols) {
		this.iSortingCols = iSortingCols;
	}

}
// IGNOREVIOLATIONS:END 
