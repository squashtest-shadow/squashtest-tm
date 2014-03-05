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
package org.squashtest.tm.service.internal.batchimport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import net.sf.cglib.core.CollectionUtils;
import net.sf.cglib.core.Transformer;

import org.apache.commons.collections.map.MultiValueMap;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService;



/**
 * Useful methods :
 *
 * -- about test cases :
 *
 * public boolean testCaseExists(TestCaseTarget target)
 * public Long getTestCaseId(TestCaseTarget target)
 * public TestCase getTestCase(TestCaseTarget target)
 * 
 * -- about projects and custom fields 
 * 
 * public Long getProjectId(String projectName)
 * public Collection<CustomField> getTestCaseCufsForProject(String projectName) 
 * public Collection<CustomField> getStepCufsForProject(String projectName)
 * public Collection<CustomField> getTestCaseCufs(TestCaseTarget target)
 * public Collection<CustomField> getStepCufs(TestStepTarget target)
 * 
 * -- for simulation of yet unknown test cases :
 * 
 * public void registerTarget(TestCaseTarget target, Long id)
 * public void removeTarget(TestCaseTarget target)
 * 
 * 
 * 
 * 
 * @author bsiri
 *
 */
@Repository
@Scope("prototype")
public class Model {

	@Inject
	private SessionFactory sessionFactory;
	
	@Inject
	private CustomFieldDao cufDao;
	
	@Inject
	private TestCaseLibraryFinderService finderService;
	
	
	private Map<TestCaseTarget, TargetStatus> testCaseStatusByTarget = new HashMap<TestCaseTarget, TargetStatus>();
	
	private Map<String, TargetStatus> projectStatusByName = new HashMap<String, TargetStatus>();
	
	private MultiValueMap tcCufsPerProjectname = new MultiValueMap();
	
	private MultiValueMap stepCufsPerProjectname = new MultiValueMap();
	
	
	
	// ************************** status managment *****************************************
	
	public TargetStatus getStatus(TestCaseTarget target){
		
		if (! testCaseStatusByTarget.containsKey(target)){
			init(target);
		}
		
		return testCaseStatusByTarget.get(target);
		
	}
	
	public void setExists(TestCaseTarget target, Long id){
		testCaseStatusByTarget.put(target, new TargetStatus(ModelizedStatus.EXISTS, id));
	}
	
	public void setToBeCreated(TestCaseTarget target){
		testCaseStatusByTarget.put(target, new TargetStatus(ModelizedStatus.TO_BE_CREATED));
	}
	
	public void setToBeDeleted(TestCaseTarget target){
		testCaseStatusByTarget.put(target, new TargetStatus(ModelizedStatus.TO_BE_DELETED));
	}

	public void setDeleted(TestCaseTarget target){
		testCaseStatusByTarget.put(target, new TargetStatus(ModelizedStatus.NOT_EXISTS));
	}

	// ************************** accessors *****************************************
	
	// may return null;
	public Long getTestCaseId(TestCaseTarget target){
		TargetStatus status = getStatus(target);
		return status.id;
	}
	
	public TestCase getTestCase(TestCaseTarget target){
		Long id = getTestCaseId(target);
		if (id == null){
			return null;
		}
		else{
			return (TestCase) sessionFactory.getCurrentSession().load(TestCase.class, id);
		}
	}
	
	// ************************** loading code **************************************
	
	public void init(TestCaseTarget target){
		init(Arrays.asList(new TestCaseTarget[] { target }));
	}
	
	public void init(List<TestCaseTarget> targets){
		// ensures unicity
		List<TestCaseTarget> uniqueTargets = uniqueList(targets);
		
		// init the test cases
		initTestCases(uniqueTargets);
		
		// init the projects
		initProjects(uniqueTargets);

	}

	

	private void initTestCases(List<TestCaseTarget> initialTargets){
		
		// filter out the test cases we already know of
		List<TestCaseTarget> targets = new LinkedList<TestCaseTarget>();
		for (TestCaseTarget target : initialTargets){
			if (! testCaseStatusByTarget.containsKey(target)){
				targets.add(target);
			}
		}
		
		// exit if they are all known
		if (targets.isEmpty()){
			return;
		}
		
		// collect their paths
		List<String> paths = CollectionUtils.transform(targets, TestCasePathCollector.INSTANCE);
	
		// find their ids
		List<Long> ids = finderService.findNodeIdsByPath(paths);
		
		// now store them as 
		for (int i=0; i< paths.size(); i++){
			
			TestCaseTarget t = targets.get(i);
			Long id = ids.get(i);
			
			ModelizedStatus existence = (id == null) ? ModelizedStatus.NOT_EXISTS : ModelizedStatus.EXISTS;
			TargetStatus status = new TargetStatus(existence, id);
			
			testCaseStatusByTarget.put(t,status);
		}
	}
	

	private void initProjects(List<TestCaseTarget> targets){
		
		List<String> allNames = collectProjects(targets);
		
		// filter out projects we already know of
		List<String> projectNames = new LinkedList<String>();
		for (String name : allNames){
			if (! projectStatusByName.containsKey(name)){
				projectNames.add(name);
			}
		}
		
		// exit if they are all known
		if (projectNames.isEmpty()){
			return;
		}
		
		// now begin
		List<Project> projects = loadProjects(projectNames);
		
		// add the projects that were found 
		for (Project p : projects){
			TargetStatus status =  new TargetStatus(ModelizedStatus.EXISTS, p.getId());
			projectStatusByName.put(p.getName(), status);			
			initCufs(p.getName());
		}
		
		// add the projects that weren't found
		Set<String> knownProjects = projectStatusByName.keySet();
		for (String name : projectNames){
			if (! knownProjects.contains(name)){
				projectStatusByName.put(name, new TargetStatus(ModelizedStatus.NOT_EXISTS));
			}
		}

	}
	
	// assumes that the project exists and that we have its ID
	private void initCufs(String projectName){
		
		Long projectId = projectStatusByName.get(projectName).id;
		
		List<CustomField> tccufs = cufDao.findAllBoundCustomFields(projectId, BindableEntity.TEST_CASE) ;
		tcCufsPerProjectname.putAll(projectName, tccufs);
		
		List<CustomField> stcufs = cufDao.findAllBoundCustomFields(projectId, BindableEntity.TEST_STEP) ;
		stepCufsPerProjectname.putAll(projectName, stcufs);
		

	}
	

	/**
	 * returns null if not found
	 * 
	 * @param target
	 * @return
	 */
	/*public Long getTestCaseId(TestCaseTarget target){
		
		if (! testCaseIdsByTarget.containsKey(target)){
			initTargets(Arrays.asList(new TestCaseTarget[]{target}));
		}
		
		return testCaseIdsByTarget.get(target);
		
	}
	
	
	public TestCase getTestCase(TestCaseTarget target){
		
		Long id = getTestCaseId(target);
		if (id == null) {
			return null;
		}
		else{
			return (TestCase)sessionFactory.getCurrentSession().load(TestCase.class, id);
		}
		
	}
	
	
	public Collection<CustomField> getTestCaseCufsForProject(String projectName){
		if (! projectIdsByName.containsKey(projectName)){
			initProjectsAndTheRest("/"+projectName+"/a");	//awful hack that transforms that in a valid path, therefore it can be processed.
		}
		return tcCufsPerProjectname.getCollection(projectName);
	}
	
	public Collection<CustomField> getStepCufsForProject(String projectName){
		if (! projectIdsByName.containsKey(projectName)){
			initProjectsAndTheRest("/"+projectName+"/a");	//awful hack that transforms that in a valid path, therefore it can be processed.
		}
		return stepCufsPerProjectname.getCollection(projectName);
	}
	
	public Collection<CustomField> getTestCaseCufs(TestCaseTarget target){
		return getTestCaseCufsForProject(Utils.extractProjectName(target.getPath()));
	}
	
	public Collection<CustomField> getStepCufs(TestStepTarget target){
		return getStepCufsForProject(Utils.extractProjectName(target.getTestCase().getPath()));
	}
	
	public Long getProjectId(String projectName){
		if (!projectIdsByName.containsKey(projectName)){
			initProjectsAndTheRest("/"+projectName+"/a");	//awful hack that transforms that in a valid path, therefore it can be processed.
		}
		return projectIdsByName.get(projectName);
	}
	
	
	public boolean testCaseExists(TestCaseTarget target){
		Long id = getTestCaseId(target);
		return (id != null);
	}
	
	
	// that method is useful if a previously non existant TestCaseTarget has been created since (either for real or simulation)
	public void registerTarget(TestCaseTarget target, Long id){
		testCaseIdsByTarget.put(target, id);
		String projectName = Utils.extractProjectName(target.getPath());
		if (! projectIdsByName.containsKey(projectName)){
			initProjectsAndTheRest(target.getPath());
		}
	}
	
	public void removeTarget(TestCaseTarget target){
		testCaseIdsByTarget.remove(target);
	}
	
	
	/**
	 * When you have a batch of test cases you might need later on, it's better 
	 * to load them all at once. That's what this method is for.
	 * 
	 * @param targets
	 */
	/*@SuppressWarnings("unchecked")
	public void initTargets(List<TestCaseTarget> targets){
		
		List<TestCaseTarget> uniques = uniqueList(targets);
		
		List<String> paths = CollectionUtils.transform(uniques, new Transformer() {
			@Override
			public Object transform(Object value) {
				return ((TestCaseTarget)value).getPath();
			}
		});
		
		List<Long> ids = finderService.findNodeIdsByPath(paths);
		for (int i=0; i< uniques.size(); i++){
			TestCaseTarget t = uniques.get(i);
			testCaseIdsByTarget.put(t, ids.get(i));
		}
		
		initProjectsAndTheRest(paths);
	}
	
	
	
	
	// ************************** private stuffs ***********************************
	
	
	private void initProjectsAndTheRest(String path){
		initProjectsAndTheRest(Arrays.asList(new String[]{path}));
	}
	
	private void initProjectsAndTheRest(List<String> paths){
		
		List<String> projectNames = Utils.extractProjectNames(paths);
		List<String> uniqueNames = uniqueList(projectNames);
		
	
		List<Project> projects = initProjects(uniqueNames);
		
		for (Project p : projects){
			
			if (! projectIdsByName.containsKey(p.getName())){
				projectIdsByName.put(p.getName(), p.getId());
				
				List<CustomField> tccufs = cufDao.findAllBoundCustomFields(p.getId(), BindableEntity.TEST_CASE) ;
				tcCufsPerProjectname.putAll(p.getName(), tccufs);
				
				List<CustomField> stcufs = cufDao.findAllBoundCustomFields(p.getId(), BindableEntity.TEST_STEP) ;
				stepCufsPerProjectname.putAll(p.getName(), stcufs);
			}
		}
		
		
	}
	

	
*/
	
	// *************************** private methods *************************************

	private <OBJ extends Object> List<OBJ> uniqueList(Collection<OBJ> orig){
		Set<OBJ> filtered =  new LinkedHashSet<OBJ>(orig);
		return new ArrayList<OBJ>(filtered);
	}
	
	
	@SuppressWarnings("unchecked")
	private List<String> collectProjects(List<TestCaseTarget> targets){		
		List<String> paths = CollectionUtils.transform(targets, TestCasePathCollector.INSTANCE);
		return Utils.extractProjectNames(paths);
	}
	
	private List<Project> loadProjects(List<String> names){
		Query q = sessionFactory.getCurrentSession().getNamedQuery("Project.findAllByName");  
		q.setParameterList("names", names);
		return q.list();
	}
	
	
	// ************************ internal declarations **********************************
	
	
	static enum ModelizedStatus{
		EXISTS,				// means : exists now in the database
		TO_BE_CREATED,		// means : will be created later on in the process
		TO_BE_DELETED,		// means : will be deleted later on in the process
		NOT_EXISTS;			// means : at this point, doesn't exists either in DB nor in anything planned later in the process
	}
	


	public static class TargetStatus{
				
		ModelizedStatus status = null;
		Long id = null;

		
		TargetStatus(ModelizedStatus status){
			if (status == ModelizedStatus.EXISTS){
				throw new IllegalArgumentException("internal error : a TargetStatus representing an actually existent target should specify an id");
			}
			this.status = status;
		}
		
		TargetStatus (ModelizedStatus status, Long id){
			this.status = status;
			this.id = id;
		}

		public ModelizedStatus getStatus() {
			return status;
		}

		public Long getId() {
			return id;
		}
		
	}
	
	
	private static class TestCasePathCollector implements Transformer{
		
		static TestCasePathCollector INSTANCE = new TestCasePathCollector();
		
		private TestCasePathCollector(){
			super();
		}
		
		@Override
		public Object transform(Object value) {
			return ((TestCaseTarget)value).getPath();
		}
	}
	
	
}
