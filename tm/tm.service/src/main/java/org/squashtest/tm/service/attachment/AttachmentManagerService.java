/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.service.attachment;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.service.foundation.collection.CollectionSorting;
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder;

@Transactional
public interface AttachmentManagerService {
	//returns the ID of the newly created Attachment
	Long addAttachment(Long attachmentListId, Attachment attachment);
	
	Attachment findAttachment(Long attachmentId);
	
	Set<Attachment> findAttachments(Long attachmentListId);
	
	void setAttachmentContent(InputStream stream, Long attachmentId);
	
	InputStream getAttachmentContent(Long attachmentId);
	
	void removeAttachmentFromList(Long attachmentListId, Long attachmentId);
	
	void removeListOfAttachments(Long attachmentListId, List<Long> attachmentIds);
	
	String findAttachmentShortName(Long attachmentId);
	
	void renameAttachment(Long attachmentId, String newName);
	
	FilteredCollectionHolder<List<Attachment>> findFilteredAttachmentForList(long attachmentListId, CollectionSorting filter);


}
