/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.web.internal.fileupload;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * This class wil watch over file uploads, by associating data to a temporary upload ticket. Once the upload is over the
 * ticket is unregistered and all the data are removed from the session.
 * 
 * That ticket will be used both for : - registering the upload status (ie, the listener), that will keep us informed of
 * the rate of completion during the actual uploading phase, - storing a summary of the whole operation when it's done
 * and that the client can query for informations like which file failed and which file succeeded.
 * 
 * Those informations are both stored into the HttpSession, and related via the Ticket.
 * 
 * 
 * Basically there are three categories of operation here : - generating and retrieving a ticket from a query string, -
 * registering, retrieving and unregistering upload listeners, - registering, retrieving and unregistering the summary
 * 
 * @author bsiri
 * 
 */

public final class UploadProgressListenerUtils {

	private static final String UPLOAD_LISTNER_MAP_KEY = "upload-listener-map";
	private static final String UPLOAD_SUMMARY_MAP = "upload-summary-map";

	private UploadProgressListenerUtils() {

	}

	/* ********************************* upload ticket section ********************************* */

	/*
	 * works only if the "upload-ticket" thing is part of the query string (ie :
	 * http://my/upload/related/request?upload-ticket=12345)
	 * 
	 * The reasons for this is if this parameter was passed as a POST parameter, one would have to parse the request
	 * body to retrieve it. In the case of a file upload, it would mean to upload the file first.
	 */
	public static String getUploadTicket(HttpServletRequest request) {
		String uploadTicket = request.getParameter("upload-ticket");
		String uploadKey;
		if (uploadTicket != null) {
			uploadKey = "upload-ticket-" + uploadTicket;
		} else {
			uploadKey = null;
		}

		return uploadKey;
	}

	/*
	 * generate a new ticket number
	 */
	public static String generateUploadTicket() {
		Double dTicket = Math.random() * (Long.MAX_VALUE - 1);
		Long newTicket = Long.valueOf(Math.round(dTicket));

		return newTicket.toString();
	}

	/*
	 * remove anything related to a ticket
	 */
	public static void unregisterTicket(HttpSession session, String ticket) {
		unregisterListeners(session, ticket);
		unregisterUploadSummary(session, ticket);
	}

	/* ****************************** upload listener section *********************************** */

	/*
	 * Registers a listener for the ticket key and into the Session session
	 */
	@SuppressWarnings("unchecked")
	public static void registerListener(HttpSession session, String key, UploadProgressListener listener) {
		MultiValueMap<String, UploadProgressListener> listenerMap = (MultiValueMap<String, UploadProgressListener>) session
				.getAttribute(UPLOAD_LISTNER_MAP_KEY);

		// create if doesn't exists already
		if (listenerMap == null) {
			listenerMap = new LinkedMultiValueMap<String, UploadProgressListener>();
			session.setAttribute(UPLOAD_LISTNER_MAP_KEY, listenerMap);
		}

		listenerMap.add(key, listener);
	}

	/*
	 * Wrapper for the above (optional)
	 */
	public static void registerListener(HttpServletRequest request, UploadProgressListener listener) {
		String ticket = getUploadTicket(request);
		HttpSession session = request.getSession();
		registerListener(session, ticket, listener);
	}

	/*
	 * Will clean the session from the content related to the given ticket
	 */
	@SuppressWarnings("unchecked")
	public static void unregisterListeners(HttpSession session, String key) {
		MultiValueMap<String, UploadProgressListener> listenerMap = (MultiValueMap<String, UploadProgressListener>) session
				.getAttribute(UPLOAD_LISTNER_MAP_KEY);
		if (listenerMap != null) {
			listenerMap.remove(key);
		}
	}

	/*
	 * get the list of all registered uploadListener for a ticket in the given session
	 * 
	 * Note : the current implementation uses a MultiValueMap, so more than one Listener could be registered for the
	 * same ticket. This was done in case we need some day 1 progress bar for each file being uploaded.
	 */
	@SuppressWarnings("unchecked")
	public static List<UploadProgressListener> getRegisteredListener(HttpSession session, String ticket) {
		MultiValueMap<String, UploadProgressListener> listenerMap = (MultiValueMap<String, UploadProgressListener>) session
				.getAttribute(UPLOAD_LISTNER_MAP_KEY);

		if (listenerMap == null) {
			return null;
		}

		return listenerMap.get(ticket);

	}

	/* *************************************** upload summary section ************************************** */

	/*
	 * Registers a summary for the ticket key and into the Session session
	 */
	@SuppressWarnings("unchecked")
	public static void registerUploadSummary(HttpSession session, String key, List<UploadSummary> summary) {
		MultiValueMap<String, List<UploadSummary>> summaryMap = (MultiValueMap<String, List<UploadSummary>>) session
				.getAttribute(UPLOAD_SUMMARY_MAP);

		// create if doesn't exists already
		if (summaryMap == null) {
			summaryMap = new LinkedMultiValueMap<String, List<UploadSummary>>();
			session.setAttribute(UPLOAD_SUMMARY_MAP, summaryMap);
		}

		summaryMap.add(key, summary);
	}

	/*
	 * Wrapper for the above (optional)
	 */
	public static void registerUploadSummary(HttpServletRequest request, List<UploadSummary> summary) {
		String ticket = getUploadTicket(request);
		HttpSession session = request.getSession();
		registerUploadSummary(session, ticket, summary);
	}

	/*
	 * Will clean the session from the content related to the given ticket
	 */
	@SuppressWarnings("unchecked")
	public static void unregisterUploadSummary(HttpSession session, String key) {
		MultiValueMap<String, List<UploadSummary>> summaryMap = (MultiValueMap<String, List<UploadSummary>>) session
				.getAttribute(UPLOAD_SUMMARY_MAP);
		if (summaryMap != null) {
			summaryMap.remove(key);
		}
	}

	/*
	 * get the list of all registered summaries for a ticket in the given session
	 */
	@SuppressWarnings("unchecked")
	public static Object getUploadSummary(HttpSession session, String ticket) {
		MultiValueMap<String, List<UploadSummary>> summaryMap = (MultiValueMap<String, List<UploadSummary>>) session
				.getAttribute(UPLOAD_SUMMARY_MAP);

		if (summaryMap == null) {
			return null;
		}

		return summaryMap.get(ticket);

	}

}
