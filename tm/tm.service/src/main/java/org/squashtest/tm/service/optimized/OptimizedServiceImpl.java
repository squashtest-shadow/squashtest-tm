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
package org.squashtest.tm.service.optimized;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.security.UserContextHolder;
import org.squashtest.tm.service.user.UserAccountService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OptimizedServiceImpl implements OptimizedService {

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private UserAccountService userAccountService;

	private JdbcTemplate jdbcTemplate;

	@Inject
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<Long> findReadableProjectIds() {
		//ACL class of projects is 1
		//ACL ids of profiles that can read are well... everybody ^^
		String username = UserContextHolder.getUsername();
		Long userId = findUserId(username);
		boolean isAdmin = userIsAdmin(userId);

		if (isAdmin) {
			return jdbcTemplate.queryForList("select PROJECT_ID from PROJECT where PROJECT_TYPE = 'P';", Long.class);
		} else {
			//find all the team of the user and it's party id it self.
			List<Long> partyIds = jdbcTemplate.queryForList("select TEAM_ID from CORE_TEAM_MEMBER where USER_ID = ?;", Long.class, userId);
			//add the user party id itself;
			partyIds.add(userId);
			List<Long> ids = jdbcTemplate.queryForList(SqlRequest.FIND_READABLE_PROJECT_IDS, Long.class, StringUtils.join(partyIds,","));
			return ids;
		}

	}

	private boolean userIsAdmin(Long userId) {
		//checking if user is admin, in that case he can read everything
		Integer count = jdbcTemplate.queryForObject(SqlRequest.USER_IS_ADMIN_COUNT, Integer.class, userId);
		return count > 0;
	}

	private Long findUserId(String username) {
		return jdbcTemplate.queryForObject("select PARTY_ID from CORE_USER where LOGIN = ?;", Long.class, username);
	}
}
