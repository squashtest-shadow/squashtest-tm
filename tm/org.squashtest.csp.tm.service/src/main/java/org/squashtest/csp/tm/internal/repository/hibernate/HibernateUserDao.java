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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.LoginAlreadyExistsException;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.internal.repository.UserDao;

@Repository
public class HibernateUserDao extends HibernateEntityDao<User> implements UserDao {

	@Override
	public List<User> findAllUsers() {
		return executeListNamedQuery("user.findAllUsers");
	}

	@Override
	public List<User> findAllUsersFiltered(CollectionFilter filter) {

		List<User> users = executeListNamedQuery("user.findAllUsers");
		int listSize = users.size();

		int startIndex = filter.getFirstItemIndex();
		int lastIndex = filter.getFirstItemIndex() + filter.getMaxNumberOfItems();

		// prevent IndexOutOfBoundException :
		if (startIndex >= listSize) {
			return new LinkedList<User>(); // ie resultset is empty
		}

		if (lastIndex >= listSize) {
			lastIndex = listSize;
		}

		return users.subList(startIndex, lastIndex);
	}

	@Override
	// FIXME : be careful of the filter
	public User findUserByLogin(final String login) {
		return executeEntityNamedQuery("user.findUserByLogin", new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("userLogin", login);
			}
		});
	}

	@Override
	public List<User> findUsersByLoginList(final List<String> idList) {

		if (idList.isEmpty()) {
			return Collections.emptyList();
		} else {

			SetQueryParametersCallback setParams = new SetQueryParametersCallback() {

				@Override
				public void setQueryParameters(Query query) {
					query.setParameterList("userIds", idList);
				}
			};
			return executeListNamedQuery("user.findUsersByLoginList", setParams);
		}
	}

	@Override
	public List<User> findByIdList(final Collection<Long> idList) {
		SetQueryParametersCallback setParams = new SetQueryParametersCallback() {
			@Override
			public void setQueryParameters(Query query) {
				query.setParameterList("idList", idList);
			}
		};
		return executeListNamedQuery("user.findAllByIdList", setParams);
	}
	
	@Override
	public void checkLoginAvailability(String login) {
		if (findUserByLogin(login) != null) {
			throw new LoginAlreadyExistsException();
		}

	}
}
