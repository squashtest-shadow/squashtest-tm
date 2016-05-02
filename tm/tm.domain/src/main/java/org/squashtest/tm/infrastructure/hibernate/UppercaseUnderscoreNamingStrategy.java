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
package org.squashtest.tm.infrastructure.hibernate;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.AssertionFailure;
import org.hibernate.cfg.DefaultNamingStrategy;
import org.hibernate.cfg.NamingStrategy;

/**
 * {@link NamingStrategy} which maps a CamelCase entity name to an UPPERCASE_WITH_UNDERSCORES table name.
 *
 * @author Gregory Fouquet
 *
 */
public class UppercaseUnderscoreNamingStrategy implements NamingStrategy {
	private NamingStrategy delegateStrategy = new DefaultNamingStrategy();

	@Override
	public String classToTableName(String className) {
		return toUppercaseUnderscore(delegateStrategy.classToTableName(className));
	}

	private String toUppercaseUnderscore(String className) {
		String underscoredName = addUnderscoresBetweenCamelCaseWords(className);
		return underscoredName.toUpperCase(); // NOSONAR No need of localization
	}

	private String addUnderscoresBetweenCamelCaseWords(String camelCaseName) {
		String[] tokens = StringUtils.splitByCharacterTypeCamelCase(camelCaseName);
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < tokens.length; i++) {
			if (i > 0) {
				sb.append('_');
			}
			sb.append(tokens[i]);
		}

		return sb.toString();
	}

	@Override
	public String propertyToColumnName(String propertyName) {
		return toUppercaseUnderscore(delegateStrategy.propertyToColumnName(propertyName));
	}

	@Override
	public String tableName(String tableName) {
		return tableName;
	}

	@Override
	public String columnName(String columnName) {
		return columnName;
	}

	@Override
	public String collectionTableName(String ownerEntity, String ownerEntityTable, String associatedEntity,
			String associatedEntityTable, String propertyName) {
		return tableName(ownerEntityTable + '_' + propertyToColumnName(propertyName));
	}

	@Override
	public String joinKeyColumnName(String joinedColumn, String joinedTable) {
		// adapted from DefaultNamingStrategy
		return columnName(joinedColumn);
	}

	@Override
	public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName,
			String referencedColumnName) {
		// Adapted from DefaultNamingStrategy

		String header = propertyName != null ? propertyToColumnName(propertyName) : propertyTableName;
		if (header == null) {
			throw new AssertionFailure("NammingStrategy not properly filled");
		}
		return columnName(header); // + "_" + referencedColumnName not used for backward compatibility
	}

	@Override
	public String logicalColumnName(String columnName, String propertyName) {
		return StringUtils.isNotEmpty(columnName) ? columnName : toUppercaseUnderscore(delegateStrategy
				.logicalColumnName(columnName, propertyName));
	}

	@Override
	public String logicalCollectionTableName(String tableName, String ownerEntityTable, String associatedEntityTable,
			String propertyName) {
		// adapted from DefaultNamingStrategy
		if (tableName != null) {
			return tableName;
		} else {
			// use of a stringbuffer to workaround a JDK bug
			return new StringBuffer(ownerEntityTable).append("_")
					.append(associatedEntityTable != null ? associatedEntityTable : propertyToColumnName(propertyName))
					.toString();
		}
	}

	@Override
	public String logicalCollectionColumnName(String columnName, String propertyName, String referencedColumn) {
		// adapted from DefaultNamingStrategy
		return StringUtils.isNotEmpty(columnName) ? columnName : propertyToColumnName(propertyName) + "_"
				+ referencedColumn;
	}
}
