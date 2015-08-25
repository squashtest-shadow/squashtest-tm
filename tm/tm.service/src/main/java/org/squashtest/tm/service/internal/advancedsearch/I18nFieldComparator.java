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
package org.squashtest.tm.service.internal.advancedsearch;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;

/**
 * 
 * UNUSED
 * UNTESTED
 * 
 * Heavily inspired by {@link FieldComparator}'s various String comparators.
 * 
 * @author bsiri
 * 
 */
public class I18nFieldComparator extends FieldComparator<String> {

	private Map<String, Short> mapsCodeToRank;
	private String field;
	private String values[];
	private Short ranks[];
	private String[] currentReaderValues;

	private Integer idxBottom;

	public I18nFieldComparator(int numHits, String field,
			Map<String, Short> mapsCodeToRank) {
		this.mapsCodeToRank = mapsCodeToRank;
		this.field = field;
		values = new String[numHits];
		ranks = new Short[numHits];
	}

	@Override
	public int compare(int slot1, int slot2) {
		final Short rank1 = ranks[slot1];
		final Short rank2 = ranks[slot2];
		int res;
		if (rank1 == null) {
			if (rank2 == null) {
				res = 0;
			}
			else{
				res = -1;
			}
		}
		else if (rank2 == null) {
			res = 1;
		}
		else {
			res = rank1.compareTo(rank2);
		}

		return res;
	}

	@Override
	public int compareBottom(int doc) {
		final String val2 = currentReaderValues[doc];

		if (idxBottom == null) {
			if (val2 == null) {
				return 0;
			}
			return -1;
		} else if (val2 == null) {
			return 1;
		}

		Short codeBottom = ranks[idxBottom];
		Short codeDoc = mapsCodeToRank.get(val2);

		return codeBottom.compareTo(codeDoc);
	}

	@Override
	public void copy(int slot, int doc) {
		String val = currentReaderValues[doc];
		values[slot] = val;
		ranks[slot] = mapsCodeToRank.get(val);
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase)
			throws IOException {
		currentReaderValues = FieldCache.DEFAULT.getStrings(reader, field);
	}

	@Override
	public void setBottom(final int bottom) {
		this.idxBottom = bottom;
	}

	@Override
	public String value(int slot) {
		return values[slot];
	}

	@Override
	public int compareValues(String val1, String val2) {
		if (val1 == null) {
			if (val2 == null) {
				return 0;
			}
			return -1;
		} else if (val2 == null) {
			return 1;
		} else {
			return val1.compareTo(val2);
		}
	}

}
