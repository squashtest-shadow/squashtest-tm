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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentContent;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.service.foundation.collection.CollectionSorting;
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
	@SuppressWarnings("unchecked")
	public Set<Attachment> findAllAttachments(Long attachmentListId) {
		List<Map<String,?>> rawResult= currentSession().createCriteria(AttachmentList.class)
										.add(Restrictions.eq("id", attachmentListId))
										.createAlias("attachments", "attach")
										.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
										.list();


		Set<Attachment> attachs = new HashSet<Attachment>();
		ListIterator<Map<String,?>> iter = rawResult.listIterator();
		while ( iter.hasNext() ) {
		    Map<String,?> map = iter.next();
		    attachs.add((Attachment) map.get("attach"));
		}


		return attachs;


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
	@SuppressWarnings("unchecked")
	public List<Attachment> findAllAttachmentsFiltered(
			Long attachmentListId,
			CollectionSorting filter) {


		Session session = currentSession();


		Criteria crit = session.createCriteria(AttachmentList.class,"AttachmentList")
		.add(Restrictions.eq("id",attachmentListId))
		.createAlias("attachments", "Attachment")
		.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

		try{
			/* add ordering */
			String sortedAttribute = filter.getSortedAttribute();
			String order = filter.getSortingOrder();

			if (order.equals("asc")){
				crit.addOrder(Order.asc(sortedAttribute));
			}
			else{
				crit.addOrder(Order.desc(sortedAttribute));
			}
		}
		catch(IllegalArgumentException ex){
			//no sorting then
		}


		/* result range */
		crit.setFirstResult(filter.getFirstItemIndex());
		crit.setMaxResults(filter.getPageSize());


		List<Map<String,?>> rawResult = crit.list();

		List<Attachment> atts = new LinkedList<Attachment>();
		ListIterator<Map<String,?>> iter = rawResult.listIterator();
		while ( iter.hasNext() ) {
		    Map<String,?> map = iter.next();
		    atts.add((Attachment) map.get("Attachment"));
		}


		return atts;
	}


}
