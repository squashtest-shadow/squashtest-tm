package org.squashtest.tm.service.internal.bugtracker

import org.squashtest.tm.domain.bugtracker.BugTrackerBinding
import org.squashtest.tm.domain.bugtracker.Issue
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.service.internal.repository.BugTrackerBindingDao
import org.squashtest.tm.service.internal.repository.BugTrackerDao
import org.squashtest.tm.service.internal.repository.IssueDao
import org.squashtest.tm.service.project.GenericProjectManagerService

import spock.lang.Specification

class BugTrackerManagerServiceImplTest extends Specification  {
    BugTrackerBindingDao bugTrackerBindingDao = Mock()
    IssueDao  issueDao = Mock()
    BugTrackerDao bugTrackerDao = Mock()
    GenericProjectManagerService genericProjectManagerService = Mock()
    BugTrackerManagerServiceImpl service = new BugTrackerManagerServiceImpl()

    def setup(){
        service.bugTrackerBindingDao = bugTrackerBindingDao
        service.bugTrackerDao = bugTrackerDao
        service.issueDao = issueDao
        service.genericProjectManagerService =  genericProjectManagerService
    }

    def "should delete bugtrackers"(){
        given :"list of ids of the bugtracker to delete"
        def bugtrackerIds = (1L..5L).collect{it}
        and : "each bugtracker is bind to 2 projects"
        (1L..5L).each {bugTrackerBindingDao.findByBugtrackerId(it)    >> [it * 10, it *10 + 1].collect{Project p = Mock(); p.getId() >> it;  return new BugTrackerBinding(project:p)}}
        and : "each bugtracker get 3 issues associated "
        (1L..5L).each{issueDao.getAllIssueFromBugTrackerId(it) >> [it * 10, it *10 + 1, it * 10 + 2].collect{ new Issue(id:it)}}


        when :
        service.deleteBugTrackers(bugtrackerIds)

        then :
        5 * bugTrackerDao.remove(_)
        10 * genericProjectManagerService.removeBugTracker(_)
        15 * issueDao.remove(_)
    }
}
