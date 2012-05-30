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
		threadLocalLocale.set(Locale.getDefault());
	}
	
	
	public void setMessageSource(MessageSource messageSource){
		this.messageSource=messageSource;
	}
	
	@Override
	public void setLocale(Locale locale){
		this.threadLocalLocale.set(locale);
	}
	
	
	@Override
	public String getPriorityLabel() {
		return getValue(strMantisGetPriority);

	}

	@Override
	public String getVersionLabel() {
		return getValue(strMantisGetVersion);
	}

	@Override
	public String getAssigneeLabel() {
		return getValue(strMantisGetAssignee);
	}

	@Override
	public String getSummaryLabel() {
		return getValue(strMantisGetSummary);
	}

	@Override
	public String getDescriptionLabel() {
		return getValue(strMantisGetDescription);
	}

	@Override
	public String getCommentLabel() {
		return getValue(strMantisGetComment);
	}


	@Override
	public String getCategoryLabel() {
		return getValue(strMantisGetCategory);
	}


	@Override
	public String getNoVersionLabel() {
		return getValue(strMantisNoVersion);
	}


	@Override
	public String getNoCategoryLabel() {
		return getValue(strMantisNoCategory);
	}
	
	@Override
	public String getIssueIdLabel(){
		return getValue(strMantisBugId);	
	}
	
	@Override
	public String getSummaryNotMandatoryLabel(){
		return getValue(strMantisSummaryNotMandatory);
	}


	@Override
	public String getReportedInLabel() {
		return getValue(strMantisReportedIn);
	}
	
	@Override
	public String getNoUserLabel(){
		return getValue(strMantisNoUser);
	}
	
	@Override 
	public String getStatusLabel(){
		return getValue(strMantisGetStatus);
	}
	
	@Override
	public String getDescriptionNotMandatoryLabel(){
		return getValue(strMantisDescriptionNotMandatory);
	}
	

	
	/* *************************** private stuffs ************************* */
	private String getValue(String key){
		return messageSource.getMessage(key, null, threadLocalLocale.get());	
	}

}
