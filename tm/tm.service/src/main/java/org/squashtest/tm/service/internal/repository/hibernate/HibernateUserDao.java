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

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.exception.user.LoginAlreadyExistsException;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.UserDao;

@Repository
public class HibernateUserDao extends HibernateEntityDao<User> implements UserDao {


	static private String FIND_ALL_MANAGER_AND_ADMIN = "SELECT  member.party_id FROM  core_group_member member inner join core_group_authority cga on cga.group_id=member.group_id WHERE cga.authority = 'ROLE_ADMIN' UNION Select auth.PARTY_ID From  CORE_PARTY_AUTHORITY auth where auth.AUTHORITY = 'ROLE_TM_PROJECT_MANAGER'";

	@Inject
	private SessionFactory sessionFactory;

	/**
	 * @return users with all properties fetched, ordered by login
	 */
	@Override
	public List<User> findAllUsersOrderedByLogin() {
		return executeListNamedQuery("user.findAllUsers");
	}

	@Override
	public List<User> findAllActiveUsersOrderedByLogin() {
		return executeListNamedQuery("user.findAllActiveUsers");
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<User> findAllUsers(PagingAndSorting sorter, Filtering filter) {

		User example = new User();
		example.setActive(true);

		String sortedAttribute = sorter.getSortedAttribute();
		SortOrder order = sorter.getSortOrder();


		Criteria crit = currentSession().createCriteria(User.class, "User");


		/* create the query with respect to the filtering */
		if (filter.isDefined()){
			crit = crit.add(filterUsers(filter));
		}

		/* add ordering */
		if (sortedAttribute != null) {
			if (order == SortOrder.ASCENDING) {
				crit.addOrder(Order.asc(sortedAttribute).ignoreCase());
			} else {
				crit.addOrder(Order.desc(sortedAttribute).ignoreCase());
			}
		}


		/* result range */
		crit.setFirstResult(sorter.getFirstItemIndex());
		crit.setMaxResults(sorter.getPageSize());

		return crit.list();

	}



	private Criterion filterUsers(Filtering oFilter){

		String filter = oFilter.getFilter();
		return Restrictions.disjunction()
				.add(Restrictions.ilike("login", filter, MatchMode.ANYWHERE))
				.add(Restrictions.ilike("firstName", filter, MatchMode.ANYWHERE))
				.add(Restrictions.ilike("lastName", filter, MatchMode.ANYWHERE))
				.add(Restrictions.ilike("email", filter, MatchMode.ANYWHERE))
				.add(Restrictions.ilike("audit.createdBy", filter, MatchMode.ANYWHERE))
				.add(Restrictions.ilike("audit.lastModifiedBy", filter, MatchMode.ANYWHERE));


	}



	@Override
	// FIXME : be careful of the filter
	public User findUserByLogin(final String login) {
		return executeEntityNamedQuery("user.findUserByLogin", new SetUserLoginParameterCallback(login));
	}

	private static final class SetUserLoginParameterCallback implements SetQueryParametersCallback {
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

	@Override
	public List<User> findAllNonTeamMembers(final long teamId) {
		return executeListNamedQuery("user.findAllNonTeamMembers", new SetTeamIdParameterCallback(teamId));
	}


	@Override
	public void checkLoginAvailability(String login) {
		if (findUserByLogin(login) != null) {
			throw new LoginAlreadyExistsException();
		}

	}

	@Override
	public int countAllTeamMembers(long teamId) {
		Query query = currentSession().getNamedQuery("user.countAllTeamMembers");
		query.setParameter("teamId", teamId, LongType.INSTANCE);
		return (Integer)query.uniqueResult();
	}


	@Override
	public void unassignUserFromAllTestPlan(long userId) {
		Query query = currentSession().getNamedQuery("user.unassignFromAllCampaignTestPlan");
		query.setParameter("userId", userId, LongType.INSTANCE);
		query.executeUpdate();

		query = currentSession().getNamedQuery("user.unassignFromAllIterationTestPlan");
		query.setParameter("userId", userId, LongType.INSTANCE);
		query.executeUpdate();
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<User> findAllTeamMembers(long teamId, PagingAndSorting paging,
			Filtering filtering) {

		Criteria crit = currentSession().createCriteria(Team.class, "Team")
				.add(Restrictions.eq("Team.id", teamId))
				.createCriteria("Team.members", "User")
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

		/* add ordering */
		String sortedAttribute = paging.getSortedAttribute();
		if (sortedAttribute != null) {
			SortingUtils.addOrder(crit, paging);
		}

		/* add filtering */
		if  (filtering.isDefined()){
			crit = crit.add(filterMembers(filtering));
		}

		/* result range */
		PagingUtils.addPaging(crit, paging);


		return collectFromMapList(crit.list(), "User");

	}

	private Criterion filterMembers(Filtering filtering){
		String filter = filtering.getFilter();
		return Restrictions.disjunction()
				.add(Restrictions.like("User.firstName", filter, MatchMode.ANYWHERE))
				.add(Restrictions.like("User.lastName", filter, MatchMode.ANYWHERE))
				.add(Restrictions.like("User.login", filter, MatchMode.ANYWHERE));
	}


	// **************** private code ****************************

	private static final class SetUserIdsParameterCallback implements SetQueryParametersCallback{
		private List<String> idList;
		private SetUserIdsParameterCallback(List<String> idList){
			this.idList = idList;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("userIds", idList);
		}
	}

	private static final class SetTeamIdParameterCallback implements SetQueryParametersCallback{
		private long teamId;
		private SetTeamIdParameterCallback(long teamId){
			this.teamId = teamId;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameter("teamId", teamId, LongType.INSTANCE);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findAllAdminOrManager() {
		Query query = sessionFactory.getCurrentSession().createSQLQuery(FIND_ALL_MANAGER_AND_ADMIN);
		query.setResultTransformer(new SqLIdResultTransformer());
		List<Long> ids = query.list();
		return  findAllByIds(ids);
	}

}
