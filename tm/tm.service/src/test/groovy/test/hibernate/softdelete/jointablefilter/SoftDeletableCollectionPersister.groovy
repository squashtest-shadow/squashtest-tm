/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package test.hibernate.softdelete.jointablefilter


import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Collection;
import org.hibernate.sql.Delete;
import org.hibernate.sql.Update;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.persister.collection.BasicCollectionPersister;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.CollectionType;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.type.Type;

class SoftDeletableCollectionPersister extends BasicCollectionPersister  {
	public SoftDeletableCollectionPersister(
	Collection collection,
	CollectionRegionAccessStrategy cacheAccessStrategy,
	Configuration cfg,
	SessionFactoryImplementor factory) throws MappingException, CacheException {
		super( collection, cacheAccessStrategy, cfg, factory );
	}
	
	@Override
	protected String generateDeleteString() {
		Update update = new Update()
				.setTableName( qualifiedTableName )
				.addPrimaryKeyColumns( keyColumnNames );
		
		update.addColumn( "deleted", dialect.toBooleanValueString(true) );
		
		if ( hasWhere ) update.setWhere( sqlWhereString );
		
		if ( getFactory().getSettings().isCommentsEnabled() ) {
			update.setComment( "delete collection " + getRole() );
		}
		
		return update.toStatementString();
	}
	
	@Override
	protected String generateDeleteRowString() {
		
		Update update = new Update( getDialect() )
				.setTableName( qualifiedTableName );
		
		update.addColumn( "deleted", dialect.toBooleanValueString(true) );
		
		if ( hasIdentifier ) {
			update.addPrimaryKeyColumns(identifierColumnName);
		}
		else if ( hasIndex && !indexContainsFormula ) {
			update.addPrimaryKeyColumns( ArrayHelper.join( keyColumnNames, indexColumnNames ) );
		}
		else {
			update.addPrimaryKeyColumns( keyColumnNames );
			update.addPrimaryKeyColumns( elementColumnNames, elementColumnIsInPrimaryKey, elementColumnWriters );
		}
		
		if ( getFactory().getSettings().isCommentsEnabled() ) {
			update.setComment( "delete collection row " + getRole() );
		}
		
		return update.toStatementString();
	}
}
