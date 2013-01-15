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
package org.squashtest.csp.tm.internal.infrastructure.hibernate;

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
 * 
 * 
 */
public class TestStepPersister extends JoinedSubclassEntityPersister {

	private static final String NONFORMATTED_TABLE_NAME = "TEST_CASE_STEPS";
	private static final String NONFORMATTED_COLUMN_NAME = "STEP_ID";
	
	private String formattedTableName;
	private String[] formattedColumnName = new String[1];
	
	/*
	 * instead of testing which column will be hidden by its name, we'll cache its index 
	 * whenever we have the chance
	 */
	private int _cachedIndex=-1;		

	public TestStepPersister(PersistentClass persistentClass,
			EntityRegionAccessStrategy cacheAccessStrategy,
			SessionFactoryImplementor factory, Mapping mapping)
			throws HibernateException {

		super(persistentClass, cacheAccessStrategy, factory, mapping);

		init(persistentClass, factory);
	}


	
	@Override
	protected String[] getSubclassTableKeyColumns(int j) {
		if (isTheJoinTable(j)){
			return formattedColumnName;
		}
		else{
			return super.getSubclassTableKeyColumns(j);
		}
	}

	
	
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
	
	//
	private void _createColumnName(SessionFactoryImplementor factory){
		Column column = new Column(NONFORMATTED_COLUMN_NAME);
		formattedColumnName[0] = column.getQuotedName(factory.getDialect());
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
	
}











































