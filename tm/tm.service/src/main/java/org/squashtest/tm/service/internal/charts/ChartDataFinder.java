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

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.chart.AttributeType;
import org.squashtest.tm.domain.chart.AxisColumn;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.DataType;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.MeasureColumn;


/**
 * <p>This is the class that will find the data matching the criteria supplied as a {@link ChartDefinition}, using the Querydsl engine.</p>
 * 
 * <h1>What is this ?</h1>
 * 
 * <p>A ChartDefinition defines what you need to formulate the query :
 * 
 * <ul>
 * 	<li>What data you want to find (the {@link MeasureColumn}s)</li>
 * 	<li>On what basis you want them (the {@link AxisColumn}s)</li>
 * 	<li>How specific you need the data to be (the {@link Filter}s)</li>
 * </ul>
 * </p>
 * 
 * <p>Based on this specification the {@link ChartDataFinder} will design the query plan and run it. The rest of this javadoc
 * is a technical documentation of its internal processes.</p>
 * 
 * <h1>Column roles</h1>
 * 
 * <p>Here we explain how these roles will be used in a query :</p>
 * 
 * <h3>Filters</h3>
 * 
 * <p>
 * 	In a query they naturally fit the role of a where clause. However a regular where clause is a simple case : sometimes you would have
 * 	it within a Having clause, and in other times it could be a whole subquery.
 * </p>
 * 
 * <h3>AxisColumns</h3>
 * 
 * <p>
 *	These columns will be grouped on (in the group by clause). They also appear in the select clause and should of course not
 *	be subject to any aggregation. In the select clause, they will appear first, and keep the same order as in the list defined in the ChartDefinition.
 * </p>
 * 
 * <h3>MeasureColumn</h3>
 * 
 * <p>
 * 	These columns appear in the select clause and will be subject to an aggregation method. The aggregation is specified in the MeasureColumn.
 * 	They appear in the select clause, after the axis columns, and keep the same order as in the list defined in the ChartDefinition.
 * </p>
 * <p>
 * 	Most of the time no specific attribute will be specified : in this case the measure column defaults to the id of the observed entity.
 * 	For instance consider a measure which aggregation is 'sum' (count). If the user is interested to know how many test cases match the given filters,
 * 	the aggregation should be made on the test case ids. However if the user picked something more specific - like the test case labels -, the semantics
 * 	becomes how many different labels exist within the test cases that match the filter. This, of course, is a problem for the tool that will design
 * 	the ChartDefinition : the current class will just create and process the query.
 * </p>
 * 
 * <h1>Query plan</h1>
 * 
 * <p>
 * 	The global query is composed of one main query and several optional subqueries depending on the filters.
 * </p>
 * 
 * <h3>Domain</h3>
 * 
 * <p>
 * 	The total domain covered by any possible ChartDefinition is the following :
 * 	Requirement &lt;-&gt; RequirementVersion &lt;-&gt; (RequirementVersionCoverage) &lt;-&gt; TestCase
 *  &lt;-&gt; Execution OR IterationTestPlanItem &lt;-&gt; Iteration &lt;-&gt; Campaign.
 *  </p>
 * 
 * 	<p>Depending on the ChartDefinition, a main query will be generated as
 * 	a subset of this domain. The specifics of its construction depend on the "Root entity", "Target entities" and
 * 	"Support entities", those concepts are defined below. </p>
 * 
 * 	<h3>Main query</h3>
 * 
 * <p>
 * 	The main building blocks that defines the main query are the following :
 * 
 * 	<ul>
 * 		<li><b>Target Entities</b> : entities on which apply at least one of the MeasureColumns, AxisColumns or Filters</li>
 * 		<li><b>Root Entity</b> : specifically, this is the Target entity referred to by the MeasureColumns. When multiple
 * 			target entities are eligible, the one with the highest MeasureColumn rank will be the Root entity.</li>
 * 		<li><b>Support Entities</b> : entities that aren't Target entities but must be joined on in order to join together all
 * 			the Target entities. For example if a ChartDefinition defines Execution as Root entity and Campaign as a TargetEntity,
 * 			then IterationTestPlanItem and Iteration are Support entities. </li>
 * 	</ul>
 * </p>
 * 
 * <p>The main query is thus defined as the minimal subset of the domain that join all the Target entities together via
 * Support Entities, starting with the Root entity. All joins in this query will be inner joins (no left nor right joins).</p>
 * 
 * 
 * <p>
 * 	The Domain defines two possible paths for the main query : TestCase &lt;-&gt; Execution and TestCase &lt;-&gt;
 * 	IterationTestPlanItem.
 * 	If this join is actually required, the path actually chosen is resolved as such :
 * 	<ul>
 * 		<li>If Execution is a Target entity but IterationTestPlanItem is not, then TestCase will join on Execution</li>
 * 		<li>In the other cases, TestCase will join on IterationTestPlanItem</li>
 * 	</ul>
 * </p>
 * 
 * <h3>Select clause generation</h3>
 * 
 * <p>
 * 	The select clause must of course contain the MeasureColumns with their appropriate aggregation function (like count(distinct ), avg(distinct ) etc).
 * 	For technical reasons they must also include the AxisColumns, because theses are the column on which a row is grouped by.
 * </p>
 * 
 * <h3>Filter application</h3>
 * 
 * <p>
 * 	Filters are restriction applied on the tuples returned by the main query.
 * 	At the atomic level a filter is a combination of a column, an comparison operator, and
 * 	one/several operands. They are translated in the appropriate Querydsl expression,
 * 	bound together by appropriate logical operators then inserted in the main query.
 * </p>
 * 
 * <p>
 * 	Mostly they are no more complex than "where" clauses, but in some cases
 * 	a subquery is required. Filters are treated according to the following process :
 * 
 *  <ol>
 *  	<li>Filters are first grouped by Target Entity</li>
 *  	<li>Filters from each groups are then combined in a logical combination</li>
 *  	<li>Depending on the filters composing the group, a strategy is resolved :
 *  		<ul>
 *  			<li>either the expression is inlined as a where clause in the main query,</li>
 *  			<li>either it will be part of a subquery</li>
 *  		 </ul>
 *  	</li>
 *  </ol>
 * </p>
 * 
 * 
 * <h4>logical combination</h4>
 * 
 * <p>
 * 	Each Filter apply on one column (eg, TestCase.label). Typically, multiple filters will apply on several
 * 	columns. However one can stack multiple Filters on the same column, (eg TestCase.label = 'bob', TestCase.label = 'mike').
 * </p>
 * 
 * <p>
 * 	For each entity, the filters are thus combined according to the following rules :
 * 	<ul>
 * 		<li>Filters applied to the same column will be OR'ed together</li>
 * 		<li>Filters applied to different columns will be AND'ed</li>
 * 	</ul>
 *
 * </p>
 * 
 * 
 * <h4>Inlined where clause strategy</h4>
 * 
 * <p>
 * 	In the simplest cases the filters will be inlined in the main query. The decision is driven by the attribute 'attributeType' of the {@link ColumnPrototype}
 * 	referenced by the filters : if all of them are of type {@link AttributeType#ATTRIBUTE} then this strategy will be applied. If so, the main query will receive
 * 	all of them as a where clause.
 * </p>
 * 
 * <h4>Subquery strategy</h4>
 * 
 * <p>In more complex cases a subquery will be required. The decision is driven by the attribute 'attributeType' of the {@link ColumnPrototype}
 * 	referenced by the filters : if at least one of them is of type {@link AttributeType#CUF} or {@link AttributeType#CALCULATED}
 * then one/several subqueries will be used. We need them for the following reasons :
 * 
 * 	<ol>
 * 		<li>Custom fields : joining on them in the main query would cause massive tuples growth and headaches about how grouping on what</li>
 * 		<li>Aggregation operations (calculated attributes) : count(), avg() etc + Having clauses would be incorrect here because the result would be affected by the
 * 			other filters applied on the main query.</li>
 * 	</ol>
 * 
 * </p>
 * 
 * <p>
 * 	Subqueries have them own Query plan. We won't study the details here because it depends on the Target entity under process.
 * 	Also, unlike the main query - which can be computed entirely with first-class citizen elements like Enums - ,
 * 	the subquery plan is data-driven (especially for the Calculated attributes) : each ColumnPrototype will be identified and
 * 	have a custom treatment, so you'll have to read the code on that.
 * </p>
 * 
 * <h3>Grouping</h3>
 * 
 * <p>
 * 	Data will be grouped on each {@link AxisColumn} in the given order. Special care is given for columns of
 * 	type {@link DataType#DATE} : indeed the desired level of aggregation may be day, month etc. We must be watchful
 * 	of not grouping together every month of December accross the years. For this reason data grouped by Day will
 * 	actually grouped by (year,month,day). Same goes for grouping by month, which actually mean grouping by (year,month)
 * </p>
 * 
 * <h1>Result </h1>
 * 
 * <p>
 * 	The result will be an array of array of Object. Each row represents a tuple of (x+m) cells, where x = card(AxisColumn)
 * 	and y = card(MeasureColumn). The first batch of cells are those of the AxisColumns, the second batch are those of the
 * 	MeasureColumns.
 * </p>
 * 
 * <p>
 * 	Note : a more appropriate representation would be one serie per MeasureColumn, with each tuple made of the x axis cells
 * 	and 1 measure cell. However we prefer to remain agnostic on how the resultset will be interpreted : series might not
 * 	be the preferred way to consume the data after all.
 * </p>
 * 
 * <h1>Scope and ACLs</h1>
 * 
 * <p>
 * 	Before any query is ran nor filter applied, an implicit filter will be processed : the Scope.
 * 	The Scope is the exhaustive list of Root Entity ids, and is added to the filters targeting Root entity.
 * 	It is the conjunction of :
 * 	<ul>
 * 		<li>the content of the projects/folders selected by the user <b>who designed</b> the ChartDefinition</li>
 * 		<li>the nodes that can actually be READ by the user <b>who is running</b> the ChartDataFinder</li>
 * 	</ul>
 * 
 * 	Which may well end up with no data available in the result set after all.
 * </p>
 * 
 * <p>
 * 	The Scope is treated separately because it is a filter of organizational nature (how the entities are
 * 	organized and who may access them), whereas the other filters are more related to business attributes of
 * 	those entities.
 * </p>
 * 
 * <p>
 * 	The Scope is computed before the Main Query plan is drawn :
 * 	<ul>
 * 		<li>The exhaustive list depending on the selection designer selection is first determined,</li>
 * 		<li>This list is then filtered by the Acl services</li>
 * </ul>
 * </p>
 * @author bsiri
 *
 */
@Component
public class ChartDataFinder {

	@Inject
	private SessionFactory sessionFactory;


	Object[][] findData(ChartDefinition definition){



		return null;
	}



	/*
	 * That method initiates the query. The base query simply sets which tables will participate in the query and
	 * formulate all the required joins.
	 */
	private Object createBaseQuery(ChartDefinition definition){

		return null;

	}



}
