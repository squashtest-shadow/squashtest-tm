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

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.service.internal.repository.ProjectFilterDao;
/***
 * 
 * DAO for org.squashtest.tm.domain.projectfilter.ProjectFilter
 * 
 * @author xpetitrenaud
 *
 */

@Repository
public class HibernateProjectFilterDao extends HibernateEntityDao<ProjectFilter> implements ProjectFilterDao {

	@Override
	public ProjectFilter findProjectFilterByUserLogin(final String givenUserLogin) {
		//first set the parameter
		SetQueryParametersCallback newCallBack = new GivenUserParametersCallback(givenUserLogin);

		return executeEntityNamedQuery("projectFilter.findByUserLogin", newCallBack);
	}

	private static final class GivenUserParametersCallback implements SetQueryParametersCallback{

		private String givenUserLogin;
		private GivenUserParametersCallback(String givenUserLogin){
			this.givenUserLogin = givenUserLogin;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setString("givenUserLogin", givenUserLogin);
		}
	}

}
