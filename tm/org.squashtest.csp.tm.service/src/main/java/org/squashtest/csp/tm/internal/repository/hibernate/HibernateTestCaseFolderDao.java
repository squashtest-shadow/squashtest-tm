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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.internal.repository.TestCaseFolderDao;


@Repository
public class HibernateTestCaseFolderDao extends HibernateEntityDao<TestCaseFolder> implements TestCaseFolderDao {

	@Override
	public List<TestCaseLibraryNode> findAllContentById(final long folderId) {
		SetQueryParametersCallback setParams = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setLong("folderId", folderId);
			}
		};

		return executeListNamedQuery("testCaseFolder.findAllContentById", setParams);

	}

	
	@Override
	public TestCaseFolder findByContent(final TestCaseLibraryNode node) {
		SetQueryParametersCallback callback = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("content", node);
			}
		};

		return executeEntityNamedQuery("testCaseFolder.findByContent", callback);
	}
	
	
	@Override
	public List<String> findNamesInFolderStartingWith(final long folderId,
			final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("containerId", folderId);
				query.setParameter("nameStart", nameStart + "%");
			}
		};
		return executeListNamedQuery(
				"testCaseFolder.findNamesInFolderStartingWith", newCallBack1);
	}

	
	@Override
	public List<String> findNamesInLibraryStartingWith(final long libraryId,
			final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("containerId", libraryId);
				query.setParameter("nameStart", nameStart + "%");
			}
		};
		return executeListNamedQuery(
				"testCaseFolder.findNamesInLibraryStartingWith", newCallBack1);
	}

	
	@Override
	public List<Long> findTestCasesFolderIdsInFolderContent(final long folderId) {
		SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("folderId", folderId);
			}
		};
		return executeListNamedQuery(
				"testCaseFolder.findTestCasesFolderIdsInFolderContent", newCallBack1);
	}

	
	@Override
	public List<Long[]> findPairedContentForList(final List<Long> ids) {

		if (ids.size()==0) return Collections.emptyList();
		
		
		SQLQuery query = currentSession().createSQLQuery(NativeQueries.testCaseFolder_sql_findPairedContentForFolders);		
		query.setParameterList("folderIds", ids, LongType.INSTANCE);
		query.addScalar("ancestor_id", LongType.INSTANCE);
		query.addScalar("descendant_id", LongType.INSTANCE);
		
		List<Object[]> result = query.list();
		
		return toArrayOfLong(result);
	}

	
	@Override
	public List<Long> findContentForList(List<Long> ids) {
		if (ids.size()==0) return Collections.emptyList();
		
		
		SQLQuery query = currentSession().createSQLQuery(NativeQueries.testCaseFolder_sql_findContentForFolder);		
		query.setParameterList("folderIds", ids, LongType.INSTANCE);
		query.addScalar("descendant_id", LongType.INSTANCE);
		
		return query.list();
	}


	@Override
	public List<TestCaseFolder> findAllFolders(final List<Long> folderIds) {
		
		SetQueryParametersCallback newCallBack = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameterList("folderIds", folderIds, LongType.INSTANCE);
			}
		};
		return executeListNamedQuery(
				"testCaseFolder.findAllFolders", newCallBack);	
		
	}

	
	private List<Long[]> toArrayOfLong(List<Object[]> input){
		List<Long[]> result = new ArrayList<Long[]>();
		
		for (Object[] pair : input){
			Long[] newPair  = new Long[]{(Long)pair[0], (Long) pair[1]};
			result.add(newPair);
		}
		
		return result;
	}



}
