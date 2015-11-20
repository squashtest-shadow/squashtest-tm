/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
import static org.squashtest.tm.domain.EntityType.REQUIREMENT;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT_FOLDER;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT_LIBRARY;
import static org.squashtest.tm.domain.EntityType.TEST_CASE;
import static org.squashtest.tm.domain.EntityType.TEST_CASE_FOLDER;
import static org.squashtest.tm.domain.EntityType.TEST_CASE_LIBRARY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.collections.map.MultiValueMap;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.Operation;
import org.squashtest.tm.service.campaign.CampaignLibraryFinderService;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService;

/**
 * <p>
 * 	This class will stuff a DetailedChartQuery with transient additional filters on which parts of the global library should be considered.
 *	There is two main modes :
 *
 *	<ul>
 *		<li>
 *			the "blanket project mode" : every and all test cases, requirement, campaign and iterations that
 *			are part of the main query must belong to that project.
 *		</li>
 *
 *		<li>
 *			the "custom mode" : the (primary) axis of the chart must belong to the scope. It can span
 *			across multiple projects, however other support and target entities will not be filtered at all.
 *		</li>
 *	</ul>
 *	
 * <p>
 * 
 * Only entities that belong to the main query are filtered that way. Entities part of subqueries won't be. We don't want to : that's why
 * they are in a separated subquery in the first place.
 *
 * In order to prevent to force a scope on entities that are not queried on, we check that the scope is actually relevant.
 * 
 * Thus we look for a match the involved entities (that is, the root plus target entities of the query) and those defined in the scope.
 * For this purpose the blanket project scope will count as test case library, requirement library and campaign library.
 * 
 * We first sort the entities involved in the chart in three sub scopes :
 * <ul>
 * 	<li>test case scope : test cases</li>
 * 	<li>requirement scope : requirement and requirement version</li>
 * 	<li>campaign scope : campaign, iteration</li>
 * </ul>
 * 
 * Same goes for the entities on which a scope is defined :
 * 
 * <ul>
 * 	<li>test case scope : test case library, test case folders, test cases</li>
 * 	<li>requirement scope : requirement library, requirement folders requirement, requirement version</li>
 * 	<li>campaign scope : campaign library, campaign folders, campaigns, iterations</li>
 * </ul>
 * 
 * If a sub scope is represented in both cases, that sub scope will be actually accounted for.
 * 
 * Last, the nodes are also checked against the ACL to ensure the user can actually read them.
 * 
 * </p>
 *
 *	<p>
 *	Once this is done, the corresponding filters are added to the detailed chart query.
 *
 *</p>
 *
 * @author bsiri
 *
 */
@Component()
@Scope("prototype")
class ScopePlanner {

	private static final long NONEXISTANT_PROJECT = -99999l;

	// infrastructure
	@Inject
	private SessionFactory sessionFactory;

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private TestCaseLibraryFinderService tcFinder;

	@Inject
	private RequirementLibraryFinderService rFinder;

	@Inject
	private CampaignLibraryFinderService cFinder;

	// work variables
	private DetailedChartQuery chartQuery;

	private List<EntityReference> scope;

	private ScopeUtils utils;	// created with @PostContruct

	// ************************ build the tool ******************************

	ScopePlanner(){
		super();
	}


	protected void setChartQuery(DetailedChartQuery chartQuery) {
		this.chartQuery = chartQuery;
	}

	protected void setScope(List<EntityReference> scope) {
		this.scope = scope;
	}

	@PostConstruct
	void afterPropertiesSet(){
		utils = new ScopeUtils(sessionFactory, permissionService);
	}

	// *********************** actual work **********************************


	void appendScopeFilters(){

		// early exit if empty
		if ( scope == null || scope.isEmpty() ){
			return;
		}

		Set<Filter> filters;
		if (isBlanketProjectMode()){
			filters = generateBlanketFilters();
		}
		else{
			filters = generateSpecificFilters();
		}

		// last : add the filters and recompute the target entities
		chartQuery.setScopeFilters(filters);
		chartQuery.computeTargetEntities();

	}



	private Set<Filter> generateBlanketFilters(){

		Set<Filter> filters = new HashSet<>();

		EntityReference projRef = scope.get(0);

		// security check
		// if project cannot be read we replace it by a fake reference
		// that (hopefully) never match anything
		if (! utils.canReadProject(projRef)){
			projRef = new EntityReference(EntityType.PROJECT, NONEXISTANT_PROJECT);
		}

		// now we can work
		// we must be careful to include filters for a subscope only if the
		// query has entities that belongs to it
		Set<SubScope> querySubScopes = findQuerySubScopes();

		for (SubScope subScope : querySubScopes){

			// use the column TEST_CASE_PROJECT, CAMPAIGN_PROJECT or REQUIREMENT_PROJECT
			String colName = subScope.toString()+"_PROJECT";	// booooo

			Filter f = utils.createFilter(colName, projRef.getId());
			filters.add(f);
		}

		return filters;
	}



	// shitty code hard to factor in further
	private Set<Filter> generateSpecificFilters(){

		Set<Filter> filters = new HashSet<>();

		Set<SubScope> querySubScopes = findQuerySubScopes();

		MultiValueMap refmap = aggregateReferences();

		if (querySubScopes.contains(SubScope.TEST_CASE)){
			Collection<Long> libIds = fetchForTypes(refmap, TEST_CASE_LIBRARY);
			Collection<Long> nodeIds = fetchForTypes(refmap, TEST_CASE, TEST_CASE_FOLDER);

			if (nonEmpty(libIds, nodeIds)){
				Collection<Long> tcIds = tcFinder.findTestCaseIdsFromSelection(libIds, nodeIds);
				Filter f = utils.createFilter("TEST_CASE_ID", tcIds.toArray(new Long[]{}));
				filters.add(f);
			}
		}

		if (querySubScopes.contains(SubScope.REQUIREMENT)){
			Collection<Long> libIds = fetchForTypes(refmap, REQUIREMENT_LIBRARY);
			Collection<Long> nodeIds = fetchForTypes(refmap, REQUIREMENT, REQUIREMENT_FOLDER);

			if (nonEmpty(libIds, nodeIds)){
				Collection<Long> rIds = rFinder.findRequirementIdsFromSelection(libIds, nodeIds);
				Filter f = utils.createFilter("REQUIREMENT_ID", rIds.toArray(new Long[]{}));
				filters.add(f);
			}
		}

		if (querySubScopes.contains(SubScope.CAMPAIGN)){
			// apply filters on campaigns
			Collection<Long> libIds = fetchForTypes(refmap, CAMPAIGN_LIBRARY);
			Collection<Long> nodeIds = fetchForTypes(refmap, CAMPAIGN, CAMPAIGN_FOLDER);

			if (nonEmpty(libIds, nodeIds)){
				Collection<Long> cIds = cFinder.findCampaignIdsFromSelection(libIds, nodeIds);
				Filter f = utils.createFilter("CAMPAIGN_ID", cIds.toArray(new Long[]{}));
				filters.add(f);
			}

			// same for iterations. One must also check for read permission
			// because we won't call a service this time.
			// also, when time's for QueryPlanning we force the inner join
			// on iterations
			Collection<Long> iterIds = fetchForTypes(refmap, ITERATION);
			if (! iterIds.isEmpty()){
				iterIds = utils.filterIterations(iterIds);
				Filter f = utils.createFilter("ITERATION_ID", iterIds.toArray(new Long[]{}));
				filters.add(f);
			}
		}

		return filters;

	}


	// ************************ utilities ******************************


	private boolean isBlanketProjectMode(){
		return (
				scope.size() == 1 &&
				scope.get(0).getType() == EntityType.PROJECT
				);
	}

	private enum SubScope{
		TEST_CASE,
		REQUIREMENT,
		CAMPAIGN,
		NONE;
	}


	private Set<SubScope> findQuerySubScopes(){

		Set<SubScope> subScopes = new HashSet<>();

		Set<InternalEntityType> targets = chartQuery.getTargetEntities();

		for (InternalEntityType type : targets){
			switch(type){

			case REQUIREMENT:
			case REQUIREMENT_VERSION :
				subScopes.add(SubScope.REQUIREMENT);
				break;

			case TEST_CASE :
				subScopes.add(SubScope.TEST_CASE);
				break;

			case CAMPAIGN :
			case ITERATION :
			case ITEM_TEST_PLAN :
			case EXECUTION :
			case ISSUE:
				subScopes.add(SubScope.CAMPAIGN);
				break;

				// default is probably nothing related : User, Milestone etc
			default :
				break;
			}

		}

		return subScopes;
	}


	private MultiValueMap aggregateReferences(){

		MultiValueMap organized = new MultiValueMap();

		for (EntityReference ref : scope){
			organized.put(ref.getType(), ref.getId() );
		}

		return organized;

	}

	private Collection<Long> fetchForTypes(MultiValueMap map, EntityType... types){
		Collection<Long> result = new ArrayList<>();
		for (EntityType type : types){
			Collection<Long> ids = map.getCollection(type);
			if (ids != null){
				result.addAll(ids);
			}
		}
		return result;
	}

	private boolean nonEmpty(Collection<?>...ids){
		boolean empty = true;
		for (Collection<?> col : ids){
			empty = empty && col.isEmpty();
		}
		return ! empty;
	}



	// ****************** internal util class *****************************

	// this class exists because it is too close to the DB
	// so we'll need to mock it in the tests. That's also why
	// it is not final.


	private static class ScopeUtils{

		private PermissionEvaluationService permissionService;
		private SessionFactory sessionFactory;

		ScopeUtils(SessionFactory sessionFactory, PermissionEvaluationService permService) {
			super();
			this.permissionService = permService;
			this.sessionFactory = sessionFactory;
		}

		Filter createFilter(String colName, Long... ids){
			ColumnPrototype proto = findColumnPrototype(colName);

			Filter f = new Filter();
			f.setColumn(proto);
			f.setOperation(Operation.IN);

			f.setValues(toString(ids));

			return f;
		}

		ColumnPrototype findColumnPrototype(String colName){
			Query q = getSession().createQuery("select p from ColumnPrototype p where p.label = :label");
			q.setParameter("label", colName);
			return (ColumnPrototype)q.uniqueResult();
		}

		boolean canReadProject(EntityReference project){
			return permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "READ", project.getId(), "org.squashtest.tm.domain.project.Project");
		}

		List<Long> filterIterations(Collection<Long> iterationIds){
			List<Long> result = new ArrayList<>();
			for (Long id : iterationIds){
				if (permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "READ", id, "org.squashtest.tm.domain.campaign.Iteration")){
					result.add(id);
				}
			}
			return result;
		}


		private List<String> toString(Long[] ids){
			List<String> strIds = new ArrayList<>(ids.length);
			for (Long i : ids){
				strIds.add(i.toString());
			}
			return strIds;
		}


		private Session getSession(){
			return sessionFactory.getCurrentSession();
		}


	}

}
