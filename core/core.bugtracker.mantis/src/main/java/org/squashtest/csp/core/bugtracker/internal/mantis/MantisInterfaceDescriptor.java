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
package org.squashtest.csp.core.bugtracker.internal.mantis;



import java.util.Locale;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;


@Component("squashtest.core.bugtracker.BugTrackerInterfaceDescriptor")
public class MantisInterfaceDescriptor implements BugTrackerInterfaceDescriptor {
	
	private static final String strMantisGetPriority = "squashtest.csp.tm.bugtracker.interface.mantis.priority.label";
	private static final String strMantisGetVersion = "squashtest.csp.tm.bugtracker.interface.mantis.version.label";
	private static final String strMantisGetAssignee = "squashtest.csp.tm.bugtracker.interface.mantis.assignee.label";
	private static final String strMantisGetSummary = "squashtest.csp.tm.bugtracker.interface.mantis.summary.label";
	private static final String strMantisGetDescription = "squashtest.csp.tm.bugtracker.interface.mantis.description.label";
	private static final String strMantisGetComment = "squashtest.csp.tm.bugtracker.interface.mantis.comment.label";
	private static final String strMantisGetCategory = "squashtest.csp.tm.bugtracker.interface.mantis.category.label";
	private static final String strMantisGetStatus = "squashtest.csp.tm.bugtracker.interface.mantis.status.label";
	
	private static final String strMantisNoVersion = "squashtest.csp.tm.bugtracker.interface.mantis.noversion.label";
	private static final String strMantisNoCategory = "squashtest.csp.tm.bugtracker.interface.mantis.nocategory.label";
	
	private static final String strMantisBugId = "squashtest.csp.tm.bugtracker.interface.mantis.bugid.label";
	private static final String strMantisSummaryNotMandatory = "squashtest.csp.tm.bugtracker.interface.mantis.summarynomandatory.label";
	private static final String strMantisReportedIn = "squashtest.csp.tm.bugtracker.interface.mantis.reportedin.label";
	private static final String strMantisNoUser = "squashtest.csp.tm.bugtracker.interface.mantis.nouser.label";
	private static final String strMantisDescriptionNotMandatory ="squashtest.csp.tm.bugtracker.interface.mantis.descriptionnomandatory.label";
	
	
	private final ThreadLocal<Locale> threadLocalLocale = new ThreadLocal<Locale>();
	
	@Inject 
	private MessageSource messageSource;
	
	
	public MantisInterfaceDescriptor(){
		threadLocalLocale.set(LocaleContextHolder.getLocale());
	}
	
	
	public void setMessageSource(MessageSource messageSource){
		this.messageSource=messageSource;
	}

	
	@Override
	public void setLocale(Locale locale){
		threadLocalLocale.set(locale);
	}
	
	// ***************** labels for the issue report popup fields *******************
	
	@Override
	public String getReportPriorityLabel() {
		return getValue(strMantisGetPriority);

	}

	@Override
	public String getReportVersionLabel() {
		return getValue(strMantisGetVersion);
	}

	@Override
	public String getReportAssigneeLabel() {
		return getValue(strMantisGetAssignee);
	}

	@Override
	public String getReportCategoryLabel() {
		return getValue(strMantisGetCategory);
	}

	@Override
	public String getReportSummaryLabel() {
		return getValue(strMantisGetSummary);
	}
	

	@Override
	public String getReportDescriptionLabel() {
		return getValue(strMantisGetDescription);
	}

	@Override
	public String getReportCommentLabel() {
		return getValue(strMantisGetComment);
	}

	
	@Override
	public String getEmptyVersionListLabel() {
		return getValue(strMantisNoVersion);
	}

	@Override
	public String getEmptyCategoryListLabel() {
		return getValue(strMantisNoCategory);
	}
	
	@Override
	public String getEmptyAssigneeListLabel(){
		return getValue(strMantisNoUser);
	}
	
	
	
	// ****************** issue tables labels ***********************
	

	@Override
	public String getTableIssueIDHeader() {
		return getValue(strMantisBugId);
	}


	@Override
	public String getTableSummaryHeader() {
		return getValue(strMantisSummaryNotMandatory);
	}


	@Override
	public String getTablePriorityHeader() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getTableStatusHeader() {
		return getValue(strMantisGetStatus);
	}


	@Override
	public String getTableDescriptionHeader() {
		return getValue(strMantisDescriptionNotMandatory);
	}


	@Override
	public String getTableAssigneeHeader() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getTableReportedInHeader() {
		return getValue(strMantisReportedIn);
	}


	@Override
	public String getTableNoAssigneeLabel() {
		// TODO Auto-generated method stub
		return null;
	}


	
	/* *************************** private stuffs ************************* */
	private String getValue(String key){
		return messageSource.getMessage(key, null, threadLocalLocale.get());	
	}


}
