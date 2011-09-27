/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.web.internal.model.viewmapper;

import java.lang.reflect.Method;

public  class DataTableMapper {
	protected String viewName;	
	protected Class<?>[] sourceClasses;

	protected AttributeRegister[] mapping;
	
	public DataTableMapper(){
		
	}
	
	public DataTableMapper(String viewName){
		this.viewName=viewName;
	}
	
	public DataTableMapper(String viewName,Class<?>... sourceClasses){
		this.viewName=viewName;		
		this.sourceClasses=sourceClasses;
	}
	
	
	public DataTableMapper setSourceClasses(Class<?>... sourceClasses){
		this.sourceClasses=sourceClasses;
		return this;
	}
	
	public Class<?>[] getSourceClasses() {
		return sourceClasses;
	}

	
	
	
	public String attrAt(int num){
		if (num>mapping.length) {
			throw new IllegalArgumentException("DataTableMapper : out of bound : "+num);
		}
		return mapping[num]==null ? null : mapping[num].fieldName;
	}
	
	
	public String getterNameAt(int num){
		if (num>mapping.length) {
			throw new IllegalArgumentException("DataTableMapper : out of bound : "+num);
		}
		return mapping[num]==null ? null : mapping[num].getter.getName();				
	}
	
	public Method getterAt(int num){
		if (num>mapping.length) {
			throw new IllegalArgumentException("DataTableMapper : out of bound : "+num);
		}
		return mapping[num]==null ? null : mapping[num].getter;			
	}
	
	/**
	 * Returns the component path of the attribute, prefixed with the short name of the class it belongs to, 
	 * ie Simpleclassname.component.path.to.the.field.
	 * 
	 * So be careful to respect that convention in other parts of the application.
	 * 
	 * @param num the index of the requested attribute.
	 * @return what I just said above.
	 */
	public String pathAt(int num){
		if (num>mapping.length){
			StringBuffer strBuf = new StringBuffer("");
			strBuf.append("DataTableMapper : out of bound : maxField : "+mapping.length);
			throw new IllegalArgumentException(strBuf.toString());
		}
		
		String toReturn = null;
		
		if (mapping[num]!=null){
			int classIndex = mapping[num].objectIndex;
			String className = sourceClasses[classIndex].getSimpleName();
			String fieldPath = mapping[num].fieldPath;
			toReturn = className+"."+fieldPath;
		}
		return toReturn;		
	}
	
	
	
	
	public Object[] toData(Object... instances){
		Object[] output = new Object[mapping.length];
		
		for (int i=0;i<mapping.length;i++){
			AttributeRegister register = mapping[i];
			if (register==null) {output[i]=null;continue;}
			
			Method getter = register.getter;
			if (getter==null) {output[i]=null;continue;}
			
			int which = register.objectIndex;
			try{
				output[i]= getter.invoke(instances[which],(Object[]) null);
			}catch(Exception e){
				output[i]=null;
			}
		}
		
		return output;
		
	}
	
	
	public DataTableMapper mapAttribute(Class<?> clazz,int attributeIndex, String attributePath, Class<?> attributeType){
		
		int objectIndex=0;
		for (objectIndex=0;objectIndex<sourceClasses.length;objectIndex++){
			if (sourceClasses[objectIndex].equals(clazz)) {
				break;
			}
		}
		
		if (objectIndex==sourceClasses.length) {
			throw new IllegalArgumentException("DataTableMapper : provided class is not supported by this instance. Be sure you provide the constructor with all the classes you map.");
		}
		
		AttributeRegister register = new AttributeRegister();
		
		register.objectIndex=objectIndex;
		register.attributeIndex=attributeIndex;
		register.fieldName=attributePath;
		register.type=attributeType;
		register.fieldPath=attributePath;
		
		mapping[attributeIndex]=register;		
		
		return this;
		
	}
	

	public DataTableMapper initMapping(int size){
		mapping = new AttributeRegister[size];
		return this;
	}
	

	


	
	
	
/**************************** private ***********************************/	



		
	
}
