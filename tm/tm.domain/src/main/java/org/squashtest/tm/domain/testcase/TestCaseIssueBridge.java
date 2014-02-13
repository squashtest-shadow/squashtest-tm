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
package org.squashtest.tm.domain.testcase;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.bridge.LuceneOptions;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.search.SessionFieldBridge;

public class TestCaseIssueBridge extends SessionFieldBridge{

	private static final Integer EXPECTED_LENGTH = 7;
	
	private String padRawValue(Long rawValue){
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
	
	private Long findNumberOfIssues(Session session, Long id){
		
		Long issuesOnExecutions = (Long) session.createCriteria(Execution.class)
				.add(Restrictions.eq("referencedTestCase.id", id))
				.createCriteria("issueList")
				.createCriteria("issues")
				.setProjection(Projections.rowCount())
				.uniqueResult();
				
		Criteria parentCriteria = session.createCriteria(ExecutionStep.class);
		parentCriteria.createCriteria("execution").add(Restrictions.eq("referencedTestCase.id", id));
		parentCriteria.createCriteria("issueList").createCriteria("issues");

		Long issuesOnExecutionSteps = (Long) parentCriteria.setProjection(Projections.rowCount()).uniqueResult();
		
		return issuesOnExecutions+issuesOnExecutionSteps;
	}

	@Override
	protected void writeFieldToDocument(String name, Session session, Object value, Document document, LuceneOptions luceneOptions) {

		TestCase testcase = (TestCase) value;
		
		Long numberOfIssues = findNumberOfIssues(session, testcase.getId());
		
		Field field = new Field(name, padRawValue(numberOfIssues), luceneOptions.getStore(),
	    luceneOptions.getIndex(), luceneOptions.getTermVector() );
	    field.setBoost( luceneOptions.getBoost());
	    document.add(field);
		
	}

}
