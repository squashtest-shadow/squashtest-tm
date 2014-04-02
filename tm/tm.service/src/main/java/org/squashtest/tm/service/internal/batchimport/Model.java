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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.MultiValueMap;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.library.structures.LibraryGraph;
import org.squashtest.tm.domain.library.structures.LibraryGraph.SimpleNode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder;
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService;



@Repository
@Scope("prototype")
public class Model {

	@Inject
	private SessionFactory sessionFactory;
	
	@Inject
	private CustomFieldDao cufDao;
	
	@Inject
	private TestCaseLibraryFinderService finderService;
	
	@Inject
	private TestCaseCallTreeFinder calltreeFinder;
	
	
	/* **********************************************************************************************************************************
	 * 
	 * The following properties are initialized all together during  init(List<TestCaseTarget>) :
	 *
	 * - testCaseStatusByTarget
	 * - testCaseStepsByTarget
	 * - projectStatusByName
	 * - tcCufsPerProjectname
	 * - stepCufsPerProjectname
	 *
	 ************************************************************************************************************************************ */

	
	/* ------------------------
	  
	  testCaseStatusByTarget :
	  
	  Maps a reference to a TestCase (namely a TestCaseTarget). It keeps track of its status (see ModelizedStatus) and 
	  possibly its id (when there is a concrete instance of it in the database). 
	  
	  Because a test case might be referenced multiple time, once a test case is loaded in that map it'll stay there.  
	  
	 --------------------------*/
	private Map<TestCaseTarget, TargetStatus> testCaseStatusByTarget = new HashMap<TestCaseTarget, TargetStatus>();
	
	
	/* ------------------------
	  
	  stepStatusByTarget : 
	  
	  Maps a test case (given its target) to a list of step models. We only care of their position (because they are identified by position) 
	  and their type (because we want to detect potential attempts of modifications of an action step whereas the target is actually a call step 
	  and conversely).
	  
	  TODO : maybe implement it as a LRU cache that doesn't scrap step lists that were modified (ie "dirty" data that
	  		differs from the DB content).
	  
	 ------------------------ */	
	private Map<TestCaseTarget, List<StepType>> testCaseStepsByTarget = new HashMap<TestCaseTarget, List<StepType>>();
	
	private Map<String, TargetStatus> projectStatusByName = new HashMap<String, TargetStatus>();
		
	private MultiValueMap tcCufsPerProjectname = new MultiValueMap();
	
	private MultiValueMap stepCufsPerProjectname = new MultiValueMap();
	
	

	/* *******************************************************************************************************
	 * 
	 * This property keeps track of the test case call graph. It is not initialized like the rest, it's rather 
	 * initialized on demand (see isCalled for instance )
	 * 
	 * ******************************************************************************************************/
	private TestCaseCallGraph callGraph = new TestCaseCallGraph();
	
	
	// ===============================================================================================
	// ===============================================================================================
	

	// ************************** project access *****************************************************
	
	public TargetStatus getProjectStatus(String projectName){
		if (! projectStatusByName.containsKey(projectName)){
			initProject(projectName);
		}
		return projectStatusByName.get(projectName);
	}
	
	// ************************** Test Case status management ****************************************
	
	public TargetStatus getStatus(TestCaseTarget target){
		
		if (! testCaseStatusByTarget.containsKey(target)){
			init(target);
		}
		
		return testCaseStatusByTarget.get(target);
		
	}
	
	public void setExists(TestCaseTarget target, Long id){
		testCaseStatusByTarget.put(target, new TargetStatus(Existence.EXISTS, id));
	}
	
	public void setToBeCreated(TestCaseTarget target){
		testCaseStatusByTarget.put(target, new TargetStatus(Existence.TO_BE_CREATED));		
		clearSteps(target);
	}
	
	public void setToBeDeleted(TestCaseTarget target){
		testCaseStatusByTarget.put(target, new TargetStatus(Existence.TO_BE_DELETED));
		clearSteps(target);	
	}

	public void setDeleted(TestCaseTarget target){
		testCaseStatusByTarget.put(target, new TargetStatus(Existence.NOT_EXISTS, null));
		clearSteps(target);
	}
	
	// virtually an alias of setDeleted
	public void setNotExists(TestCaseTarget target){
		testCaseStatusByTarget.put(target, new TargetStatus(Existence.NOT_EXISTS, null));
		clearSteps(target);
	}
	
	private void clearSteps(TestCaseTarget target){
		if (testCaseStepsByTarget.containsKey(target)){
			testCaseStepsByTarget.get(target).clear();
		}	
	}

	// ************************** Test Case accessors *****************************************
	
	// may return null
	public Long getId(TestCaseTarget target){
		TargetStatus status = getStatus(target);
		return status.id;
	}
	
	
	public TestCase get(TestCaseTarget target){
		Long id = getId(target);
		if (id == null){
			return null;
		}
		else{
			return (TestCase) sessionFactory.getCurrentSession().load(TestCase.class, id);
		}
	}
	
	// ************************ test case calls code ***********************************
	
	/*
	 *  returns true if the test case is being called by another test case or else false.
	 *  
	 *  Note : the problem arises only if the test case already exists in the database 
	 *  (test cases instructions are all processed before step-instructions are thus newly 
	 *  imported test cases aren't bound to any test step yet).
	 *  
	 */
	public boolean isCalled(TestCaseTarget target){
		
		if (! callGraph.knowsNode(target)){
			initCallerGraph(target);
		}

		return callGraph.isCalled(target);
	}
	
	public boolean wouldCreateCycle(TestStepTarget step, TestCaseTarget destTestCase){
		TestCaseTarget srcTestCase = step.getTestCase();
		
		if (! callGraph.knowsNode(srcTestCase)){
			initCallerGraph(srcTestCase);
		}
		
		if (! callGraph.knowsNode(destTestCase)){
			initCallerGraph(destTestCase);
		}
		
		return callGraph.wouldCreateCycle(srcTestCase, destTestCase);
	}
	
	
	// initialize from the database.
	// not that we don't always want the target to be fully initialized because we only care of the   
	// caller test case graph. So we initialize that information from the DB only and 
	// we don't need to fully fill the model with it.
	private void initCallerGraph(TestCaseTarget target){
		
		Long id = finderService.findNodeIdByPath(target.getPath());
		
		if (id != null){
			LibraryGraph<NamedReference, SimpleNode<NamedReference>> targetCallers 
				= calltreeFinder.getCallerGraph(Arrays.asList(new Long[]{id}));
			
			// some data transform now
			Collection<SimpleNode<NamedReference>> refs = targetCallers.getNodes();
			swapNameForPath(refs);
			
			// now create the graph
			callGraph.addGraph(targetCallers);
		}
		else{
			callGraph.addNode(target);
		}
	}

	private void addCallGraphEdge(TestCaseTarget src, TestCaseTarget dest){
		
		if (! callGraph.knowsNode(src)){
			initCallerGraph(src);
		}
		
		if (! callGraph.knowsNode(dest)){
			initCallerGraph(dest);
		}
		
		callGraph.addEdge(src, dest);
	}
	
	// ************************ Test Step management ********************************

	/**
	 * Adds a step of the specified type to the model. Not to the database.
	 * 
	 * @param target
	 * @param type
	 * @return
	 */
	// returns the index at which the step was created
	public Integer addActionStep(TestStepTarget target){		
		return addStep(target, StepType.ACTION);
	}
	
	public Integer addCallStep(TestStepTarget target, TestCaseTarget calledTestCase){	
		
		addCallGraphEdge(target.getTestCase(), calledTestCase);
		
		return addStep(target, StepType.CALL);
		
	}
	
	private Integer addStep(TestStepTarget target, StepType type){
		
		TestCaseTarget tc = target.getTestCase();
		Integer index = target.getIndex();
		
		if (! testCaseStatusByTarget.containsKey(tc)){
			init(tc);
		}

		
		List<StepType> types = testCaseStepsByTarget.get(tc);
		
		if (index == null || index >= types.size()){
			index = types.size();
		}
		
		types.add(index, type);
		
		return index;
		
	}
	
	
	public void remove(TestStepTarget target){
		
		if (! stepExists(target)){
			throw new IllegalArgumentException("cannot remove non existant step '"+target+"'");
		}
		
		TestCaseTarget tc = target.getTestCase();
		Integer index = target.getIndex();
		
		if (! testCaseStatusByTarget.containsKey(tc)){
			init(tc);
		}
		
		List<StepType> types = testCaseStepsByTarget.get(tc);

		// .intValue() desambiguate with the other method - remove(Object) -
		types.remove(index.intValue()); 
		
	}
	
	
	// ************************ Test Step accessors *********************************
	
	public boolean stepExists(TestStepTarget target){
		
		TestCaseTarget tc = target.getTestCase();
		Integer index = target.getIndex();
		
		if (! testCaseStatusByTarget.containsKey(tc)){
			init(tc);
		}
		
		List<StepType> types = testCaseStepsByTarget.get(tc);
		
		// if index is defined just check it is within range
		// otherwise it doesn't exist.
		return (index != null) ? (types.size() > index) : false;
		
	}
	
	
	// returns null if the step is unknown
	public StepType getType(TestStepTarget target){
		
		TestCaseTarget tc = target.getTestCase();
		Integer index = target.getIndex();
		
		if (! testCaseStatusByTarget.containsKey(tc)){
			init(tc);
		}
		
		List<StepType> types = testCaseStepsByTarget.get(tc);
		
		if (index != null && types.size() > index){
			return types.get(index);
		}
		else{
			return null;
		}
	}
	
	
	// may return null
	public Long getStepId(TestStepTarget target){
		
		Long tcId = getStatus(target.getTestCase()).id;
		Integer index = target.getIndex();
		
		// this condition is heavily defensive and the caller code should check those beforehand
		if (! stepExists(target) || tcId == null || index == null){
			return null;
		}

		Query q = sessionFactory.getCurrentSession().getNamedQuery("testStep.findIdByTestCaseAndPosition");
		q.setParameter(":tcId", tcId);
		q.setParameter("position", index);
		
		return (Long)q.uniqueResult();
		
	}
	
	
	// may return null
	public TestStep getStep(TestStepTarget target){
		
		Long tcId = getStatus(target.getTestCase()).id;
		Integer index = target.getIndex();
		
		// this condition is heavily defensive and the caller code should check those beforehand
		if (! stepExists(target) || tcId == null || index == null){
			return null;
		}

		Query q = sessionFactory.getCurrentSession().getNamedQuery("testStep.findByTestCaseAndPosition");
		q.setParameter(":tcId", tcId);
		q.setParameter("position", index);
		
		return (TestStep)q.uniqueResult();
		
	}
	
	// ************************* CUFS accessors *************************************
	
	@SuppressWarnings("unchecked")
	public Collection<CustomField> getTestCaseCufs(TestCaseTarget target){
		if (! testCaseStatusByTarget.containsKey(target)){
			init(target);
		}
		String projectName = Utils.extractProjectName(target.getPath());
		return tcCufsPerProjectname.getCollection(projectName);
	}
	

	@SuppressWarnings("unchecked")
	public Collection<CustomField> getTestStepCufs(TestStepTarget target){
		TestCaseTarget tc = target.getTestCase();
		
		if (! testCaseStatusByTarget.containsKey(tc)){
			init(tc);
		}
		
		String projectName = Utils.extractProjectName(tc.getPath());
		return stepCufsPerProjectname.getCollection(projectName);
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
		
		// init the steps
		initTestSteps(uniqueTargets);
		
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
		List<String> paths = collectPaths(targets);
	
		// find their ids
		List<Long> ids = finderService.findNodeIdsByPath(paths);
		
		// now store them 
		for (int i=0; i< paths.size(); i++){
			
			TestCaseTarget t = targets.get(i);
			Long id = ids.get(i);
		
			Existence existence = (id == null) ? Existence.NOT_EXISTS : Existence.EXISTS;
			TargetStatus status = new TargetStatus(existence, id);
			
			testCaseStatusByTarget.put(t,status);
		}
	}
	
	// this method assumes that the targets were all processed through initTestCases(targets) beforehand. 
	private void initTestSteps(List<TestCaseTarget> targets){
		
		for (TestCaseTarget target : targets){

			// do not double process the steps
			if ( testCaseStepsByTarget.containsKey( target ) ){
				continue;
			}
			
			TargetStatus status = testCaseStatusByTarget.get(target);
			List<StepType> types = null;
			if (status.id != null && status.status != Existence.TO_BE_DELETED){
				types = loadStepTypes(status.id);
			}
			else{
				types = new ArrayList<StepType>();
			}
			
			testCaseStepsByTarget.put(target, types);
			
		}
		
	}
	
	private void initProject(String projectName){
		initProjectsByName(Arrays.asList(new String[]{projectName}));
	}

	private void initProjects(List<TestCaseTarget> targets){
		initProjectsByName(collectProjects(targets));
	}
	

	private void initProjectsByName(List<String> allNames){


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
			TargetStatus status =  new TargetStatus(Existence.EXISTS, p.getId());
			projectStatusByName.put(p.getName(), status);			
			initCufs(p.getName());
		}
		
		// add the projects that weren't found
		Set<String> knownProjects = projectStatusByName.keySet();
		for (String name : projectNames){
			if (! knownProjects.contains(name)){
				projectStatusByName.put(name, new TargetStatus(Existence.NOT_EXISTS));
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
	

	// *************************** private methods *************************************

	private <OBJ extends Object> List<OBJ> uniqueList(Collection<OBJ> orig){
		Set<OBJ> filtered =  new LinkedHashSet<OBJ>(orig);
		return new ArrayList<OBJ>(filtered);
	}
	
	
	private List<String> collectProjects(List<TestCaseTarget> targets){		
		List<String> paths = collectPaths(targets);
		return Utils.extractProjectNames(paths);
	}
	

	@SuppressWarnings("unchecked")
	private List<String> collectPaths(List<TestCaseTarget> targets){
		return (List<String>)CollectionUtils.collect(targets, TestCasePathCollector.INSTANCE, new ArrayList<String>(targets.size()));
	}
	
	private List<Project> loadProjects(List<String> names){
		Query q = sessionFactory.getCurrentSession().getNamedQuery("Project.findAllByName");  
		q.setParameterList("names", names);
		return q.list();
	}
	
	private List<StepType> loadStepTypes(Long tcId){
		Query query = sessionFactory.getCurrentSession().getNamedQuery("testStep.findBasicInfosByTcId");
		query.setParameter("tcId", tcId, LongType.INSTANCE);
		List<String> stepdata = query.list();
		
		List<StepType> res = new ArrayList<StepType>(stepdata.size());
		for (String strtype : stepdata){
			res.add(StepType.valueOf(strtype));
			
		}
		return res;
	}	
	
	
	// substitute the value of the name attribute of NamedReference so that it becomes a path instead.
	// all references are supposed to exist in the database
	// that's foul play but saves more bloat
	@SuppressWarnings("unchecked")
	private void swapNameForPath(Collection<SimpleNode<NamedReference>> references){
		
		// first ensures that the references will be iterated in a constant order
		List<SimpleNode<NamedReference>> listedRefs = new ArrayList<LibraryGraph.SimpleNode<NamedReference>>(references);
		
		// now collect the ids. Node : the javadoc claims that the result is a new list.
		List<Long> ids = (List<Long>)CollectionUtils.collect(listedRefs, NamedReferenceIdCollector.INSTANCE);
		
		List<String> paths = finderService.getPathsAsString(ids);
		
		for (int i=0; i < paths.size(); i++){
			SimpleNode<NamedReference> currentNode = listedRefs.get(i);
			Long id = ids.get(i);
			String path = paths.get(i);
			currentNode.setKey(new NamedReference(id, path));
		}
		
	}
	
	// ************************ internal types for TestCase Management **********************************
	
	
	/**
	 * That enum sort of represent the level of existence of a test case. It can be either physically present, or virtually present, or virtually non existent,
	 * or default to physically non existant. It helps us keeping track of the fate of a test case during the import process (which is, remember, essentially 
	 * a batch processing).
	 * 
	 * @author bsiri
	 *
	 */
	static enum Existence{
		EXISTS,				// means : exists now in the database
		TO_BE_CREATED,		// means : will be created later on in the process
		TO_BE_DELETED,		// means : will be deleted later on in the process
		NOT_EXISTS;			// means : at this point, doesn't exists either in DB nor in anything planned later in the process
	}
	

	public static class TargetStatus{
				
		Existence status = null;
		Long id = null;
		
		
		TargetStatus(Existence status){
			if (status == Existence.EXISTS){
				throw new IllegalArgumentException("internal error : a TargetStatus representing an actually existent target should specify an id");
			}
			this.status = status;
		}
		
		TargetStatus (Existence status, Long id){
			this.status = status;
			this.id = id;
		}

		public Existence getStatus() {
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
	
	private static class NamedReferenceIdCollector implements Transformer{
		static NamedReferenceIdCollector INSTANCE = new NamedReferenceIdCollector();
		
		private NamedReferenceIdCollector(){
			super();
		}
		
		@Override
		public Long transform(Object input) {
			return ((SimpleNode<NamedReference>)input).getKey().getId();
		}
	}
	
	// ********************************** Internal types for Test Step management *************************
	

	static enum StepType {
		ACTION,
		CALL;
	}
	

	
}
