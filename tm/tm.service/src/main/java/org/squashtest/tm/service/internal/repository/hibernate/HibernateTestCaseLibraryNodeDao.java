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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.internal.repository.ParameterNames;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryNodeDao;

@Repository("squashtest.tm.repository.TestCaseLibraryNodeDao")
public class HibernateTestCaseLibraryNodeDao extends HibernateEntityDao<TestCaseLibraryNode> implements
TestCaseLibraryNodeDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getParentsName(long entityId) {
		Query query = currentSession().getNamedQuery("TestCasePathEdge.findSortedParentNames");
		query.setParameter(ParameterNames.NODE_ID, entityId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getParentsIds(long entityId) {
		Query query = currentSession().getNamedQuery("TestCasePathEdge.findSortedParentIds");
		query.setParameter(ParameterNames.NODE_ID, entityId);
		return query.list();
	}

	@Override
	public List<TestCaseLibraryNode> findNodesByPath(List<String> path) {
		List<Long> ids = findNodeIdsByPath(path);
		List<TestCaseLibraryNode> result = findAllByIds(ids);

		// post process the result to ensure the correct order of the result
		TestCaseLibraryNode[] toReturn = new TestCaseLibraryNode[ids.size()];
		for (TestCaseLibraryNode node : result) {
			toReturn[ids.indexOf(node.getId())] = node;
		}

		return Arrays.asList(toReturn);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findNodeIdsByPath(List<String> paths) {
		if (!paths.isEmpty()) {
			// process the paths parameter : we don't want escaped '/' in there
			List<String> effectiveParameters = unescapeSlashes(paths);
			Query query = currentSession().getNamedQuery("TestCasePathEdge.findNodeIdsByPath");
			query.setParameterList("paths", effectiveParameters);
			List<Object[]> result = query.list();

			// now ensures that the results are returned in the correct order
			Long[] toReturn = new Long[effectiveParameters.size()];

			/*
			 * There is one case where the database could return a path we haven't requested :
			 * the DB is MySQL and the charset is case insensitive for select
			 * (see http://dev.mysql.com/doc/refman/5.0/en/case-sensitivity.html)
			 *
			 * This leads to problem because our system is always case sensitive : MySQL could
			 * return paths it considers valid (give or take a couple of uppercased letters)
			 * that weren't queried for.
			 *
			 * That's why we test again that the result from the DB actually has a positive index
			 * before assigning it to the result.
			 *
			 */
			for (Object[] res : result) {
				String path = (String) res[0];
				int idx = effectiveParameters.indexOf(path);
				if (idx>-1) {
					toReturn[idx] = (Long) res[1];
				}
			}

			return Arrays.asList(toReturn);
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.squashtest.tm.service.internal.repository.LibraryNodeDao#findNodeIdByPath(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Long findNodeIdByPath(String path) {
		String effectiveParameters = unescapeSlashes(path);
		Query query = currentSession().getNamedQuery("TestCasePathEdge.findNodeIdsByPath");
		query.setParameterList("paths", new String[] { effectiveParameters });
		List<Object[]> result = query.list();

		if (result.isEmpty() || ! effectiveParameters.equals(result.get(0)[0])) {
			return null;
		}

		return (Long) result.get(0)[1];

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.squashtest.tm.service.internal.repository.LibraryNodeDao#findNodeByPath(java.lang.String)
	 */
	@Override
	public TestCaseLibraryNode findNodeByPath(String path) {
		Long id = findNodeIdByPath(path);

		return (TestCaseLibraryNode) (id != null ? currentSession().load(TestCaseLibraryNode.class, id) : null);
	}


	@Override
	public int countSiblingsOfNode(long nodeId) {

		Query q;
		Integer count;

		q = currentSession().getNamedQuery("testCase.countSiblingsInFolder");
		q.setParameter(ParameterNames.NODE_ID, nodeId);
		count = (Integer)q.uniqueResult();

		if (count == null ){
			q = currentSession().getNamedQuery("testCase.countSiblingsInLibrary");
			q.setParameter(ParameterNames.NODE_ID, nodeId);
			count = (Integer)q.uniqueResult();
		}

		// if NPE here it's probably because nodeId corresponds to nothing. The +1 is because the queries use 'maxindex' instead of 'count'
		return count + 1;
	}



	private List<String> unescapeSlashes(List<String> paths) {
		List<String> unescaped = new ArrayList<>(paths.size());
		for (String orig : paths) {
			unescaped.add(orig.replaceAll("\\\\/", "/"));
		}
		return unescaped;
	}

	private String unescapeSlashes(String path) {
		return path.replaceAll("\\\\/", "/");
	}

}
