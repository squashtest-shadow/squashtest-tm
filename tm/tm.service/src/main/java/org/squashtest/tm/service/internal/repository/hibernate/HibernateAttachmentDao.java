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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentContent;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.AttachmentDao;

@Repository
public class HibernateAttachmentDao extends HibernateEntityDao<Attachment>
 implements AttachmentDao {

	@Override
	public Attachment findAttachmentWithContent(Long attachmentId) {
		Attachment attachment = findById(attachmentId);
		Hibernate.initialize(attachment.getContent());
		return attachment;
	}

	@Override
	public Set<Attachment> findAllAttachments(Long attachmentListId) {
		
		Criteria crit = currentSession().createCriteria(AttachmentList.class)
										.add(Restrictions.eq("id", attachmentListId))
										.createAlias("attachments", "Attachment")
										.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

		return collectFromMapListToSet(crit);

	}

	@Override
	public void removeAttachment(Long attachmentId) {

		Attachment attachment = findById(attachmentId);

//		//[Issue 1456 problem with h2 database that will try to delete 2 times the same lob in lobs.db]
		AttachmentContent content = attachment.getContent();
		content.setContent(null);
		
		flush();
//		//End [Issue 1456]
		
		removeEntity(attachment);

	}

	@Override
	public List<Attachment> findAllAttachmentsFiltered(Long attachmentListId,PagingAndSorting pas) {


		Session session = currentSession();

		Criteria crit = session.createCriteria(AttachmentList.class,"AttachmentList")
		.add(Restrictions.eq("id",attachmentListId))
		.createAlias("attachments", "Attachment")
		.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

		
		PagingUtils.addPaging(crit, pas);
		SortingUtils.addOrder(crit, pas);

		return collectFromMapList(crit);
	}


}
