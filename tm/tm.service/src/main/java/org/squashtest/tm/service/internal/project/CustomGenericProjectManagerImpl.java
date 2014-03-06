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
package org.squashtest.tm.service.internal.project;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.domain.bugtracker.BugTrackerBinding;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.library.PluginReferencer;
import org.squashtest.tm.domain.project.AdministrableProject;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.LibraryPluginBinding;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.exception.NoBugTrackerBindingException;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.security.acls.PermissionGroup;
import org.squashtest.tm.service.internal.repository.BugTrackerBindingDao;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.PartyDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.internal.testautomation.InsecureTestAutomationManagementService;
import org.squashtest.tm.service.project.CustomGenericProjectManager;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.security.ObjectIdentityService;
import org.squashtest.tm.service.security.PermissionEvaluationService;

/**
 * @author Gregory Fouquet
 * 
 */
@Service("CustomGenericProjectManager")
@Transactional
public class CustomGenericProjectManagerImpl implements CustomGenericProjectManager {
	
	private static final String IS_ADMIN_OR_MANAGER = "hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')";
	private static final String IS_ADMIN = "hasRole('ROLE_ADMIN')";
	
	@Inject
	private GenericProjectDao genericProjectDao;
	@Inject
	private ProjectDao projectDao;	
	@Inject
	private BugTrackerBindingDao bugTrackerBindingDao;
	@Inject
	private BugTrackerDao bugTrackerDao;
	@Inject
	private SessionFactory sessionFactory;
	@Inject
	private UserDao userDao;
	@Inject
	private PartyDao partyDao;
	@Inject
	private ExecutionDao executionDao;
	@Inject
	private ObjectIdentityService objectIdentityService;
	@Inject
	private Provider<GenericToAdministrableProject> genericToAdministrableConvertor;
	@Inject
	private ProjectsPermissionManagementService permissionsManager;
	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	@Inject
	private InsecureTestAutomationManagementService autotestService;
	@Inject
	private ProjectDeletionHandler projectDeletionHandler;

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomGenericProjectManagerImpl.class);

	
	// ************************* finding projects wrt user role ****************************
	
	/**
	 * @see org.squashtest.tm.service.project.CustomGenericProjectManager#findSortedProjects(org.squashtest.tm.core.foundation.collection.PagingAndSorting)
	 */
	/*
	 * Implementation note :
	 * 
	 * Here for once the paging will not be handled by the database, but programmatically. The reason is that we want to filter the projects according to the 
	 * caller's permissions, something that isn't doable using hql alone (the acl system isn't part of the domain and thus wasn't modeled).
	 * 
	 * So, we just load all the projects and apply paging on the resultset
	 * 
	 */
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize(IS_ADMIN_OR_MANAGER)
	public PagedCollectionHolder<List<GenericProject>> findSortedProjects(PagingAndSorting pagingAndSorting, Filtering filter) {
		
		List<? extends GenericProject> resultset;
		PagingAndSorting unpaged = Pagings.disablePaging(pagingAndSorting);
		
		if (permissionEvaluationService.hasRole("ROLE_ADMIN")){
			resultset = findAllSortedProjects(unpaged, filter);
		}
		else{
			resultset = findSortedActualProjects(unpaged, filter);
		}

		// filter on permissions
		List<? extends GenericProject> securedResultset = new LinkedList<GenericProject>(resultset);
		CollectionUtils.filter(securedResultset, new IsManagerOnObject());
		
		// manual paging
		int listsize = securedResultset.size();
		int firstIdx = Math.min(listsize, pagingAndSorting.getFirstItemIndex());
		int lastIdx = Math.min(listsize, firstIdx + pagingAndSorting.getPageSize());
		securedResultset = securedResultset.subList(firstIdx, lastIdx);

		return new PagingBackedPagedCollectionHolder<List<GenericProject>>(pagingAndSorting, listsize , (List<GenericProject>) securedResultset);
	}
	
	
	private List<GenericProject> findAllSortedProjects(PagingAndSorting pagingAndSorting, Filtering filter) {
		if (filter.isDefined()){
			return genericProjectDao.findProjectsFiltered(pagingAndSorting, "%"+filter.getFilter()+"%");
		}
		else{
			return genericProjectDao.findAll(pagingAndSorting);
		}
	}
	
	private List<Project> findSortedActualProjects(PagingAndSorting pagingAndSorting, Filtering filter) {
		if (filter.isDefined()){
			return projectDao.findProjectsFiltered(pagingAndSorting, "%"+filter.getFilter()+"%");
		}
		else{
			return projectDao.findAll(pagingAndSorting);
		}
	}

	// ************************* finding projects wrt user role ****************************	
	
	
	@Override
	@PreAuthorize(IS_ADMIN)
	public void persist(GenericProject project) {
		Session session = sessionFactory.getCurrentSession();

		CampaignLibrary cl = new CampaignLibrary();
		project.setCampaignLibrary(cl);
		session.persist(cl);

		RequirementLibrary rl = new RequirementLibrary();
		project.setRequirementLibrary(rl);
		session.persist(rl);

		TestCaseLibrary tcl = new TestCaseLibrary();
		project.setTestCaseLibrary(tcl);
		session.persist(tcl);

		session.persist(project);
		session.flush(); // otherwise ids not available

		objectIdentityService.addObjectIdentity(project.getId(), project.getClass());
		objectIdentityService.addObjectIdentity(tcl.getId(), tcl.getClass());
		objectIdentityService.addObjectIdentity(rl.getId(), rl.getClass());
		objectIdentityService.addObjectIdentity(cl.getId(), cl.getClass());

	}

	/**
	 * @see org.squashtest.tm.service.project.CustomGenericProjectManager#coerceTemplateIntoProject(long)
	 */
	@Override	
	@PreAuthorize(IS_ADMIN)
	public void coerceTemplateIntoProject(long templateId) {
		Project project = genericProjectDao.coerceTemplateIntoProject(templateId);

		objectIdentityService.addObjectIdentity(templateId, Project.class);
		permissionsManager.copyAssignedUsersFromTemplate(project, templateId);
		permissionsManager.removeAllPermissionsFromProjectTemplate(templateId);
		objectIdentityService.removeObjectIdentity(templateId, ProjectTemplate.class);
	}

	@Override
	@PreAuthorize(IS_ADMIN_OR_MANAGER)
	public void deleteProject(long projectId) {
		projectDeletionHandler.deleteProject(projectId);
	}

	@Override
	public AdministrableProject findAdministrableProjectById(long projectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		return genericToAdministrableConvertor.get().convertToAdministrableProject(genericProject);
	}



	@Override
	public void addNewPermissionToProject(long userId, long projectId, String permission) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		permissionsManager.addNewPermissionToProject(userId, projectId, permission);
	}

	@Override
	public void removeProjectPermission(long userId, long projectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		permissionsManager.removeProjectPermission(userId, projectId);

	}

	@Override
	public List<PartyProjectPermissionsBean> findPartyPermissionsBeansByProject(long projectId) {
		return permissionsManager.findPartyPermissionsBeanByProject(projectId);
	}
	
	@Override
	public PagedCollectionHolder<List<PartyProjectPermissionsBean>> findPartyPermissionsBeanByProject(
			PagingAndSorting sorting, Filtering filtering, long projectId) {
		return permissionsManager.findPartyPermissionsBeanByProject(sorting, filtering, projectId);
	}
	
	@Override
	public List<PermissionGroup> findAllPossiblePermission() {
		return permissionsManager.findAllPossiblePermission();
	}

	@Override
	public List<Party> findPartyWithoutPermissionByProject(long projectId) {
		return permissionsManager.findPartyWithoutPermissionByProject(projectId);
	}

	@Override
	public Party findPartyById(long partyId) {
		return partyDao.findById(partyId);
	}
	
	// ********************************** Test automation section *************************************

	@Override
	public void bindTestAutomationProject(long projectId, TestAutomationProject taProject) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		TestAutomationProject persistedProject = autotestService.persistOrAttach(taProject);
		genericProject.bindTestAutomationProject(persistedProject);
	}

	@Override
	public TestAutomationServer getLastBoundServerOrDefault(long projectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		if (genericProject.hasTestAutomationProjects()) {
			return genericProject.getServerOfLatestBoundProject();
		}

		else {
			return autotestService.getDefaultServer();
		}
	}

	@Override
	public List<TestAutomationProject> findBoundTestAutomationProjects(long projectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		return genericProjectDao.findBoundTestAutomationProjects(projectId);
	}

	@Override
	public void unbindTestAutomationProject(long projectId, long taProjectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		genericProject.unbindTestAutomationProject(taProjectId);

	}

	// ********************************** bugtracker section *************************************

	@Override
	public void changeBugTracker(long projectId, Long newBugtrackerId) {

		GenericProject project = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(project);
		BugTracker newBugtracker = bugTrackerDao.findById(newBugtrackerId);
		if (newBugtracker != null) {
			changeBugTracker(project, newBugtracker);
		} else {
			throw new UnknownEntityException(newBugtrackerId, BugTracker.class);
		}

	}

	@Override
	public void changeBugTracker(GenericProject project, BugTracker newBugtracker) {
		LOGGER.debug("changeBugTracker for project " + project.getId() + " bt: " + newBugtracker.getId());
		checkManageProjectOrAdmin(project);
		// the project doesn't have bug-tracker connection yet
		if (!project.isBugtrackerConnected()) {
			BugTrackerBinding bugTrackerBinding = new BugTrackerBinding(project.getName(), newBugtracker, project);
			project.setBugtrackerBinding(bugTrackerBinding);
		}
		// the project has a bug-tracker connection
		else {
			// and the new one is different from the old one
			if (projectBugTrackerChanges(newBugtracker.getId(), project)) {
				project.getBugtrackerBinding().setBugtracker(newBugtracker);
			}
		}
	}

	private boolean projectBugTrackerChanges(Long newBugtrackerId, GenericProject project) {
		boolean change = true;
		BugTrackerBinding bugtrackerBinding = project.getBugtrackerBinding();
		long bugtrackerId = bugtrackerBinding.getBugtracker().getId();
		if (bugtrackerId == newBugtrackerId) {
			change = false;
		}
		return change;
	}

	@Override
	public void removeBugTracker(long projectId) {
		LOGGER.debug("removeBugTracker for project " + projectId);
		GenericProject project = genericProjectDao.findById(projectId);	
		checkManageProjectOrAdmin(project);
		if (project.isBugtrackerConnected()) {
			BugTrackerBinding bugtrackerBinding = project.getBugtrackerBinding();
			project.removeBugTrackerBinding();
			bugTrackerBindingDao.remove(bugtrackerBinding);
		}
	}

	@Override
	public void changeBugTrackerProjectName(long projectId, String projectBugTrackerName) {
		GenericProject project = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(project);
		BugTrackerBinding bugtrackerBinding = project.getBugtrackerBinding();
		if (bugtrackerBinding == null) {
			throw new NoBugTrackerBindingException();
		}
		bugtrackerBinding.setProjectName(projectBugTrackerName);

	}
	
	// **************************** wizards section **********************************
	
	
	@Override
	@PreAuthorize(IS_ADMIN_OR_MANAGER)
	public void enableWizardForWorkspace(long projectId, WorkspaceType workspace, String wizardId) {
		PluginReferencer library = findLibrary(projectId, workspace);
		library.enablePlugin(wizardId);
	}
	
	
	@Override
	@PreAuthorize(IS_ADMIN_OR_MANAGER)
	public void disableWizardForWorkspace(long projectId, WorkspaceType workspace, String wizardId) {
		PluginReferencer library = findLibrary(projectId, workspace);
		library.disablePlugin(wizardId);
	}
	
	@Override
	// this information is read-only and public, no need for security
	public Map<String, String> getWizardConfiguration(long projectId, WorkspaceType workspace, String wizardId) {
		PluginReferencer library = findLibrary(projectId, workspace);
		LibraryPluginBinding binding = library.getPluginBinding(wizardId);
		if (binding != null){
			return binding.getProperties();
		}
		else{
			return new HashMap<String, String>();
		}
	}
	
	@Override
	@PreAuthorize(IS_ADMIN_OR_MANAGER)
	public void setWizardConfiguration(long projectId, WorkspaceType workspace,
			String wizardId, Map<String, String> configuration) {
		
		PluginReferencer library = findLibrary(projectId, workspace);
		if (! library.isPluginEnabled(wizardId)){
			library.enablePlugin(wizardId);
		}
		
		LibraryPluginBinding binding = library.getPluginBinding(wizardId);
		binding.setProperties(configuration);
	}

	// ************************** status configuration section ****************************
	
	@Override
	public void enableExecutionStatus(long projectId, ExecutionStatus executionStatus) {
		GenericProject project = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(project);
		project.getCampaignLibrary().enableStatus(executionStatus);
	}


	@Override
	public void disableExecutionStatus(long projectId, ExecutionStatus executionStatus) {
		GenericProject project = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(project);
		project.getCampaignLibrary().disableStatus(executionStatus);
	}


	@Override
	public Set<ExecutionStatus> enabledExecutionStatuses(long projectId) {
		GenericProject project = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(project);
		Set<ExecutionStatus> statuses = new HashSet<ExecutionStatus>();
		statuses.addAll(Arrays.asList(ExecutionStatus.values())); 
		Set<ExecutionStatus> disabledStatuses = project.getCampaignLibrary().getDisabledStatuses();
		statuses.removeAll(disabledStatuses);
		return statuses;
	}


	@Override
	public Set<ExecutionStatus> disabledExecutionStatuses(long projectId) {
		GenericProject project = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(project);
		return project.getCampaignLibrary().getDisabledStatuses();
	}
	
	
	@Override
	public void replaceExecutionStatus(long projectId, ExecutionStatus source, ExecutionStatus target) {
		List<ExecutionStep> steps = executionDao.findAllExecutionStepsWithStatus(projectId, source);
		for(ExecutionStep step : steps){
			step.setExecutionStatus(target);
		}
		List<IterationTestPlanItem> testPlanItems = executionDao.findAllIterationTestPlanItemsWithStatus(projectId, source);
		for(IterationTestPlanItem testPlanItem : testPlanItems){
			testPlanItem.setExecutionStatus(target);
		}
	}
	
	@Override
	public boolean isExecutionStatusEnabledForProject(long projectId, ExecutionStatus executionStatus) {
		Set<ExecutionStatus> statuses = disabledExecutionStatuses(projectId);
		if(statuses.contains(executionStatus)){
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean executionStatusUsedByProject(long projectId, ExecutionStatus executionStatus) {
		return executionDao.hasStepOrExecutionWithStatus(projectId, executionStatus);
	}

	// **************** private stuffs **************
	
	private PluginReferencer findLibrary(long projectId, WorkspaceType workspace){
		GenericProject project = genericProjectDao.findById(projectId);
		
		switch(workspace){
			case TEST_CASE_WORKSPACE : 		return project.getTestCaseLibrary();
			case REQUIREMENT_WORKSPACE : 	return project.getRequirementLibrary();
			case CAMPAIGN_WORKSPACE : 		return project.getCampaignLibrary();
			default : throw new IllegalArgumentException("WorkspaceType "+workspace+" is unknown and is not covered");
		}
	}
	
	
	private void checkManageProjectOrAdmin(GenericProject genericProject) {
		permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "MANAGEMENT", genericProject);
	}
	
	private final class IsManagerOnObject implements Predicate{
		@Override
		public boolean evaluate(Object object) {
			return permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "MANAGEMENT", object);
		}
	}
}
