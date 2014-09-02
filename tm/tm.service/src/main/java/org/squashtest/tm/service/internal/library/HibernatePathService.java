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

package org.squashtest.tm.service.internal.library;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateTestCaseDao;

/**
 * DAO for computing nodes paths. Factored out of {@link HibernateTestCaseDao}
 * 
 * @author Gregory Fouquet
 * 
 * 
 */
@Service
@Transactional(readOnly = true)
public class HibernatePathService implements PathService {
	/**
	 * Path separator as returned by the queries - because edges name can contain "/" which shall be escaped.
	 */
	private static final String PATH_SEP = "\u241E";

	@Inject
	private SessionFactory sessionFactory;

	private Session currentSession() {
		return sessionFactory.getCurrentSession();
	}

	@SuppressWarnings("unchecked")
	private List<String> findPathById(long id) {
		Query query = currentSession().getNamedQuery("TestCasePathEdge.findPathById");
		query.setParameter("nodeId", id);
		return query.list();
	}

	/**
	 * @see org.squashtest.tm.service.internal.library.PathService#buildTestCasePath(long)
	 */
	@Override
	public String buildTestCasePath(long id) {
		List<String> paths = findPathById(id);

		if (paths.size() == 0) {
			return null;
		}

		return paths.get(0).replace("/", "\\/").replace(PATH_SEP, "/");
	}

	/**
	 * @see org.squashtest.tm.service.internal.library.PathService#buildTestCasesPaths(java.util.List)
	 */
	@Override
	public List<String> buildTestCasesPaths(List<Long> ids) {
		List<Object[]> paths = findPathsByIds(ids);

		String[] res = new String[ids.size()];

		for (Object[] path : paths) {

			int pos = ids.indexOf((Long) path[0]);
			res[pos] = escapePath((String) path[1]);
		}

		return Arrays.asList(res);
	}

	/**
	 * @param string
	 * @return
	 */
	private String escapePath(String fetchedPath) {
		return fetchedPath != null ? fetchedPath.replace("/", "\\/").replace(PATH_SEP, "/") : null;
	}

	/**
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Object[]> findPathsByIds(List<Long> ids) {
		Query query = currentSession().getNamedQuery("TestCasePathEdge.findPathsByIds");
		query.setParameterList("nodeIds", ids);
		return query.list();
	}
}
