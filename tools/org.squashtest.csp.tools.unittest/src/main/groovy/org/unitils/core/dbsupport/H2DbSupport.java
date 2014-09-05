/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.unitils.core.dbsupport;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import org.unitils.core.UnitilsException;
import static org.unitils.thirdparty.org.apache.commons.dbutils.DbUtils.closeQuietly;

/**
 * Implementation of {@link org.unitils.core.dbsupport.DbSupport} for a H2 database
 * 
 * @author Mark Thomas
 */
public class H2DbSupport extends DbSupport {

	/**
	 * Creates support for H2 databases.
	 */
	public H2DbSupport() {
		super("h2");
	}

	/**
	 * Returns the names of all tables in the database.
	 * 
	 * @return The names of all tables in the database
	 */
	@Override
	public Set<String> getTableNames() {
		return getSQLHandler().getItemsAsStringSet(
				"select TABLE_NAME from " + "INFORMATION_SCHEMA.TABLES where TABLE_TYPE = 'TABLE' AND "
						+ "TABLE_SCHEMA = '" + getSchemaName() + "'"); // NOSONAR mind-your-own-business,-it's-not-production-code
	}

	/**
	 * Gets the names of all columns of the given table.
	 * 
	 * @param tableName
	 *            The table, not null
	 * @return The names of the columns of the table with the given name
	 */
	@Override
	public Set<String> getColumnNames(String tableName) {
		return getSQLHandler().getItemsAsStringSet(
				"select COLUMN_NAME from " + "INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = '" + tableName
						+ "' AND TABLE_SCHEMA = '" + getSchemaName() + "'"); // NOSONAR mind-your-own-business,-it's-not-production-code
	}

	/**
	 * Gets the names of all primary columns of the given table.
	 * 
	 * @param tableName
	 *            The table, not null
	 * @return The names of the primary key columns of the table with the given name
	 */
	@Override
	public Set<String> getIdentityColumnNames(String tableName) {
		return getSQLHandler().getItemsAsStringSet(
				"select COLUMN_NAME from " + "INFORMATION_SCHEMA.INDEXES where PRIMARY_KEY = 'TRUE' AND "
						+ "TABLE_NAME = '" + tableName + "' AND TABLE_SCHEMA = '" + getSchemaName() + "'"); // NOSONAR mind-your-own-business,-it's-not-production-code
	}

	/**
	 * Retrieves the names of all the views in the database schema.
	 * 
	 * @return The names of all views in the database
	 */
	@Override
	public Set<String> getViewNames() {
		return getSQLHandler().getItemsAsStringSet(
				"select TABLE_NAME from " + "INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = '" + getSchemaName() + "'"); // NOSONAR mind-your-own-business,-it's-not-production-code
	}

	/**
	 * Retrieves the names of all the sequences in the database schema.
	 * 
	 * @return The names of all sequences in the database
	 */
	@Override
	public Set<String> getSequenceNames() {
		return getSQLHandler().getItemsAsStringSet(
				"select SEQUENCE_NAME from " + "INFORMATION_SCHEMA.SEQUENCES where SEQUENCE_SCHEMA = '"
						+ getSchemaName() + "'"); // NOSONAR mind your own business, it's not production code
	}

	/**
	 * Retrieves the names of all the triggers in the database schema.
	 * 
	 * @return The names of all triggers in the database
	 */
	@Override
	public Set<String> getTriggerNames() {
		return getSQLHandler().getItemsAsStringSet(
				"select TRIGGER_NAME from " + "INFORMATION_SCHEMA.TRIGGERS where TRIGGER_SCHEMA = '" + getSchemaName()
						+ "'"); // NOSONAR mind your own business, it's not production code
	}

	/**
	 * Returns the value of the sequence with the given name.
	 * <p/>
	 * Note: this can have the side-effect of increasing the sequence value.
	 * 
	 * @param sequenceName
	 *            The sequence, not null
	 * @return The value of the sequence with the given name
	 */
	@Override
	public long getSequenceValue(String sequenceName) {
		return getSQLHandler().getItemAsLong(
				"select CURRENT_VALUE from " + "INFORMATION_SCHEMA.SEQUENCES where SEQUENCE_SCHEMA = '"
						+ getSchemaName() + "' and SEQUENCE_NAME = '" + sequenceName + "'"); // NOSONAR mind-your-own-business,-it's-not-production-code
	}

	/**
	 * Sets the next value of the sequence with the given sequence name to the given sequence value.
	 * 
	 * @param sequenceName
	 *            The sequence, not null
	 * @param newSequenceValue
	 *            The value to set
	 */
	@Override
	public void incrementSequenceToValue(String sequenceName, long newSequenceValue) {
		getSQLHandler()
				.executeUpdate("alter sequence " + qualified(sequenceName) + " restart with " + newSequenceValue); // NOSONAR mind-your-own-business,-it's-not-production-code
	}

	/**
	 * Increments the identity value for the specified identity column on the specified table to the given value.
	 * 
	 * @param tableName
	 *            The table with the identity column, not null
	 * @param identityColumnName
	 *            The column, not null
	 * @param identityValue
	 *            The new value
	 */
	@Override
	public void incrementIdentityColumnToValue(String tableName, String identityColumnName, long identityValue) {
		getSQLHandler().executeUpdate(
				"alter table " + qualified(tableName) + " alter column " + quoted(identityColumnName)
						+ " RESTART WITH " + identityValue); // NOSONAR mind-your-own-business,-it's-not-production-code
	}

	/**
	 * Disables all referential constraints (e.g. foreign keys) on all tables in the schema
	 */
	@Override
	public void disableReferentialConstraints() {
		getSQLHandler().executeUpdate("SET REFERENTIAL_INTEGRITY FALSE");
	}

	/**
	 * Disables all value constraints (e.g. not null) on all tables in the schema
	 */
	@Override
	public void disableValueConstraints() {
		disableCheckAndUniqueConstraints();
		disableNotNullConstraints();
	}

	/**
	 * Disables all check and unique constraints on all tables in the schema
	 */
	protected void disableCheckAndUniqueConstraints() {
		Connection connection = null;
		Statement queryStatement = null;
		Statement alterStatement = null;
		ResultSet resultSet = null;
		try {
			connection = getSQLHandler().getDataSource().getConnection();
			queryStatement = connection.createStatement();
			alterStatement = connection.createStatement();

			resultSet = queryStatement.executeQuery("select TABLE_NAME, "
					+ "CONSTRAINT_NAME from INFORMATION_SCHEMA.CONSTRAINTS where "
					+ "CONSTRAINT_TYPE IN ('CHECK', 'UNIQUE') AND CONSTRAINT_SCHEMA " + "= '" + getSchemaName() + "'"); // NOSONAR mind-your-own-business,-it's-not-production-code
			while (resultSet.next()) {
				String tableName = resultSet.getString("TABLE_NAME");
				String constraintName = resultSet.getString("CONSTRAINT_NAME");
				alterStatement.executeUpdate("alter table " + qualified(tableName) + " drop constraint "
						+ quoted(constraintName)); // NOSONAR mind-your-own business,-it's-not-production-code
			}
		} catch (Exception e) {
			throw new UnitilsException("Error while disabling check and unique " + "constraints on schema "
					+ getSchemaName(), e);
		} finally {
			closeQuietly(queryStatement);
			closeQuietly(connection, alterStatement, resultSet);
		}
	}

	/**
	 * Disables all not null constraints on all tables in the schema
	 */
	protected void disableNotNullConstraints() {
		Connection connection = null;
		Statement queryStatement = null;
		Statement alterStatement = null;
		ResultSet resultSet = null;
		try {
			connection = getSQLHandler().getDataSource().getConnection();
			queryStatement = connection.createStatement();
			alterStatement = connection.createStatement();

			// Do not remove PK constraints
			resultSet = queryStatement.executeQuery("select col.TABLE_NAME, "
					+ "col.COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS col where "
					+ "col.IS_NULLABLE = 'NO' and col.TABLE_SCHEMA = '" + getSchemaName() + "' "
					+ "AND NOT EXISTS (select COLUMN_NAME "
					+ "from INFORMATION_SCHEMA.INDEXES pk where pk.TABLE_NAME = "
					+ "col.TABLE_NAME and pk.COLUMN_NAME = col.COLUMN_NAME and " + "pk.TABLE_SCHEMA = '"
					+ getSchemaName() + "' AND pk.PRIMARY_KEY = TRUE)"); // NOSONAR mind-your-own
																			// business,-it's-not-production-code
			while (resultSet.next()) {
				String tableName = resultSet.getString("TABLE_NAME");
				String columnName = resultSet.getString("COLUMN_NAME");
				alterStatement.executeUpdate("alter table " + qualified(tableName) + " alter column "
						+ quoted(columnName) + " set null"); // NOSONAR mind-your-own business,-it's-not-production-code
			}
		} catch (Exception e) {
			throw new UnitilsException("Error while disabling not null " + "constraints on schema " + getSchemaName(),
					e);
		} finally {
			closeQuietly(queryStatement);
			closeQuietly(connection, alterStatement, resultSet);
		}
	}

	/**
	 * Sequences are supported.
	 * 
	 * @return True
	 */
	@Override
	public boolean supportsSequences() {
		return true;
	}

	/**
	 * Triggers are supported.
	 * 
	 * @return True
	 */
	@Override
	public boolean supportsTriggers() {
		return true;
	}

	/**
	 * Identity columns are supported.
	 * 
	 * @return True
	 */
	@Override
	public boolean supportsIdentityColumns() {
		return true;
	}

	/**
	 * Cascade are supported.
	 * 
	 * @return True
	 */
	@Override
	public boolean supportsCascade() {
		return true;
	}
}
