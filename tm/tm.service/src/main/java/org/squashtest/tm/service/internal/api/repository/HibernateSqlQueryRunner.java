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
package org.squashtest.tm.service.internal.api.repository;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;
import org.squashtest.tm.api.repository.SqlQueryRunner;

/**
 * Hinernate based implementation of {@link SqlQueryRunner}. Plugins can query Squash's database through SQL using this
 * service.
 * 
 * @author Gregory Fouquet
 * 
 */
@Service("squash.api.repository.SqlQueryRunner")
public class HibernateSqlQueryRunner implements SqlQueryRunner {
	@Inject
	private DataSource dataSource;

	private static final QueryExecution<Query> EXECUTE_LIST = new QueryExecution<Query>() {
		@SuppressWarnings("unchecked")
		public <R> R executeQuery(Query query) {
			return (R) query.list();
		}
	};

	private static final QueryExecution<Query> EXECUTE_SINGLE = new QueryExecution<Query>() {
		@SuppressWarnings("unchecked")
		public <R> R executeQuery(Query query) {
			return (R) query.uniqueResult();
		}
	};

	@Inject
	private SessionFactory sessionFactory;

	/**
	 * @see org.squashtest.tm.api.repository.SqlQueryRunner#executeSql(java.lang.String)
	 */
	@Override
	public <T> List<T> executeSelect(String selectQuery) {
		return executeQuery(selectQuery, EXECUTE_LIST);
	}

	private <T> T executeQuery(String selectQuery, QueryExecution<Query> execution) {
		StatelessSession s = sessionFactory.openStatelessSession();
		Transaction tx = s.beginTransaction();

		T res = null;

		try {
			SQLQuery q = s.createSQLQuery(selectQuery);
			res = execution.<T>executeQuery(q);
			tx.commit();
		} catch (HibernateException e) {
			tx.rollback();
			throw e;
		} finally {
			s.close();

		}

		return res;
	}

	/**
	 * @see org.squashtest.tm.api.repository.SqlQueryRunner#executeUniqueSelect(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T executeUniqueSelect(String selectQuery) {
		return (T) executeQuery(selectQuery, EXECUTE_SINGLE);
	}

	/**
	 * @see org.squashtest.tm.api.repository.SqlQueryRunner#executeSelect(java.lang.String, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> executeSelect(String selectQuery, Map<String, ?> namedParameters) {
		return (List<T>) executeQuery(selectQuery, new NamedParamsListExecution(namedParameters));
	}

	/**
	 * @see org.squashtest.tm.api.repository.SqlQueryRunner#executeUniqueSelect(java.lang.String, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T executeUniqueSelect(String selectQuery, Map<String, ?> namedParameters) {
		return (T) executeQuery(selectQuery, new NamedParamsUniqueResultExecution(namedParameters));
	}

}
