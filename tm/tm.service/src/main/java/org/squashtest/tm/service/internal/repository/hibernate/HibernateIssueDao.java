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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueList;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.IssueDao;

@Repository
public class HibernateIssueDao extends HibernateEntityDao<Issue> implements IssueDao {

	private static final String WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_AND_EXEC_STEP =
	// ------------------------------------Where issues is from the given Executions
	"where Issue.id in ( select issueExec.id "
			+ "from Execution exec "
			// way to issue id for exec
			+ "join exec.issueList issueListExec "
			+ "join issueListExec.issues issueExec "
			+ "join issueExec.bugtracker issuExecBT "
			// way to bug-tracker for exec
			+ "join exec.testPlan tp " + "join tp.iteration iter " + "join iter.campaign camp "
			+ "join camp.project project "
			+ "join project.bugtrackerBinding btb "
			// restriction for execution's bugtracker
			+ "where exec.id in :executionsIds "
			+ "and btb.bugtracker.id = issuExecBT.id ) "
			// -----------------------------------------------Or from the given ExecutionSteps
			+ "or Issue.id in (select issueExecStep.id "
			+ "from ExecutionStep execStep "
			// way to issue id for execStep
			+ "join execStep.issueList issueListExecStep " + "join issueListExecStep.issues issueExecStep "
			+ "join issueExecStep.bugtracker issueExecStepBT "
			// way to bug-tracker for execStep
			+ "join execStep.execution exec " + "join exec.testPlan tp " + "join tp.iteration iter "
			+ "join iter.campaign camp " + "join camp.project project " + "join project.bugtrackerBinding btb " +
			// restriction for executionStep's bugtracker
			"where execStep.id in :executionStepsIds " + "and btb.bugtracker.id = issueExecStepBT.id) ";

	
	private static final String WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_STEP =
	// ------------------------------------Where issues is from the given Executions
	"where Issue.id in (select issueExecStep.id "
			+ "from ExecutionStep execStep "
			// way to issue id for execStep
			+ "join execStep.issueList issueListExecStep " + "join issueListExecStep.issues issueExecStep "
			+ "join issueExecStep.bugtracker issueExecStepBT "
			// way to bug-tracker for execStep
			+ "join execStep.execution exec " + "join exec.testPlan tp " + "join tp.iteration iter "
			+ "join iter.campaign camp " + "join camp.project project " + "join project.bugtrackerBinding btb " +
			// restriction for executionStep's bugtracker
			"where execStep.id in :executionStepsIds " + "and btb.bugtracker.id = issueExecStepBT.id) ";
	
	
	/**
	 * 
	 * @see org.squashtest.tm.service.internal.repository.IssueDao#countIssuesfromIssueList(java.util.List)
	 */
	@Override
	public Integer countIssuesfromIssueList(final List<Long> issueListIds) {

		if (!issueListIds.isEmpty()) {
			Query query = currentSession().getNamedQuery("issueList.countIssues");
			query.setParameterList("issueListIds", issueListIds);
			Long result = (Long) query.uniqueResult();

			return result.intValue();
		} else {
			return 0;
		}

	}

	/**
	 * 
	 * @see org.squashtest.tm.service.internal.repository.IssueDao#countIssuesfromIssueList(java.util.Collection,
	 *      java.lang.Long)
	 */
	@Override
	public Integer countIssuesfromIssueList(Collection<Long> issueListIds, Long bugTrackerId) {
		if (!issueListIds.isEmpty()) {
			Query query = currentSession().getNamedQuery("issueList.countIssuesByTracker");
			query.setParameterList("issueListIds", issueListIds);
			query.setParameter("bugTrackerId", bugTrackerId);
			Long result = (Long) query.uniqueResult();

			return result.intValue();
		} else {
			return 0;
		}
	}

	/**
	 * @see {@linkplain IssueDao#findSortedIssuesFromIssuesLists(List, PagingAndSorting, Long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> findSortedIssuesFromIssuesLists(final Collection<Long> issueListIds,
			final PagingAndSorting sorter, Long bugtrackerId) {
		if (issueListIds.isEmpty()) {
			return Collections.emptyList();
		}

		// Issue alias is needed for sorting
		Criteria crit = currentSession()
				.createCriteria(IssueList.class, "IssueList")
				.createAlias("IssueList.issues", "Issue")
				.setProjection(
						Projections.projectionList().add(Projections.property("IssueList.id"), "issueListId")
								.add(Projections.property("Issue.remoteIssueId"), "remoteId")
								.add(Projections.property("Issue.id"), "localId"))
				.add(Restrictions.in("IssueList.id", issueListIds))
				.add(Restrictions.eq("Issue.bugtracker.id", bugtrackerId));
		
		SortingUtils.addOrder(crit, sorter);
		PagingUtils.addPaging(crit, sorter);

		return crit.list();

	}

	/*
	 * Will fetch all the issues which is is within the given id list, and pair them with their owner. The issue will be
	 * part of the result set if and only if its owner is of the given class.
	 * 
	 * @param <X> the actual implementing class of Bugged that own the issues
	 * 
	 * @param buggedIds the list of id of the issues we look for
	 * 
	 * @param concreteClass the Class object representing <X>
	 * 
	 * @return a non null but possibly empty list of IssueOwnership, each of them existing if and only if an instance of
	 * <X> was found.
	 */
	protected <X extends IssueDetector> List<IssueOwnership<Issue>> findIssueOwnerOfClass(List<Long> issueIds,
			Class<X> concreteClass) {

		String shortName = concreteClass.getSimpleName();

		List<IssueOwnership<Issue>> result = new LinkedList<IssueOwnership<Issue>>();

		// due to a bug of of MySqlDialect regarding Restrictions.in(Collection)
		// (see bug http://opensource.atlassian.com/projects/hibernate/browse/HHH-2776)
		// we have to handle the case where the issueIds list is empty separately. The best is
		// not to perform the operation at all if we aren't looking for any issue (ie, empty list)

		if (issueIds.size() > 0) {

			Criteria crit = currentSession().createCriteria(concreteClass, shortName)
					.createAlias("issueList", "issueList").createAlias("issueList.issues", "issue")
					.add(Restrictions.in("issue.id", issueIds)).setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

			List<Map<String, ?>> rawResult = crit.list();

			ListIterator<Map<String, ?>> iterator = rawResult.listIterator();

			while (iterator.hasNext()) {
				Map<String, ?> map = iterator.next();
				Issue issue = (Issue) map.get("issue");
				IssueDetector bugged = (IssueDetector) map.get(shortName);

				IssueOwnership<Issue> ownership = new IssueOwnership<Issue>(issue, bugged);
				result.add(ownership);
			}
		}
		return result;

	}

	/**
	 * @see {@linkplain IssueDao#findSortedIssuesFromExecutionAndExecutionSteps(List, List, PagingAndSorting)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> findSortedIssuesFromExecutionAndExecutionSteps(List<Long> executionsIds,
			List<Long> executionStepsIds, PagingAndSorting sorter) {
		if (!executionsIds.isEmpty() && !executionStepsIds.isEmpty()) {

			String queryString = "select issueList.id, Issue.remoteIssueId, Issue.id, Issue.bugtracker.id "
					+ "from IssueList issueList join issueList.issues Issue "
					+ WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_AND_EXEC_STEP;

			queryString += "order by " + sorter.getSortedAttribute() + " " + sorter.getSortOrder().getCode();

			Query query = currentSession().createQuery(queryString);
			query.setParameterList("executionsIds", executionsIds);
			query.setParameterList("executionStepsIds", executionStepsIds);

			if (!sorter.shouldDisplayAll()) {
				PagingUtils.addPaging(query, sorter);
			}

			return query.list();

		}

		return Collections.emptyList();
	}

	@Override
	public Integer countIssuesfromExecutionAndExecutionSteps(List<Long> executionsIds, List<Long> executionStepsIds) {
		if (!executionsIds.isEmpty() && !executionStepsIds.isEmpty()) {
			String queryString = "select count(Issue)  " + "from Issue Issue "
					+ WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_AND_EXEC_STEP;
			Query query = currentSession().createQuery(queryString);
			query.setParameterList("executionsIds", executionsIds);
			query.setParameterList("executionStepsIds", executionStepsIds);
			Long result = (Long) query.uniqueResult();
			return result.intValue();

		} else {
			return 0;
		}
	}
	
	@Override
	public Integer countIssuesfromExecutionSteps(List<Long> executionStepsIds) {
		if (!executionStepsIds.isEmpty()) {
			String queryString = "select count(Issue)  " + "from Issue Issue "
					+ WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_STEP;
			Query query = currentSession().createQuery(queryString);
			query.setParameterList("executionStepsIds", executionStepsIds);
			Long result = (Long) query.uniqueResult();
			return result.intValue();

		} else {
			return 0;
		}
	}

	@Override
	public List<Issue> findAllForIteration(Long id) {
		return executeListNamedQuery("Issue.findAllForIteration", new SetIdParameter("id", id));

	}

	@Override
	public List<Issue> findAllForTestSuite(Long id) {
		return executeListNamedQuery("Issue.findAllForTestSuite", new SetIdParameter("id", id));
	}

	@Override
	public IssueDetector findIssueDetectorByIssue(long id) {
		Execution exec = executeEntityNamedQuery("Issue.findExecution", new SetIdParameter("id", id));
		if(exec != null ){
			return exec	;
		}else{
			return executeEntityNamedQuery("Issue.findExecutionStep", new SetIdParameter("id", id));
		}
	}

}
