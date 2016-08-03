/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.it.infrastructure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.dbunit.dataset.IDataSet;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.unitils.dbunit.datasetloadstrategy.impl.CleanInsertLoadStrategy;
import org.unitils.dbunit.util.DbUnitDatabaseConnection;

public class DeleteDataSetLoadStrategy extends CleanInsertLoadStrategy {

	public Connection getConnection(Session session) {  
		try {
			SessionFactoryImplementor sfi = (SessionFactoryImplementor) session.getSessionFactory();
	    	ConnectionProvider cp = sfi.getConnectionProvider();
	    	return cp.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
    }
	
	public void delete(Session session, IDataSet dataSet){
		try{
			String [] tables = dataSet.getTableNames();
			StringBuilder builder = new StringBuilder();
			for (int i=tables.length-1; i>=0;i--){
				builder.append("delete from "+tables[i]+";");
			}
			
			Query query = session.createSQLQuery(builder.toString());
			
			query.executeUpdate();
	
			getConnection(session).commit();
			
			
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
		
	}
	
	
	public void execute(Connection connection, IDataSet dataSet){
	try{
			
			String [] tables = dataSet.getTableNames();
			StringBuilder builder = new StringBuilder();
			for (int i=tables.length-1; i>=0;i--){
				builder.append("delete from "+tables[i]+";");
			}
			
			
			PreparedStatement statement = connection.prepareStatement(builder.toString());
			
			statement.execute();

			connection.commit();
			
			
			
		}catch(Exception e){
			throw new RuntimeException(e);
		}	
	}
	
	@Override
	public void execute(DbUnitDatabaseConnection dbUnitDatabaseConnection,
			IDataSet dataSet) {
		try{
			execute(dbUnitDatabaseConnection.getConnection(), dataSet);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

}
