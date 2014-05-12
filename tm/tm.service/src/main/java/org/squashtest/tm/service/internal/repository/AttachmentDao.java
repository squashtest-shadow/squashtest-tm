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
package org.squashtest.tm.service.internal.repository;

import java.util.List;
import java.util.Set;

import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.attachment.Attachment;

public interface AttachmentDao extends EntityDao<Attachment>{
	
	Attachment findAttachmentWithContent(Long attachmentId);
	
	Set<Attachment> findAllAttachments(Long attachmentListId);
	
	//cannot override the final remove(long) method, so I add here a new one
	void removeAttachment(Long attachmentId);
	
	List<Attachment> findAllAttachmentsFiltered(Long attachmentListId, 	PagingAndSorting pas);
}
