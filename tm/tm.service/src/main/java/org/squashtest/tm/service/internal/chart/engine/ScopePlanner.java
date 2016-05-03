/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.internal.chart.engine;

import static org.squashtest.tm.domain.EntityType.CAMPAIGN;
import static org.squashtest.tm.domain.EntityType.CAMPAIGN_FOLDER;
import static org.squashtest.tm.domain.EntityType.CAMPAIGN_LIBRARY;
import static org.squashtest.tm.domain.EntityType.ITERATION;
import static org.squashtest.tm.domain.EntityType.PROJECT;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT_FOLDER;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT_LIBRARY;
import static org.squashtest.tm.domain.EntityType.TEST_CASE;
import static org.squashtest.tm.domain.EntityType.TEST_CASE_FOLDER;
import static org.squashtest.tm.domain.EntityType.TEST_CASE_LIBRARY;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.campaign.QCampaign;
import org.squashtest.tm.domain.campaign.QCampaignPathEdge;
import org.squashtest.tm.domain.campaign.QIteration;
import org.squashtest.tm.domain.chart.ChartQuery;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.MeasureColumn;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.requirement.QRequirement;
import org.squashtest.tm.domain.requirement.QRequirementPathEdge;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.QTestCasePathEdge;
import org.squashtest.tm.service.security.Authorizations;
import org.squashtest.tm.service.security.PermissionEvaluationService;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;

/**
 * <p>
 * 	This class will stuff a DetailedChartQuery with transient additional filters on which parts of the global repository should be considered.
 *	</p>
 *
 *	<h3>specification for TM 1.13</h3>
 *
 * <p>
 * 		A scope is defined as the combination of one or more of the following elements :
 *
 * 		<ol>
 * 			<li>a whole project ,</li>
 * 			<li>some testcase/requirement/campaign libraries,</li>
 * 			<li>some testcase/requirement/campaign folders,</li>
 * 			<li>or testcases/requirement/campaign/iterations</li>
 * 		</ol>
 *
 * 		All of these define a hierarchy of nodes, and only nodes that belong to that hierarchy will be accounted for in the chart. The user is also required to have the
 * 		READ permission on them.
 * </p>
 *
 *	<p>
 * 		Only entities that belong to the main query are filtered that way. Entities part of subqueries won't be,
 * 		and we don't want to : that's why they are in a separated subquery in the first place.
 * </p>
 *
 * <p>
 * 		When one or several project are elected for a scope, then all test cases, campaign, requirement, executions etc must belong to that project.
 *		Note that this holds only for entities that actually appear in the main query. For example if a query only encompasses test cases, there
 *		is no point in restricting the scope for campaigns.
 * </p>
 *
 *
 * <h3> How is this done </h3>
 *
 * <p>
 * 		Depending on the content of the scope the main query may be appended with additional joins and/or subqueries, and additional where clauses will be added. The optional
 * 	join queries ensure that some key entities will be present in the main query (they will be joined on) because some useful tests will be applied on them by the "where" clauses.
 *
 * For example, if the Scope says that "elements must belong to CampaignFolder 15" and the main query only treat the Execution, in order to test whether this execution belong to
 * that folder we must then append to the main query all required joins from Execution to Campaign because there are no other way to test the ancestry of the Executions.
 *
 * 		In a second phase, when this is done, some "where" clauses will be added. In our example, the "where" clause would test that the campaign (to which belong the executions)
 * 	is itself a child or grandchild of CampaignFolder 15.
 * </p>
 *
 * <p>
 * 		It was said earlier that we apply a scope only if it is meaningful to the main query (remember : if your chart treats of campaigns only there is no point in scoping on
 * 		the campaigns). To help with the computation of that we will use a concept named SubScope. A SubScopebasically represent the three main business ensembles,
 * 		namely the business of test case-related entities, requirement-related entities and campaign-related entities.
 *
 * 		<ul>
 * 			<li>test case scope : test case library, test case folders, test cases</li>
 * 			<li>requirement scope : requirement library, requirement folders requirement, requirement version</li>
 * 			<li>campaign scope : campaign library, campaign folders, campaigns, iteration, testplan, execution, issues</li>
 * 		</ul>
 *
 * 		A scope may define entity references that fall in one of those three subscope (scope Project is equivalent to all three subscopes at once). Also, a chart may define measures,
 * 		filters or axes that also belong to these subscopes. The Scope will apply to the chart only if the entities of the chart and of the scoped entity belong to the same subscopes.
 *
 *
 * </p>
 *
 * @author bsiri
 *
 */
@Component()
@Scope("prototype")
class ScopePlanner {
	// infrastructure
	@PersistenceContext
	private EntityManager em;

	@Inject
	private PermissionEvaluationService permissionService;

	// work variables
	private DetailedChartQuery chartQuery;

	private List<EntityReference> scope;

	private ExtendedHibernateQuery<?> hibQuery;

	private ScopeUtils utils;    // created with @PostContruct

	// ************************ build the tool ******************************

	ScopePlanner() {
		super();
	}

	void setHibernateQuery(ExtendedHibernateQuery<?> hibQuery) {
		this.hibQuery = hibQuery;
	}

	void setChartQuery(DetailedChartQuery chartQuery) {
		this.chartQuery = chartQuery;
	}

	void setScope(List<EntityReference> scope) {
		this.scope = scope;
	}

	@PostConstruct
	void afterPropertiesSet() {
		utils = new ScopeUtils(em, permissionService);
	}

	// *********************** main method **********************************

	protected void appendScope() {

		// step 1 : test the ACLs
		filterByACLs();

		// early exit if empty
		if (!isScopeRelevant()) {
			addImpossibleCondition();
			return;
		}

		// step 2 : join the main query with projects and/or libraries if some are specified
		addExtraJoins();

		// step 3 : add the filters
		addWhereClauses();

	}


	// *********************** step 1 ***********************


	private void filterByACLs() {

		List<EntityReference> filtered = new ArrayList<>();

		for (EntityReference ref : scope) {
			if (utils.checkPermissions(ref)) {
				filtered.add(ref);
			}
		}

		scope = filtered;

	}


	// *********************** step 2 ***********************


	/*
	 * In order to extend the main query we will create a dummy query, that will be merged
	 * with the main query just like inlined subqueries do.
	 *
	 * The aim of that dummy query is to make sure that the entities from which a project can be joined on
	 * will be present in the query (even if in some occurences they are already present).
	 *
	 *
	 */
	private void addExtraJoins() {

		Set<String> fakeColnames = findExtraJoinColumnNames();

		if (fakeColnames.isEmpty()) {
			return;
		}

		// now we can create the dummy query
		ChartQuery dummy = createDummyQuery(fakeColnames);
		DetailedChartQuery detailDummy = new DetailedChartQuery(dummy);

		// ... and then run it in a QueryPlanner
		QueryPlanner planner = new QueryPlanner(detailDummy);
		planner.appendToQuery(hibQuery);
		planner.modifyQuery();
	}


	private Set<String> findExtraJoinColumnNames() {

		Set<SubScope> querySubscopes = findQuerySubScopes();

		Set<EntityType> scopeTypes = findEntitiesFromScope();

		/*
		 * now we start to sketch the future dummy query
		 * by registering which extra columns we need
		 *
		 * Note : for each subscope we do so only this
		 * subscope is defined in both the filter and the query
		 */
		Set<String> fakeColnames = new HashSet<>();

		for (EntityType type : scopeTypes) {
			SubScope typeScope = toSubScope(type);

			/*
			 * if there is a match -> go add the column
			 * don't be shy to add it regardless it already exists
			 * in the main query or not, the query planner
			 * will make the difference.
			 */
			if (querySubscopes.contains(typeScope)) {

				// there is a quirk when the scopeType == ITERATION. Indeed
				// the required column is then "ITERATION_ID" and not the regular one
				if (type == ITERATION) {
					fakeColnames.add("ITERATION_ID");
				} else {
					fakeColnames.add(typeScope.getRequiredColumn());
				}

			}
		}
		return fakeColnames;
	}


	/*
	 *	The goal here is to create a Query with as little detail as possible, we just add what
	 * a QueryPlanner would need to add some join clauses to an existing query.
	 *
	 * For that we forge that Query using the same axis than the HibernateQuery we want to extend,
	 * and fake measure columns that exists only to make the QueryPlanner join on them.
	 */
	private ChartQuery createDummyQuery(Set<String> fakeMeasureColLabels) {

		ChartQuery dummy = new ChartQuery();

		// the axis
		dummy.setAxis(chartQuery.getAxis());

		// now the dummy measures
		List<MeasureColumn> fakeMeasures = new ArrayList<>();
		for (String fakeMeasure : fakeMeasureColLabels) {
			ColumnPrototype mProto = utils.findColumnPrototype(fakeMeasure);
			MeasureColumn meas = new MeasureColumn();
			meas.setColumn(mProto);
			fakeMeasures.add(meas);
		}

		dummy.setMeasures(fakeMeasures);

		// now we have defined the extension of our query
		// we can return
		return dummy;
	}


	// *********************** step 3 ***********************


	/*
	 * Once the main query is properly extended, we can add the where clauses.
	 *
	 * According to the class-level documentation, those where clauses will take two forms :
	 *
	 * 1/ where entity.project.id in (....)
	 * 2/ where exists (select 1 from TestCasePathEdge edge where edge.ancestorId in (...) and edge.descendantId = entity.id)
	 *
	 *  All the conditions will be or'ed together within the same subscope, and and'ed together between subscopes.
	 *
	 *  For example :
	 *  where (
	 *  	(
	 *  		campaign.project.id in (..) or
	 *  		exists (select 1 <blabla> = campaign.id)
	 *  	)
	 *  	and
	 *  	(
	 *  		exists (select 1 <blabla> = testcase.id)
	 *  	)
	 *  )
	 *
	 */
	private void addWhereClauses() {

		BooleanBuilder generalCondition = new BooleanBuilder();

		Map<EntityType, Collection<Long>> refmap = mapScopeByType();
		Set<SubScope> subscopes = findQuerySubScopes();

		if (subscopes.contains(SubScope.TEST_CASE)) {
			BooleanBuilder testcaseClause = whereClauseForTestcases(refmap);
			generalCondition.and(testcaseClause);
		}

		if (subscopes.contains(SubScope.REQUIREMENT)) {
			BooleanBuilder requirementClause = whereClauseForRequirements(refmap);
			generalCondition.and(requirementClause);
		}

		if (subscopes.contains(SubScope.CAMPAIGN)) {
			BooleanBuilder campaignClause = whereClauseForCampaigns(refmap);
			generalCondition.and(campaignClause);
		}

		hibQuery.where(generalCondition);

	}


	private BooleanBuilder whereClauseForTestcases(Map<EntityType, Collection<Long>> refmap) {

		BooleanBuilder builder = new BooleanBuilder();
		Collection<Long> ids;
		QTestCase testCase = QTestCase.testCase;

		// project
		ids = refmap.get(PROJECT);
		if (notEmpty(ids)) {
			builder.or(testCase.project.id.in(ids));
		}

		// library
		ids = refmap.get(TEST_CASE_LIBRARY);
		if (notEmpty(ids)) {
			builder.or(testCase.project.testCaseLibrary.id.in(ids));
		}

		// test case and test case folders
		ids = fetchForTypes(refmap, TEST_CASE, TEST_CASE_FOLDER);
		if (notEmpty(ids)) {
			QTestCasePathEdge edge = QTestCasePathEdge.testCasePathEdge;

			ExtendedHibernateQuery<QTestCasePathEdge> subq = new ExtendedHibernateQuery<>();
			subq.select(Expressions.constant(1))
				.from(edge)
				.where(edge.ancestorId.in(ids))
				.where(testCase.id.eq(edge.descendantId));

			Predicate predicate = Expressions.predicate(Ops.EXISTS, subq);

			builder.or(predicate);
		}

		return builder;

	}

	private BooleanBuilder whereClauseForRequirements(Map<EntityType, Collection<Long>> refmap) {

		BooleanBuilder builder = new BooleanBuilder();
		Collection<Long> ids;
		QRequirement requirement = QRequirement.requirement;

		// project
		ids = refmap.get(PROJECT);
		if (notEmpty(ids)) {
			builder.or(requirement.project.id.in(ids));
		}

		// library
		ids = refmap.get(REQUIREMENT_LIBRARY);
		if (notEmpty(ids)) {
			builder.or(requirement.project.requirementLibrary.id.in(ids));
		}

		// requirement and requirement folders
		ids = fetchForTypes(refmap, REQUIREMENT, REQUIREMENT_FOLDER);
		if (notEmpty(ids)) {
			QRequirementPathEdge edge = QRequirementPathEdge.requirementPathEdge;

			ExtendedHibernateQuery<QRequirementPathEdge> subq = new ExtendedHibernateQuery<>();
			subq.select(Expressions.constant(1))
				.from(edge)
				.where(edge.ancestorId.in(ids))
				.where(requirement.id.eq(edge.descendantId));

			Predicate predicate = Expressions.predicate(Ops.EXISTS, subq);

			builder.or(predicate);
		}

		return builder;

	}


	private BooleanBuilder whereClauseForCampaigns(Map<EntityType, Collection<Long>> refmap) {

		BooleanBuilder builder = new BooleanBuilder();
		Collection<Long> ids;
		QCampaign campaign = QCampaign.campaign;
		QIteration iteration = QIteration.iteration;

		// project
		ids = refmap.get(PROJECT);
		if (notEmpty(ids)) {
			builder.or(campaign.project.id.in(ids));
		}

		// library
		ids = refmap.get(CAMPAIGN_LIBRARY);
		if (notEmpty(ids)) {
			builder.or(campaign.project.requirementLibrary.id.in(ids));
		}

		// requirement and requirement folders
		ids = fetchForTypes(refmap, CAMPAIGN, CAMPAIGN_FOLDER);
		if (notEmpty(ids)) {
			QCampaignPathEdge edge = QCampaignPathEdge.campaignPathEdge;

			ExtendedHibernateQuery<QCampaignPathEdge> subq = new ExtendedHibernateQuery<>();
			subq.select(Expressions.constant(1))
				.from(edge)
				.where(edge.ancestorId.in(ids))
				.where(campaign.id.eq(edge.descendantId));

			Predicate predicate = Expressions.predicate(Ops.EXISTS, subq);

			builder.or(predicate);
		}

		// and also, iterations
		ids = fetchForTypes(refmap, ITERATION);
		if (notEmpty(ids)) {
			builder.or(iteration.id.in(ids));
		}

		return builder;

	}


	// ************************ utilities ******************************


	/*
	 * This method transform a list of item that pair an id with a type,
	 * into a map that map a type to a list of id.
	 */
	private Map<EntityType, Collection<Long>> mapScopeByType() {

		Map<EntityType, Collection<Long>> map = new EnumMap<>(EntityType.class);

		for (EntityReference ref : scope) {
			EntityType type = ref.getType();
			Collection<Long> list = map.get(type);
			if (list == null) {
				list = new ArrayList<>();
				map.put(type, list);
			}
			list.add(ref.getId());
		}

		return map;


	}

	private Collection<Long> fetchForTypes(Map<EntityType, Collection<Long>> map, EntityType... types) {
		Collection<Long> result = new ArrayList<>();
		for (EntityType type : types) {
			Collection<Long> ids = map.get(type);
			if (ids != null) {
				result.addAll(ids);
			}
		}
		return result;
	}


	// A Scope is relevant if the main query and the
	// Scope have at least one subscope in common
	private boolean isScopeRelevant() {

		Set<SubScope> querySubscopes = findQuerySubScopes();

		Set<EntityType> scopeTypes = findEntitiesFromScope();

		for (EntityType type : scopeTypes) {
			SubScope typeScope = toSubScope(type);
			if (querySubscopes.contains(typeScope)) {
				return true;
			}
		}

		return false;
	}


	private boolean notEmpty(Collection<?> collection) {
		return collection != null && !collection.isEmpty();
	}

	private void addImpossibleCondition() {
		Expression<?> zero = Expressions.constant(0);
		Expression<?> one = Expressions.constant(1);
		Predicate impossible = Expressions.predicate(Ops.EQ, zero, one);
		hibQuery.where(impossible);
	}

	// *********************** subscope section *************************

	private enum SubScope {
		TEST_CASE("TEST_CASE_ID"), // TEST_CASE_ID or whichever column that makes sure that table TEST_CASE will be joined on
		REQUIREMENT("REQUIREMENT_ID"), // ditto for requirement
		CAMPAIGN("CAMPAIGN_ID");    // ditto for campaign

		private String requiredColumn;

		SubScope(String column) {
			this.requiredColumn = column;
		}

		private String getRequiredColumn() {
			return requiredColumn;
		}
	}


	private SubScope toSubScope(EntityType type) {
		SubScope subscope;
		switch (type) {
			case REQUIREMENT_LIBRARY:
			case REQUIREMENT_FOLDER:
			case REQUIREMENT:
				subscope = SubScope.REQUIREMENT;
				break;
			case TEST_CASE_LIBRARY:
			case TEST_CASE_FOLDER:
			case TEST_CASE:
				subscope = SubScope.TEST_CASE;
				break;
			case CAMPAIGN_LIBRARY:
			case CAMPAIGN_FOLDER:
			case CAMPAIGN:
			case ITERATION:
				subscope = SubScope.CAMPAIGN;
				break;
			default:
				throw new IllegalArgumentException(type.toString() + " is not legal as a chart perimeter.");
		}

		return subscope;
	}

	private Set<SubScope> findQuerySubScopes() {

		Set<SubScope> subScopes = new HashSet<>();

		Set<InternalEntityType> targets = chartQuery.getTargetEntities();

		for (InternalEntityType type : targets) {
			switch (type) {

				case REQUIREMENT:
				case REQUIREMENT_VERSION:
					subScopes.add(SubScope.REQUIREMENT);
					break;

				case TEST_CASE:
					subScopes.add(SubScope.TEST_CASE);
					break;

				case CAMPAIGN:
				case ITERATION:
				case ITEM_TEST_PLAN:
				case EXECUTION:
				case ISSUE:
					subScopes.add(SubScope.CAMPAIGN);
					break;

				// default is probably nothing related : User, Milestone etc
				default:
					break;
			}

		}

		return subScopes;
	}


	/*
	 *  find which entity types were defined in the scope
	 *  Also, if the type "PROJECT" is present, we will replace it
	 *  explicitly by TEST_CASE_LIBRARY, REQUIREMENT_LIBRARY
	 *  and CAMPAIGN_LIBRARY instead (it makes things easier
	 *  later on).
	 */
	private Set<EntityType> findEntitiesFromScope() {
		Set<EntityType> scopeTypes = new HashSet<>(mapScopeByType().keySet());
		if (scopeTypes.contains(PROJECT)) {
			scopeTypes.remove(PROJECT);
			scopeTypes.addAll(Arrays.asList(TEST_CASE_LIBRARY, REQUIREMENT_LIBRARY, CAMPAIGN_LIBRARY));
		}
		return scopeTypes;
	}


	// ****************** internal util class *****************************

	// this class exists because it is too close to the DB
	// so we'll need to mock it in the tests. That's also why
	// it is not final.


	private static class ScopeUtils {
		private static final Map<EntityType, String> CLASS_NAME_BY_ENTITY = new EnumMap<>(EntityType.class);

		static {
			CLASS_NAME_BY_ENTITY.put(PROJECT, "org.squashtest.tm.domain.project.Project");
			CLASS_NAME_BY_ENTITY.put(TEST_CASE_LIBRARY, "org.squashtest.tm.domain.testcase.TestCaseLibrary");
			CLASS_NAME_BY_ENTITY.put(TEST_CASE_FOLDER, "org.squashtest.tm.domain.testcase.TestCaseLibraryNode");
			CLASS_NAME_BY_ENTITY.put(TEST_CASE, "org.squashtest.tm.domain.testcase.TestCaseLibraryNode");
			CLASS_NAME_BY_ENTITY.put(REQUIREMENT_LIBRARY, "org.squashtest.tm.domain.requirement.RequirementLibrary");
			CLASS_NAME_BY_ENTITY.put(REQUIREMENT_FOLDER, "org.squashtest.tm.domain.requirement.RequirementLibraryNode");
			CLASS_NAME_BY_ENTITY.put(REQUIREMENT, "org.squashtest.tm.domain.requirement.RequirementLibraryNode");
			CLASS_NAME_BY_ENTITY.put(CAMPAIGN_LIBRARY, "org.squashtest.tm.domain.campaign.CampaignLibrary");
			CLASS_NAME_BY_ENTITY.put(CAMPAIGN_FOLDER, "org.squashtest.tm.domain.campaign.CampaignLibraryNode");
			CLASS_NAME_BY_ENTITY.put(CAMPAIGN, "org.squashtest.tm.domain.campaign.CampaignLibraryNode");
			CLASS_NAME_BY_ENTITY.put(ITERATION, "org.squashtest.tm.domain.campaign.Iteration");
		}

		private static final String READ = Authorizations.READ;
		private static final String ROLE_ADMIN = Authorizations.ROLE_ADMIN;
		private PermissionEvaluationService permissionService;
		private EntityManager em;

		ScopeUtils(EntityManager entityManager, PermissionEvaluationService permService) {
			super();
			this.permissionService = permService;
			this.em = entityManager;
		}

		boolean checkPermissions(EntityReference ref) {
			String classname = classname(ref);
			Long id = ref.getId();
			return permissionService.hasRoleOrPermissionOnObject(ROLE_ADMIN, READ, id, classname);
		}


		ColumnPrototype findColumnPrototype(String colName) {
			Query q = getSession().createQuery("select p from ColumnPrototype p where p.label = :label");
			q.setParameter("label", colName);
			return (ColumnPrototype) q.uniqueResult();
		}


		private Session getSession() {
			return em.unwrap(Session.class);
		}


		private String classname(EntityReference ref) {

			String className = CLASS_NAME_BY_ENTITY.get(ref.getType());

			if (className == null) {
				throw new IllegalArgumentException(ref.getType() + " is not a valid type for a chart perimeter. Please reconfigure the perimeter of your chart.");
			}
			return className;
		}


	}

}
