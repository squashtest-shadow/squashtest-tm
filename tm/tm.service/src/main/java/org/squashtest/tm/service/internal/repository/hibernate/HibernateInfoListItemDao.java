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

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.infolist.SystemInfoListItemCode;
import org.squashtest.tm.domain.infolist.SystemListItem;
import org.squashtest.tm.service.internal.repository.InfoListItemDao;

import javax.persistence.Query;

@Repository
public class HibernateInfoListItemDao extends HibernateEntityDao<InfoListItem> implements InfoListItemDao {

	private static final String ITEM_CODE = "itemCode";
	private static final String PROJECT_ID = "projectId";

	@Override
	public SystemListItem getSystemRequirementCategory() {
		return (SystemListItem) em.createNamedQuery("systemListItem.getSystemRequirementCategory")
				.getSingleResult();
	}

	@Override
	public SystemListItem getSystemTestCaseNature() {
		return (SystemListItem) em.createNamedQuery("systemListItem.getSystemTestCaseNature").getSingleResult();
	}

	@Override
	public SystemListItem getSystemTestCaseType() {
		return (SystemListItem) currentSession().getNamedQuery("systemListItem.getSystemTestCaseType").uniqueResult();
	}

	@Override
	public InfoListItem findByCode(String code) {
		Query q = em.createNamedQuery("infoListItem.findByCode");
		q.setParameter("code", code);
		try {
			return (InfoListItem) q.getSingleResult();
		} catch (EmptyResultDataAccessException e) {//NOSONAR some null checks in calling code
			return null;
		}
	}

	@Override
	public InfoListItem findReference(ListItemReference reference) {
		return findByCode(reference.getCode());
	}

	@Override
	public InfoListItem findDefaultRequirementCategory(long projectId) {
		Query q = em.createNamedQuery("infoListItem.findDefaultRequirementCategoryForProject");
		q.setParameter(PROJECT_ID, projectId);
		return (InfoListItem) q.getSingleResult();
	}

	@Override
	public InfoListItem findDefaultTestCaseNature(long projectId) {
		Query q = em.createNamedQuery("infoListItem.findDefaultTestCaseNatureForProject");
		q.setParameter(PROJECT_ID, projectId);
		return (InfoListItem) q.getSingleResult();
	}

	@Override
	public InfoListItem findDefaultTestCaseType(long projectId) {
		Query q = em.createNamedQuery("infoListItem.findDefaultTestCaseTypeForProject");
		q.setParameter(PROJECT_ID, projectId);
		return (InfoListItem) q.getSingleResult();
	}

	@Override
	public boolean isCategoryConsistent(long projectId, String itemCode) {
		Query q = em.createNamedQuery("infoListItem.foundCategoryInProject");
		q.setParameter(PROJECT_ID, projectId);
		q.setParameter(ITEM_CODE, itemCode);
		return (Long) q.getSingleResult() == 1;
	}

	@Override
	public boolean isNatureConsistent(long projectId, String itemCode) {
		Query q = em.createNamedQuery("infoListItem.foundNatureInProject");
		q.setParameter(PROJECT_ID, projectId);
		q.setParameter(ITEM_CODE, itemCode);
		return (Long) q.getSingleResult() == 1;
	}

	@Override
	public boolean isTypeConsistent(long projectId, String itemCode) {
		Query q = em.createNamedQuery("infoListItem.foundTypeInProject");
		q.setParameter(PROJECT_ID, projectId);
		q.setParameter(ITEM_CODE, itemCode);
		return (Long) q.getSingleResult() == 1;
	}

	@Override
	public void unbindFromLibraryObjects(long infoListId) {
		InfoListItem defaultReqCat = findByCode(SystemInfoListItemCode.CAT_UNDEFINED.getCode());
		execUpdateQuery(infoListId, "infoList.setReqCatToDefault", defaultReqCat);
		InfoListItem defaultTcNat = findByCode(SystemInfoListItemCode.NAT_UNDEFINED.getCode());
		execUpdateQuery(infoListId, "infoList.setTcNatToDefault", defaultTcNat);
		InfoListItem defaultTcType = findByCode(SystemInfoListItemCode.TYP_UNDEFINED.getCode());
		execUpdateQuery(infoListId, "infoList.setTcTypeToDefault", defaultTcType);

	}

	private void execUpdateQuery(long infoListId, String queryName, InfoListItem defaultParam) {
		Query query = em.createNamedQuery(queryName);
		query.setParameter("default", defaultParam);
		query.setParameter("id", infoListId);
		query.executeUpdate();

	}

	@Override
	public boolean isUsed(long infoListItemId) {
		Query q = em.createNamedQuery("infoListItem.isUsed");
		q.setParameter("id", infoListItemId);
		return (Long) q.getSingleResult() > 0;
	}

	@Override
	public void removeInfoListItem(long infoListItemId, InfoListItem defaultItem) {

		execUpdateQuery(infoListItemId, "infoListItem.setReqCatToDefault", defaultItem);
		execUpdateQuery(infoListItemId, "infoListItem.setTcNatToDefault", defaultItem);
		execUpdateQuery(infoListItemId, "infoListItem.setTcTypeToDefault", defaultItem);

	}

}
