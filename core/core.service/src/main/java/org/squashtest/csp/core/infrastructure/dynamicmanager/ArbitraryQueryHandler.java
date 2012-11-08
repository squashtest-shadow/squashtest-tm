/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.core.infrastructure.dynamicmanager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.squashtest.tm.core.foundation.collection.Paging;


/**
 * Executes arbitrary queries, using named parameters if any.
 * <h3>
 * 	How it works :
 * </h3>
 * <p>
 *  in most generic terms, it looks for a <u>{@link Query}</u>, applies the <u>parameters</u> and return the <u>result</u>.
 * </p>
 * 
 * <h3>Query</h3>
 * <p>query name must be &lt;entityname&gt;.&lt;methodname&gt;</p>
 * 
 * <h3>Parameters</h3>
 * <p>Accepts any number of parameters that may be :
 * <ul>
 * 	<li>a {@link Paging} (or subclass),</li>
 * 	<li>an annotated Collection (or subclass), </li>
 * 	<li>an annotated Object,</li>
 * </ul>
 * 
 * In the third case an object will be treated as a scalar. A note about Collection arguments : if that collection is empty the query will not be executed and 
 * a default return value will be returned.
 * </p>
 * <p>
 * {@link Paging} arguments don't have to be annotated. Multiple Paging will all be applied but only the last one will count. Other arguments MUST all be annotated 
 * using @{@link QueryParam}. The value of that annotation must correspond to the named parameter that will be looked for in the query. You may supply more annotations 
 * if you want to as long as at least QueryParam is supplied.  
 * </p>
 * 
 * <h3>Result</h3>
 * <p>
 * 	It depends on the result type of the method. When the query is executed the returned values are : 
 * <ul>
 * 	<li>void : returns null</li>
 * 	<li>Collection (or subclass) : returns a List</li>
 * 	<li>other : returns a scalar</li>
 * </ul>
 *  
 * If the query was aborted because a collection argument is empty, the returned values will be instead :
 * 
 * <ul>
 * 	<li>void : returns null</li>
 * 	<li>Collection (or sublcass) : returns an empty List</li>
 * 	<li>other : returns null or 0 if the result expects a type primitive </li>
 * </ul>
 * </p>
 * 
 * @author bsiri
 *
 * @param <ENTITY>
 */
public class ArbitraryQueryHandler<ENTITY> implements DynamicComponentInvocationHandler {

	private final Class<ENTITY> entityType;
	private final SessionFactory sessionFactory;	
	

	
	public ArbitraryQueryHandler(Class<ENTITY> entityType,
			SessionFactory sessionFactory) {
		super();
		this.entityType = entityType;
		this.sessionFactory = sessionFactory;
	}

	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		try{
			Query q = setupQuery(method, args);
			return executeQuery(method, q);
		}
		catch(EmptyCollectionException ex){
			return abortQuery(method);
		}
		
	}

	
	@Override
	public boolean handles(Method method) {
		return _queryExistsCheck(method) && _parametersCheck(method);
	}
	
	
	
	
	// ************************ private methods ***************************
	
	
	private Query _findQuery(Method method){
		String queryName = entityType.getSimpleName()+"."+method.getName();
		return sessionFactory.getCurrentSession().getNamedQuery(queryName);
	}

	
	private QueryParam _findQueryParam(Annotation[] paramAnnotations){
		for (int i=0;i<paramAnnotations.length;i++){
			if (_isQueryParam(paramAnnotations[i])){
				return (QueryParam)paramAnnotations[i];
			}
		}
		return null;
	}
	
	
	
	// ************************* predicates ************************
	
	private boolean _queryExistsCheck(Method method){
		try{
			Query q = _findQuery(method);
			return (q!=null);
		}catch(HibernateException ex){
			return false;
		}
	}
	

	
	private boolean _parametersCheck(Method method){
		
		Annotation[][] allAnnotations = method.getParameterAnnotations();
		Class<?>[] allParamTypes = method.getParameterTypes();

		for (int i=0;i<allParamTypes.length;i++){
			if ( (! _isPaging(allParamTypes[i])) && (_findQueryParam(allAnnotations[i]) == null)){
				return false;
			}
		}
		
		return true;
	}
	

	
	private boolean _isPaging(Class<?> paramType){
		return Paging.class.isAssignableFrom(paramType);
	}
	
	private boolean _isCollection(Class<?> paramType){
		return Collection.class.isAssignableFrom(paramType);
	}

	private boolean _isPaging(Object argument){
		return Paging.class.isAssignableFrom(argument.getClass());
	}
	
	private boolean _isCollection(Object argument){
		return Collection.class.isAssignableFrom(argument.getClass());
	}
	
	private boolean _isVoid(Class<?> type){
		return Void.TYPE.equals(type);
	}
	

	
	private boolean _isQueryParam(Annotation ann){
		return (ann.annotationType().equals(QueryParam.class));
	}

	
	// ************************** Query processing check *************** 
	
	private Query setupQuery(Method method, Object[] args){
		
		Query query = _findQuery(method);
		
		Annotation[][] allAnnotations = method.getParameterAnnotations();
		
		for (int i=0;i<args.length;i++){
			
			Object currentArg = args[i];
			
			if (_isPaging(currentArg)){
				processPaging(query, currentArg);
			}
			else if (_isCollection(currentArg)){
				setAsCollection(query, currentArg, allAnnotations[i]);
			}
			else{
				setAsScalar(query, currentArg, allAnnotations[i]);
			}
		}		
		
		return query;
	}
	
	private Object executeQuery(Method method, Query query){
		
		Class<?> returnType = method.getReturnType();
		
		if (_isVoid(returnType)){
			query.executeUpdate();
			return null;
		}
		else if (_isCollection(returnType)){
			return query.list();
		}
		else{
			return query.uniqueResult();
		}
		
	}
	
	private Object abortQuery(Method method){

		Class<?> returnType = method.getReturnType();
		
		if (_isCollection(returnType)){
			return new ArrayList(0);
		}
		else if (returnType.isPrimitive()){
			return _newPrimitiveZero(returnType);
		}
		else{
			return null;
		}
	}
	
	
	private void processPaging(Query query, Object arg) {
		Paging paging = (Paging) arg;
		query.setFirstResult(paging.getFirstItemIndex());
		query.setMaxResults(paging.getPageSize());
	}
	
	@SuppressWarnings("rawtypes")
	private void setAsCollection(Query query, Object arg, Annotation[] paramAnnotations)  {
		QueryParam paramName = _findQueryParam(paramAnnotations);
		Collection argument = (Collection)arg;
		if (argument.isEmpty()){
			throw new EmptyCollectionException();
		}
		query.setParameterList(paramName.value(),argument);
	}
	
	private void setAsScalar(Query query, Object arg, Annotation[] paramAnnotations){
		QueryParam paramName = _findQueryParam(paramAnnotations);
		query.setParameter(paramName.value(),arg);		
	}
	
	private Object _newPrimitiveZero(Class<?> returnType){
		if (returnType.equals(Short.TYPE)){
			return 0;
		}		
		else if (returnType.equals(Integer.TYPE)){
			return 0;
		}
		else if (returnType.equals(Float.TYPE)){
			return 0f;
		}
		else if (returnType.equals(Double.TYPE)){
			return 0d;
		}
		else if (returnType.equals(Long.TYPE)){
			return 0l;
		}
		else if (returnType.equals(Byte.TYPE)){
			return (byte)0;
		}
		else {
			return (char)0;
		}
	}
	
	private static final class EmptyCollectionException extends RuntimeException{}
}
