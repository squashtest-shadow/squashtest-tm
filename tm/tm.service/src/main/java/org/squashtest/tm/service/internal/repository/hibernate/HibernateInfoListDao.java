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

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.SystemInfoListCode;
import org.squashtest.tm.service.internal.repository.InfoListDao;


@Repository
public class HibernateInfoListDao extends HibernateEntityDao<InfoList> implements InfoListDao{

	@Inject
	private SessionFactory sessionFactory;

	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	@Override
	public InfoList findByCode(String code) {
		Query query = currentSession().getNamedQuery("infoList.findByCode");
		query.setParameter("code", code);
		return (InfoList)query.uniqueResult();
	}

	@Override
	public boolean isUsedByOneOrMoreProject(long infoListId) {
		Query query = currentSession().getNamedQuery("infoList.findProjectUsingInfoList");
		query.setParameter("id", infoListId);
		if (query.list().size() > 0){
			return true;
		}
		return false;
	}


	@Override
	public void removeInfoListFromProjects(long infoListId) {

		
		InfoList defaultReqCatList = findByCode(SystemInfoListCode.REQUIREMENT_CATEGORY.getCode());
		execUpdateQuery(infoListId, "infoList.project.setReqCatListToDefault", defaultReqCatList);
		InfoList defaultTcNatList = findByCode(SystemInfoListCode.TEST_CASE_NATURE.getCode());
		execUpdateQuery(infoListId, "infoList.project.setTcNatListToDefault", defaultTcNatList);
		InfoList defaultTcTypeList = findByCode(SystemInfoListCode.TEST_CASE_TYPE.getCode());
		execUpdateQuery(infoListId, "infoList.project.setTcTypeListToDefault", defaultTcTypeList);

		
	}

	
	private void execUpdateQuery(long infoListId, String queryName, Object defaultParam){
		Query query = currentSession().getNamedQuery(queryName);
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
		execUpdateQuery(projectId,"infoList.setProjectCategoryToDefaultItem", defaultItem);
	}

	@Override
	public void setDefaultNatureForProject(long projectId, InfoListItem defaultItem) {
		execUpdateQuery(projectId,"infoList.setProjectNatureToDefaultItem", defaultItem);
		
	}

	@Override
	public void setDefaultTypeForProject(long projectId, InfoListItem defaultItem) {
		execUpdateQuery(projectId,"infoList.setProjectTypeToDefaultItem", defaultItem);
		
	}
	
	
	

}
