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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.springframework.stereotype.Component;
import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.library.structures.LibraryGraph;
import org.squashtest.tm.domain.library.structures.LibraryGraph.SimpleNode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.ParameterAssignationMode;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.internal.batchimport.TestCaseCallGraph.Node;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;
import org.squashtest.tm.service.internal.repository.DatasetDao;
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder;
import org.squashtest.tm.service.testcase.ParameterFinder;
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService;

@Component
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

	@Inject
	private ParameterFinder paramFinder;

	@Inject
	private DatasetDao dsDao;

	/* **********************************************************************************************************************************
	 * 
	 * The following properties are initialized all together during
	 * init(List<TestCaseTarget>) :
	 * 
	 * - testCaseStatusByTarget - testCaseStepsByTarget - projectStatusByName -
	 * tcCufsPerProjectname - stepCufsPerProjectname
	 * 
	 * **************************************************************************
	 * **************************************** *****************
	 */

	/**
	 * 
	 * Maps a reference to a TestCase (namely a TestCaseTarget). It keeps track
	 * of its status (see ModelizedStatus) and possibly its id (when there is a
	 * concrete instance of it in the database).<br/>
	 * <br/>
	 * Because a test case might be referenced multiple time, once a test case
	 * is loaded in that map it'll stay there.
	 */
	private Map<TestCaseTarget, TargetStatus> testCaseStatusByTarget = new HashMap<TestCaseTarget, TargetStatus>();

	/**
	 * Maps a test case (given its target) to a list of step models. We only
	 * care of their position (because they are identified by position), their
	 * type (because we want to detect potential attempts of modifications of an
	 * action step whereas the target is actually a call step and conversely),
	 * and possibly a called test case (if the step is a call step, and we want
	 * to keep track of possible cycles).
	 * 
	 */
	private Map<TestCaseTarget, List<InternalStepModel>> testCaseStepsByTarget = new HashMap<TestCaseTarget, List<InternalStepModel>>();

	/**
	 * nothing special, plain wysiwyg
	 */
	private Map<String, ProjectTargetStatus> projectStatusByName = new HashMap<String, ProjectTargetStatus>();

	/**
	 * caches the custom fields defined for the test cases in a given project.
	 * This is a multimap that matches a project name against a collection of
	 * CustomField
	 */
	private MultiValueMap tcCufsPerProjectname = new MultiValueMap();

	/**
	 * same as tcCufsPerProjectname, but regarding the test steps
	 */
	private MultiValueMap stepCufsPerProjectname = new MultiValueMap();

	/**
	 * keeps track of which parameters are defined for which test cases
	 */
	private Map<TestCaseTarget, Collection<ParameterTarget>> parametersByTestCase = new HashMap<TestCaseTarget, Collection<ParameterTarget>>();

	/**
	 * keeps track of which datasets are defined for which test cases
	 */
	private Map<TestCaseTarget, Set<DatasetTarget>> datasetsByTestCase = new HashMap<TestCaseTarget, Set<DatasetTarget>>();

	/**
	 * This property keeps track of the test case call graph. It is not
	 * initialized like the rest, it's rather initialized on demand (see
	 * isCalled for instance )
	 */
	private TestCaseCallGraph callGraph = new TestCaseCallGraph();

	// ===============================================================================================
	// ===============================================================================================

	// ************************** project access
	// *****************************************************

	public ProjectTargetStatus getProjectStatus(String projectName) {
		if (!projectStatusByName.containsKey(projectName)) {
			initProject(projectName);
		}
		return projectStatusByName.get(projectName);
	}

	// ************************** Test Case status management
	// ****************************************

	public TargetStatus getStatus(TestCaseTarget target) {

		if (!testCaseStatusByTarget.containsKey(target)) {
			init(target);
		}

		return testCaseStatusByTarget.get(target);

	}

	public void setExists(TestCaseTarget target, Long id) {
		testCaseStatusByTarget.put(target, new TargetStatus(Existence.EXISTS, id));
	}

	public void setToBeCreated(TestCaseTarget target) {
		testCaseStatusByTarget.put(target, new TargetStatus(Existence.TO_BE_CREATED));
		clearSteps(target);
	}

	public void setToBeDeleted(TestCaseTarget target) {
		TargetStatus oldStatus = testCaseStatusByTarget.get(target);
		testCaseStatusByTarget.put(target, new TargetStatus(Existence.TO_BE_DELETED, oldStatus.id));
		clearSteps(target);
		callGraph.removeNode(target);
	}

	public void setDeleted(TestCaseTarget target) {
		testCaseStatusByTarget.put(target, new TargetStatus(Existence.NOT_EXISTS, null));
		clearSteps(target);
		callGraph.removeNode(target);
	}

	/** virtually an alias of setDeleted */
	public void setNotExists(TestCaseTarget target) {
		testCaseStatusByTarget.put(target, new TargetStatus(Existence.NOT_EXISTS, null));
		clearSteps(target);
	}

	private void clearSteps(TestCaseTarget target) {
		if (testCaseStepsByTarget.containsKey(target)) {
			testCaseStepsByTarget.get(target).clear();
		}
	}

	// ************************** Test Case accessors
	// *****************************************

	/** may return null */
	public Long getId(TestCaseTarget target) {
		return getStatus(target).id;
	}

	public TestCase get(TestCaseTarget target) {
		Long id = getId(target);
		if (id == null) {
			return null;
		} else {
			return (TestCase) sessionFactory.getCurrentSession().load(TestCase.class, id);
		}
	}

	// ************************ test case calls code
	// ***********************************

	/**
	 * returns true if the test case is being called by another test case or
	 * else false.
	 * 
	 * Note : the problem arises only if the test case already exists in the
	 * database (test cases instructions are all processed before
	 * step-instructions are thus newly imported test cases aren't bound to any
	 * test step yet).
	 */
	public boolean isCalled(TestCaseTarget target) {

		if (!callGraph.knowsNode(target)) {
			initCallGraph(target);
		}

		return callGraph.isCalled(target);
	}

	public boolean isCalledBy(TestCaseTarget called, TestCaseTarget caller) {
		return wouldCreateCycle(called, caller);
	}

	public boolean wouldCreateCycle(TestCaseTarget srcTestCase, TestCaseTarget destTestCase) {
		if (!callGraph.knowsNode(srcTestCase)) {
			initCallGraph(srcTestCase);
		}

		if (!callGraph.knowsNode(destTestCase)) {
			initCallGraph(destTestCase);
		}

		return callGraph.wouldCreateCycle(srcTestCase, destTestCase);
	}

	public boolean wouldCreateCycle(TestStepTarget step, TestCaseTarget destTestCase) {
		return wouldCreateCycle(step.getTestCase(), destTestCase);
	}

	/**
	 * initialize the call graph from the database. The whole graph will be
	 * pulled so be carefull to load it only when necessary.
	 */
	private void initCallGraph(TestCaseTarget target) {

		Long id = finderService.findNodeIdByPath(target.getPath());
		if (id == null) {
			// this is probably a new node
			callGraph.addNode(target);
			return;
		}

		LibraryGraph<NamedReference, SimpleNode<NamedReference>> targetCallers = calltreeFinder.getExtendedGraph(Arrays
				.asList(id));

		// some data transform now
		Collection<SimpleNode<NamedReference>> refs = targetCallers.getNodes();
		swapNameForPath(refs);

		// now create the graph
		callGraph.addGraph(targetCallers);
	}

	private void addCallGraphEdge(TestCaseTarget src, TestCaseTarget dest) {

		if (!callGraph.knowsNode(src)) {
			initCallGraph(src);
		}

		if (!callGraph.knowsNode(dest)) {
			initCallGraph(dest);
		}

		callGraph.addEdge(src, dest);
	}

	// ************************ Test Step management
	// ********************************

	/**
	 * Adds a step of the specified type to the model. Not to the database.
	 * 
	 * @param target
	 * @param type
	 * @return the index at which the step was created
	 */
	public Integer addActionStep(TestStepTarget target) {
		return addStep(target, StepType.ACTION, null);
	}

	public Integer addCallStep(TestStepTarget target, TestCaseTarget calledTestCase, CallStepParamsInfo paramInfo) {

		Boolean delegates = (paramInfo.getParamMode().equals(ParameterAssignationMode.DELEGATE)) ? true : false;

		// set the call graph
		addCallGraphEdge(target.getTestCase(), calledTestCase);

		// set the model
		return addStep(target, StepType.CALL, calledTestCase, delegates);

	}

	private Integer addStep(TestStepTarget target, StepType type, TestCaseTarget calledTestCase, Boolean delegates) {

		List<InternalStepModel> steps = findInternalStepModels(target);

		Integer index = target.getIndex();

		if (index == null || index >= steps.size() || index < 0) {
			index = steps.size();
		}

		steps.add(index, new InternalStepModel(type, calledTestCase, delegates));

		return index;

	}

	private Integer addStep(TestStepTarget target, StepType type, TestCaseTarget calledTestCase) {

		List<InternalStepModel> steps = findInternalStepModels(target);

		Integer index = target.getIndex();

		if (index == null || index >= steps.size() || index < 0) {
			index = steps.size();
		}

		steps.add(index, new InternalStepModel(type, calledTestCase));

		return index;

	}

	/**
	 * warning : won't check that the operation will not create a cycle. Such
	 * check needs to be done beforehand.
	 */
	public void updateCallStepTarget(TestStepTarget step, TestCaseTarget newTarget, CallStepParamsInfo paramInfo) {

		if (!stepExists(step)) {
			throw new IllegalArgumentException("cannot update non existant step '" + step + "'");
		}

		if (getType(step) != StepType.CALL) {
			throw new IllegalArgumentException("cannot update the called test case for step '" + step
					+ "' because that step is not a call step");
		}

		InternalStepModel model = findInternalStepModel(step);
		Boolean delegates = (paramInfo.getParamMode().equals(ParameterAssignationMode.DELEGATE)) ? true : false;
		model.setDelegates(delegates);
		TestCaseTarget src = step.getTestCase();
		TestCaseTarget oldDest = model.getCalledTC();

		// update the model
		model.setCalledTC(newTarget);

		// update the call graph
		callGraph.removeEdge(src, oldDest);
		callGraph.addEdge(src, newTarget);

	}

	public void remove(TestStepTarget target) {

		if (!stepExists(target)) {
			throw new IllegalArgumentException("cannot remove non existant step '" + target + "'");
		}

		List<InternalStepModel> steps = findInternalStepModels(target);
		Integer index = target.getIndex();

		// remove from the model
		// .intValue() desambiguate with the other method - remove(Object) -
		InternalStepModel step = steps.remove(index.intValue());

		// remove from the callgraph
		if (step.type == StepType.CALL) {
			callGraph.removeEdge(target.getTestCase(), step.getCalledTC());
		}

	}

	// ************************ Test Step accessors
	// *********************************

	public boolean stepExists(TestStepTarget target) {

		InternalStepModel model = findInternalStepModel(target);

		return (model != null);

	}

	public boolean indexIsFirstAvailable(TestStepTarget target) {
		Integer index = target.getIndex();
		TestCaseTarget tc = target.getTestCase();
		if (!testCaseStatusByTarget.containsKey(tc)) {
			init(tc);
		}
		List<InternalStepModel> steps = testCaseStepsByTarget.get(tc);
		if (index == null || steps == null) {
			return false;
		} else {
			return index == steps.size();
		}
	}

	/**
	 * @param target
	 * @return null if the step is unknown
	 */
	public StepType getType(TestStepTarget target) {

		InternalStepModel model = findInternalStepModel(target);

		if (model != null) {
			return model.getType();
		} else {
			return null;
		}
	}

	/** may return null */
	public Long getStepId(TestStepTarget target) {

		Long tcId = getStatus(target.getTestCase()).id;
		Integer index = target.getIndex();

		// this condition is heavily defensive and the caller code should check
		// those beforehand
		if (!stepExists(target) || tcId == null || index == null) {
			return null;
		}

		Query q = sessionFactory.getCurrentSession().getNamedQuery("testStep.findIdByTestCaseAndPosition");
		q.setParameter(":tcId", tcId);
		q.setParameter("position", index);

		return (Long) q.uniqueResult();

	}

	/** may return null */
	public TestStep getStep(TestStepTarget target) {

		Long tcId = getStatus(target.getTestCase()).id;
		Integer index = target.getIndex();

		// this condition is heavily defensive and the caller code should check
		// those beforehand
		if (!stepExists(target) || tcId == null || index == null) {
			return null;
		}

		Query q = sessionFactory.getCurrentSession().getNamedQuery("testStep.findByTestCaseAndPosition");
		q.setParameter("tcId", tcId);
		q.setParameter("position", index);

		return (TestStep) q.uniqueResult();

	}

	private List<InternalStepModel> findInternalStepModels(TestStepTarget step) {
		TestCaseTarget tc = step.getTestCase();

		if (!testCaseStatusByTarget.containsKey(tc)) {
			init(tc);
		}

		return testCaseStepsByTarget.get(tc);

	}

	private InternalStepModel findInternalStepModel(TestStepTarget step) {
		Integer index = step.getIndex();
		List<InternalStepModel> steps = findInternalStepModels(step);

		if (index != null && steps.size() > index && index >= 0) {
			return steps.get(index.intValue());
		} else {
			return null;
		}
	}

	// ************************** parameters
	// ****************************************

	public boolean doesParameterExists(ParameterTarget target) {
		TestCaseTarget tc = target.getOwner();
		if (!parametersByTestCase.containsKey(tc)) {
			initParameters(Arrays.asList(tc));
		}

		return parametersByTestCase.get(tc).contains(target);
	}

	public void addParameter(ParameterTarget target) {
		TestCaseTarget tc = target.getOwner();
		if (!parametersByTestCase.containsKey(tc)) {
			initParameters(Arrays.asList(tc));
		}

		parametersByTestCase.get(tc).add(target);
	}

	public void removeParameter(ParameterTarget target) {
		TestCaseTarget tc = target.getOwner();
		if (!parametersByTestCase.containsKey(tc)) {
			initParameters(Arrays.asList(tc));
		}

		parametersByTestCase.get(tc).remove(target);
	}

	/**
	 * returns the parameters owned by this test case. It doesn't include all
	 * the parameters from the test case call tree of this test case.
	 */
	public Collection<ParameterTarget> getOwnParameters(TestCaseTarget testCase) {
		if (!parametersByTestCase.containsKey(testCase)) {
			initParameters(Arrays.asList(testCase));
		}

		return parametersByTestCase.get(testCase);
	}

	/**
	 * returns all parameters available to a test case. This includes every
	 * ParameterTarget from the test cases being called directly or indirectly
	 * by this test case, not just the one owner by the test case (unlike
	 * getOwnParameters). Parameters from downstream test cases will be included
	 * iif they are inherited in some ways.
	 * 
	 * @param testCase
	 * @return
	 */
	public Collection<ParameterTarget> getAllParameters(TestCaseTarget testCase) {

		if (!callGraph.knowsNode(testCase)) {
			initCallGraph(testCase);
		}

		Collection<ParameterTarget> result = new HashSet<ParameterTarget>();
		LinkedList<Node> processing = new LinkedList<Node>();
		Set<Node> processed = new HashSet<Node>();

		processing.add(callGraph.getNode(testCase));

		while (!processing.isEmpty()) {
			Node current = processing.pop();
			result.addAll(getOwnParameters(current.getKey()));

			// modification patron
			for (Node child : current.getOutbounds()) {

				List<InternalStepModel> steps = testCaseStepsByTarget.get(current.getKey());
				if (steps != null) {
					for (InternalStepModel step : steps) {
						if (step.type == StepType.CALL && step.calledTC.equals(child.getKey()) && step.getDeleguates()
								&& !processed.contains(step.calledTC)) {
							processing.add(child);
						}
					}
				}
				processed.add(current);
			}
			// existant
			// processing.addAll(current.getOutbounds());
		}

		return result;

	}

	/**
	 * 
	 * @return true if the parameter legitimately belongs to the dataset, false
	 *         otherwise
	 */
	public boolean isParamInDataset(ParameterTarget param, DatasetTarget ds) {
		Collection<ParameterTarget> allparams = getAllParameters(ds.getTestCase());
		return (allparams.contains(param));
	}

	// **************************** datasets
	// ****************************************

	public boolean doesDatasetExists(DatasetTarget target) {
		TestCaseTarget tc = target.getTestCase();
		if (!datasetsByTestCase.containsKey(tc)) {
			initDatasets(Arrays.asList(tc));
		}

		return datasetsByTestCase.get(tc).contains(target);
	}

	/**
	 * This operation is imdepotent
	 * 
	 * @param target
	 */
	public void addDataset(DatasetTarget target) {
		TestCaseTarget tc = target.getTestCase();
		if (!datasetsByTestCase.containsKey(tc)) {
			initDatasets(Arrays.asList(tc));
		}

		datasetsByTestCase.get(tc).add(target);
	}

	public void removeDataset(DatasetTarget target) {
		TestCaseTarget tc = target.getTestCase();
		if (!datasetsByTestCase.containsKey(tc)) {
			initDatasets(Arrays.asList(tc));
		}

		datasetsByTestCase.get(tc).remove(target);
	}

	/**
	 * returns the parameters owned by this test case. It doesn't include all
	 * the parameters from the test case call tree of this test case.
	 */
	public Collection<DatasetTarget> getDatasets(TestCaseTarget testCase) {
		if (!datasetsByTestCase.containsKey(testCase)) {
			initDatasets(Arrays.asList(testCase));
		}

		return datasetsByTestCase.get(testCase);
	}

	// ************************* CUFS accessors
	// *************************************

	@SuppressWarnings("unchecked")
	public Collection<CustomField> getTestCaseCufs(TestCaseTarget target) {
		if (!testCaseStatusByTarget.containsKey(target)) {
			init(target);
		}

		String projectName = PathUtils.extractProjectName(target.getPath());
		Collection<CustomField> cufs = tcCufsPerProjectname.getCollection(projectName);

		if (cufs != null) {
			return cufs;
		} else {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<CustomField> getTestStepCufs(TestStepTarget target) {
		TestCaseTarget tc = target.getTestCase();

		if (!testCaseStatusByTarget.containsKey(tc)) {
			init(tc);
		}

		String projectName = PathUtils.extractProjectName(tc.getPath());
		Collection<CustomField> cufs = stepCufsPerProjectname.getCollection(projectName);
		if (cufs != null) {
			return cufs;
		} else {
			return Collections.emptyList();
		}
	}

	// ************************** loading code
	// **************************************

	public void init(TestCaseTarget target) {
		init(Arrays.asList(new TestCaseTarget[] { target }));
	}

	public void init(List<TestCaseTarget> targets) {

		// ensures unicity
		List<TestCaseTarget> uniqueTargets = uniqueList(targets);

		// init the test cases
		initTestCases(uniqueTargets);

		// init the steps
		initTestSteps(uniqueTargets);

		// init the projects
		initProjects(uniqueTargets);

	}

	private void initTestCases(List<TestCaseTarget> initialTargets) {

		// filter out the test cases we already know of
		List<TestCaseTarget> targets = new LinkedList<TestCaseTarget>();
		for (TestCaseTarget target : initialTargets) {
			if (!testCaseStatusByTarget.containsKey(target)) {
				targets.add(target);
			}
		}

		// exit if they are all known
		if (targets.isEmpty()) {
			return;
		}

		// collect their paths
		List<String> paths = collectPaths(targets);

		// find their ids
		List<Long> ids = finderService.findNodeIdsByPath(paths);

		// now store them
		for (int i = 0; i < paths.size(); i++) {

			TestCaseTarget t = targets.get(i);
			Long id = ids.get(i);

			Existence existence = (id == null) ? Existence.NOT_EXISTS : Existence.EXISTS;
			TargetStatus status = new TargetStatus(existence, id);

			testCaseStatusByTarget.put(t, status);
		}
	}

	private void initParameters(List<TestCaseTarget> initialTargets) {

		for (TestCaseTarget t : initialTargets) {

			if (parametersByTestCase.containsKey(t)) {
				continue;
			}

			TargetStatus status = getStatus(t);

			if (status.id != null && status.status != Existence.TO_BE_DELETED) {
				Collection<Parameter> params = paramFinder.findOwnParameters(status.id);
				Collection<ParameterTarget> parameters = new HashSet<ParameterTarget>(params.size());
				for (Parameter p : params) {
					parameters.add(new ParameterTarget(t, p.getName()));
				}
				parametersByTestCase.put(t, parameters);
			} else {
				parametersByTestCase.put(t, new HashSet<ParameterTarget>());
			}
		}
	}

	private void initDatasets(List<TestCaseTarget> testCases) {

		for (TestCaseTarget t : testCases) {

			if (datasetsByTestCase.containsKey(t)) {
				continue;
			}

			TargetStatus status = getStatus(t);

			if (status.id != null && status.status != Existence.TO_BE_DELETED) {
				Collection<Dataset> datasets = dsDao.findOwnDatasetsByTestCase(status.id);
				Set<DatasetTarget> dstargets = new HashSet<DatasetTarget>(datasets.size());
				for (Dataset ds : datasets) {
					dstargets.add(new DatasetTarget(t, ds.getName()));
				}
				datasetsByTestCase.put(t, dstargets);
			} else {
				datasetsByTestCase.put(t, new HashSet<DatasetTarget>());
			}
		}
	}

	/**
	 * this method assumes that the targets were all processed through
	 * initTestCases(targets) beforehand.
	 */
	private void initTestSteps(List<TestCaseTarget> targets) {

		for (TestCaseTarget target : targets) {

			// do not double process the steps
			if (testCaseStepsByTarget.containsKey(target)) {
				continue;
			}

			TargetStatus status = testCaseStatusByTarget.get(target);

			List<InternalStepModel> steps = null;
			if (status.id != null && status.status != Existence.TO_BE_DELETED) {
				steps = loadStepsModel(status.id);
			} else {
				steps = new ArrayList<InternalStepModel>();
			}

			testCaseStepsByTarget.put(target, steps);

		}

	}

	private void initProject(String projectName) {
		initProjectsByName(Arrays.asList(new String[] { projectName }));
	}

	private void initProjects(List<TestCaseTarget> targets) {
		initProjectsByName(collectProjects(targets));
	}

	private void initProjectsByName(List<String> allNames) {

		// filter out projects we already know of
		List<String> projectNames = new LinkedList<String>();
		for (String name : allNames) {
			if (!projectStatusByName.containsKey(name)) {
				projectNames.add(name);
			}
		}

		// exit if they are all known
		if (projectNames.isEmpty()) {
			return;
		}

		// now begin
		List<Project> projects = loadProjects(projectNames);

		// add the projects that were found
		for (Project p : projects) {
			ProjectTargetStatus status = new ProjectTargetStatus(Existence.EXISTS, p.getId(), p.getTestCaseLibrary().getId());
			projectStatusByName.put(p.getName(), status);
			initCufs(p.getName());
		}

		// add the projects that weren't found
		Set<String> knownProjects = projectStatusByName.keySet();
		for (String name : projectNames) {
			if (!knownProjects.contains(name)) {
				projectStatusByName.put(name, new ProjectTargetStatus(Existence.NOT_EXISTS));
			}
		}

	}

	/** assumes that the project exists and that we have its ID */
	private void initCufs(String projectName) {

		Long projectId = projectStatusByName.get(projectName).id;

		List<CustomField> tccufs = cufDao.findAllBoundCustomFields(projectId, BindableEntity.TEST_CASE);
		tcCufsPerProjectname.putAll(projectName, tccufs);

		List<CustomField> stcufs = cufDao.findAllBoundCustomFields(projectId, BindableEntity.TEST_STEP);
		stepCufsPerProjectname.putAll(projectName, stcufs);

	}

	// *************************** private methods
	// *************************************

	private <OBJ extends Object> List<OBJ> uniqueList(Collection<OBJ> orig) {
		Set<OBJ> filtered = new LinkedHashSet<OBJ>(orig);
		return new ArrayList<OBJ>(filtered);
	}

	private List<String> collectProjects(List<TestCaseTarget> targets) {
		List<String> paths = collectPaths(targets);
		return PathUtils.extractProjectNames(paths);
	}

	@SuppressWarnings("unchecked")
	private List<String> collectPaths(List<TestCaseTarget> targets) {
		return (List<String>) CollectionUtils.collect(targets, TestCasePathCollector.INSTANCE, new ArrayList<String>(
				targets.size()));
	}

	@SuppressWarnings("unchecked")
	private List<Project> loadProjects(List<String> names) {
		Query q = sessionFactory.getCurrentSession().getNamedQuery("Project.findAllByName");
		q.setParameterList("names", names);
		return q.list();
	}

	@SuppressWarnings("unchecked")
	private List<InternalStepModel> loadStepsModel(Long tcId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("testStep.findBasicInfosByTcId");
		query.setParameter("tcId", tcId, LongType.INSTANCE);

		List<Object[]> stepdata = query.list();

		List<InternalStepModel> steps = new ArrayList<InternalStepModel>(stepdata.size());
		for (Object[] tuple : stepdata) {
			StepType type = StepType.valueOf((String) tuple[0]);
			TestCaseTarget calledTC = null;
			boolean delegates = false;
			if (type == StepType.CALL) {
				String path = finderService.getPathAsString((Long) tuple[1]);
				calledTC = new TestCaseTarget(path);
				delegates = (Boolean) tuple[2];
			}
			steps.add(new InternalStepModel(type, calledTC, delegates));
		}

		return steps;
	}

	/**
	 * substitutes the value of the name attribute of NamedReference so that it
	 * becomes a path instead.<br/>
	 * All references are supposed to exist in the database that's foul play but
	 * saves more bloat
	 */
	@SuppressWarnings("unchecked")
	private void swapNameForPath(Collection<SimpleNode<NamedReference>> references) {

		// first ensures that the references will be iterated in a constant
		// order
		List<SimpleNode<NamedReference>> listedRefs = new ArrayList<LibraryGraph.SimpleNode<NamedReference>>(references);

		// now collect the ids. Node : the javadoc claims that the result is a
		// new list.
		List<Long> ids = (List<Long>) CollectionUtils.collect(listedRefs, NamedReferenceIdCollector.INSTANCE);

		List<String> paths = finderService.getPathsAsString(ids);

		for (int i = 0; i < paths.size(); i++) {
			SimpleNode<NamedReference> currentNode = listedRefs.get(i);
			Long id = ids.get(i);
			String path = paths.get(i);
			currentNode.setKey(new NamedReference(id, path));
		}

	}

	// ************************ internal types for TestCase Management
	// **********************************

	/**
	 * That enum sort of represents the level of existence of a test case. It
	 * can be either physically present, or virtually present, or virtually non
	 * existent, or default to physically non existant.<br/>
	 * It helps us keeping track of the fate of a test case during the import
	 * process (which is, remember, essentially a batch processing).
	 * 
	 * @author bsiri
	 * 
	 */
	static enum Existence {
		/**
		 * exists now in the database
		 */
		EXISTS,
		/**
		 * will be created later on in the process
		 */
		TO_BE_CREATED,
		/**
		 * will be deleted later on in the process
		 */
		TO_BE_DELETED,
		/**
		 * at this point, doesn't exists either in DB nor in anything planned
		 * later in the process
		 */
		NOT_EXISTS;
	}

	/**
	 * Holds the {@link #id} and the {@link #status} of an entity concerned by
	 * the import.
	 * 
	 */
	static class TargetStatus {// NOSONAR this class is not final so that it can
		// be tested in ValidationFacilityTest
		/**
		 * The {@link Existence} status of the concerned entity.
		 */
		Existence status = null; // NOSONAR this attribute is local to the
		// package and the implementor knows what
		// he's
		// doing
		/**
		 * The id of the concerned entity.
		 */
		Long id = null; // NOSONAR this attribute is local to the package and

		// the implementor knows what he's doing

		private TargetStatus(Existence status) {
			if (status == Existence.EXISTS) {
				throw new IllegalArgumentException(
						"internal error : a TargetStatus representing an actually existent target should specify an id");
			}
			this.status = status;
		}

		private TargetStatus(Existence status, Long id) {
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

	static class ProjectTargetStatus extends TargetStatus {
		Long testCaseLibraryId;

		private ProjectTargetStatus(Existence status, Long id, Long testCaseLibraryId) {
			super(status, id);
			this.testCaseLibraryId = testCaseLibraryId;
		}
		private ProjectTargetStatus(Existence status) {
			super(status);
		}

		public Long getTestCaseLibraryId() {
			return testCaseLibraryId;
		}
	}

	private static final class TestCasePathCollector implements Transformer {

		private static final TestCasePathCollector INSTANCE = new TestCasePathCollector();

		private TestCasePathCollector() {
			super();
		}

		@Override
		public Object transform(Object value) {
			return ((TestCaseTarget) value).getPath();
		}
	}

	private static final class NamedReferenceIdCollector implements Transformer {
		private static final NamedReferenceIdCollector INSTANCE = new NamedReferenceIdCollector();

		private NamedReferenceIdCollector() {
			super();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Long transform(Object input) {
			return ((SimpleNode<NamedReference>) input).getKey().getId();
		}
	}

	// ********************************** Internal types for Test Step
	// management *************************

	static enum StepType {
		ACTION, CALL;
	}

	private static final class InternalStepModel {
		private StepType type;
		private TestCaseTarget calledTC;
		private Boolean delegates = null;

		public InternalStepModel(StepType type, TestCaseTarget calledTC) {
			this.type = type;
			this.calledTC = calledTC;
		}

		public InternalStepModel(StepType type, TestCaseTarget calledTC, boolean delegates) {
			this.type = type;
			this.calledTC = calledTC;
			this.delegates = delegates;
		}

		public void setDelegates(boolean delegates) {
			this.delegates = delegates;
		}

		public boolean getDeleguates() {
			return delegates;
		}

		public TestCaseTarget getCalledTC() {
			return calledTC;
		}

		public void setCalledTC(TestCaseTarget calledTC) {
			this.calledTC = calledTC;
		}

		public StepType getType() {
			return type;
		}

	}

}
