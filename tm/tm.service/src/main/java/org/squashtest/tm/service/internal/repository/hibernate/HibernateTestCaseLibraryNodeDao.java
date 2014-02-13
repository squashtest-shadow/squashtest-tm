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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;


@Repository("squashtest.tm.repository.TestCaseLibraryNodeDao")
public class HibernateTestCaseLibraryNodeDao extends HibernateEntityDao<TestCaseLibraryNode> implements LibraryNodeDao<TestCaseLibraryNode>{

	

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getParentsName(long entityId) {
		SQLQuery query = currentSession().createSQLQuery(NativeQueries.TCLN_FIND_SORTED_PARENT_NAMES);
		query.setParameter("nodeId", entityId, LongType.INSTANCE);
		return query.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getParentsIds(long entityId) {
		SQLQuery query = currentSession().createSQLQuery(NativeQueries.TCLN_FIND_SORTED_PARENT_IDS);
		query.setResultTransformer(new SqLIdResultTransformer());
		query.setParameter("nodeId", entityId, LongType.INSTANCE);
		return query.list();
	}
	

	@Override
	public List<String> getPathsAsString(List<Long> ids) {
		
		if (! ids.isEmpty()){
			SQLQuery query = currentSession().createSQLQuery(NativeQueries.TCLN_GET_PATHS_AS_STRING);
			query.setParameterList("nodeIds", ids, LongType.INSTANCE);
			List<Object[]>  result = query.list();
			
			// now ensures that the results are returned in the correct order
			String[] toReturn = new String[ids.size()];
			
			for (Object[] res : result){
				Long id = ((BigInteger) res[0]).longValue();
				toReturn[ids.indexOf(id)] =  (String)res[1];
			}
			
			return Arrays.asList(toReturn);
		}
		else{
			
			return Collections.emptyList();
		}
	}

	@Override
	public List<TestCaseLibraryNode> findNodesByPath(List<String> path) {
		throw new UnsupportedOperationException("unimplemented yet");
	}

	@Override
	public List<Long> findNodeIdsByPath(List<String> path) {
		throw new UnsupportedOperationException("unimplemented yet");
	}
	
	
	
}
