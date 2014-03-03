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
			// also post process the resulting string to escape the '/' in 
			// node names and reinstate '/' as the legitimate path separator
			
			// See NativeQueries.PATH_SEPARATOR and the associated comment
			String[] toReturn = new String[ids.size()];
			
			for (Object[] res : result){
				Long id = ((BigInteger) res[0]).longValue();
				String path = (String)res[1];
				path = path.replaceAll("\\/", "\\\\/").replaceAll(NativeQueries.PATH_SEPARATOR, "/");
				toReturn[ids.indexOf(id)] = path;
			}
			
			return Arrays.asList(toReturn);
		}
		else{
			
			return Collections.emptyList();
		}
	}

	@Override
	public List<TestCaseLibraryNode> findNodesByPath(List<String> path) {
		List<Long> ids = findNodeIdsByPath(path);
		List<TestCaseLibraryNode>  result = findAllByIds(ids);
		
		// post process the result to ensure the correct order of the result
		TestCaseLibraryNode[] toReturn = new TestCaseLibraryNode[ids.size()];
		for (TestCaseLibraryNode node :  result){
			toReturn[ids.indexOf(node.getId())] = node;
		}
		
		return Arrays.asList(toReturn);
	}

	@Override
	public List<Long> findNodeIdsByPath(List<String> paths) {
		if (!paths.isEmpty()){
			// process the paths parameter : we don't want escaped '/' in there
			List<String> effectiveParameters = unescapeSlashes(paths);
			
			SQLQuery query = currentSession().createSQLQuery(NativeQueries.TCLN_FIND_NODE_IDS_BY_PATH);
			query.setParameterList("paths", effectiveParameters);
			List<Object[]>  result = query.list();
			
			// now ensures that the results are returned in the correct order
			Long[] toReturn = new Long[effectiveParameters.size()];
			
			for (Object[] res : result){
				String path = (String) res[0];
				toReturn[effectiveParameters.indexOf(path)] =  ((BigInteger)res[1]).longValue();
			}
			
			return Arrays.asList(toReturn);
		}
		else{
			return Collections.emptyList();
		}
	}
	
	private List<String> unescapeSlashes(List<String> paths){
		List<String> unescaped = new ArrayList<String>(paths.size());
		for (String orig : paths){
			unescaped.add(orig.replaceAll("\\\\/", "/"));
		}
		return unescaped;
	}
	
}
