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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.chart.ChartQuery;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.ColumnPrototypeInstance;
import org.squashtest.tm.domain.chart.DataType;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.Operation;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.infolist.InfoListItem;

import com.querydsl.core.JoinExpression;
import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Ops.AggOps;
import com.querydsl.core.types.Ops.DateTimeOps;
import com.querydsl.core.types.Ops.MathOps;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.TemplateExpression;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimpleOperation;
import com.querydsl.jpa.hibernate.HibernateQuery;

class QuerydslToolbox {

	String subContext;

	QuerydslToolbox(){
		super();
	}


	QuerydslToolbox(String subContext){
		super();
		this.subContext = subContext;
	}

	void setSubContext(String subContext) {
		this.subContext = subContext;
	}

	String getSubContext(){
		return subContext;
	}

	// ************** info retrievers ***************************

	/*
	 *	The following methods ensure that the entities are aliased appropriately
	 *	according to a context.
	 * 
	 * @param type
	 * @return
	 */
	String getQName(InternalEntityType type){
		EntityPathBase<?> path = type.getQBean();
		String name = path.getMetadata().getName();
		if (subContext == null){
			return name;
		}
		else{
			return name+"_"+subContext;
		}
	}

	EntityPathBase<?> getQBean(InternalEntityType type){
		String name = getQName(type);
		return type.getAliasedQBean(name);
	}

	EntityPathBase<?> getQBean(EntityType domainType){
		InternalEntityType type = InternalEntityType.fromDomainType(domainType);
		return getQBean(type);
	}

	EntityPathBase<?> getQBean(ColumnPrototypeInstance column){
		InternalEntityType type = InternalEntityType.fromDomainType(column.getEntityType());
		return getQBean(type);
	}


	/**
	 * Returns the aliases registered in the "from" clause of
	 * the given query
	 * 
	 * @param query
	 * @return
	 */
	Set<String> getJoinedAliases(HibernateQuery<?> query){
		AliasCollector collector = new AliasCollector();
		for (JoinExpression join : query.getMetadata().getJoins()){
			join.getTarget().accept(collector, collector.getAliases());
		}
		return collector.getAliases();
	}

	// ***************************** high level API ***********************


	Expression<?> createAsSelect(ColumnPrototypeInstance col){

		Expression<?> selectElement = null;

		ColumnPrototype proto = col.getColumn();

		switch(proto.getColumnType()){
		case ATTRIBUTE :
			selectElement = createAttributeSelect(col);
			break;

		case CALCULATED :
			EntityPathBase<?> colBean = getQBean(col);
			QueryBuilder qbuilder = createSubquery(col).asSubselectQuery().joinAxesOn(colBean);
			selectElement = qbuilder.createQuery();
			break;

		default :
			throw new IllegalArgumentException("columns of column type '"+proto.getColumnType()+"' are not yet supported");
		}

		return selectElement;
	}



	/**
	 * creates an Expression like 'baseExp' 'operation' 'operand1', 'operand2' ... suitable for a 'where' clause
	 * 
	 * @param filter
	 * @return
	 */
	BooleanExpression createAsPredicate(Filter filter){
		BooleanExpression predicate = null;

		ColumnPrototype proto = filter.getColumn();

		switch(proto.getColumnType()){
		case ATTRIBUTE :
			predicate = createAttributePredicate(filter);
			break;

		case CALCULATED :

			//create the subquery
			QueryBuilder qbuilder = createSubquery(filter).asSubwhereQuery().filterMeasureOn(filter);
			Expression<?> subquery = qbuilder.createQuery();

			// now integrate the subquery
			Expression<?> entityIdPath = idPath(filter);

			predicate = Expressions.predicate(Ops.IN, entityIdPath, subquery);

			break;

		default :
			throw new IllegalArgumentException("columns of column type '"+proto.getColumnType()+"' are not yet supported");
		}

		return predicate;
	}



	// ********************* low level API *********************


	@SuppressWarnings("rawtypes")
	PathBuilder makePath(InternalEntityType src, InternalEntityType dest, String attribute){

		Class<?> srcClass = src.getEntityClass();
		Class<?> destClass = dest.getEntityClass();
		String srcAlias = getQName(src);

		return new PathBuilder<>(srcClass, srcAlias).get(attribute, destClass);
	}


	/**
	 * Creates an expression fit for a "select" clause
	 * 
	 * @param column
	 * @return
	 */
	Expression<?> createAttributeSelect(ColumnPrototypeInstance column){
		Expression attribute = attributePath(column);
		Operation operation = column.getOperation();

		if (operation != Operation.NONE){
			attribute = applyOperation(operation, attribute);
		}

		return attribute;

	}

	BooleanExpression createAttributePredicate(Filter filter){
		DataType datatype = filter.getDataType();
		Operation operation = filter.getOperation();

		// make the expression on which the filter is applied
		Expression<?> attrExpr = attributePath(filter);

		// convert the operands
		List<Expression<?>> valExpr = makeOperands(datatype, filter.getValues());
		Expression<?>[] operands = valExpr.toArray(new Expression[]{});

		return createPredicate(operation, attrExpr, operands);
	}


	SimpleOperation<?> applyOperation(Operation operation, Expression<?> baseExp, Expression... operands){

		Operator operator = getOperator(operation);

		Expression[] expressions = prepend(baseExp, operands);

		return Expressions.operation(operator.getType(), operator, expressions);

	}

	/**
	 * creates an Expression like 'baseExp' 'operation' 'operand1', 'operand2' ...
	 * 
	 * @param filter
	 * @return
	 */
	BooleanExpression createPredicate(Operation operation, Expression<?> baseExp, Expression... operands){

		Operator operator = getOperator(operation);

		Expression[] expressions = prepend(baseExp, operands);

		return Expressions.predicate(operator, expressions);
	}

	List<Expression<?>>  createOperands(Filter filter){
		DataType type = filter.getDataType();
		List<String> values = filter.getValues();
		return makeOperands(type, values);
	}


	// ******************************* private stuffs *********************


	@SuppressWarnings("rawtypes")
	private PathBuilder makePath(Class<?> srcClass, String srcAlias, Class<?> attributeClass, String attributeAlias){
		return new PathBuilder<>(srcClass, srcAlias).get(attributeAlias, attributeClass);
	}


	/*
	 * should be invoked only on columns of AttributeType = ATTRIBUTE
	 * 
	 */
	private PathBuilder attributePath(ColumnPrototypeInstance column){

		ColumnPrototype prototype = column.getColumn();

		InternalEntityType type = InternalEntityType.fromDomainType(prototype.getEntityType());

		String alias = getQName(type);
		Class<?> clazz = type.getClass();
		String attribute = prototype.getAttributeName();
		Class<?> attributeType = classFromDatatype(prototype.getDataType());

		return makePath(clazz, alias, attributeType, attribute);

	}

	// returns the path to the ID of the entity
	private PathBuilder idPath(ColumnPrototypeInstance column){

		ColumnPrototype prototype = column.getColumn();

		InternalEntityType type = InternalEntityType.fromDomainType(prototype.getEntityType());

		String alias = getQName(type);
		Class<?> clazz = type.getClass();

		return makePath(clazz, alias, Long.class, "id");
	}



	List<Expression<?>> makeOperands(DataType type, List<String> values ){
		try{
			List<Expression<?>> expressions = new ArrayList<>(values.size());

			for (String val : values){
				Object operand;
				switch(type){
				case STRING :
					operand = val;
					break;
				case NUMERIC :
					operand = Long.valueOf(val);
					break;
				case DATE :
					operand = DateUtils.parseIso8601Date(val);
					break;
				default : throw new IllegalArgumentException("type '"+type+"' not yet supported");
				}

				expressions.add(Expressions.constant(operand));
			}

			return expressions;
		}catch(ParseException ex){
			throw new RuntimeException(ex);
		}
	}


	private QueryBuilder createSubquery(ColumnPrototypeInstance col){

		ColumnPrototype prototype = col.getColumn();
		ChartQuery queryDef = prototype.getSubQuery();
		DetailedChartQuery detailedDef = new DetailedChartQuery(queryDef);

		return new QueryBuilder(detailedDef);
	}


	private Operator getOperator(Operation operation){
		Operator operator;

		switch(operation){
		case EQUALS : operator = Ops.EQ; break;
		case LIKE : operator = Ops.LIKE; break;
		case BY_YEAR : operator = DateTimeOps.YEAR; break;
		case BY_MONTH : operator = DateTimeOps.YEAR_MONTH; break;
		case COUNT : operator = AggOps.COUNT_DISTINCT_AGG; break;
		case GREATER : operator = Ops.GT; break;
		default : throw new IllegalArgumentException("Operation '"+operation+"' not yet supported");
		}

		return operator;
	}

	private Expression[] prepend(Expression head, Expression... tail){
		Expression[] res = new Expression[tail.length+1];
		res[0] = head;
		System.arraycopy(tail, 0, res, 1, tail.length);
		return res;
	}


	private Class<?> classFromDatatype(DataType type){
		Class<?> result;

		switch(type){
		case DATE : result = Date.class; break;
		case STRING : result = String.class; break;
		case NUMERIC : result = Long.class; break;
		case EXECUTION_STATUS : result = ExecutionStatus.class; break;
		case INFO_LIST_ITEM : result = InfoListItem.class; break;

		default : throw new IllegalArgumentException("datatype '"+type+"' is not yet supported");
		}

		return result;
	}



	private static final class AliasCollector implements Visitor<Void, Set<String>>{

		private Set<String> aliases = new HashSet<>();


		@Override
		public Void visit(Constant<?> expr, Set<String> context) {
			return null;
		}

		@Override
		public Void visit(FactoryExpression<?> expr, Set<String> context) {
			return null;
		}

		@Override
		public Void visit(com.querydsl.core.types.Operation<?> expr, Set<String> context) {
			for (Expression<?> subexpr : expr.getArgs()){
				subexpr.accept(this, context);
			}
			return null;
		}

		@Override
		public Void visit(ParamExpression<?> expr, Set<String> context) {
			return null;
		}

		@Override
		public Void visit(Path<?> expr, Set<String> context) {
			PathMetadata metadata = expr.getMetadata();
			if (metadata.isRoot()){
				context.add(expr.getMetadata().getName());
			}
			else{
				metadata.getParent().accept(this, context);
			}

			return null;
		}

		@Override
		public Void visit(SubQueryExpression<?> expr, Set<String> context) {
			return null;
		}

		@Override
		public Void visit(TemplateExpression<?> expr, Set<String> context) {
			return null;
		}

		Set<String> getAliases(){
			return aliases;
		}

	}


}
