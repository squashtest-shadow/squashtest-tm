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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.internal.repository.CustomCustomFieldBindingDao;


@Repository("CustomCustomFieldBindingDao")
public class HibernateCustomCustomFieldBindingDao extends HibernateEntityDao<CustomFieldBinding> implements CustomCustomFieldBindingDao {
	
	@Override
	public void removeCustomFieldBindings(List<Long> bindingIds) {
		
		if (!bindingIds.isEmpty()){
			
			executeUpdateListQuery("CustomFieldBinding.removeCustomFieldBindings", new SetBindingIdsParameterCallback(bindingIds));
			List<NewBindingPosition> newPositions = recomputeBindingPositions();
			updateBindingPositions(newPositions);
			
		}
	}

	@Override
	public List<CustomFieldBinding> findAllByIds(Collection<Long> ids){
		return executeListNamedQuery("CustomFieldBinding.findAllByIds", new SetBindingIdsParameterCallback(new ArrayList<Long>(ids)));
	}
	
	public List<CustomFieldBinding> findAllByIds(List<Long> ids){
		return executeListNamedQuery("CustomFieldBinding.findAllByIds", new SetBindingIdsParameterCallback(ids));		
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
			return (formerPosition!=newPosition);
		}
		
	}

}
