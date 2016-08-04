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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.squashtest.tm.infrastructure.hibernate;

import java.util.Locale;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Shamelessly ripped from org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy
 * and now defunct org.squashtest.tm.infrastructure.hibernate.UppercaseUnderscoreNamingStrategy
 * 
 * @author bsiri
 */
public class UppercaseUnderscorePhysicalNaming 
implements PhysicalNamingStrategy{
    
   	@Override
	public Identifier toPhysicalCatalogName(Identifier name,
			JdbcEnvironment jdbcEnvironment) {
                // not modified
		return name;
	}

	@Override
	public Identifier toPhysicalSchemaName(Identifier name,
			JdbcEnvironment jdbcEnvironment) {
                // not modified
		return name;
	}

	@Override
	public Identifier toPhysicalTableName(Identifier name,
			JdbcEnvironment jdbcEnvironment) {
                return apply(name);
	}

	@Override
	public Identifier toPhysicalSequenceName(Identifier name,
			JdbcEnvironment jdbcEnvironment) {
                // not modified
		return name;
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier name,
			JdbcEnvironment jdbcEnvironment) {
		return apply(name);
	}

	private Identifier apply(Identifier name) {
		if (name == null) {
			return null;
		}
		StringBuilder text = new StringBuilder(name.getText().replace('.', '_'));
		for (int i = 1; i < text.length() - 1; i++) {
			if (isUnderscoreRequired(text.charAt(i - 1), text.charAt(i))) {
				text.insert(i++, '_');
			}
		}
		return new Identifier(text.toString().toUpperCase(Locale.ROOT), name.isQuoted());
	}

	private boolean isUnderscoreRequired(char before, char current) {
		return Character.isLowerCase(before) && Character.isUpperCase(current);
	}
    
}
