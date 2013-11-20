/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.domain.testcase;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.bridge.LuceneOptions;
import org.squashtest.tm.domain.search.SessionFieldBridge;


public class TestCaseAttachmentBridge extends SessionFieldBridge{

	private static final Integer EXPECTED_LENGTH = 7;
	
	private String padRawValue(int rawValue){
		String rawValueAsString = String.valueOf(rawValue);
		StringBuilder builder = new StringBuilder();
		int length = rawValueAsString.length();
		int zeroesToAdd = EXPECTED_LENGTH - length;
		for(int i=0; i<zeroesToAdd; i++){
			builder.append("0");
		}
		builder.append(rawValueAsString);
		return builder.toString();
	}
	
	@Override
	protected void writeFieldToDocument(String name, Session session, Object value, Document document, LuceneOptions luceneOptions){
		
		TestCase testcase = (TestCase) value;
		testcase = (TestCase) session.createCriteria(TestCase.class).add(Restrictions.eq("id", testcase.getId())).uniqueResult(); //NOSONAR session is never null
		
		Field field = new Field(name,  padRawValue(testcase.getAttachmentList().size()), luceneOptions.getStore(),
	    luceneOptions.getIndex(), luceneOptions.getTermVector() );
	    field.setBoost( luceneOptions.getBoost());
	    document.add(field);
	}
}
