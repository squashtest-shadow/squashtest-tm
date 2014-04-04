/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

package org.squashtest.tm.service.internal.batchimport.testcase.excel;

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.TC_DESCRIPTION;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.TC_NAME;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.TC_NATURE;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.TC_NUM;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.TC_PATH;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.TC_REFERENCE;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.TC_TYPE;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.TC_WEIGHT;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.TC_WEIGHT_AUTO;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.*;

import java.util.HashMap;
import java.util.Map;

import org.squashtest.tm.service.internal.batchimport.excel.PropertySetter;
import org.squashtest.tm.service.internal.batchimport.excel.ReflectionFieldSetter;
import org.squashtest.tm.service.internal.batchimport.excel.ReflectionMutatorSetter;

/**
 * @author Gregory Fouquet
 * 
 */
public class PropertySetterRepository {
	public static final PropertySetterRepository INSTANCE = new PropertySetterRepository();
	
	private Map<TestCaseSheetColumn, PropertySetter<?, ?>> propSetterByColumn = new HashMap<TestCaseSheetColumn, PropertySetter<?, ?>>();

	private PropertySetterRepository() {
		// target
		propSetterByColumn.put(TC_PATH, ReflectionFieldSetter.forField("path"));
		propSetterByColumn.put(TC_NUM, ReflectionFieldSetter.forOptionalField("order"));
		
		// test case
		propSetterByColumn.put(TC_REFERENCE, ReflectionFieldSetter.forOptionalField("reference"));
		propSetterByColumn.put(TC_NAME, ReflectionFieldSetter.forField("name"));
		propSetterByColumn.put(TC_WEIGHT_AUTO, ReflectionFieldSetter.forOptionalField("importanceAuto"));
		propSetterByColumn.put(TC_WEIGHT, ReflectionFieldSetter.forOptionalField("importance"));
		propSetterByColumn.put(TC_NATURE, ReflectionFieldSetter.forOptionalField("nature"));
		propSetterByColumn.put(TC_TYPE, ReflectionFieldSetter.forOptionalField("type"));
		propSetterByColumn.put(TC_STATUS, ReflectionFieldSetter.forOptionalField("status"));
		propSetterByColumn.put(TC_DESCRIPTION, ReflectionFieldSetter.forOptionalField("description"));
		propSetterByColumn.put(TC_PRE_REQUISITE, ReflectionFieldSetter.forOptionalField("prerequisite"));
		// createdOn and createdBy field name is not known, we use mutators to set'em
		propSetterByColumn.put(TC_CREATED_ON, ReflectionMutatorSetter.forOptionalProperty("createdOn"));
		propSetterByColumn.put(TC_CREATED_BY, ReflectionMutatorSetter.forOptionalProperty("createdBy"));
		
		// instruction
		propSetterByColumn.put(ACTION, ReflectionMutatorSetter.forOptionalProperty("mode"));
	}

	@SuppressWarnings("unchecked")
	public <V, T> PropertySetter<V, T> findPropSetter(TestCaseSheetColumn col) {
		return (PropertySetter<V, T>) propSetterByColumn.get(col);
	}
}
