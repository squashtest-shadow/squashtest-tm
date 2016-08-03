/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.repository.CustomCustomFieldBindingDao;


public class CustomFieldBindingDaoImpl extends HibernateEntityDao<CustomFieldBinding> implements CustomCustomFieldBindingDao {

	@Override
	public List<CustomFieldBinding> findAllForProjectAndEntity(long projectId, BindableEntity boundEntity,
			Paging paging) {
		Query q = currentSession().getNamedQuery("CustomFieldBinding.findAllForProjectAndEntity");
		q.setParameter("projectId", projectId);
		q.setParameter("entityType", boundEntity);

		PagingUtils.addPaging(q, paging);
		return q.list();
	}


	@Override
	public void removeCustomFieldBindings(List<Long> bindingIds) {

		if (!bindingIds.isEmpty()){

			executeUpdateListQuery("CustomFieldBinding.removeCustomFieldBindings", new SetBindingIdsParameterCallback(bindingIds));

			List<NewBindingPosition> newPositions = recomputeBindingPositions();
			updateBindingPositions(newPositions);

		}
	}



	@SuppressWarnings("unchecked")
	protected List<NewBindingPosition> recomputeBindingPositions(){

		Session session = currentSession();
		Query q = session.getNamedQuery("CustomFieldBinding.recomputeBindingPositions");
		q.setResultTransformer(Transformers.aliasToBean(NewBindingPosition.class));

		return q.list();

	}


	protected void updateBindingPositions(List<NewBindingPosition> newPositions){

		Query q = currentSession().getNamedQuery("CustomFielBinding.updateBindingPosition");

		for (NewBindingPosition newPos : newPositions){
			if (newPos.needsUpdate()){
				q.setInteger("newPos", newPos.getNewPosition());
				q.setLong("id", newPos.getBindingId());
				q.executeUpdate();
			}
		}

	}



	// ********************** static classes ******************************


	private static final class SetBindingIdsParameterCallback implements SetQueryParametersCallback{

		private List<Long> ids;

		private SetBindingIdsParameterCallback(List<Long> ids) {
			this.ids = ids;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("cfbIds", ids);
		}
	}


	public static class NewBindingPosition {

		private Long bindingId;
		private int formerPosition;
		private int newPosition;

		public NewBindingPosition() {
		}

		public Long getBindingId() {
			return bindingId;
		}

		public void setBindingId(Long bindingId) {
			this.bindingId = bindingId;
		}

		public int getFormerPosition() {
			return formerPosition;
		}

		public void setFormerPosition(int formerPosition) {
			this.formerPosition = formerPosition;
		}

		public int getNewPosition() {
			return newPosition;
		}

		public void setNewPosition(Long newPosition) {
			this.newPosition = newPosition.intValue();
		}

		public boolean needsUpdate(){
			return formerPosition!=newPosition;
		}

	}

}
