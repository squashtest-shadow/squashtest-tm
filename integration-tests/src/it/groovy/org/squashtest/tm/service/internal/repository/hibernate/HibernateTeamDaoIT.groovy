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
package org.squashtest.tm.service.internal.repository.hibernate

import java.util.List;

import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.RootEntityResultTransformer;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.users.Team;
import org.unitils.dbunit.annotation.DataSet;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SortOrder;

import spock.unitils.UnitilsSupport;


@UnitilsSupport
@Transactional
class HibernateTeamDaoIT extends DbunitDaoSpecification{
	
	@Inject SessionFactory sessionFactory
	@Inject HibernateTeamDao dao
	
	
	@DataSet("HibernateTeamDaoIT.setup.xml")
	def "should sort the teams by name desc"(){
		
		given :
			PagingAndSorting paging = new DefaultPagingAndSorting(sortedAttribute:"Team.name", order : SortOrder.DESCENDING)
	
		and :
			Filtering filter = DefaultFiltering.NO_FILTERING
		
		when :
			def res = dao.findSortedTeams(paging, filter)
		
		then :
			res.collect{it.name} == ["triple team", "simple team", "double team"]
		
		
	}
	
	@DataSet("HibernateTeamDaoIT.setup.xml")
	def "should sort the teams by size desc"(){
		
		given :
			PagingAndSorting paging = new DefaultPagingAndSorting(sortedAttribute:"Team.size", order : SortOrder.DESCENDING)
	
		and :
			Filtering filter = DefaultFiltering.NO_FILTERING
		
		when :
			def res = dao.findSortedTeams(paging, filter)
		
		then :
			res.collect{it.name} == ["triple team", "double team", "simple team"]
		
		
	}
	
	@DataSet("HibernateTeamDaoIT.setup.xml")
	def "should sort the teams by size asc and looking only for those having 'ple' in their name"(){
		
		given :
			PagingAndSorting paging = new DefaultPagingAndSorting(sortedAttribute:"Team.size", order : SortOrder.ASCENDING)
	
		and :
			Filtering filter = new DefaultFiltering(null, "ple")
		
		when :
			def res = dao.findSortedTeams(paging, filter)
		
		then :
			res.collect{it.name} == ["simple team","triple team" ]
		
		
	}
	
}
