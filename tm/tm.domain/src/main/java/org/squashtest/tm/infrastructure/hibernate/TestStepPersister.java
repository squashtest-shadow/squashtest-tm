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
package org.squashtest.tm.infrastructure.hibernate;

import java.util.Iterator;

import org.hibernate.HibernateException;
import org.hibernate.cache.access.EntityRegionAccessStrategy;
import org.hibernate.engine.Mapping;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;


/*
 * Since version 1.5.0 :
 * 
 * 
 * What
 * ====================== 
 * 
 * This class works around a bug on the reverse mapping between the test steps and the test case that own them.
 * 
 * 
 * Why
 * ======================
 * 
 * Hibernate 3.6.10 (and probably other releases) cannot process properly the following case :
 * 
 * - bi-directional ManyToOne (owned by the 1 side),
 * - using a join table,
 * - using an index column,
 * - mapped in a superclass using the joined-sublasses strategy
 * 
 * Specifically the problem lies in org.hibernate.persister.entity.JoinedSubclassEntityPersister constructor, line 277 -> 281.
 * In that section of the code, the foreign key between the master table (TEST_STEP) and the join table (TEST_CASE_STEPS) is 
 * wrongly identified : it is assumed to be the the primary key of the join table regardless of what the annotations says  
 * (and the primary key isn't right anyway). 
 * 
 * The consequence is that Hibernate believe it must join on TEST_CASE_ID, while the correct column is STEP_ID. 
 * 
 * 
 * How
 * =======================
 * 
 * To work around this we override the function #getSubclassTableKeyColumns(), that returns accepts an index as input and returns 
 * the foreign key for this index. This method is invoked anytime the persister generates a join sql fragment. 
 * When the overriden function is requested for the foreign key of the table TEST_CASE_STEPS, it will return the correct foreign key
 * (STEP_ID), instead of the wrong one (TEST_CASE_ID). 
 * 
 * This class reuses some bits of the initialization code in order to generate the final name of the table and column, according to 
 * target database dialect.
 * 
 * 
 * 
 */
public class TestStepPersister extends JoinedSubclassEntityPersister {

	private static final String NONFORMATTED_TABLE_NAME = "TEST_CASE_STEPS";
	private static final String NONFORMATTED_COLUMN_NAME = "STEP_ID";
	
	private String formattedTableName;
	private String[] formattedColumnName = new String[1];
	
	/*
	 * At first, when Hibernate invokes getSubclassTableKeyColumns(int) we must test if 
	 * the override applies by comparing the string names of the requested table.
	 * 
	 * In order to prevent systematic and expensive string comparison we later on 
	 * cache the index of that data, once it is known to us.
	 */
	private int _cachedIndex=-1;		

	public TestStepPersister(PersistentClass persistentClass,
			EntityRegionAccessStrategy cacheAccessStrategy,
			SessionFactoryImplementor factory, Mapping mapping)
			throws HibernateException {

		super(persistentClass, cacheAccessStrategy, factory, mapping);

		init(persistentClass, factory);
	}


	
	/*
	 * This override is the very reason of that class(non-Javadoc)
	 * @see org.hibernate.persister.entity.JoinedSubclassEntityPersister#getSubclassTableKeyColumns(int)
	 */
	@Override
	protected String[] getSubclassTableKeyColumns(int j) {
		if (isTheJoinTable(j)){
			return formattedColumnName;
		}
		else{
			return super.getSubclassTableKeyColumns(j);
		}
	}

	
	private boolean isTheJoinTable(int index){
		if (_cachedIndex==-1){
			boolean isTheOne = getSubclassTableName(index).equals(formattedTableName);
			if (isTheOne){
				_cachedIndex=index;
			}
			return isTheOne;
		}
		else{
			return (_cachedIndex == index);
		}
	}
	
	
	// **************************** init **************************
	
	private void init(PersistentClass persistentClass, SessionFactoryImplementor factory){
		_createTableNamePattern(persistentClass, factory);
		_createColumnName(factory);
		
	}
	
	
	private void _createTableNamePattern(PersistentClass persistentClass, SessionFactoryImplementor factory){
		Iterator joinIter = persistentClass.getJoinClosureIterator(); 
		while (joinIter.hasNext()){
			Table tab = ((Join) joinIter.next()).getTable();
			if (tab.getName().toUpperCase().equals(NONFORMATTED_TABLE_NAME)){
				formattedTableName = tab.getQualifiedName(factory.getDialect(), 
										factory.getSettings().getDefaultCatalogName(), 
										factory.getSettings().getDefaultSchemaName());
				return;
			}
		}
		throw new IllegalArgumentException("TestStepPersister : could not find the join table TEST_CASE_STEPS");
	}
	
	
	private void _createColumnName(SessionFactoryImplementor factory){
		Column column = new Column(NONFORMATTED_COLUMN_NAME);
		formattedColumnName[0] = column.getQuotedName(factory.getDialect());
	}

}

