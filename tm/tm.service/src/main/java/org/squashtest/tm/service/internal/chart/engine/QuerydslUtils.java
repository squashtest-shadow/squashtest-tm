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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.DataType;
import org.squashtest.tm.domain.chart.Operation;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.infolist.InfoListItem;

import com.querydsl.core.JoinExpression;
import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Ops.AggOps;
import com.querydsl.core.types.Ops.StringOps;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.TemplateExpression;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.Ops.DateTimeOps;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.hibernate.HibernateQuery;

class QuerydslUtils {

	static QuerydslUtils INSTANCE = new QuerydslUtils();

	private QuerydslUtils(){
		super();
	}

	@SuppressWarnings("rawtypes")
	PathBuilder makePath(InternalEntityType src, InternalEntityType dest, String attribute){

		Class<?> srcClass = src.getEntityClass();
		Class<?> destClass = dest.getEntityClass();
		String srcAlias = src.getQBean().getMetadata().getName();

		return new PathBuilder<>(srcClass, srcAlias).get(attribute, destClass);
	}

	@SuppressWarnings("rawtypes")
	PathBuilder makePath(Class<?> srcClass, String srcAlias, Class<?> attributeClass, String attributeAlias){
		return new PathBuilder<>(srcClass, srcAlias).get(attributeAlias, attributeClass);
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


	Expression makePath(ColumnPrototype prototype){

		PathBuilder<?> result;

		switch(prototype.getAttributeType()){
		case ATTRIBUTE :
			result = attributePath(prototype);
			break;

		default : throw new IllegalArgumentException("columns of type '"+prototype.getAttributeType()+"' are not yet supported");
		}

		return result;
	}


	Expression<?> addOperation(DataType datatype, Operation operation, Expression<?> baseExp, Expression... operands){
		Expression<?> res;

		switch(datatype){
		case DATE : res = addDateOperation(operation, baseExp, operands); break;
		case STRING : res = addStringOperation(operation, baseExp, operands); break;
		case NUMERIC : res = addNumericOperation(operation, baseExp, operands); break;
		default : throw new IllegalArgumentException("cannot yet apply operations on columns of type '"+datatype+"'");
		}

		return res;
	}



	// ******************************* private stuffs *********************

	private PathBuilder attributePath(ColumnPrototype prototype){

		InternalEntityType type = InternalEntityType.fromDomainType(prototype.getEntityType());

		String alias = type.getQBean().getMetadata().getName();
		Class<?> clazz = type.getClass();
		String attribute = prototype.getAttributeName();
		Class<?> attributeType = classFromDatatype(prototype.getDataType());

		return makePath(clazz, alias, attributeType, attribute);

	}


	private Expression<?> addDateOperation(Operation operation, Expression<?> dateCol, Expression<?>... operands){
		Operator operator = getOperator(operation);

		Expression[] expressions = prepend(dateCol, operands);

		return Expressions.dateOperation(Date.class, operator, expressions);
	}

	private Expression<?> addNumericOperation(Operation operation, Expression<?> numCol, Expression<?>... operands){
		Operator operator = getOperator(operation);

		Expression[] expressions = prepend(numCol, operands);

		return Expressions.numberOperation(Long.class, operator, expressions);
	}

	private Expression<?> addStringOperation(Operation operation, Expression<?> strCol, Expression<?>... operands){
		Operator operator = getOperator(operation);

		Expression[] expressions = prepend(strCol, operands);

		return Expressions.stringOperation(operator, expressions);
	}



	private Operator getOperator(Operation operation){
		Operator operator;

		switch(operation){
		case LIKE : operator = Ops.LIKE; break;
		case BY_YEAR : operator = DateTimeOps.YEAR; break;
		case BY_MONTH : operator = DateTimeOps.YEAR_MONTH; break;
		case COUNT : operator = AggOps.COUNT_DISTINCT_AGG; break;
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
