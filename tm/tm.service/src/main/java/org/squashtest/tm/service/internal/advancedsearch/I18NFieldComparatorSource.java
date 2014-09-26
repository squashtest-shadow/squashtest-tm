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
package org.squashtest.tm.service.internal.advancedsearch;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

/**
 * UNUSED
 * UNTESTED
 * 
 * The factory for {@link I18nFieldComparator}
 * 
 * @author bsiri
 *
 */
public class I18NFieldComparatorSource extends FieldComparatorSource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * that map associates a String (typically an enum code) to a rank;
	 */
	private Map<String, Short> mapsCodeToRank;
	
	public I18NFieldComparatorSource(Map<String, Short> mapsCodeToLocalized){
		this.mapsCodeToRank = mapsCodeToLocalized;
	}
	
	@Override
	public FieldComparator<String> newComparator(String fieldname, int numHits,
			int sortPos, boolean reversed) throws IOException {
		
		return new I18nFieldComparator(numHits, fieldname, mapsCodeToRank);
	}

}
