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
package org.squashtest.tm.service.internal.requirement;

import java.io.IOException;
import java.util.Locale;

import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.springframework.context.MessageSource;

public class RequirementVersionCategoryComparatorSource extends FieldComparatorSource{

	private static final long serialVersionUID = 1L;
	private MessageSource source;
	private Locale locale;
	
	public MessageSource getSource() {
		return source;
	}

	public Locale getLocale() {
		return locale;
	}

	public RequirementVersionCategoryComparatorSource(MessageSource source, Locale locale) {
		this.source = source;
		this.locale = locale;
	}

	@Override
	public FieldComparator<?> newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
		return new RequirementVersionCategoryComparator(numHits, fieldname, this.source, this.locale);
	}
}
