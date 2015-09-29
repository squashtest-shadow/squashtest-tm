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

import static org.squashtest.tm.domain.chart.EntityType.BUG;
import static org.squashtest.tm.domain.chart.EntityType.CAMPAIGN;
import static org.squashtest.tm.domain.chart.EntityType.EXECUTION;
import static org.squashtest.tm.domain.chart.EntityType.ITEM_TEST_PLAN;
import static org.squashtest.tm.domain.chart.EntityType.ITERATION;
import static org.squashtest.tm.domain.chart.EntityType.REQUIREMENT;
import static org.squashtest.tm.domain.chart.EntityType.REQUIREMENT_VERSION;
import static org.squashtest.tm.domain.chart.EntityType.TEST_CASE;

import java.util.Collection;

import org.squashtest.tm.domain.chart.EntityType;
import org.squashtest.tm.domain.library.structures.LibraryGraph;
import org.squashtest.tm.domain.library.structures.LibraryGraph.SimpleNode;
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
class DomainGraph extends LibraryGraph<EntityType, SimpleNode<EntityType>>{

	private DomainGraph(){
		super();

		// declare all the nodes
		SimpleNode<EntityType> campaignNode = new SimpleNode<EntityType>(CAMPAIGN);
		SimpleNode<EntityType> iterationNode = new SimpleNode<EntityType>(ITERATION);
		SimpleNode<EntityType> itemNode = new SimpleNode<EntityType>(ITEM_TEST_PLAN);
		SimpleNode<EntityType> executionNode = new SimpleNode<EntityType>(EXECUTION);
		SimpleNode<EntityType> issueNode = new SimpleNode<EntityType>(BUG);
		SimpleNode<EntityType> testcaseNode = new SimpleNode<EntityType>(TEST_CASE);
		SimpleNode<EntityType> rversionNode = new SimpleNode<EntityType>(REQUIREMENT_VERSION);
		SimpleNode<EntityType> requirementNode = new SimpleNode<EntityType>(REQUIREMENT);

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

		addEdge(itemNode, testcaseNode);
		addEdge(testcaseNode, itemNode);

		addEdge(testcaseNode, rversionNode);
		addEdge(rversionNode, testcaseNode);

		addEdge(rversionNode, requirementNode);
		addEdge(requirementNode, rversionNode);
	}


	QueryPlan getQueryPlan(DetailedChartDefinition definition){

		EntityType rootEntity = definition.getRootEntity();
		Collection<EntityType> targetEntities = definition.getTargetEntities();

		//eturn "IMPLEMENTMEEEE";
		return null;
	}

	// ********************* returned types (sort of a typedef) ************************************

	static final class QueryPlan extends LibraryTree<EntityType, TraversedEntity>{

	}

	static final class TraversedEntity extends TreeNode<EntityType, TraversedEntity>{
		@Override
		protected void updateWith(TraversedEntity newData) {
		}
	}

}
