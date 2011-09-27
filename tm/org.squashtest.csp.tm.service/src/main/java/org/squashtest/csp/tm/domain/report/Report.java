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

import org.squashtest.csp.tm.domain.report.query.ReportQuery;
import org.squashtest.csp.tm.domain.report.query.ReportQueryFactory;
import org.squashtest.csp.tm.domain.report.view.ReportViewCatalog;


/**
 *  A Report is functionally the main concept of the Report API. However it's not exactly the most technical hard to 
 *  implement.
 *  
 *  A Report belongs to a ReportCategory, to which it must register to (more on that later), and is of a ReportType.
 *  Let's repeat it again, a Report should be a bean, a singleton
 *  
 *  A Report have a name and a description. The name is, like some other entities of this package, a key that will be
 *  searched for in a MessageSource. 
 *  
 *  A Report is registered to a ReportCategory, is of a ReportType, and must be provided with a ReportQueryFactory 
 *  (that will generate a query corresponding to fill that Report).
 *  
 *  FIXME
 *  In this version the description is not yet localized.
 *  
 *  
 *  Implementing a Report :
 *  =======================
 *  
 *  To implement a report you need : 
 *    - to set the key and the description in an init section or via the constructor, 
 *    - implement the method setReportType(),
 *    - implement the method setReportCategory() (that must call the said ReportCategory.addReport() method),
 *    - call those two setters with the right instances of ReportType and ReportCategory.
 *    
 *   You may also set a particular ReportQueryFactory at constructor time but you also can let it injected via 
 *   the IoC engine (if you have one). 
 *   
 *   Likewise, you can set up its view catalog at construction time.
 *   
 *   NOTE : the reason why you need to implement the setReportCategory() and setReportType() is that you may
 *   then add an @Resource annotation on it. The container will this automagically call the setter at
 *   construction time and will feed it with the resource you named (the real instances of ReportType or ReportCategory)
 *    
 * 
 * 
 * @author bsiri
 *
 */

public abstract class Report {
	
	
	private Integer id;	
	protected ReportType reportType;
	protected String resourceKeyName;
	protected String resourceKeyDescription;
	protected ReportCategory reportCategory;
	protected ReportQueryFactory queryFactory;
	protected ReportViewCatalog viewCatalog;

	
	/**
	 * 
	 * @return the id of this Report.
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @return the ReportType instance to which this Report is assigned.
	 */
	public ReportType getReportType() {
		return reportType;
	}

	/**
	 * 
	 * @return the ReportCategory instance to which this Report is assigned.
	 */
	public ReportCategory getReportCategory() {
		return reportCategory;
	}


	void setId(Integer id) {
		this.id = id;
	}

	/**
	 * 
	 * @return the name of this Report as a key for a MessageSource.
	 */
	public String getResourceKeyName() {
		return resourceKeyName;
	}

	/**
	 * Best called at construction time (it's protected)
	 * @param resourceKeyName the key corresponding to that Report in the MessageSource.
	 */
	protected void setResourceKeyName(String resourceKeyName) {
		this.resourceKeyName = resourceKeyName;
	}
	
	/**
	 * 
	 * @return the description of this Report as a key for a MessageSource.
	 */
	public String getResourceKeyDescription() {
		return resourceKeyName;
	}

	/**
	 * Best called at construction time (it's protected).
	 * @param resourceKeyDescription the key corresponding to that description in the MessageSource.
	 */
	protected void setResourceKeyDescription(String resourceKeyDescription) {
		this.resourceKeyDescription = resourceKeyDescription;
	}

	
	/**
	 * 
	 * @return the ReportQueryFactory instance that had been assigned to that Report.
	 */
	public ReportQueryFactory getQueryFactory(){
		return queryFactory;
	}
	
	/**
	 * 
	 * @param queryFactory the ReportQueryFactory being assigned to that Report.
	 */
	public void setQueryFactory(ReportQueryFactory queryFactory){
		this.queryFactory=queryFactory;
	}
	
	
	/**
	 * This method is simply a wrapper that will call makeReportQuery() of the ReportQueryFactory
	 * @return the ReportQuery corresponding to that Report.
	 */
	public ReportQuery createReportQuery(){
		return getQueryFactory().makeReportQuery();
	}

	
	protected void setViewCatalog(ReportViewCatalog viewCatalog){
		this.viewCatalog=viewCatalog;
	}
	
	public ReportViewCatalog getViewCatalog(){
		return viewCatalog;
	}
	

	/* ** those setters should be private and autowired with respect to the corresponding beans */


	/**
	 * will set the ReportType for this report.
	 */
	/*
	 * this method is still abstract because I need to get the actual bean from the app context and pass it
	 * as a parameter. That's unpractical, let's change that asap. 
	 */
	abstract protected void setReportType(ReportType reportType);

	/**
	 * will set the ReportCategory for this report. It also must call the ReportCategory.addReport() in order 
	 * to get itself registered. 
	 */
	/*
	 * this method is still abstract because I need to get the actual bean from the app context and pass it
	 * as a parameter. That's unpractical, let's change that asap. 
	 */
	abstract protected void setReportCategory(ReportCategory reportCategory);
	
}
