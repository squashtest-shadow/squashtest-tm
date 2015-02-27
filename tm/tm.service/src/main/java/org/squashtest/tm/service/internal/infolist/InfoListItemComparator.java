/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.infolist;

import java.io.IOException;
import java.util.Locale;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;
import org.springframework.context.MessageSource;

public class InfoListItemComparator extends FieldComparator<Object> {

	private String[] values;
	private String[] currentReaderValues;
	private final String field;
	private String bottom;
	private String i18nRoot;
	private MessageSource source;
	private Locale locale;

	InfoListItemComparator(int numHits, String field, String i18nRoot, MessageSource source, Locale locale) {
		values = new String[numHits];
		this.field = field;
		this.i18nRoot = i18nRoot;
		this.source = source;
		this.locale = locale;
	}

	@Override
	public int compare(int slot1, int slot2) {
		final String val1 = values[slot1];
		final String val2 = values[slot2];

		int result = 0;
		if (val1 == null) {
			if (val2 != null) {
				result = -1;
			}
		} else if (val2 == null) {
			result = 1;
		} else {
			String internationalizedVal1 = source.getMessage(i18nRoot + val1, null, val1, locale);
			String internationalizedVal2 = source.getMessage(i18nRoot + val2, null, val2, locale);
			result = internationalizedVal1.compareTo(internationalizedVal2);
		}
		return result;
	}

	@Override
	public int compareBottom(int doc) {
		final String val2 = currentReaderValues[doc];

		int result = 0;
		if (bottom == null) {
			if (val2 != null) {
				result = -1;
			}
		} else if (val2 == null) {
			result = 1;
		} else {
			String internationalizedVal2 = source.getMessage(i18nRoot + val2, null, val2, locale);
			result = bottom.compareTo(internationalizedVal2);
		}

		return result;
	}

	@Override
	public void copy(int slot, int doc) {
		values[slot] = currentReaderValues[doc];
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		currentReaderValues = FieldCache.DEFAULT.getStrings(reader, field);
	}

	@Override
	public void setBottom(final int bottom) {
		this.bottom = values[bottom];
	}

	@Override
	public Comparable<?> value(int slot) {
		return values[slot];
	}
}
