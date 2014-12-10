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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.infolist.SystemListItem;
import org.squashtest.tm.service.internal.repository.InfoListItemDao;

@Repository
public class HibernateInfoListItemDao extends HibernateEntityDao<InfoListItem> implements InfoListItemDao{

	@Override
	public SystemListItem getSystemRequirementCategory() {
		return (SystemListItem) currentSession().getNamedQuery("systemListItem.getSystemRequirementCategory").uniqueResult();
	}


	@Override
	public SystemListItem getSystemTestCaseNature() {
		return (SystemListItem) currentSession().getNamedQuery("systemListItem.getSystemTestCaseNature").uniqueResult();
	}


	@Override
	public SystemListItem getSystemTestCaseType() {
		return (SystemListItem) currentSession().getNamedQuery("systemListItem.getSystemTestCaseType").uniqueResult();
	}

	@Override
	public InfoListItem findByCode(String code){
		Query q = currentSession().getNamedQuery("infoListItem.findByCode");
		q.setParameter("code", code);
		return (InfoListItem)q.uniqueResult();
	}


	@Override
	public InfoListItem findReference(ListItemReference reference){
		return findByCode(reference.getCode());
	}


	@Override
	public InfoListItem findDefaultRequirementCategory(long projectId) {
		Query q = currentSession().getNamedQuery("infoListItem.findDefaultRequirementCategoryForProject");
		q.setParameter("projectId", projectId);
		return (InfoListItem)q.uniqueResult();
	}


	@Override
	public InfoListItem findDefaultTestCaseNature(long projectId) {
		Query q = currentSession().getNamedQuery("infoListItem.findDefaultTestCaseNatureForProject");
		q.setParameter("projectId", projectId);
		return (InfoListItem)q.uniqueResult();
	}


	@Override
	public InfoListItem findDefaultTestCaseType(long projectId) {
		Query q = currentSession().getNamedQuery("infoListItem.findDefaultTestCaseTypeForProject");
		q.setParameter("projectId", projectId);
		return (InfoListItem)q.uniqueResult();
	}


	@Override
	public boolean isCategoryConsistent(long projectId, String itemCode) {
		Query q = currentSession().getNamedQuery("infoListItem.foundCategoryInProject");
		q.setParameter("projectId", projectId);
		q.setParameter("itemCode", itemCode);
		return ((Long)q.uniqueResult()==1);
	}


	@Override
	public boolean isNatureConsistent(long projectId, String itemCode) {
		Query q = currentSession().getNamedQuery("infoListItem.foundNatureInProject");
		q.setParameter("projectId", projectId);
		q.setParameter("itemCode", itemCode);
		return ((Long)q.uniqueResult()==1);
	}


	@Override
	public boolean isTypeConsistent(long projectId, String itemCode) {
		Query q = currentSession().getNamedQuery("infoListItem.foundTypeInProject");
		q.setParameter("projectId", projectId);
		q.setParameter("itemCode", itemCode);
		return ((Long)q.uniqueResult()==1);
	}


	@Override
	public void removeInfoListItems(long infoListId) {
		InfoListItem defaultReqCat = findById(6);
		execUpdateQuery(infoListId, "infoList.setReqCatToDefault", defaultReqCat);
		InfoListItem defaultTcNat = findById(12);
		execUpdateQuery(infoListId, "infoList.setTcNatToDefault", defaultTcNat);
		InfoListItem defaultTcType = findById(20);
		execUpdateQuery(infoListId, "infoList.setTcTypeToDefault", defaultTcType);
		
	}


	private void execUpdateQuery(long infoListId, String queryName, InfoListItem defaultParam) {
		Query query = currentSession().getNamedQuery(queryName);
		query.setParameter("default", defaultParam);
		query.setParameter("id", infoListId);
		query.executeUpdate();
		
	}




}
