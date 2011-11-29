/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

package org.squashtest.csp.tm.internal.service


import java.net.URL;

import javax.inject.Inject 
import org.spockframework.util.NotThreadSafe 
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNotFoundException 
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject 
import org.squashtest.csp.core.bugtracker.domain.Category;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.domain.User;
import org.squashtest.csp.core.bugtracker.domain.Version;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor 
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus 
import org.squashtest.csp.tm.domain.bugtracker.Issue;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership 
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting 
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder 
import org.squashtest.csp.tm.service.BugTrackerLocalService 
import org.unitils.dbunit.annotation.DataSet 
import spock.unitils.UnitilsSupport 




/*
 * @author bsiri
 * 
 *  BugTracker Test Server Configuration :
 *
 *  This test class is meant for a Mantis bugtracker
 *  (see bugtracker.properties and bugtracker-core-context-IT.xml for configuration)
 *
 *  this test requires on the Mantis instance :
 *  	a default administrator account : login "administrator", password "root"
 *  	a project named 'squashbt', 
 *  		- with version 1.0, 1.01, 1.02 (declared in that order)
 *  		- belonging to categories General, Non Squash, Squash, Squash tm
 *  	a user with an access level of 'reporter' for all projects
 *  	that no project named "non-existant project" exists 
 *
 */


@NotThreadSafe
@UnitilsSupport
@ContextConfiguration(["classpath:bugtracker-core-context-IT.xml"])
@Transactional
class BugTrackerLocalServiceIT_Disabled extends DbunitServiceSpecification  {


	/*
	 *  should test the following methods :
	 * 
	 <X extends Bugged> X findBuggedEntity(Long entityId, Class<X> entityClass);
	 void addIssue( Long entityId, Class<? extends Bugged> entityClass, Issue issue);
	 String findProjectName(Bugged entity);
	 BugTrackerStatus checkBugTrackerStatus();
	 void setCredentials(String username, String password);
	 BTProject findRemoteProject(String name);
	 List<Priority> getRemotePriorities();
	 */	

	@Inject
	private BugTrackerLocalService btService;


	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should find an execution-step as a Bugged entity using its id"(){
		given :
		def execStepId = new Long(1);
		when :
		ExecutionStep estep = btService.findBuggedEntity(execStepId, ExecutionStep.class);
		then :
		estep!=null;
		estep.id==1l
		estep.action=="click"
		estep.expectedResult=="should work"
		estep.executionStatus==ExecutionStatus.FAILURE
		estep.comment=="it's bugged"
		estep.lastExecutedBy=="tester"
	}


	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should find the name of the project to which the bugged entity belongs to"(){
		given :
		ExecutionStep estep = btService.findBuggedEntity(1l, ExecutionStep.class)
		when :
		def name = estep.getProject().getName();
		then :
		name=="squashbt"


	}
	
	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should get an issue from a given execution step"(){
		given :
			ExecutionStep estep = btService.findBuggedEntity(1l, ExecutionStep.class)
		
		when :
			def issue = estep.getIssueList().findIssue(2l);
		
		then :
			issue.id==2l;
	}
	
	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should get all the issues for a given execution step"(){
		
		given :
				
			ExecutionStep estep = btService.findBuggedEntity(1l, ExecutionStep.class)		
		when :
			List<Issue> issues = estep.getIssueList().getAllIssues();
		
		then :
			issues.size() == 3
			issues.collect { it -> it.id } == [2l, 4l, 6l];
		
	}
	
	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should get a list of paired issues for a step"(){
		
		given :
			ExecutionStep estep = btService.findBuggedEntity(1l, ExecutionStep.class)
			
			CollectionSorting sorter = new CollectionSorting(){
				

			   public int getFirstItemIndex(){
				   return 0;
			   }
			

			   public int getMaxNumberOfItems(){
				   return 10;
			   }

			   public int getPageSize(){
				   return 10;
			   }
			   
			   String getSortedAttribute(){
				   return "Issue.id";
			   }
		   

			   String getSortingOrder(){
				   return "desc";
			   }
			}
		
		when :
			FilteredCollectionHolder<List<IssueOwnership<Issue>>> ownedIssues = 
					btService.findSquashIssues(estep, sorter);
		
		then :
			ownedIssues.unfilteredResultCount == 3;
			List<IssueOwnership<Issue>> list = ownedIssues.filteredCollection;
			
			list.collect { it -> it.issue.id} == [6l, 4l, 2l]
		
		
		
	}
	
	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should get a list of paired issues for an execution"(){
		
		given :
			Execution exec = btService.findBuggedEntity(1l, Execution.class)
			ExecutionStep step1 = btService.findBuggedEntity(1l, ExecutionStep.class)
			ExecutionStep step2 = btService.findBuggedEntity(2l, ExecutionStep.class)
			
			CollectionSorting sorter = new CollectionSorting(){
				

			   public int getFirstItemIndex(){
				   return 0;
			   }
			

			   public int getMaxNumberOfItems(){
				   return 10;
			   }
	
			   public int getPageSize(){
				   return 10;
			   }
	
			   String getSortedAttribute(){
				   return "Issue.id";
			   }
		   

			   String getSortingOrder(){
				   return "desc";
			   }
			}
		
		when :
			FilteredCollectionHolder<List<IssueOwnership<Issue>>> ownedIssues =
					btService.findSquashIssues(exec, sorter);
					
			
		
		then :
			ownedIssues.unfilteredResultCount == 8;
			List<IssueOwnership<Issue>> list = ownedIssues.filteredCollection;
			
			list.collect { it -> it.issue.id} == [8l, 7l, 6l, 5l, 4l, 3l,  2l, 1l]
			
			def ownership8 = list.get(0);
			def ownership7 = list.get(1);
			def ownership6 = list.get(2);
			def ownership5 = list.get(3);
			def ownership4 = list.get(4);
			def ownership3 = list.get(5);
			def ownership2 = list.get(6);
			def ownership1 = list.get(7);
			
			ownership8.owner == ownership7.owner 
			ownership7.owner == exec
			
			ownership2.owner == ownership4.owner
			ownership4.owner == ownership6.owner
			ownership6.owner == step1
			
			ownership1.owner == ownership3.owner 
			ownership3.owner == ownership5.owner
			ownership5.owner == step2
		
	}
	
	
	
	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should not find an issue from a given execution step"(){
		given :
			ExecutionStep estep = btService.findBuggedEntity(1l, ExecutionStep.class)
		
		when :
			def issue = estep.getIssueList().findIssue(8l);
		
		then :
			issue==null;
	}


	def "should 1) warn that the bugtracker needs credential, 2) set credentials then say it's all green"(){
		given :

		when :
		BugTrackerStatus status1 = btService.checkBugTrackerStatus();
		btService.setCredentials("administrator", "root")
		BugTrackerStatus status2 = btService.checkBugTrackerStatus();
		then :
		status1 == BugTrackerStatus.BUGTRACKER_NEEDS_CREDENTIALS
		status2 == BugTrackerStatus.BUGTRACKER_READY

	}

	def "should get the list of Mantis priorities"(){
		given :
		btService.setCredentials("administrator", "root")
		when :
		def priorities = btService.getRemotePriorities();
		then :
		priorities.collect{it.name} == [
			"feature",
			"trivial",
			"text",
			"tweak",
			"minor",
			"major",
			"crash",
			"block"
		]


	}

	def "should find a remote Project based on its name"(){
		given :
		def projectname="squashbt"
		btService.setCredentials("administrator", "root")

		when :
		BTProject project = btService.findRemoteProject(projectname)

		then :
		project != null
		project.name == "squashbt"
		project.users.collect {it.name}.contains ("administrator");
		project.users.collect {it.name}.contains ("user");
		
		project.versions.collect { it.name } == ["1.02", "1.01", "1.0"]
		project.categories.collect { it.name }== ["General", "Non Squash", "Squash", "Squash tm"]

		def admPerms = [
			"viewer",
			"reporter",
			"updater",
			"developer",
			"manager",
			"administrator"
		]
		def userPerms =  ["viewer", "reporter"]

		User user1 = project.users.get(0)
		User user2 = project.users.get(1)

		for (User user : [user1, user2]){
			if (user.name=="administrator"){
				user.permissions.collect{it.name} == admPerms;
			}else{
				user.permissions.collect{it.name} == userPerms;
			}
		}

	}

	def "should find an issue list"(){
		given:
			btService.setCredentials("administrator", "root")
		
		and :
			//need a bug tracker like mantis or JIRA and you have to know the issue id
			String issueId1 = "1";
			String issueId2 = "3";
			List<String> issueIdList = new ArrayList<String>()
			issueIdList.add(issueId1)
			issueIdList.add(issueId2)

		when:
			List<BTIssue> btIssueList = btService.getIssues(issueIdList)


		then:
			btIssueList.size() > 0
			btIssueList.get(0).id.equals(issueId1)
			btIssueList.get(1).id.equals(issueId2)
	}

	def "should throw an exception when fetching a remote project that doesn't exists"(){
		given :
		def projectname="non-existant project"
		btService.setCredentials("administrator", "root")

		when :
		BTProject project = btService.findRemoteProject(projectname)

		then :
		thrown BugTrackerNotFoundException

	}



	def "should fetch the labels for the interface"(){

		given :
		
		btService.setCredentials("administrator", "root")


		when :
		BugTrackerInterfaceDescriptor descriptor = btService.getInterfaceDescriptor();

		then :
		descriptor!=null;
		descriptor.getCategoryLabel().contains("Cat")
		descriptor.getCategoryLabel().contains("gor")
	}
	

	
	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should create an issue on the remote bugtracker and persist its Id locally"(){
		
		
		given :
		
			btService.setCredentials("administrator", "root")
		
		and :
			ExecutionStep estep = btService.findBuggedEntity(1l, ExecutionStep.class)
			
			BTProject project = btService.findRemoteProject(estep.getProject().getName())
			Version version = project.getVersions().get(0)
			Category category = project.getCategories().get(0)
			User assignee = project.getUsers().get(0)
			Priority priority = btService.getRemotePriorities().get(0)
			
		and :
			
			BTIssue issue = new BTIssue()
			issue.setProject(project)
			issue.setAssignee(assignee)
			issue.setVersion(version)
			issue.setCategory(category)
			issue.setPriority(priority)
			issue.setSummary("test bug # 1")
			issue.setDescription(estep.getDefaultDescription())
			issue.setComment("this is a comment for test bug # 1")
			
		
		when :			
			BTIssue reIssue = btService.createIssue(estep, issue)
			
			//we update the content of the step by refetching it
			estep = btService.findBuggedEntity (1l, ExecutionStep.class)
					
		then :
			
			reIssue.getId()!=null;
			reIssue.getAssignee().getName()== assignee.getName()
			reIssue.getVersion().getName() == version.getName()
			
			//etc
			
		
	}
	
	
	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should find the URL of a given issue"(){
		
		
		given :
			String issueId = "00001"

		when :
			URL url = btService.getIssueUrl(issueId)
		
		then :
			url.toExternalForm().contains("http://localhost/mantisbt")
			url.toExternalForm().contains("view.php?id="+issueId)
		
		
	}
	

}
