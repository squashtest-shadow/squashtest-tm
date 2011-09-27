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
package org.squashtest.csp.core.bugtracker.spi;

import java.util.Locale;


/**
 * That interface will hold the label of the fields corresponding to the specific 
 * implementation of the bugtracker.  
 * 
 * All implementations of a connector should implement that too.
 * 
 * @author bsiri
 *
 */
public interface  BugTrackerInterfaceDescriptor {
	
	void setLocale(Locale locale);
	String getPriorityLabel();
	String getVersionLabel();
	String getAssigneeLabel();
	String getSummaryLabel();
	String getDescriptionLabel();
	String getCommentLabel();
	String getCategoryLabel();
	String getStatusLabel();
	
	String getNoVersionLabel();
	String getNoCategoryLabel();
	
	String getIssueIdLabel();
	String getSummaryNotMandatoryLabel();
	String getDescriptionNotMandatoryLabel();
	
	String getReportedInLabel();
	String getNoUserLabel();
	

}
