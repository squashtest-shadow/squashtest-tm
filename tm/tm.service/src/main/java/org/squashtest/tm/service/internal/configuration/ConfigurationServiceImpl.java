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
package org.squashtest.tm.service.internal.configuration;

import javax.inject.Inject;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.service.configuration.ConfigurationService;

@Service("squashtest.core.configuration.ConfigurationService")
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {

	private static final String INSERT_KEY_SQL = "insert into CORE_CONFIG (STR_KEY, VALUE) values (?, ?)";
	private static final String FIND_VALUE_BY_KEY_SQL = "select VALUE from CORE_CONFIG where STR_KEY = ?";
	private static final String UPDATE_KEY_SQL = "update CORE_CONFIG set VALUE = ? where STR_KEY = ?";

	@Inject
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void createNewConfiguration(String key, String value) {
		Session session = currentSession();
		Query sqlQuery = session.createSQLQuery(INSERT_KEY_SQL);
		sqlQuery.setString(0, key);
		sqlQuery.setString(1, value);
		sqlQuery.executeUpdate();
	}

	@Override
	public void updateConfiguration(String key, String value) {
		Session session = currentSession();
		Query sqlQuery = session.createSQLQuery(UPDATE_KEY_SQL);
		sqlQuery.setString(0, value);
		sqlQuery.setString(1, key);
		sqlQuery.executeUpdate();

	}

	private Session currentSession() throws HibernateException {
		Session session = sessionFactory.getCurrentSession();
		return session;
	}

	@Override
	public String findConfiguration(String key) {
		Object value = findValue(key);
		return value == null ? null : value.toString();
	}

	private Object findValue(String key) throws HibernateException {
		Session session = currentSession();
		Query sqlQuery = session.createSQLQuery(FIND_VALUE_BY_KEY_SQL);
		sqlQuery.setParameter(0, key);
		Object value = sqlQuery.uniqueResult();
		return value;
	}

	/**
	 * As per interface spec, when stored value is "true" (ignoring case), this returns <code>true</code>, otherwise it
	 * returns <code>false</code>
	 *
	 * @see org.squashtest.tm.service.configuration.ConfigurationService#getBoolean(java.lang.String)
	 */
	@Override
	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(findConfiguration(key));
	}

	/**
	 * @see org.squashtest.tm.service.configuration.ConfigurationService#set(java.lang.String, boolean)
	 */
	@Override
	public void set(String key, boolean value) {
		String strVal = Boolean.toString(value);
		set(key, strVal);
	}

	/**
	 *
	 * @see org.squashtest.tm.service.configuration.ConfigurationService#set(java.lang.String, java.lang.String)
	 */
	public void set(String key, String value) {
		if (findValue(key) == null) {
			createNewConfiguration(key, value);
		} else {
			updateConfiguration(key, value);
		}
	}

}
