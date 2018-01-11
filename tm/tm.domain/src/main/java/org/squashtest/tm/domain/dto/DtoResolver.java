/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.domain.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.squashtest.tm.domain.customfield.InputType;


/**
 *
 *
 * @author bsiri
 *
 */
public class DtoResolver extends TypeIdResolverBase implements TypeIdResolver{


	private JavaType baseType ;

	@Override
	public void init(JavaType baseType){
		this.baseType = baseType;
	}

	@Override
	public Id getMechanism(){
		return Id.CUSTOM;
	}

	@Override
	public String idFromValue(Object obj){
		return idFromValueAndType(obj, obj.getClass());
	}

	@Override
	public String idFromBaseType(){
		// FIXME (GRF) code below always issues a NPE yet I don't know what we should do
		return idFromValueAndType(null, baseType.getRawClass());
	}

	@Override
	public String idFromValueAndType(Object obj, Class<?> clazz){
		return ((CustomFieldModel)obj).getInputType().getEnumName();
	}

	@Override
	public JavaType typeFromId(DatabindContext context, String type){

		JavaType toReturn;

		InputType enumType = InputType.valueOf(type);
		return baseType;
	}


}