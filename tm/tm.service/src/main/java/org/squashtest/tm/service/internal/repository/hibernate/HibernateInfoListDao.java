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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.SystemInfoListCode;
import org.squashtest.tm.service.internal.repository.InfoListDao;

@Repository
public class HibernateInfoListDao extends HibernateEntityDao<InfoList> implements InfoListDao {

	@PersistenceContext
	private EntityManager em;

	@Override
	public InfoList findByCode(String code) {
		Query query = em.createNamedQuery("infoList.findByCode");
		query.setParameter("code", code);
		try {
			return (InfoList) query.getSingleResult();
		} catch (EmptyResultDataAccessException e) {//NOSONAR this method is used with null checks in business rules. We need null and no log...
			return null;
		}
	}

	@Override
	public boolean isUsedByOneOrMoreProject(long infoListId) {
		Query query = em.createNamedQuery("infoList.findProjectUsingInfoList");
		query.setParameter("id", infoListId);
		return !query.getResultList().isEmpty();
	}

	@Override
	public void unbindFromProject(long infoListId) {
		InfoList defaultReqCatList = findByCode(SystemInfoListCode.REQUIREMENT_CATEGORY.getCode());
		execUpdateQuery(infoListId, "infoList.project.setReqCatListToDefault", defaultReqCatList);
		InfoList defaultTcNatList = findByCode(SystemInfoListCode.TEST_CASE_NATURE.getCode());
		execUpdateQuery(infoListId, "infoList.project.setTcNatListToDefault", defaultTcNatList);
		InfoList defaultTcTypeList = findByCode(SystemInfoListCode.TEST_CASE_TYPE.getCode());
		execUpdateQuery(infoListId, "infoList.project.setTcTypeListToDefault", defaultTcTypeList);
	}

	private void execUpdateQuery(long infoListId, String queryName, Object defaultParam) {
		Query query = em.createNamedQuery(queryName);
		query.setParameter("default", defaultParam);
		query.setParameter("id", infoListId);
		query.executeUpdate();
	}

	@Override
	public List<InfoList> findAllOrdered() {
		return executeListNamedQuery("infoList.findAllOrdered");

	}

	@Override
	public void setDefaultCategoryForProject(long projectId, InfoListItem defaultItem) {
		execUpdateQuery(projectId, "infoList.setProjectCategoryToDefaultItem", defaultItem);
	}

	@Override
	public void setDefaultNatureForProject(long projectId, InfoListItem defaultItem) {
		execUpdateQuery(projectId, "infoList.setProjectNatureToDefaultItem", defaultItem);

	}

	@Override
	public void setDefaultTypeForProject(long projectId, InfoListItem defaultItem) {
		execUpdateQuery(projectId, "infoList.setProjectTypeToDefaultItem", defaultItem);

	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.InfoListDao#findAllBound()
	 */
	@Override
	public List<InfoList> findAllBound() {
		return executeListNamedQuery("infoList.findAllBound");
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.InfoListDao#findAllUnbound()
	 */
	@Override
	public List<InfoList> findAllUnbound() {
		return executeListNamedQuery("infoList.findAllUnbound");
	}

}
