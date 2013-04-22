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

import org.codehaus.jackson.annotate.JsonAnySetter;

/**
 * Parameters of the draw request sent by a datatable.
 *
 * @author Gregory Fouquet
 *
 */
// NOSONAR names have to match JSON structure  
public class DataTableDrawParameters {
	
	private static final String M_DATA_PROP_PREFIX = "mDataProp_";
	private static final int M_DATA_PROP_SUFFIX_INDEX = M_DATA_PROP_PREFIX.length() +1 ;
	
	private String sEcho;
	
	private int iDisplayStart;
	private int iDisplayLength;
	
	private int iSortCol_0;
	private String sSortDir_0;
	private String sSearch;
	private Map<Integer, Object> mDataProp = new HashMap<Integer, Object>();



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
	
	
	/** 
	 * use #getsSortedAttribute_0() instead 
	 */
	@Deprecated
	public int getiSortCol_0() { // NOSONAR names have to match JSON structure
		return iSortCol_0;
	}

	public void setiSortCol_0(int iSortCol_0) { // NOSONAR names have to match JSON structure
		this.iSortCol_0 = iSortCol_0;
	}

	
	public String getsSortDir_0() { // NOSONAR names have to match JSON structure
		return sSortDir_0;
	}
	
	public Object getsSortedAttribute_0(){
		Object o = mDataProp.get(iSortCol_0);
		if (o==null){
			o = iSortCol_0;
		}
		return o;
	}

	public void setsSortDir_0(String sSortDir_0) { // NOSONAR names have to match JSON structure
		this.sSortDir_0 = sSortDir_0;
	}

	public String getsSearch() {
		return sSearch;
	}

	public void setsSearch(String sSearch) {
		this.sSearch = sSearch;
	}
	
	public Map<Integer, Object> getmDataProp(){
		return mDataProp;
	}
	
	public Object getmDataProp(Integer index){
		return mDataProp.get(index);
	}
	
	@JsonAnySetter	
	public void setUnknown(String unknownAttribute, Object value){
		if (unknownAttribute.matches(M_DATA_PROP_PREFIX)){
			Integer propIndex = Integer.valueOf(unknownAttribute.substring(M_DATA_PROP_SUFFIX_INDEX));
			mDataProp.put(propIndex, value);
		}
//		else  f*** it 
	}
	
}
