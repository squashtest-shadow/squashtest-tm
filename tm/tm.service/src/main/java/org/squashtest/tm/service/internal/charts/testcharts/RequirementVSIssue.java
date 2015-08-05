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
package org.squashtest.tm.service.internal.charts.testcharts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.charts.Column;
import org.squashtest.tm.service.charts.Datatype;
import org.squashtest.tm.service.charts.Perimeter;
import org.squashtest.tm.service.charts.PerimeterQuery;
import org.squashtest.tm.service.charts.PerimeterResponse;
import org.squashtest.tm.service.internal.charts.PerimeterUtils;

@Component
public class RequirementVSIssue implements Perimeter {

	private static final String PERIMETER_ID = "requirement-vs-issues";

	private static final String NAME = "Périmètre Anomalies";

	@Inject
	private SessionFactory sessionFactory;

	private static final Collection<Column> availableColumns;


	private static final String FROM_CLAUSE_STEP_ISSUES =
			" from ExecutionStep step "+
					"inner join step.issueList ilist inner join ilist.issues issue " +
					"inner join step.execution exec inner join exec.testPlan tp " +
					"inner join tp.iteration it inner join it.campaign c " +
					"inner join tp.referencedTestCase tc inner join tc.requirementVersionCoverages cov " +
					"inner join cov.verifiedRequirementVersion rv ";


	private static final String FROM_CLAUSE_EXEC_ISSUES =
			" from Execution exec "+
					"inner join exec.issueList ilist inner join ilist.issues issue " +
					"inner join exec.testPlan tp inner join tp.iteration it inner join it.campaign c " +
					"inner join tp.referencedTestCase tc inner join tc.requirementVersionCoverages cov " +
					"inner join cov.verifiedRequirementVersion rv ";

	static {

		Collection<Column> columns = new ArrayList<>(7);

		columns.add(new Column("requirement-criticality", Datatype.CRITICALITY, "criticality", "rv.criticality"));
		columns.add(new Column("requirement-category", Datatype.STRING, "category", "rv.category.label"));
		columns.add(new Column("requirement-creator", Datatype.STRING, "creator login", "rv.audit.createdBy"));
		columns.add(new Column("testcase-importance", Datatype.IMPORTANCE, "test case importance", "tc.importance"));
		columns.add(new Column("campaign-name", Datatype.STRING, "campaign name", "c.name"));
		columns.add(new Column("iteration-name", Datatype.STRING, "iteration name", "it.name"));
		columns.add(new Column("issue-id", Datatype.INT, "issue id", "issue.remoteIssueId"));

		availableColumns = Collections.unmodifiableCollection(columns);
	}

	@Override
	public String getId() {
		return PERIMETER_ID;
	}

	@Override
	public String getLabel() {
		return NAME;
	}

	@Override
	public Collection<Column> getAvailableColumns() {
		return availableColumns;
	}

	/*
	 * Here we must deal with issues, that may be defined at the exec step level of the execution level.
	 * This is a pain in the a** because we have to generate two result sets then merge them.
	 * 
	 * (non-Javadoc)
	 * @see org.squashtest.tm.service.charts.Perimeter#process(org.squashtest.tm.service.charts.PerimeterQuery)
	 */
	@Override
	public PerimeterResponse process(PerimeterQuery query) {

		PerimeterUtils utils = new PerimeterUtils();

		Session session = sessionFactory.getCurrentSession();

		// get the step-level issues
		String stepIssuesHQL = utils.getHQL(query, FROM_CLAUSE_STEP_ISSUES);
		Query stepQuery = session.createQuery(stepIssuesHQL);
		List<Object[]> stepTuple = stepQuery.list();

		// get the exec-level issues
		String execIssuesHQL = utils.getHQL(query, FROM_CLAUSE_EXEC_ISSUES);
		Query execQuery = session.createQuery(execIssuesHQL);
		List<Object[]> execTuple = execQuery.list();

		// now merge the results
		List<Object[]> res = utils.mergeResultSet(query, stepTuple, execTuple);


		PerimeterResponse response = new PerimeterResponse();
		response.setData(res);

		return response;

	}

}
