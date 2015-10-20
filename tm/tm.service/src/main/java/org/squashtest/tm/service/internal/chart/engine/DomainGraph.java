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



import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.CAMPAIGN;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.EXECUTION;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.ISSUE;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.ITEM_TEST_PLAN;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.ITERATION;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.REQUIREMENT;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.REQUIREMENT_VERSION;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.REQUIREMENT_VERSION_COVERAGE;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.TEST_CASE;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.TEST_CASE_STEP;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.USER;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.MILESTONE;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.INFO_LIST_ITEM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.library.structures.GraphNode;
import org.squashtest.tm.domain.library.structures.LibraryGraph;
import org.squashtest.tm.service.internal.chart.engine.PlannedJoin.JoinType;
import org.squashtest.tm.service.internal.chart.engine.QueryPlan.TraversedEntity;

/**
 * <p>
 * That graph describe which paths can lead from one {@link EntityType} to other EntityTypes, according to the
 * business domain.
 * </p>
 * <p>
 * 	Its purpose is to provide a query plan. It is defined as the spanning tree that originates from a root entity and spreads until
 * 	every target entity is reached.
 * </p>
 * <p>Please note that, for that purpose different enum is used here : {@link InternalEntityType}.</p>
 * <p>See javadoc on ChartDataFinder for details on this. Excerpt pasted below for convenience.</p>
 * 
 * <p>
 *  <table>
 * 	<tr>
 * 		<td>Campaign</td>
 * 		<td>&lt;-&gt;</td>
 * 		<td>Iteration</td>
 * 		<td>&lt;-&gt;</td>
 * 		<td>IterationTestPlanItem</td>
 * 		<td>&lt;-&gt;</td>
 * 		<td>TestCase</td>
 * 		<td>&lt;-&gt;</td>
 * 		<td>(RequirementVersionCoverage)</td>
 * 		<td>&lt;-&gt;</td>
 * 		<td>RequirementVersion</td>
 * 		<td>&lt;-&gt; </td>
 * 		<td>Requirement</td>
 * 	</tr>
 * 	<tr>
 * 		<td>Issue</td>
 * 		<td>&lt;-&gt;</td>
 * 		<td>Execution</td>
 * 		<td>&lt;--</td>
 * 		<td>^</td>
 * 		<td></td>
 * 		<td></td>
 * 		<td></td>
 * 		<td></td>
 * 		<td></td>
 * 		<td></td>
 * 	</tr>
 * </table>
 *</p>
 *
 *<p>
 *	UPDATE : now the graph also contains more hidden entities, used in subqueries only :
 *	<ul>
 *		<li>TEST_CASE_STEP : joinable from TestCase</li>
 *		<li>INFO_LIST_ITEM : joinable from TestCase and RequirementVersion</li>
 *		<li>MILESTONE : joinable from TestCase and RequirementVersion</li>
 *		<li>USER : joinable from ItemTestPlan</li>
 *	</ul>
 *</p>
 * @author bsiri
 *
 */
/*
 * PLEASE UPDATE THE DOCUMENTATION IF THE DOMAIN CHANGES !
 */

/*
 * Implementation note : I ditched the "extends LibraryGraph" because due to ill conception this class
 * doesn't allow edges customization with metadata, sorry for the engineering fail.
 */
class DomainGraph {

	private DetailedChartQuery definition;

	private Set<TraversableEntity> nodes = new HashSet<>();

	private Collection<PreferedJoin> preferedJoins = new ArrayList<>();

	// this one is used only in "shouldNavigate" and "morphToQueryPlan()"
	private Set<InternalEntityType> visited = new HashSet<>();

	// **************************** main methods ******************************


	/*
	 * The creation of a query plan is a two step process :
	 * 
	 * 1/ transform the undirected domain graph in a directed graph (a tree), radiating from the node representing the root entity,
	 * 2/ on the result, prune the leaves until a target entity node is encountered
	 * 
	 * The result is a tree with the root entity as root node, and by walking it top-down one will find
	 * which entities are traversed from which (indicating which join should be made).
	 * 
	 */

	QueryPlan getQueryPlan(){

		DomainGraph domain = new DomainGraph(definition);

		QueryPlan plan = domain.morphToQueryPlan();

		plan.trim(definition);

		return plan;

	}

	/**
	 * 
	 * When multiple joins exist between the same source type and destination type,
	 * you may specify which one you intent to use.
	 * 
	 * @param src
	 * @param dest
	 * @param pathname
	 */
	void addPreferedJoin(EntityType src, EntityType dest, String pathname ){
		InternalEntityType internalSrc = InternalEntityType.fromDomainType(src);
		InternalEntityType internalDest = InternalEntityType.fromDomainType(dest);
		preferedJoins.add(new PreferedJoin(internalSrc, internalDest, pathname));
	}


	// **************************** under the hood ****************************

	DomainGraph(DetailedChartQuery def){
		super();

		this.definition = def;

		// declare all the nodes
		TraversableEntity campaignNode = new TraversableEntity(CAMPAIGN);
		TraversableEntity iterationNode = new TraversableEntity(ITERATION);
		TraversableEntity itemNode = new TraversableEntity(ITEM_TEST_PLAN);
		TraversableEntity executionNode = new TraversableEntity(EXECUTION);
		TraversableEntity issueNode = new TraversableEntity(ISSUE);
		TraversableEntity testcaseNode = new TraversableEntity(TEST_CASE);
		TraversableEntity reqcoverageNode = new TraversableEntity(REQUIREMENT_VERSION_COVERAGE);
		TraversableEntity rversionNode = new TraversableEntity(REQUIREMENT_VERSION);
		TraversableEntity requirementNode = new TraversableEntity(REQUIREMENT);

		// nodes for "hidden" entities, normally attainable from calculated columns only

		TraversableEntity teststepNode = new TraversableEntity(TEST_CASE_STEP);
		TraversableEntity userNode = new TraversableEntity(USER);
		TraversableEntity infoitemNode= new TraversableEntity(INFO_LIST_ITEM);
		TraversableEntity milestoneNode = new TraversableEntity(MILESTONE);

		// add them all
		nodes.addAll(Arrays.asList(new TraversableEntity[]{
				campaignNode, iterationNode, itemNode, executionNode, issueNode, testcaseNode,
				reqcoverageNode, rversionNode, requirementNode, teststepNode,
				userNode, infoitemNode, milestoneNode
		}));


		// this graph consider that most relation is navigable both ways.

		addEdge(campaignNode, iterationNode, "iterations");
		addEdge(iterationNode, campaignNode, "campaign");

		addEdge(iterationNode, itemNode, "testPlans");
		addEdge(itemNode, iterationNode, "iteration");

		addEdge(itemNode, executionNode, "executions");
		addEdge(executionNode, itemNode, "testPlan");

		addEdge(executionNode, issueNode, "issues");
		addEdge(issueNode, executionNode, "execution");

		addEdge(itemNode, testcaseNode, "referencedTestCase");
		addEdge(testcaseNode, itemNode,  "referencedTestCase", JoinType.WHERE);

		addEdge(testcaseNode, reqcoverageNode, "requirementVersionCoverages");
		addEdge(reqcoverageNode, testcaseNode, "verifyingTestCase");

		addEdge(reqcoverageNode, rversionNode, "verifiedRequirementVersion");
		addEdge(rversionNode, reqcoverageNode, "requirementVersionCoverages");

		addEdge(rversionNode, requirementNode, "requirement");
		addEdge(requirementNode, rversionNode, "versions");


		// the 'hidden' entities relations.

		addEdge(rversionNode, milestoneNode, "milestones");
		addEdge(milestoneNode, rversionNode, "requirementVersions");

		addEdge(rversionNode, infoitemNode, "category");

		addEdge(testcaseNode, milestoneNode, "milestones");
		addEdge(milestoneNode, testcaseNode, "testCases");

		addEdge(testcaseNode, teststepNode, "steps");

		addEdge(testcaseNode, infoitemNode, "nature");
		addEdge(testcaseNode, infoitemNode, "type");

		addEdge(itemNode, userNode, "user");


	}


	private void addEdge(TraversableEntity src, TraversableEntity dest, String attribute){
		PlannedJoin join = new PlannedJoin(src.type(), dest.type(), attribute);
		src.addJoinInfo(join);
	}

	private void addEdge(TraversableEntity src, TraversableEntity dest, String attribute, JoinType jointype){
		PlannedJoin join = new PlannedJoin(src.type(), dest.type(), attribute, jointype);
		src.addJoinInfo(join);
	}


	private TraversableEntity getNode(InternalEntityType type){
		for (TraversableEntity node : nodes){
			if (node.type().equals(type)){
				return node;
			}
		}
		return null;
	}

	/**
	 * This method should decide whether navigating from parent to child should
	 * be allowed.
	 * 
	 * @return
	 */
	protected boolean shouldNavigate(PlannedJoin join){
		// first : check that the end node wasn't visited already
		InternalEntityType dest = join.getDest();
		if (visited.contains(dest)){
			return false;
		}
		return true;
	}

	/**
	 *	<p>returns an exhaustive QueryPlan (it still needs to be trimmed afterward, using {@link QueryPlan#trim(DetailedChartQuery)})</p>
	 *	<p>warning : this instance of DomainGraph will be altered in the process</p>
	 * 
	 */

	/*
	 * Implementation : breadth first, nodes can be visited only once
	 */

	/*
	 * Developper from the Future, read this !
	 * 
	 * Step 1 details :
	 * 	by default any outbound node from the root entity (and thereafter) is legit for joining. However
	 * 	in some cases it might not be acceptable in the future : the domain graph could contain loops,
	 *  which means that many directed paths are possible between two nodes.
	 * 
	 *  For instance in the future one could join TestCase with Execution and/or TestCase with Item and/or Item with
	 *  Execution : we don't want all three happen at the same time. Thus, we need an additional validation step
	 *  to prevent this.
	 * 
	 *  This step is included in the process, and returns always true for now.
	 * 
	 */
	private QueryPlan morphToQueryPlan(){

		QueryPlan plan = new QueryPlan();

		InternalEntityType rootType = definition.getRootEntity();

		TraversableEntity rootNode = getNode(rootType);

		// init the query plan
		TraversedEntity treeRoot = rootNode.toTraversedEntity();
		plan.addNode(null, treeRoot);

		// init the loop
		Queue<TraversableEntity> queue = new LinkedList<>();
		queue.add(rootNode);

		// main loop
		while (! queue.isEmpty()){

			TraversableEntity currentNode = queue.remove();
			InternalEntityType currentEntity = currentNode.type();

			for (Iterator<PlannedJoin> iter = currentNode.getJoinInfos().iterator();  iter.hasNext();) {

				PlannedJoin currentJoin = iter.next();

				TraversableEntity outNode = getNode(currentJoin.getDest());

				// if this edge should be navigated on
				if (shouldNavigate(currentJoin)){

					// add the path to the plan
					TraversedEntity outTree = outNode.toTraversedEntity();
					plan.addNode(currentEntity, outTree, currentJoin);

					// update the graph : make the path one-way by removing the other way
					outNode.removeEdges(currentEntity);

					// push the out node for further processing
					queue.add(outNode);
					visited.add(outNode.type());
				}
				else{
					// forget it then
					iter.remove();
				}
			}

		}

		return plan;
	}



	// ********************* returned types (sort of a typedef) ************************************

	/**
	 * A node in the Domain graph : it represents an entity type (table) that can potentially be traversed
	 * 
	 * @author bsiri
	 *
	 */
	static final class TraversableEntity{

		private InternalEntityType type;

		// define which joins are available from this entity
		private Collection<PlannedJoin> joinInfos = new ArrayList<>();

		private TraversableEntity(InternalEntityType type){
			this.type = type;
		}

		TraversedEntity toTraversedEntity(){
			return new TraversedEntity(type);
		}

		public String toString(){
			return type.toString();
		}

		void addJoinInfo(PlannedJoin joininfo){
			joinInfos.add(joininfo);
		}

		void removeJoinInfo(PlannedJoin joininfo){
			joinInfos.remove(joininfo);
		}

		Collection<PlannedJoin> getJoinInfos(){
			return joinInfos;
		}

		void clearJoins(){
			joinInfos.clear();
		}

		void removeEdges(InternalEntityType dest){
			Iterator<PlannedJoin> iter = joinInfos.iterator();
			while (iter.hasNext()){
				PlannedJoin nextJoin = iter.next();
				if (nextJoin.getDest().equals(dest)){
					iter.remove();
				}
			}
		}

		InternalEntityType type(){
			return type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			TraversableEntity other = (TraversableEntity) obj;
			if (type != other.type) {
				return false;
			}
			return true;
		}

	}

	private static final class PreferedJoin{
		private final InternalEntityType src;
		private final InternalEntityType dest;
		private final String pathname;

		private PreferedJoin(InternalEntityType src, InternalEntityType dest, String pathname){
			this.src = src;
			this.dest = dest;
			this.pathname = pathname;
		}
	}


}
