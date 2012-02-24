/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.attachment.AttachmentContent;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.internal.repository.DeletionDao;

public abstract class HibernateDeletionDao implements DeletionDao {
	
	@Inject
	private SessionFactory sessionFactory;
	
	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	

	@Override
	@SuppressWarnings("unchecked")
	public void removeAttachmentsLists(final List<Long> attachmentListIds) {
		
		List<Object[]> attachmentContentIds ;
		
		if (attachmentListIds.isEmpty()) return;

		// we handle the cascade ourselves to make sure that the deletion is carried properly
		attachmentContentIds = executeSelectNamedQuery("attachment.getAttachmentAndContentIdsFromList", "listIds", attachmentListIds);

		// due to foreign key constraints and the like, the following
		// operations
		// must be carried in that order :
	
		if (! attachmentContentIds.isEmpty()){

			Collection<Long> attachIds = CollectionUtils.collect(attachmentContentIds,
					new Transformer() {

						@Override
						public Object transform(Object input) {
							return ((Object[]) input)[0];
						}
					});

			Collection<Long> contentIds = CollectionUtils.collect(attachmentContentIds,
					new Transformer() {
						@Override
						public Object transform(Object input) {
							return ((Object[]) input)[1];
						}
					});

			executeDeleteNamedQuery("attachment.removeAttachments", "attachIds", attachIds);
			executeDeleteNamedQuery("attachment.removeContents", "contentIds", contentIds);
		}
		
		executeDeleteNamedQuery("attachment.deleteAttachmentLists", "listIds", attachmentListIds);

	}

	

	@Override
	public void removeEntity(Object entity) {
		getSession().delete(entity);

	}

	@Override
	/*
	 * dev note : if the damn fks in the DB were correctly set we wouldn't do the awkward 
	 * treatment here. Well, hibernate mapping decided it.
	 * 
	 */
	public void removeAttachmentList(AttachmentList list) {
		
		if (list == null ) return ;
		
		List<AttachmentContent> contentList = new LinkedList<AttachmentContent>();
		Set<Attachment> attachmentList = list.getAllAttachments();
	
		for (Attachment attachment : attachmentList){
			contentList.add(attachment.getContent());
		}
		
		
		for (Attachment attachment : attachmentList){
			removeEntity(attachment);
		}
		
		for (AttachmentContent content : contentList){
			removeEntity(content);
		}
		

		removeEntity(list);
		
	}
	
	@Override
	public void flush(){
		getSession().flush();
	}
	
	
/* **************** convenient shorthands **************************************************** */
	
	protected void executeDeleteNamedQuery(String namedQuery, String paramName, Collection<Long> ids){
		if (! ids.isEmpty()){
			Query query = getSession().getNamedQuery(namedQuery);
			query.setParameterList(paramName, ids, LongType.INSTANCE);
			query.executeUpdate();		
		}
	}
	
	
	protected <R> List<R> executeSelectNamedQuery(String namedQuery, String paramName, Collection<Long> ids){
		if (! ids.isEmpty()){
			Query query = getSession().getNamedQuery(namedQuery);
			query.setParameterList(paramName, ids, LongType.INSTANCE);
			return query.list();		
		}else{
			return Collections.emptyList();
		}
	}
	
	protected void executeDeleteSQLQuery(String queryString, String paramName, Collection<Long> ids){
		if (! ids.isEmpty()){
			Query query = getSession().createSQLQuery(queryString);
			query.setParameterList(paramName, ids, LongType.INSTANCE);
			query.executeUpdate();		
		}
	}
	
	
	protected <R> List<R> executeSelectSQLQuery(String queryString, String paramName, Collection<Long> ids){
		if (! ids.isEmpty()){
			Query query = getSession().createSQLQuery(queryString);
			query.setParameterList(paramName, ids, LongType.INSTANCE);
			return query.list();		
		}else{
			return Collections.emptyList();
		}
	}
	

}
