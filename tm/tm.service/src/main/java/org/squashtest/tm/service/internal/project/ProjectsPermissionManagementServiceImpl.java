/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectPermission;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.project.ProjectVisitor;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UserProjectPermissionsBean;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.PartyDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.security.acls.PermissionGroup;
import org.squashtest.tm.service.security.acls.model.ObjectAclService;

@Service("squashtest.tm.service.ProjectsPermissionManagementService")
public class ProjectsPermissionManagementServiceImpl implements ProjectsPermissionManagementService {

	private static final String NAMESPACE = "squashtest.acl.group.tm";
	private static final String PROJECT_CLASS_NAME = "org.squashtest.tm.domain.project.Project";

	@Inject
	private ObjectAclService aclService;

	@Inject private GenericProjectDao genericProjectFinder;

	@Inject
	private UserDao userDao;

	@Inject
	private PartyDao partyDao;
	
	@Override
	public List<PermissionGroup> findAllPossiblePermission() {
		return aclService.findAllPermissionGroupsByNamespace(NAMESPACE);
	}

	@Override
	public void deleteUserProjectOldPermission(String userLogin, long projectId) {
		ObjectIdentity entityRef = createProjectIdentity(projectId);
		aclService.removeAllResponsibilities(userLogin, entityRef);

		GenericProject project = genericProjectFinder.findById(projectId);

		ObjectIdentity rlibraryRef = createRequirementLibraryIdentity(project);
		aclService.removeAllResponsibilities(userLogin, rlibraryRef);

		ObjectIdentity tclibraryRef = createTestCaseLibraryIdentity(project);
		aclService.removeAllResponsibilities(userLogin, tclibraryRef);

		ObjectIdentity clibraryRef = createCampaignLibraryIdentity(project);
		aclService.removeAllResponsibilities(userLogin, clibraryRef);
	}

	private ObjectIdentity createProjectIdentity(long projectId) {
		GenericProject project = genericProjectFinder.findById(projectId);
		final Class<?>[] projectType = {null};
		
		project.accept(new ProjectVisitor() {
			
			@Override
			public void visit(ProjectTemplate projectTemplate) {
				projectType[0] = ProjectTemplate.class;
			}
			
			@Override
			public void visit(Project project) {
				projectType[0] = Project.class;
			}
		});
		
		return new ObjectIdentityImpl(projectType[0], projectId);
	}

	private ObjectIdentity createCampaignLibraryIdentity(GenericProject project) {
		return new ObjectIdentityImpl(CampaignLibrary.class, project.getCampaignLibrary().getId());
	}

	private ObjectIdentity createTestCaseLibraryIdentity(GenericProject project) {
		return new ObjectIdentityImpl(TestCaseLibrary.class, project.getTestCaseLibrary()
				.getId());
	}

	private ObjectIdentity createRequirementLibraryIdentity(GenericProject project) {
		return new ObjectIdentityImpl(RequirementLibrary.class, project.getRequirementLibrary().getId());
	}

	@Override
	public List<ProjectPermission> findProjectPermissionByLogin(String userLogin) {
		List<ProjectPermission> newResult = new ArrayList<ProjectPermission>();
		List<Object[]> result = aclService.retrieveClassAclGroupFromUserLogin(userLogin, PROJECT_CLASS_NAME);
		for (Object[] objects : result) {
			GenericProject project = genericProjectFinder.findById((Long) objects[0]);
			newResult.add(new ProjectPermission(project, (PermissionGroup) objects[1]));
		}
		return newResult;
	}

	@Override
	public List<Project> findProjectWithoutPermissionByLogin(String userLogin) {
		List<Long> idList = aclService.findObjectWithoutPermissionByLogin(userLogin, PROJECT_CLASS_NAME);
		if (idList == null || idList.isEmpty()) {
			return null;
		}
		return genericProjectFinder.findAllByIds(idList);
	}

	
	@Override
	public List<ProjectPermission> findProjectPermissionByParty(long partyId) {
		List<ProjectPermission> newResult = new ArrayList<ProjectPermission>();
		List<Object[]> result = aclService.retrieveClassAclGroupFromPartyId(partyId, PROJECT_CLASS_NAME);
		for (Object[] objects : result) {
			GenericProject project = genericProjectFinder.findById((Long) objects[0]);
			newResult.add(new ProjectPermission(project, (PermissionGroup) objects[1]));
		}
		return newResult;
	}

	@Override
	public List<Project> findProjectWithoutPermissionByParty(long partyId) {
		List<Long> idList = aclService.findObjectWithoutPermissionByPartyId(partyId, PROJECT_CLASS_NAME);
		if (idList == null || idList.isEmpty()) {
			return null;
		}
		return genericProjectFinder.findAllByIds(idList);
	}
	
	@Override
	public void addNewPermissionToProject(long partyId, long projectId, String permissionName) {
		ObjectIdentity projectRef = createProjectIdentity(projectId);
		Party party = partyDao.findById(partyId);
		aclService.addNewResponsibility(party.getId(), projectRef, permissionName);

		GenericProject project = genericProjectFinder.findById(projectId);

		ObjectIdentity rlibraryRef = createRequirementLibraryIdentity(project);
		aclService.addNewResponsibility(party.getId(), rlibraryRef, permissionName);

		ObjectIdentity tclibraryRef = createTestCaseLibraryIdentity(project);
		aclService.addNewResponsibility(party.getId(), tclibraryRef, permissionName);

		ObjectIdentity clibraryRef = createCampaignLibraryIdentity(project);
		aclService.addNewResponsibility(party.getId(), clibraryRef, permissionName);

	}

	@Override
	public void removeProjectPermission(long partyId, long projectId) {
		ObjectIdentity projectRef = createProjectIdentity(projectId);

		aclService.removeAllResponsibilities(partyId, projectRef);

		GenericProject project = genericProjectFinder.findById(projectId);

		ObjectIdentity rlibraryRef = createRequirementLibraryIdentity(project);
		aclService.removeAllResponsibilities(partyId, rlibraryRef);

		ObjectIdentity tclibraryRef = createTestCaseLibraryIdentity(project);
		aclService.removeAllResponsibilities(partyId, tclibraryRef);

		ObjectIdentity clibraryRef = createCampaignLibraryIdentity(project);
		aclService.removeAllResponsibilities(partyId, clibraryRef);
	}

	@Override
	public List<UserProjectPermissionsBean> findUserPermissionsBeanByProject(long projectId) {
		
		Class<?> projectClass = genericProjectFinder.isProjectTemplate(projectId) ? ProjectTemplate.class : Project.class;
		
		return findUserPermissionBeanByProjectOfGivenType(projectId, projectClass);

	}
	
	@Override
	public PagedCollectionHolder<List<UserProjectPermissionsBean>> findUserPermissionsBeanByProject(
			PagingAndSorting sorting, Filtering filtering, long projectId) {
	
		Class<?> projectClass = genericProjectFinder.isProjectTemplate(projectId) ? ProjectTemplate.class : Project.class;
	
		return findUserPermissionBeanByProjectOfGivenType(projectId, projectClass, sorting, filtering);
		
	}
	
	@Override
	public List<PartyProjectPermissionsBean> findPartyPermissionsBeanByProject(long projectId) {
		
		Class<?> projectClass = genericProjectFinder.isProjectTemplate(projectId) ? ProjectTemplate.class : Project.class;
		
		return findPartyPermissionBeanByProjectOfGivenType(projectId, projectClass);

	}
	
	@Override
	public PagedCollectionHolder<List<PartyProjectPermissionsBean>> findPartyPermissionsBeanByProject(
			PagingAndSorting sorting, Filtering filtering, long projectId) {
	
		Class<?> projectClass = genericProjectFinder.isProjectTemplate(projectId) ? ProjectTemplate.class : Project.class;
	
		return findPartyPermissionBeanByProjectOfGivenType(projectId, projectClass, sorting, filtering);
		
	}
	
	@Override
	public List<User> findUserWithoutPermissionByProject(long projectId) {
		List<Long> idList = aclService.findUsersWithoutPermissionByObject(projectId, PROJECT_CLASS_NAME);
		if (idList == null || idList.isEmpty()) {
			return null;
		}
		return userDao.findAllByIds(idList);
	}

	@Override
	public List<Party> findPartyWithoutPermissionByProject(long projectId) {
		List<Long> idList = aclService.findPartiesWithoutPermissionByObject(projectId, PROJECT_CLASS_NAME);
		if (idList == null || idList.isEmpty()) {
			return null;
		}
		return partyDao.findAllByIds(idList);
	}
	
	/**
	 * @see ProjectsPermissionManagementService#copyAssignedUsersFromTemplate(Project, ProjectTemplate)
	 */
	@Override
	public void copyAssignedUsersFromTemplate(Project newProject, ProjectTemplate projectTemplate) {
		long templateId = projectTemplate.getId();
		copyAssignedUsersFromTemplate(newProject, templateId);
	}

	private List<UserProjectPermissionsBean> findUserPermissionsBeanByProjectTemplate(long projectId) {
		return findUserPermissionBeanByProjectOfGivenType(projectId, ProjectTemplate.class);
	}

	//clearly suboptimal, on the other hand this method is seldomely invoked
	private List<UserProjectPermissionsBean> findUserPermissionBeanByProjectOfGivenType(long projectId, Class<?> projectType) {
		List<UserProjectPermissionsBean> newResult = new ArrayList<UserProjectPermissionsBean>();

		List<Object[]> result = aclService.retriveUserAndAclGroupNameFromIdentityAndClass(projectId, projectType);
		for (Object[] objects : result) {
			User user = userDao.findById((Long) objects[0]);
			newResult.add(new UserProjectPermissionsBean(user, (PermissionGroup) objects[1]));
		}
		return newResult;

	}
	
	//TODO finish
	private List<PartyProjectPermissionsBean> findPartyPermissionBeanByProjectOfGivenType(long projectId, Class<?> projectType) {
		List<PartyProjectPermissionsBean> newResult = new ArrayList<PartyProjectPermissionsBean>();

		List<Object[]> result = aclService.retriveUserAndAclGroupNameFromIdentityAndClass(projectId, projectType);
		for (Object[] objects : result) {
			Party party = partyDao.findById((Long) objects[0]);
			newResult.add(new PartyProjectPermissionsBean(party, (PermissionGroup) objects[1]));
		}
		return newResult;

	}
	
	//clearly suboptimal, on the other hand this method is seldomely invoked
	private PagedCollectionHolder<List<UserProjectPermissionsBean>> findUserPermissionBeanByProjectOfGivenType(long projectId, Class<?> projectType, PagingAndSorting sorting, Filtering filtering) {
		

		List<Object[]> result = aclService.retriveUserAndAclGroupNameFromIdentityAndClass(projectId, projectType, sorting, filtering);
		
		int total = result.size();
		
		int startIndex = sorting.getFirstItemIndex();
		int nbItems = Math.min(sorting.getPageSize(), total - startIndex);
		
		result = result.subList(startIndex, nbItems);

		List<UserProjectPermissionsBean> newResult = new ArrayList<UserProjectPermissionsBean>(result.size());
		
		for (Object[] objects : result) {
			User user = userDao.findById((Long) objects[0]);
			newResult.add(new UserProjectPermissionsBean(user, (PermissionGroup) objects[1]));
		}
		
		return new PagingBackedPagedCollectionHolder<List<UserProjectPermissionsBean>>(sorting, total, newResult);

	}
	
	//TODO finish
	private PagedCollectionHolder<List<PartyProjectPermissionsBean>> findPartyPermissionBeanByProjectOfGivenType(long projectId, Class<?> projectType, PagingAndSorting sorting, Filtering filtering) {
		

		List<Object[]> result = aclService.retrievePartyAndAclGroupNameFromIdentityAndClass(projectId, projectType, sorting, filtering);
		
		int total = result.size();
		
		int startIndex = sorting.getFirstItemIndex();
		int nbItems = Math.min(sorting.getPageSize(), total - startIndex);
		
		result = result.subList(startIndex, nbItems);

		List<PartyProjectPermissionsBean> newResult = new ArrayList<PartyProjectPermissionsBean>(result.size());
		
		for (Object[] objects : result) {
			Party party = partyDao.findById((Long) objects[0]);
			newResult.add(new PartyProjectPermissionsBean(party, (PermissionGroup) objects[1]));
		}
		
		return new PagingBackedPagedCollectionHolder<List<PartyProjectPermissionsBean>>(sorting, total, newResult);

	}

	/**
	 * @see org.squashtest.tm.service.project.ProjectsPermissionManagementService#copyAssignedUsersFromTemplate(org.squashtest.tm.domain.project.Project,
	 *      long)
	 */
	@Override
	public void copyAssignedUsersFromTemplate(Project project, long templateId) {
		List<UserProjectPermissionsBean> templateUserPermissions = findUserPermissionsBeanByProjectTemplate(templateId);

		for (UserProjectPermissionsBean userPermission : templateUserPermissions) {
			long userId = userPermission.getUser().getId();
			long projectId = project.getId();
			String permissionName = userPermission.getPermissionGroup().getQualifiedName();
			addNewPermissionToProject(userId, projectId, permissionName);
		}
	}

	/**
	 * @see org.squashtest.tm.service.project.ProjectsPermissionManagementService#removeAllPermissionsFromProjectTemplate(long)
	 */
	@Override
	public void removeAllPermissionsFromProjectTemplate(long templateId) {
		ObjectIdentity projectRef = new ObjectIdentityImpl(ProjectTemplate.class, templateId);
		aclService.removeAllResponsibilities(projectRef);
	}
	
	/**
	 * @see  org.squashtest.tm.service.project.ProjectsPermissionManagementService#removeAllPermissionsFromObject(Class, long)
	 */
	@Override
	public void removeAllPermissionsFromObject(Class<?> clazz, long id){
		ObjectIdentity ref = new ObjectIdentityImpl(clazz, id);
		aclService.removeAllResponsibilities(ref);
	}
	
	@Override
	public boolean isInPermissionGroup(String userLogin, Long projectId, String permissionGroup){
		
		boolean isInGroup = false;
		List<UserProjectPermissionsBean> permissions = findUserPermissionsBeanByProject(projectId);
		for(UserProjectPermissionsBean permission : permissions){
			if(permission.getUser().getLogin().equals(userLogin)){
				if(permission.getPermissionGroup().getQualifiedName().equals(permissionGroup)){
					isInGroup = true;
				}
			}
		}
		
		return isInGroup;
	}
}
