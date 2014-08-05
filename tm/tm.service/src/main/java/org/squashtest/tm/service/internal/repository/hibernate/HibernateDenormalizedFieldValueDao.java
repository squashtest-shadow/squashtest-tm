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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections.map.MultiValueMap;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.service.internal.repository.CustomDenormalizedFieldValueDao;

@Repository("CustomDenormalizedFieldValueDao")
public class HibernateDenormalizedFieldValueDao extends HibernateDao<DenormalizedFieldValue> implements
		CustomDenormalizedFieldValueDao {

	
	/*
	 * This is done in four steps : 
	 * 1/ insert the values (DENORMALIZED_FIELD_VALUES)
	 * 2/ insert the rendering locations (DENORMALIZED_FIELD_RENDERING_LOCATION)
	 * 3/ insert the options for the selects (DENORMALIZED_FIELD_OPTIONS)
	 * 
	 * (non-Javadoc)
	 * @see org.squashtest.tm.service.internal.repository.CustomDenormalizedFieldValueDao#fastCreateDenormalizedValuesForSteps(long)
	 */
	@Override
	public void fastCreateDenormalizedValuesForSteps(long executionId) {
		Session session = currentSession();
		
		// first, synchronize the db content before acting
		session.flush();
		
		// **************** step 1 ****************
		Query query1 = session.createSQLQuery(NativeQueries.FAST_CREATE_EXECUTION_STEP_DENORMALIZED_VALUES);
		query1.setParameter("executionId", executionId);
		query1.executeUpdate();
		
		// **************** step 2 ****************
		Query query2 = session.createSQLQuery(NativeQueries.FAST_CREATE_EXECUTION_STEP_DENORMALIZED_LOCATION);
		query2.setParameter("executionId", executionId);
		query2.executeUpdate();
		
		// **************** step 3 ****************
		Query query3 = session.createSQLQuery(NativeQueries.FAST_CREATE_EXECTUTION_STEP_DENORMALIZED_OPTIONS);
		query3.setParameter("executionId", executionId);
		query3.executeUpdate();
	
	}
	

}
