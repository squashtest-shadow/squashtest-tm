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
package org.squashtest.it.infrastructure;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.springframework.aop.framework.Advised;

public class AopSquashProxyUtil implements SquashITProxyUtil {

	@Override
	public void invoke(Object proxy, String methodName, Object... methodArgs) {
		
		try{
		
			Object targetObject = getTarget(proxy);
			
			Method method = findMethod( targetObject, methodName, methodArgs);
			
			method.invoke(targetObject, methodArgs);
			
			
		}catch(Exception exception){
			throw new RuntimeException(exception);
		}
		
	}
	
	/**
	 * returns a method if found with that signature, or null if it doesn't.
	 * 
	 * you should subclass-rewrite it to fit your taste if needed.
	 * 
	 */
	protected Method findMethod(Object target, String methodName, Object[] arguments){
		Class<?> targetClass = target.getClass();
		Class<?>[] parameterClasses=null;
			
		Method[] allMethods = targetClass.getMethods();
		
		Method[] nameMatchedMethods = findNameMatchedMethods(allMethods, methodName);
		
		if (nameMatchedMethods.length==0){
			throw new RuntimeException("AopSquashProxyUtil : method "+methodName+" wasn't found for object "+
					targetClass.getName()+" with the supplied arguments.");
		}
		
		Method completeMatchMethod = findCompleteMatchMethod(nameMatchedMethods, arguments);
		
		if (completeMatchMethod==null){
			throw new RuntimeException("AopSquashProxyUtil : method "+methodName+" wasn't found for object "+
				targetClass.getName()+" with the supplied arguments.");
		}
		
		return completeMatchMethod;
		
		
	}
	
	private Method[] findNameMatchedMethods(Method[] methodSet, String name){
		List<Method> methodList = new LinkedList<Method>();
		for (Method method : methodSet){
			if (method.getName().equals(name)){
				methodList.add(method);
			}
		}
		
		return methodList.toArray(new Method[0]);
	}
	
	private Method findCompleteMatchMethod(Method[] methodSet, Object[] arguments){
		
		Method toreturn = null;
		
		for (Method method : methodSet){
			Class<?>[] parameterTypes = method.getParameterTypes();
			
			//first check : number of arguments. If the last type is Array the calling method should ensure that parameters contains 
			//itself an Array (and not the expanded version allowed by the Object... style).
			if (parameterTypes.length != arguments.length) continue;
			
			//second check : are all the parameters assignables ?
			boolean checkFailed=false;
			int i=0;
			for (i=0;i<parameterTypes.length;i++){
				Class<?> paramType = parameterTypes[i];
				Class<?> argType = arguments[i].getClass();
				
				if (! paramType.isAssignableFrom(argType)){
					checkFailed=true;
					break;
				}
			}
			
			if (! checkFailed){
				return method; //that's the right one ! Safe for cases of polymorphism of course. 
			}
						
		}
		
		//if we reach that point then we didn't find our method.
		return null;
		
	}

	@Override
	public boolean isProxySupported(Object potentialProxy) {
		return (potentialProxy instanceof Advised);
	}

	@Override
	public Object getTarget(Object proxy) {
		try{
			return ((Advised)proxy).getTargetSource().getTarget();
		}catch(Exception e){
			return new RuntimeException(e);
		}
	}

}
