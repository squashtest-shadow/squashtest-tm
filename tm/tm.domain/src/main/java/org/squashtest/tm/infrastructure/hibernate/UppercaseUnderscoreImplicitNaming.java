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
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.model.source.spi.AttributePath;

/**
 * That classe fixes naming problems for Hibernate initialization. Due to the large amount of methods to implement
 * the goal here is to type as few code as possible to make it work. Currently I have a problem with the @Any in CustomReportLibraryNode
 * and putting this solves the problem, don't ask me why.
 * 
 * @author bsiri
 */
public class UppercaseUnderscoreImplicitNaming extends ImplicitNamingStrategyJpaCompliantImpl implements ImplicitNamingStrategy{

        @Override
    	protected String transformAttributePath(AttributePath attributePath) {
            return apply(attributePath.getProperty());
	}
        
        private String apply(String basename) {
		if (basename == null) {
			return null;
		}
		StringBuilder text = new StringBuilder(basename.replace('.', '_'));
		for (int i = 1; i < text.length() - 1; i++) {
			if (isUnderscoreRequired(text.charAt(i - 1), text.charAt(i))) {
				text.insert(i++, '_');
			}
		}
		return text.toString().toUpperCase(Locale.ROOT);
	}

	private boolean isUnderscoreRequired(char before, char current) {
		return Character.isLowerCase(before) && Character.isUpperCase(current);
	}
}
