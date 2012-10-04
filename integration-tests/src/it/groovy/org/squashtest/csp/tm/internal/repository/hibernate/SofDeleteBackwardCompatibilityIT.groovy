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
package org.squashtest.csp.tm.internal.repository.hibernate

import javax.inject.Inject;

import org.hibernate.Query;
import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestCaseFolderDao;
import org.unitils.dbunit.annotation.DataSet 

import spock.unitils.UnitilsSupport;


@NotThreadSafe
@UnitilsSupport
@Transactional
class SofDeleteBackwardCompatibilityIT extends DbunitDaoSpecification {

	@Inject
	private TestCaseDao tcDao;
	
	@Inject
	private TestCaseFolderDao fDao;
	
	

	
	@DataSet("SofDeleteBackwardCompatibility.test.xml")
	def "should not retrieve a soft previously soft deleted test case, though still present in base"(){
		when :
			def tc = tcDao.findById(1l);
			
		then :
			tc == null
			found("test_case", "tcln_id", 1l)
		
		
	}
	
	@DataSet("SofDeleteBackwardCompatibility.test.xml")
	def "should not retrieve a soft previously soft deleted test case folder, though still present in base"(){
		when :
			def f = fDao.findById(2l);
			
		then :
			f == null
			found("test_case_folder", "tcln_id", 2l)
		
		
	}
	
	
	private boolean found(String tableName, String idColumnName, Long id){
		String sql = "select count(*) from "+tableName+" where "+idColumnName+" = :id";
		Query query = getSession().createSQLQuery(sql);
		query.setParameter("id", id);
		
		def result = query.uniqueResult();
		return (result != 0)
	}
	
	private Object findEntity(Class<?> entityClass, Long id){
		return getSession().get(entityClass, id);
	}
}
