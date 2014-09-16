/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.service.attachment.AttachmentManagerService;
import org.squashtest.tm.web.internal.fileupload.UploadContentFilterUtil;
import org.squashtest.tm.web.internal.fileupload.UploadProgress;
import org.squashtest.tm.web.internal.fileupload.UploadProgressListener;
import org.squashtest.tm.web.internal.fileupload.UploadProgressListenerUtils;
import org.squashtest.tm.web.internal.fileupload.UploadSummary;
import org.squashtest.tm.web.internal.http.ContentTypes;

@Controller
@RequestMapping("/attach-list/{attachListId}/attachments")
public class AttachmentController {

	private static final int EOF = -1;
	private static final int NO_PROGRESS = -1;
	private static final String UPLOAD_URL = "/upload";
	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentController.class);

	@Inject
	private AttachmentManagerService attachmentManagerService;

	@Inject
	private MessageSource messageSource;

	@Inject
	private UploadContentFilterUtil filterUtil;

	private static final String STR_UPLOAD_STATUS_OK = "dialog.attachment.summary.statusok.label";
	private static final String STR_UPLOAD_STATUS_WRONGFILETYPE = "dialog.attachment.summary.statuswrongtype.label";

	@InitBinder
	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
		binder.registerCustomEditor(Attachment.class, new UploadedDataPropertyEditorSupport());
	}

	/* ****************************** attachments *************************************** */

	@RequestMapping(value = "/display", method = RequestMethod.GET)
	public ModelAndView displayAttachments(@PathVariable("attachListId") long attachListId) {
		Set<Attachment> attachmentSet = attachmentManagerService.findAttachments(attachListId);

		ModelAndView mav = new ModelAndView("fragment/generics/attachment-display");
		mav.addObject("attachmentSet", attachmentSet);
		mav.addObject("attachListId", attachListId);

		return mav;
	}

	/* *********************************** upload ************************************** */

	/*
	 *
	 * Four operations are defined here :
	 *
	 * - prelude : will give a Ticket to that particular upload request that'll be used to store and retrieve
	 * informations later. - upload : will upload the files themselves - poll : while uploading the client may asks how
	 * far the job is done - finalize : sends a summary back to the client and relieve the resources
	 */

	// prelude to the upload in order to get a ticket
	@RequestMapping(value = UPLOAD_URL, method = RequestMethod.POST, params = "!upload-ticket")
	public @ResponseBody
	String prepareUpload() {
		return UploadProgressListenerUtils.generateUploadTicket();
	}

	// uploads the file themselves and build the upload summary on the fly
	@RequestMapping(value = UPLOAD_URL, method = RequestMethod.POST, params = "upload-ticket")
	public ModelAndView uploadAttachment(HttpServletRequest servletRequest,
			@RequestParam("attachment[]") List<UploadedData> attachments, @PathVariable long attachListId,
			Locale locale)
					throws IOException {

		List<UploadSummary> summary = new LinkedList<UploadSummary>();

		for (UploadedData upload : attachments) {

			LOGGER.trace("AttachmentController : adding attachment " + upload.name);

			// file type checking
			boolean shouldProceed = filterUtil.isTypeAllowed(upload);
			if (!shouldProceed) {
				summary.add(new UploadSummary(upload.name, getUploadSummary(STR_UPLOAD_STATUS_WRONGFILETYPE,
						locale), UploadSummary.INT_UPLOAD_STATUS_WRONGFILETYPE));
			} else {
				attachmentManagerService.addAttachment(attachListId, upload);

				summary.add(new UploadSummary(upload.name, getUploadSummary(STR_UPLOAD_STATUS_OK, locale),
						UploadSummary.INT_UPLOAD_STATUS_OK));
			}
		}

		// by design the last file uploaded is empty and has no name. We'll strip that from the summary.
		summary = stripEmptySummary(summary);

		// store the summary then return
		UploadProgressListenerUtils.registerUploadSummary(servletRequest, summary);
		return  new ModelAndView("fragment/import/attachment-success");
	}

	// by design the last file uploaded is empty and has no name. We'll strip that from the summary.
	private List<UploadSummary> stripEmptySummary(List<UploadSummary> summary) {
		int totalAttachment = summary.size();
		if (totalAttachment > 0 && summary.get(totalAttachment - 1).getName().isEmpty()) {
			summary.remove(totalAttachment - 1);
		}
		return summary;
	}

	// answers the polls regarding upload status
	@RequestMapping(value = UPLOAD_URL, method = RequestMethod.GET, params = "upload-ticket", produces=ContentTypes.APPLICATION_JSON)
	public @ResponseBody
	UploadProgress pollUploadStatus(HttpServletRequest request) {
		String ticket = UploadProgressListenerUtils.getUploadTicket(request);
		UploadProgress progress;

		if (ticket == null) {
			throw new IllegalArgumentException("Requested upload status for an unspecified upload ticket");
		}

		List<UploadProgressListener> listenerList = UploadProgressListenerUtils.getRegisteredListener(
				request.getSession(), ticket);

		// todo : better handling of exceptions.
		if (listenerList == null) {
			progress = createProgress();
		} else {
			UploadProgressListener listener = listenerList.get(0);
			if (listener == null) {
				progress = createProgress();
			} else {
				progress = listener.getStatus();
			}
		}

		return progress;
	}

	private UploadProgress createProgress() {
		UploadProgress progress;
		progress = new UploadProgress();
		progress.setPercentage(NO_PROGRESS);
		return progress;
	}

	// finalize the upload and deallocate the resources.
	@SuppressWarnings("unchecked")
	@RequestMapping(value = UPLOAD_URL, method = RequestMethod.DELETE, params = "upload-ticket")
	public @ResponseBody
	List<UploadSummary> finalizeUpload(HttpServletRequest servletRequest) {

		// get the ticket
		HttpSession session = servletRequest.getSession();
		String uploadTicket = UploadProgressListenerUtils.getUploadTicket(servletRequest);

		List<UploadSummary> summary = null;

		if (uploadTicket == null) {
			// unlikely to happen. If it does it's probably a bug, but meh
			LOGGER.trace("AttachmentController : WARNING : completed upload request without upload ticket");
		} else {
			// get the summary;
			summary = (List<UploadSummary>) UploadProgressListenerUtils.getUploadSummary(session, uploadTicket);

			// unregister the ticket and all the content bound to it
			UploadProgressListenerUtils.unregisterTicket(session, uploadTicket);
		}

		return summary;

	}

	/* ***************************** download ************************************* */

	@RequestMapping(value = "/download/{attachemendId}", method = RequestMethod.GET)
	public @ResponseBody
	void downloadAttachment(@PathVariable("attachemendId") long attachmentId, HttpServletResponse response) {

		try {
			Attachment attachment = attachmentManagerService.findAttachment(attachmentId);
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition",
					"attachment; filename=" + attachment.getName().replace(" ", "_"));

			ServletOutputStream outStream = response.getOutputStream();

			attachmentManagerService.writeContent(attachmentId, outStream);
		} catch (IOException e) {
			LOGGER.warn("Error happened during attachment download : " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		} finally {
		}
	}

	private String getUploadSummary(String key, Locale locale) {
		return messageSource.getMessage(key, null, locale);
	}

}
