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

import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.LoginAlreadyExistsException;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.UserDao;

@Repository
public class HibernateUserDao extends HibernateEntityDao<User> implements UserDao {

	/**
	 * @return users with all properties fetched, ordered by login
	 */
	@Override
	public List<User> findAllUsersOrderedByLogin() {
		return executeListNamedQuery("user.findAllUsers");
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findAllUsersFiltered(CollectionSorting filter) {
		Session session = currentSession();
		
		String sortedAttribute = filter.getSortedAttribute();
		String order = filter.getSortingOrder();
		
		Criteria crit = session.createCriteria(User.class, "User");
		
		/* add ordering */
		if (sortedAttribute != null) {
			if (order.equals("asc")) {
				crit.addOrder(Order.asc(sortedAttribute).ignoreCase());
			} else {
				crit.addOrder(Order.desc(sortedAttribute).ignoreCase());
			}
		}
		
		/* result range */
		crit.setFirstResult(filter.getFirstItemIndex());
		crit.setMaxResults(filter.getPageSize());

		return crit.list();
		
	}

	@Override
	// FIXME : be careful of the filter 
	public User findUserByLogin(final String login) {
		return executeEntityNamedQuery("user.findUserByLogin", new SetUserLoginParameterCallback(login));
	}
	
	private static class SetUserLoginParameterCallback implements SetQueryParametersCallback {
		private String login;
		private SetUserLoginParameterCallback(String login){
			this.login = login;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameter("userLogin", login);
		}
	}

	@Override
	public List<User> findUsersByLoginList(final List<String> idList) {

		if (idList.isEmpty()) {
			return Collections.emptyList();
		} else {

			SetQueryParametersCallback setParams = new SetUserIdsParameterCallback(idList);
			return executeListNamedQuery("user.findUsersByLoginList", setParams);
		}
	}
	private static class SetUserIdsParameterCallback implements SetQueryParametersCallback{
		private List<String> idList;
		private SetUserIdsParameterCallback(List<String> idList){
			this.idList = idList;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("userIds", idList);
		}
	}
	
	@Override
	public void checkLoginAvailability(String login) {
		if (findUserByLogin(login) != null) {
			throw new LoginAlreadyExistsException();
		}

	}
}
