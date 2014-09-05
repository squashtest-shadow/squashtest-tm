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
package org.squashtest.tm.service.internal.attachment;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentContent;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.attachment.AttachmentManagerService;
import org.squashtest.tm.service.internal.repository.AttachmentContentDao;
import org.squashtest.tm.service.internal.repository.AttachmentDao;
import org.squashtest.tm.service.internal.repository.AttachmentListDao;

/*
 * FIXME !
 *
 * we can't secure the operations on attachments yet, because they delegate the security permissions
 * to the owning entity. The problem being that the ownership of an AttachmentList is one way : from the
 * entity to the list.
 *
 * Therefore we can't perform a permission check using the AttachmentList id alone, one would need to fetch
 * the owning entity back first. That would require additional work Dao-side.
 *
 * See task #102 on ci.squashtest.org/mantis
 *
 */
@Service("squashtest.tm.service.AttachmentManagerService")
public class AttachmentManagerServiceImpl implements AttachmentManagerService {

	@Inject
	private AttachmentDao attachmentDao;

	@Inject
	private AttachmentContentDao contentDao;

	@Inject
	private AttachmentListDao attachmentListDao;
	
	@Inject
	private IndexationService indexationService; 

	@Override
	public Long addAttachment(Long attachmentListId, Attachment attachment) {
		attachment.setAddedOn(new Date());
		AttachmentList list = attachmentListDao.findById(attachmentListId);

		list.addAttachment(attachment);

		// check first if content is provided along.
		AttachmentContent content = attachment.getContent();
		if (content != null) {
			contentDao.persist(content);
		}
		
		attachmentDao.persist(attachment);
		
		TestCase testCase = attachmentListDao.findAssociatedTestCaseIfExists(attachmentListId);
		if(testCase != null){
			this.indexationService.reindexTestCase(testCase.getId());
		}
		
		RequirementVersion requirementVersion = attachmentListDao.findAssociatedRequirementVersionIfExists(attachmentListId);
		if( requirementVersion != null){
			this.indexationService.reindexRequirementVersion(requirementVersion.getId());
		}
		
		return attachment.getId();
	}

	@Override
	public Attachment findAttachment(Long attachmentId) {
		return attachmentDao.findById(attachmentId);
	}

	@Override
	public Set<Attachment> findAttachments(Long attachmentListId) {
		return attachmentDao.findAllAttachments(attachmentListId);
	}

	@Override
	public void setAttachmentContent(InputStream stream, Long attachmentId) {
		Attachment attachment = attachmentDao.findAttachmentWithContent(attachmentId);

		AttachmentContent content = attachment.getContent();

		if (content == null) {
			content = new AttachmentContent();
			content.setContent(stream);
			contentDao.persist(content);
			attachment.setContent(content);
		}

		content.setContent(stream);
	}

	@Override
	public InputStream getAttachmentContent(Long attachmentId) {
		Attachment attachment = attachmentDao.findAttachmentWithContent(attachmentId);
		return attachment.getContent().getContent();
	}

	@Override
	public void removeAttachmentFromList(Long attachmentListId, Long attachmentId) {
		AttachmentList list = attachmentListDao.findById(attachmentListId);
		Attachment attachment = attachmentDao.findById(attachmentId);

		list.removeAttachment(attachment);
		attachmentDao.removeAttachment(attachment.getId());
		
		TestCase testCase = attachmentListDao.findAssociatedTestCaseIfExists(attachmentListId);
		if(testCase != null){
			this.indexationService.reindexTestCase(testCase.getId());
		}
		
		RequirementVersion requirementVersion = attachmentListDao.findAssociatedRequirementVersionIfExists(attachmentListId);
		if( requirementVersion != null){
			this.indexationService.reindexRequirementVersion(requirementVersion.getId());
		}
	}

	@Override
	public void removeListOfAttachments(Long attachmentListId, List<Long> attachmentIds) {

		Iterator<Attachment> iterAttach = attachmentListDao.findById(attachmentListId).getAllAttachments().iterator();

		while (iterAttach.hasNext()) {
			Attachment att = iterAttach.next();
			ListIterator<Long> iterIds = attachmentIds.listIterator();

			while (iterIds.hasNext()) {
				Long id = iterIds.next();
				if (id.equals(att.getId())) {
					iterAttach.remove();
					iterIds.remove();
					attachmentDao.removeAttachment(att.getId());
					break;
				}
			}
			if (attachmentIds.size() == 0) {
				break;
			}
		}
		
		TestCase testCase = attachmentListDao.findAssociatedTestCaseIfExists(attachmentListId);
		if(testCase != null){
			this.indexationService.reindexTestCase(testCase.getId());
		}
		
		RequirementVersion requirementVersion = attachmentListDao.findAssociatedRequirementVersionIfExists(attachmentListId);
		if( requirementVersion != null){
			this.indexationService.reindexRequirementVersion(requirementVersion.getId());
		}
		
	}

	@Override
	public void renameAttachment(Long attachmentId, String newName) {
		Attachment attachment = attachmentDao.findById(attachmentId);
		attachment.setShortName(newName);
	}

	@Override
	public String findAttachmentShortName(Long attachmentId) {
		Attachment attachment = attachmentDao.findById(attachmentId);
		return attachment.getShortName();
	}

	@Override
	public PagedCollectionHolder<List<Attachment>> findPagedAttachments(long attachmentListId,
			PagingAndSorting pas) {
		List<Attachment> atts = attachmentDao.findAllAttachmentsFiltered(attachmentListId, pas);
		long count = attachmentDao.findAllAttachments(attachmentListId).size();
		return new PagingBackedPagedCollectionHolder<List<Attachment>>(pas, count, atts);
	}
	
	@Override
	public PagedCollectionHolder<List<Attachment>> findPagedAttachments(
			AttachmentHolder attached, PagingAndSorting pas) {
		return findPagedAttachments(attached.getAttachmentList().getId(), pas);
	}

}
