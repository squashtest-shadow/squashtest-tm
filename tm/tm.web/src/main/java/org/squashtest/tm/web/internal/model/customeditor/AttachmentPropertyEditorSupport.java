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
package org.squashtest.tm.web.internal.model.customeditor;

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentContent;

public class AttachmentPropertyEditorSupport extends PropertyEditorSupport {

	@Override
	public void setValue(Object value) {
		if (value instanceof MultipartFile) {
			MultipartFile multipartFile = (MultipartFile) value;
			try {
				Attachment attachment = new Attachment(multipartFile.getOriginalFilename());
				AttachmentContent content = new AttachmentContent();
				content.setContent(multipartFile.getInputStream());
				attachment.setContent(content);
				attachment.setSize(multipartFile.getSize());

				super.setValue(attachment);
			} catch (IOException ex) {
				throw new IllegalArgumentException("Cannot read contents of multipart file", ex);
			}

		} else if (value instanceof byte[]) {
			Attachment attachment = new Attachment();
			AttachmentContent content = new AttachmentContent();
			content.setContent(new ByteArrayInputStream((byte[]) value));
			attachment.setContent(content);
			super.setValue(attachment);
		} else {
			if (value == null) {
				super.setValue(null);
			} else {
				Attachment attachment = new Attachment();
				AttachmentContent content = new AttachmentContent();
				content.setContent(new ByteArrayInputStream(value.toString().getBytes()));
				attachment.setContent(content);
				super.setValue(attachment);
			}
		}

	}

	@Override
	public String getAsText() {
		InputStream value = (InputStream) getValue();
		return (value != null ? value.toString() : "");
	}

	@Override
	public void setAsText(String text) {
	}

	// own method
	public static Attachment makeAttachment(MultipartFile file) {
		try {
			Attachment attachment = new Attachment(file.getOriginalFilename());
			AttachmentContent content = new AttachmentContent();
			content.setContent(file.getInputStream());
			attachment.setContent(content);
			attachment.setSize(file.getSize());

			return attachment;
		} catch (IOException ex) {
			throw new IllegalArgumentException("Cannot read contents of multipart file", ex);
		}
	}

}
