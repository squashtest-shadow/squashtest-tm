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
package org.squashtest.csp.tm.web.internal.utils;



/**
 * 
 * 	UNUSED SINCE : squashtest tag IT-08-end (unofficially, rev 1046). 
 * 
 *   Formerly used in the listener system for file upload.
 *   
 *   This ServletRequestListener was in charge of unregistering unused UploadProgressListeners once the request
 *   was treated (i.e., cleaning the HttpSession from now-useless data).
 *   
 *     
 *   This operation is now handled by the AttachmentController.uploadAttachment() method.
 *   
 */



import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.squashtest.csp.tm.web.internal.fileuploadutils.UploadProgressListenerUtils;

public class FileUploadRequestListener implements ServletRequestListener {

	
	//this listener will destroy in the session any content-upload-related informations in
	//the session. The target informations are mapped by a 'ticket' 
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		
		ServletRequest request = sre.getServletRequest();
		String contentType = request.getContentType();
		
		if (contentType==null){
			return;
		}

		
		if ((contentType.contains("multipart/form-data"))&&(request instanceof HttpServletRequest)){
			
			HttpServletRequest servletRequest = (HttpServletRequest) request;
			HttpSession session = servletRequest.getSession();
	
			
			String uploadTicket = UploadProgressListenerUtils.getUploadTicket(servletRequest);
			
			if (uploadTicket==null){
				//unlikely to happen. If it does it's probably a bug, but meh
				return;
			}
			
			//unregister all the listeners		
			UploadProgressListenerUtils.unregisterListeners(session, uploadTicket);
	
		}
	}

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		//not interested in the init part		
	}

}
