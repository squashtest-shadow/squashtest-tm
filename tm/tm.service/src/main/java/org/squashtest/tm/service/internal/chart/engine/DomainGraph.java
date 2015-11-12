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

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
 * @author bsiri
 *
 */
/*
 * PLEASE UPDATE THE DOCUMENTATION IF THE DOMAIN CHANGES !
 */
class DomainGraph extends LibraryGraph<InternalEntityType, DomainGraph.TraversableEntity>{

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

	static QueryPlan getQueryPlan(DetailedChartQuery definition){

		DomainGraph domain = new DomainGraph(definition);

		QueryPlan plan = domain.morphToQueryPlan();

		plan.trim(definition);

		return plan;

	}


	// **************************** under the hood ****************************

	private DetailedChartQuery definition;

	private DomainGraph(DetailedChartQuery def){
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

		// this graph consider that each relation is navigable both ways.
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
	}


	private void addEdge(TraversableEntity src, TraversableEntity dest, String attribute){
		addEdge(src, dest);

		PlannedJoin join = new PlannedJoin(src.getKey(), dest.getKey(), attribute);
		src.addJoinInfo(dest.getKey(), join);
	}

	private void addEdge(TraversableEntity src, TraversableEntity dest, String attribute, JoinType jointype){
		addEdge(src, dest);

		PlannedJoin join = new PlannedJoin(src.getKey(), dest.getKey(), attribute, jointype);
		src.addJoinInfo(dest.getKey(), join);
	}

	@Override
	public void removeEdge(InternalEntityType src, InternalEntityType dest) {
		super.removeEdge(src, dest);
		TraversableEntity srcNode = getNode(src);
		srcNode.removeJoinInfo(dest);
	}


	/**
	 * This method should decide whether navigating from parent to child should
	 * be allowed.
	 * 
	 * @param parent
	 * @param child
	 * @return
	 */
	boolean shouldNavigate(TraversableEntity parent, TraversableEntity child){
		return true;
	}

	/**
	 *	<p>returns an exhaustive QueryPlan (it still needs to be trimmed afterward, using {@link QueryPlan#trim(DetailedChartQuery)})</p>
	 *	<p>warning : this instance of DomainGraph will be altered in the process</p>
	 * 
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
		Deque<TraversableEntity> stack = new LinkedList<>();
		stack.push(rootNode);

		// main loop
		while (! stack.isEmpty()){

			TraversableEntity currentNode = stack.pop();
			InternalEntityType currentEntity = currentNode.getKey();

			for (TraversableEntity outNode : currentNode.getOutbounds()){

				// if outNode should be accessed from current :
				if (shouldNavigate(currentNode, outNode)){

					// add the path to the plan
					TraversedEntity outTree = outNode.toTraversedEntity();
					PlannedJoin join = currentNode.getJoinInfo(outNode.getKey());
					plan.addNode(currentEntity, outTree, join);

					// update the graph : make the path one-way by removing the other way
					removeEdge(outNode.getKey(), currentNode.getKey());

					// push the out node for further processing
					stack.push(outNode);
				}
				else{
					// else none can navigate to the other
					disconnect(outNode.getKey(), currentNode.getKey());
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
	static final class TraversableEntity extends GraphNode<InternalEntityType, TraversableEntity>{

		private Map<InternalEntityType, PlannedJoin> joinInfos = new HashMap<>();

		private TraversableEntity(InternalEntityType type){
			super(type);
		}

		TraversedEntity toTraversedEntity(){
			return new TraversedEntity(key);
		}

		public String toString(){
			return key.toString();
		}

		void addJoinInfo(InternalEntityType outboundType, PlannedJoin joininfo){
			joinInfos.put(outboundType, joininfo);
		}

		void removeJoinInfo(InternalEntityType outboundType){
			joinInfos.remove(outboundType);
		}

		PlannedJoin getJoinInfo(InternalEntityType outboundType){
			return joinInfos.get(outboundType);
		}

	}


}
