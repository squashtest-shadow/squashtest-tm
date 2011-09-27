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
package org.squashtest.csp.core.bugtracker.mantis;

import java.rmi.RemoteException;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;


/**
 * that class will convert a Mantis RemoteException to a BugTrackerRemoteException.
 * 
 * It's goal is to :
 * 	- provide a more detailed message for an user,
 *  - internationalize it,
 * 
 * @author bsiri
 *
 */

/*
 * 
 * as a developper, each RemoteException will be mapped by two keys in the MessageSource :
 * 		- the key that returns a String we will compare to remoteException.getMessage(), aka Mantis Key
 * 		- the key to look up for the internationalized version, aka Squash Key
 * 
 */
@Component("squashtest.core.bugtracker.BugTrackerExceptionConverter")
public class MantisExceptionConverter {

	private final ThreadLocal<Locale> threadLocalLocale = new ThreadLocal<Locale>();
	
	
	@Inject 
	private MessageSource messageSource;
	
	private boolean initialized=false;


	/* *************** keys that should match the Mantis error messages. Their initial values will hopefully match 
	 * the error messages if the key wasn't found in the message source. ********************* */
	private String strWrongCredentialsMantisMessage="Access Denied";
	private String strMandatorySummaryRequiredMantisMessage="Mandatory field \'summary\'";
	private String strMandatoryDescriptionRequiredMantisMessage="Mandatory field \'description\'";
	
	
	/* ***** keys for the message source to fetch the mantis messages ************************* */
	
	private final String strWrongCredentialsMantisKey = "squashtest.csp.tm.bugtracker.exception.mantis.mantismessage.accessdenied";
	private final String strMandatorySummaryRequiredMantisKey = "squashtest.csp.tm.bugtracker.exception.mantis.mantismessage.summary.mandatory";
	private final String strMandatoryDescriptionRequiredMantisKey = "squashtest.csp.tm.bugtracker.exception.mantis.mantismessage.description.mandatory";

	/* ***** keys for the message source to fetch an internationalized version **************** */
	
	private final String strWrongCredentialsSquashKey = "squashtest.csp.tm.bugtracker.exception.mantis.squashmessage.accessdenied";
	private final String strMandatorySummaryRequiredSquashKey = "squashtest.csp.tm.bugtracker.exception.mantis.squashmessage.summary.mandatory";
	private final String strMandatoryDescriptionRequiredSquashKey = "squashtest.csp.tm.bugtracker.exception.mantis.squashmessage.description.mandatory";
	
	private final String strUnkownExceptionSquashKey = "squashtest.csp.tm.bugtracker.exception.mantis.squashmessage.unknownexception";
	
	

	
	public MantisExceptionConverter(){
		threadLocalLocale.set(null);

	
	}

	
	public void setMessageSource (MessageSource messageSource){
		this.messageSource=messageSource;
	}
	
	public void setLocale(Locale locale){
		threadLocalLocale.set(locale);
	}
	
	public BugTrackerRemoteException convertException(RemoteException remoteException){
		
		BugTrackerRemoteException exception;
		
		if (! initialized) {
			init();
		}
		
		exception = setIfAccessDenied(remoteException);
		
		if (exception == null){
			exception = setIfMandatorySummaryNotSet(remoteException);
		}
		if (exception == null){
			exception = setIfMandatoryDescriptionNotSet(remoteException);
		}
		if (exception == null){
			exception = setUnknownException(remoteException);
		}		
		return exception;
		
	}
	
	
	/* ********************* private stuffs ************************************** */
	
	
	

	/* that init code will map the Mantis poor error messages to the appropriate 
	 * fields above.
	 * 
	 * if not found, the fields will be set with default value that (hopefully) match
	 * the default messages from Mantis Server.
	 * 
	 * The locale is irrelevant yet.
	 * 
	 * as a developper, you should feed that constructor with more labels when you meet uncovered RemoteExceptions
	 */
	private void init(){
		Locale locale = Locale.getDefault();
		
		strWrongCredentialsMantisMessage = messageSource.getMessage(strWrongCredentialsMantisKey, null, strWrongCredentialsMantisMessage,locale);
		strMandatorySummaryRequiredMantisMessage = messageSource.getMessage(strMandatorySummaryRequiredMantisKey, null, strMandatorySummaryRequiredMantisMessage, locale);
		strMandatoryDescriptionRequiredMantisMessage = messageSource.getMessage(strMandatoryDescriptionRequiredMantisKey, null, strMandatoryDescriptionRequiredMantisMessage, locale);
		
		
		initialized=true;
	}
	
	
	
	private BugTrackerRemoteException setIfAccessDenied(RemoteException remoteException){
		String message = remoteException.getMessage();
		if (message.equals(strWrongCredentialsMantisMessage )){
			String translation = messageSource.getMessage(strWrongCredentialsSquashKey, null, threadLocalLocale.get());
			return new BugTrackerNoCredentialsException(translation, remoteException);
		}
		return null;
	}
	
	private BugTrackerRemoteException setIfMandatorySummaryNotSet(RemoteException remoteException){
		String message = remoteException.getMessage();
		if (message.contains(strMandatorySummaryRequiredMantisMessage )){
			String translation = messageSource.getMessage(strMandatorySummaryRequiredSquashKey, null, threadLocalLocale.get());
			return new BugTrackerRemoteException(translation, remoteException);
		}
		return null;		
	}
	
	private BugTrackerRemoteException setIfMandatoryDescriptionNotSet(RemoteException remoteException){
		String message = remoteException.getMessage();
		if (message.contains(strMandatoryDescriptionRequiredMantisMessage )){
			String translation = messageSource.getMessage(strMandatoryDescriptionRequiredSquashKey, null, threadLocalLocale.get());
			return new BugTrackerRemoteException(translation, remoteException);
		}
		return null;		
	}	
	
	
	
	private BugTrackerRemoteException setUnknownException(RemoteException remoteException){
		String translation = messageSource.getMessage(strUnkownExceptionSquashKey, null, threadLocalLocale.get());
		return new BugTrackerRemoteException(translation+remoteException.getMessage(), remoteException );
	}
	
}
