/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.internal.repository.CustomCustomFieldBindingDao;


@Repository("CustomCustomFieldBindingDao")
public class HibernateCustomCustomFieldBindingDao extends HibernateEntityDao<CustomField> implements CustomCustomFieldBindingDao {
	

	
	@Override
	public void removeCustomFieldBindings(List<Long> bindingIds) {
		if (!bindingIds.isEmpty()){
			executeUpdateListQuery("CustomFieldBinding.removeCustomFieldBindings", new SetBindingIdsParameterCallback(bindingIds));
		}
	}
	
	@SuppressWarnings("unchecked")
	protected List<NewBindingPosition> recomputeBindingPositions(){
		Session session = currentSession();
		Query q = session.getNamedQuery("CustomFieldBinding.recomputeBindingPositions");
		q.setResultTransformer(Transformers.aliasToBean(NewBindingPosition.class));
		List<NewBindingPosition> newPositions = q.list();
		return newPositions;
	}
	
	
	private static class SetBindingIdsParameterCallback implements SetQueryParametersCallback{
		
		List<Long> ids;
		
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
		private Long newPosition;
		
		public NewBindingPosition() {
			// TODO Auto-generated constructor stub
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

		public Long getNewPosition() {
			return newPosition;
		}

		public void setNewPosition(Long newPosition) {
			this.newPosition = newPosition;
		}
		
		
		
	}

}
