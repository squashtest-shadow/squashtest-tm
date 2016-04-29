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
package org.squashtest.tm.domain.search;

import java.lang.reflect.Field;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.joda.time.LocalDateTime;
import org.springframework.util.ReflectionUtils;

public class NotGMTDateBridge implements FieldBridge {




	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if (value == null) {
			return;
		}

		
		Date date = (Date) value;
		Field miliField = ReflectionUtils.findField(LocalDateTime.class, "iLocalMillis");
		try {

			miliField.setAccessible(true);
			long numericDate = (long) ReflectionUtils.getField(miliField,
					new LocalDateTime(date.getTime()).withTime(0, 0, 0, 0));
			luceneOptions.addNumericFieldToDocument(name, numericDate, document);
			
		} finally{
			miliField.setAccessible(false);
		}
	

	
	}


}
