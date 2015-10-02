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
package org.squashtest.tm.service.internal.charts;

import static org.squashtest.tm.domain.EntityType.CAMPAIGN;
import static org.squashtest.tm.domain.EntityType.EXECUTION;
import static org.squashtest.tm.domain.EntityType.ISSUE;
import static org.squashtest.tm.domain.EntityType.ITEM_TEST_PLAN;
import static org.squashtest.tm.domain.EntityType.ITERATION;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT_VERSION;
import static org.squashtest.tm.domain.EntityType.TEST_CASE;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.library.structures.GraphNode;
import org.squashtest.tm.domain.library.structures.LibraryGraph;
import org.squashtest.tm.domain.library.structures.LibraryTree;
import org.squashtest.tm.domain.library.structures.TreeNode;

/**
 * <p>
 * That graph describe which paths can lead from one {@link EntityType} to other EntityTypes, according to the
 * business domain.
 * </p>
 * <p>
 * 	Its purpose is to provide a query plan. It is defined as the spanning tree that originates from a root entity and spreads until
 * 	every target entity is reached.
 * </p>
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
class DomainGraph extends LibraryGraph<EntityType, DomainGraph.TraversableEntity>{

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

	static QueryPlan getQueryPlan(DetailedChartDefinition definition){

		DomainGraph domain = new DomainGraph(definition);

		QueryPlan plan = domain.morphToQueryPlan();

		plan.trim(definition);

		return plan;

	}


	// **************************** under the hood ****************************

	private DetailedChartDefinition definition;

	private DomainGraph(DetailedChartDefinition def){
		super();

		this.definition = def;

		// declare all the nodes
		TraversableEntity campaignNode = new TraversableEntity(CAMPAIGN);
		TraversableEntity iterationNode = new TraversableEntity(ITERATION);
		TraversableEntity itemNode = new TraversableEntity(ITEM_TEST_PLAN);
		TraversableEntity executionNode = new TraversableEntity(EXECUTION);
		TraversableEntity issueNode = new TraversableEntity(ISSUE);
		TraversableEntity testcaseNode = new TraversableEntity(TEST_CASE);
		TraversableEntity rversionNode = new TraversableEntity(REQUIREMENT_VERSION);
		TraversableEntity requirementNode = new TraversableEntity(REQUIREMENT);

		// this graph consider that each relation is navigable both ways.
		addEdge(campaignNode, iterationNode);
		addEdge(iterationNode, campaignNode);

		addEdge(iterationNode, itemNode);
		addEdge(itemNode, iterationNode);

		addEdge(itemNode, executionNode);
		addEdge(executionNode, itemNode);

		addEdge(executionNode, issueNode);
		addEdge(issueNode, executionNode);

		addEdge(itemNode, testcaseNode);
		addEdge(testcaseNode, itemNode);

		addEdge(testcaseNode, rversionNode);
		addEdge(rversionNode, testcaseNode);

		addEdge(rversionNode, requirementNode);
		addEdge(requirementNode, rversionNode);
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
	 *	<p>returns an exhaustive QueryPlan (it still needs to be pruned afterward)</p>
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

		EntityType rootType = definition.getRootEntity();

		TraversableEntity rootNode = getNode(rootType);


		// the root node have no inbounds anymore
		/*	Collection<TraversableEntity> ins = new ArrayList<>(rootNode.getInbounds());
		for (TraversableEntity inNode : ins){
			inNode.disconnect(rootNode);
		}*/

		// init the query plan
		TraversedEntity treeRoot = rootNode.toTraversedEntity();
		plan.addNode(null, treeRoot);

		// init the loop
		LinkedList<TraversableEntity> stack = new LinkedList<>();
		stack.push(rootNode);

		// main loop
		while (! stack.isEmpty()){

			TraversableEntity currentNode = stack.pop();
			EntityType currentEntity = currentNode.getKey();

			for (TraversableEntity outNode : currentNode.getOutbounds()){

				// if outNode should be accessed from current :
				if (shouldNavigate(currentNode, outNode)){

					// add the path to the plan
					TraversedEntity outTree = outNode.toTraversedEntity();
					plan.addNode(currentEntity, outTree);

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
	static final class TraversableEntity extends GraphNode<EntityType, TraversableEntity>{
		private TraversableEntity(EntityType type){
			super(type);
		}

		TraversedEntity toTraversedEntity(){
			return new TraversedEntity(key);
		}

		@Override
		public String toString(){
			return key.toString();
		}

	}



	static final class QueryPlan extends LibraryTree<EntityType, TraversedEntity>{

		void trim(DetailedChartDefinition definition){

			Collection<EntityType> targets = new HashSet<>(definition.getTargetEntities());

			LinkedList<TraversedEntity> fifo = new LinkedList<>(getLeaves());

			while (! fifo.isEmpty()){

				TraversedEntity current = fifo.pop();

				if (current == null){
					continue;
				}

				EntityType curType = current.getKey();

				// 1/ if that node is not one of the targets,
				// 2/ has no children (because not yet processed, or have a target in their own children)
				// -> prune it and enqueue the parent
				if (! targets.contains(curType) && current.getChildren().isEmpty()){
					TraversedEntity parent = current.getParent();
					if (! fifo.contains(parent)){
						fifo.add(parent);
					}
					remove(curType);
				}

			}
		}

	}


	/**
	 * A node in the QueryPlan : it represents an entity type that WILL be traversed.
	 *
	 * @author bsiri
	 *
	 */
	static final class TraversedEntity extends TreeNode<EntityType, TraversedEntity>{

		private TraversedEntity(EntityType type) {
			super(type);
		}

		@Override
		protected void updateWith(TraversedEntity newData) {
			// NOOP
		}


		@Override
		public String toString(){
			return getKey().toString();
		}
	}

}
