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
package org.squashtest.tm.service.attachment;

import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentHolder;

@Transactional(readOnly = true)
public interface AttachmentFinderService {
	PagedCollectionHolder<List<Attachment>> findPagedAttachments(long attachmentListId, PagingAndSorting pas);

	PagedCollectionHolder<List<Attachment>> findPagedAttachments(AttachmentHolder attached, PagingAndSorting pas);

	Attachment findAttachment(Long attachmentId);

	Set<Attachment> findAttachments(Long attachmentListId);

	String findAttachmentShortName(Long attachmentId);

}
