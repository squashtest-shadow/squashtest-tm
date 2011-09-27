/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.domain.report;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *  This is the abstract class representing a ReportCategory, which is logically an qualifying attribute of a Report
 *  and practically a container of Report.
 *  
 *  Registering or unregistering a Report to/from a ReportCategory just happens the same way than a ReportFactory 
 *  registers/unregisters a ReportCategory, please refer to its documentation for more details.
 * 
 *  
 *  Implementing ReportCategory :
 *  =============================
 *  
 *  * just set the key name in an init section or in the constructor, using setResourceKeyName(). This key name is meant
 *  to be used as a key in a MessageSource.
 *  
 *  * remember that the constructor must (explicitely or implicitely) call super() in order to register
 *  the new instance of a subclass of ReportCategory against the ReportFactory.
 * 
 * @author bsiri
 *
 */

public abstract class ReportCategory {
	

	private static Integer idReportCounter=0;
	
	private Integer id;
		
	private final List<Report> reportList = new LinkedList<Report>();
	
	
	private String resourceKeyName;

	private final ReportFactory factoryInstance = ReportFactory.getInstance();
	
	public ReportCategory(){
		factoryInstance.addCategory(this);
	}


	/**
	 * @return the id of this ReportCategory bean instance. 
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 */
	void setId(Integer id) {
		this.id = id;
	}


	/** 
	 * @return the list of the Reports registered in this ReportCategory.
	 */
	public List<Report> getReportList() {
		return reportList;
	}
	
	/**
	 * @param report : the Report to register in this Category.
	 */
	public void addReport(Report report){
		registerReport(report);
		reportList.add(report);
	}
	
	/*
	 * This is the dirty piece of code that will generate and attach an id to a report.
	 */
	private void registerReport(Report report){
		report.setId(idReportCounter++);
	}

	/**
	 * @param id the id of the Report to find. 
	 * @return the Report registered under this id, or null if not found.
	 */
	public Report findReportById(Integer id){
		for (Report report : reportList){
			if (report.getId().equals(id)) return report;
		}
		return null;
	}
	
	/** 
	 * @return the key to look for in a MessageSource to get a localized name for this ReportCategory.
	 */
	public String getResourceKeyName() {
		return resourceKeyName;
	}

	
	/**
	 * @param resourceKeyName the key corresponding to this ReportCategory.
	 */
	protected void setResourceKeyName(String resourceKeyName) {
		this.resourceKeyName = resourceKeyName;
	}
	
	/**
	 * Will unregister a previously registered Report.
	 * 
	 * @param report a Report to unregister
	 */
	public void removeReport(Report report){
		removeReport(report.getId());
	}
	
	/**
	 * Will unregister a previously registered Report.
	 * 
	 * @param reportId the id of the report to unregister.
	 */
	public void removeReport(Integer reportId){
		ListIterator<Report> iterator = reportList.listIterator();
		while (iterator.hasNext()){
			Report report  = iterator.next();
			if (report.getId().equals(reportId)){
				iterator.remove();
				return;
			}
		}
	}

}
