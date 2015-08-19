/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

public class ProgressAwareMultipartResolver extends CommonsMultipartResolver {

	@Override
	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		String encoding = determineEncoding(request);
		FileUpload fileUpload = prepareFileUpload(encoding);

		UploadProgressListener newListener = new UploadProgressListener();

		fileUpload.setProgressListener(newListener);

		try {
			/* create the wrapper for the request */
			MultipartHttpServletRequest multiRequest = createMultipartHttpServletRequest(request, encoding, fileUpload);

			/* register the listener in the session */
			String ticket = UploadProgressListenerUtils.getUploadTicket(request);
			HttpSession session = request.getSession();
			UploadProgressListenerUtils.registerListener(session, ticket, newListener);

			return multiRequest;

		} catch (FileUploadBase.SizeLimitExceededException ex) {
			throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);

		} catch (FileUploadException ex) {
			throw new MultipartException("Could not parse multipart servlet request", ex);
		}
	}

	@SuppressWarnings("unchecked")
	private MultipartHttpServletRequest createMultipartHttpServletRequest(HttpServletRequest request, String encoding,
			FileUpload fileUpload) throws FileUploadException {

		List<FileItem> fileItems = ((ServletFileUpload) fileUpload).parseRequest(request); // NOSONAR
		MultipartParsingResult parsingResult = parseFileItems(fileItems, encoding);

		return new DefaultMultipartHttpServletRequest(request, parsingResult.getMultipartFiles(),
				parsingResult.getMultipartParameters(), parsingResult.getMultipartParameterContentTypes());
	}

}