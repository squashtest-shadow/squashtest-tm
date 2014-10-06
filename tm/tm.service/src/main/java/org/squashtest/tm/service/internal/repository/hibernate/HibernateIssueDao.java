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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.IssueDao;

@Repository
public class HibernateIssueDao extends HibernateEntityDao<Issue> implements IssueDao {

    private static final String WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_AND_EXEC_STEP =
    // ------------------------------------Where issues is from the given
    // Executions
    "where Issue.id in ( select issueExec.id "
            + "from Execution exec "
            // way to issue id for exec
            + "join exec.issueList issueListExec " + "join issueListExec.issues issueExec "
            + "join issueExec.bugtracker issuExecBT "
            + "join exec.testPlan tp "
            + "join tp.iteration it "
            + "join it.campaign cp "
            + "join cp.project proj "
            + "join proj.bugtrackerBinding binding "
            + "join binding.bugtracker projbt "
            // restriction for execution's bugtracker
            + "where exec.id in (:executionsIds) "
            + "and projbt.id = issuExecBT.id ) "
            // -----------------------------------------------Or from the given
            // ExecutionSteps
            + "or Issue.id in (select issueExecStep.id "
            + "from ExecutionStep execStep "
            // way to issue id for execStep
            + "join execStep.issueList issueListExecStep " + "join issueListExecStep.issues issueExecStep "
            + "join issueExecStep.bugtracker issueExecStepBT " + "join execStep.execution exec "
            + "join exec.testPlan tp " + "join tp.iteration it " + "join it.campaign cp " + "join cp.project proj "
            + "join proj.bugtrackerBinding binding " + "join binding.bugtracker projbt "
            // restriction for executionStep's bugtracker
            + "where execStep.id in (:executionStepsIds) " + "and projbt.id = issueExecStepBT.id) ";

    private static final String WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_STEP =
    // ------------------------------------Where issues is from the given
    // Executions
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
    public List<Issue> findSortedIssuesFromIssuesLists(final Collection<Long> issueListIds,
            final PagingAndSorting sorter, Long bugtrackerId) {

        if (issueListIds.isEmpty()) {
            return Collections.emptyList();
        }

        Criteria crit = currentSession().createCriteria(Issue.class, "Issue")
                .add(Restrictions.in("Issue.issueList.id", issueListIds))
                .add(Restrictions.eq("Issue.bugtracker.id", bugtrackerId));

        SortingUtils.addOrder(crit, sorter);
        PagingUtils.addPaging(crit, sorter);

        return crit.list();

    }

    /**
     * @see {@linkplain IssueDao#findSortedIssuesFromExecutionAndExecutionSteps(List, List, PagingAndSorting)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Issue> findSortedIssuesFromExecutionAndExecutionSteps(List<Long> executionsIds,
            List<Long> executionStepsIds, PagingAndSorting sorter) {
        if (!executionsIds.isEmpty() && !executionStepsIds.isEmpty()) {

            String queryString = "select Issue from Issue Issue " + WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_AND_EXEC_STEP;

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
            String queryString = "select count(Issue)  " + "from Issue Issue " + WHERE_CLAUSE_FOR_ISSUES_FROM_EXEC_STEP;
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
        if (exec != null) {
            return exec;
        } else {
            return executeEntityNamedQuery("Issue.findExecutionStep", new SetIdParameter("id", id));
        }
    }

    @Override
    public TestCase findTestCaseRelatedToIssue(long id) {

        TestCase testCase = null;

        Execution exec = executeEntityNamedQuery("Issue.findExecution", new SetIdParameter("id", id));
        if (exec != null) {
            testCase = exec.getReferencedTestCase();
        } else {
            ExecutionStep step = executeEntityNamedQuery("Issue.findExecutionStep", new SetIdParameter("id", id));
            if (step != null && step.getExecution() != null) {
                testCase = step.getExecution().getReferencedTestCase();
            }
        }

        return testCase;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Issue> getAllIssueFromBugTrackerId(Long bugtrackerId) {
       final Criteria crit = currentSession().createCriteria(Issue.class, "Issue")
               .add(Restrictions.eq("Issue.bugtracker.id", bugtrackerId));

        return crit.list();
    }

}
