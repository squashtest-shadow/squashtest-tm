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

import org.squashtest.csp.tm.internal.repository.hibernate.HibernateCustomCustomFieldBindingDao.NewBindingPosition;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;


@UnitilsSupport
@DataSet
class HibernateCustomCustomFieldBindingDaoIT extends DbunitDaoSpecification {

	@Inject
	HibernateCustomCustomFieldBindingDao dao
	
	
	def "should get correct indexes from a messed up table"(){

		when :
			List<NewBindingPosition> newPositions = dao.recomputeBindingPositions();
		
		then :
			newPositions.size() == 9
		
	}
	
}
