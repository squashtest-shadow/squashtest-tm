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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;


/**
 * that class will convert a Mantis RemoteException to a BugTrackerRemoteException.
 * 
 * Its goal is to :
 * 	- provide a more detailed message for an user,
 *  - internationalize it,
 * 
 * @author bsiri
 * @reviewed-on 2011/11/23
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
	private interface MantisMessageKeys {
		String WRONG_CREDENTIAL = "squashtest.csp.tm.bugtracker.exception.mantis.mantismessage.accessdenied";
		String MANDATORY_SUMMARY_REQUIRED = "squashtest.csp.tm.bugtracker.exception.mantis.mantismessage.summary.mandatory";
		String MANDATORY_DESCRIPTION_REQUIRED = "squashtest.csp.tm.bugtracker.exception.mantis.mantismessage.description.mandatory";		
	}
	
	private interface SquashMessageKeys {
		String WRONG_CREDENTIAL = "squashtest.csp.tm.bugtracker.exception.mantis.squashmessage.accessdenied";
		String MANDATORY_SUMMARY_REQUIRED = "squashtest.csp.tm.bugtracker.exception.mantis.squashmessage.summary.mandatory";
		String MANDATORY_DESCRIPTION_REQUIRED = "squashtest.csp.tm.bugtracker.exception.mantis.squashmessage.description.mandatory";
		
		String UNKNOWN_EXCEPTION = "squashtest.csp.tm.bugtracker.exception.mantis.squashmessage.unknownexception";
	}

	private final ThreadLocal<Locale> threadLocalLocale = new ThreadLocal<Locale>();
	
	
	@Inject 
	private MessageSource messageSource;
	

	/* *************** keys that should match the Mantis error messages. Their initial values will hopefully match 
	 * the error messages if the key wasn't found in the message source. ********************* */
	private String wrongCredentialsMantisMessage = "Access Denied";
	private String mandatorySummaryRequiredMantisMessage = "Mandatory field \'summary\'";
	private String mandatoryDescriptionRequiredMantisMessage = "Mandatory field \'description\'";	
	
	
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
		
		BugTrackerRemoteException exception = setIfAccessDenied(remoteException);
		
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
	@PostConstruct
	public void init(){
		Locale locale = Locale.getDefault();
		
		wrongCredentialsMantisMessage = messageSource.getMessage(MantisMessageKeys.WRONG_CREDENTIAL, null, wrongCredentialsMantisMessage,locale);
		mandatorySummaryRequiredMantisMessage = messageSource.getMessage(MantisMessageKeys.MANDATORY_SUMMARY_REQUIRED, null, mandatorySummaryRequiredMantisMessage, locale);
		mandatoryDescriptionRequiredMantisMessage = messageSource.getMessage(MantisMessageKeys.MANDATORY_DESCRIPTION_REQUIRED, null, mandatoryDescriptionRequiredMantisMessage, locale);
	}
	
	private BugTrackerRemoteException setIfAccessDenied(RemoteException remoteException){
		String message = remoteException.getMessage();
		if (message.equals(wrongCredentialsMantisMessage )){
			String translation = messageSource.getMessage(SquashMessageKeys.WRONG_CREDENTIAL, null, threadLocalLocale.get());
			return new BugTrackerNoCredentialsException(translation, remoteException);
		}
		return null;
	}
	
	private BugTrackerRemoteException setIfMandatorySummaryNotSet(RemoteException remoteException){
		String message = remoteException.getMessage();
		if (message.contains(mandatorySummaryRequiredMantisMessage )){
			String translation = messageSource.getMessage(SquashMessageKeys.MANDATORY_SUMMARY_REQUIRED, null, threadLocalLocale.get());
			return new BugTrackerRemoteException(translation, remoteException);
		}
		return null;		
	}
	
	private BugTrackerRemoteException setIfMandatoryDescriptionNotSet(RemoteException remoteException){
		String message = remoteException.getMessage();
		if (message.contains(mandatoryDescriptionRequiredMantisMessage )){
			String translation = messageSource.getMessage(SquashMessageKeys.MANDATORY_DESCRIPTION_REQUIRED, null, threadLocalLocale.get());
			return new BugTrackerRemoteException(translation, remoteException);
		}
		return null;		
	}	
	
	private BugTrackerRemoteException setUnknownException(RemoteException remoteException){
		String translation = messageSource.getMessage(SquashMessageKeys.UNKNOWN_EXCEPTION, null, threadLocalLocale.get());
		return new BugTrackerRemoteException(translation+remoteException.getMessage(), remoteException );
	}
	
}
