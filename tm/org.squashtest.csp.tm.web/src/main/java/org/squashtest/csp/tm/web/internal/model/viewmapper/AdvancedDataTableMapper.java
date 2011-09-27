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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.squashtest.csp.tm.domain.viewmapping.AtView;
import org.squashtest.csp.tm.domain.viewmapping.AtViews;

public class AdvancedDataTableMapper extends DataTableMapper {

	private int maxField = 0;
	private static final String STR_PACKAGE_FILTER = "org.squashtest";

	// to prevent infinite scan recursion with recursive structures
	private List<Class<?>> exploredClasses = new LinkedList<Class<?>>();

	/*************** public *************/

	public AdvancedDataTableMapper(String viewName) {
		this.viewName = viewName;
	}

	public AdvancedDataTableMapper(String viewName, Class<?>... sourceClasses) {
		this.viewName = viewName;
		this.sourceClasses = sourceClasses;
	}

	public AdvancedDataTableMapper initAll() {
		// check.
		initMapping();
		if (mapping.length == 0) {
			throw new IllegalArgumentException(
					"DataTableMapper : provided classes do not feed nor is fed from the view " + viewName);
		}

		fillAttributes();
		fillGettersSetters();

		return this;

	}

	@Override
	public String attrAt(int num) {
		if (num > mapping.length) {
			throw new IllegalArgumentException("DataTableMapper : no property set for column " + num);
		}
		return mapping[num] == null ? null : mapping[num].fieldName;
	}

	@Override
	public Object[] toData(Object... instances) {
		Object[] output = new Object[mapping.length];

		for (int i = 0; i < mapping.length; i++) {
			AttributeRegister register = mapping[i];
			if (register == null) {
				output[i] = null;
				continue;
			}

			Method getter = register.getter;
			if (getter == null) {
				output[i] = null;
				continue;
			}

			int which = register.objectIndex;
			try {
				output[i] = getter.invoke(instances[which], (Object[]) null);
			} catch (Exception e) {
				output[i] = null;
			}
		}

		return output;

	}

	/**************************** private ***********************************/

	protected boolean isBound(Field field) {
		return (getAnnotationForView(field) != null);
	}

	protected boolean isClassExplored(Class<?> clazz) {
		return exploredClasses.contains(clazz);
	}

	protected AtView getAnnotationForView(Field field) {
		AtViews annots = field.getAnnotation(AtViews.class);
		if (annots == null) {
			return null;
		}

		for (AtView atView : annots.values()) {
			if (atView.view().equals(viewName)) {
				return atView;
			}
		}
		return null;
	}

	private void initMapping() {
		exploredClasses.clear();
		for (Class<?> clazz : sourceClasses) {
			countClassTree(clazz);
		}
		mapping = new AttributeRegister[maxField + 1];
	}

	private void countClassTree(Class<?> clazz) {
		// no need to check classes we didn't write ourself
		if (clazz == null) {
			return;
		}

		if (isClassExplored(clazz)) {
			return;
		}

		try {
			Package clazzPackage = clazz.getPackage();
			if (clazzPackage == null) {
				return;
			}
			if (clazz.getPackage().getName().contains(STR_PACKAGE_FILTER) == false) {
				return;
			}
		} catch (Exception e) {
			return; // damn anonymous types
		}

		// add me to the explored class list
		exploredClasses.add(clazz);

		// expore superclass
		countClassTree(clazz.getSuperclass());

		// explore inner classes
		Field[] fieldList = clazz.getDeclaredFields();
		for (Field field : fieldList) {
			countFieldTree(field);
		}

	}

	private void countFieldTree(Field field) {
		if (field == null) {
			return;
		}

		// explore myself;
		if (isBound(field)) {
			AtView annot = getAnnotationForView(field);
			Integer column = Integer.valueOf(annot.column());
			maxField = (column > maxField ? column : maxField);
		}

		// explore me as a class
		Class<?> fieldClass = field.getType();

		countClassTree(fieldClass);

	}

	private void fillAttributes() {
		exploredClasses.clear();
		for (int i = 0; i < sourceClasses.length; i++) {
			Class<?> clazz = sourceClasses[i];

			exploreClassTree(i, "", clazz);
		}

		// remove the dot prefix prefix in the Attribute path
		for (AttributeRegister register : mapping) {
			if (register == null) {
				continue;
			}
			register.fieldPath = register.fieldPath.substring(1);
		}
	}

	// recursively treats super classes and fields
	private void exploreClassTree(int index, String path, Class<?> clazz) {

		// no need to check classes we didn't write ourself
		if (clazz == null) {
			return;
		}
		if (isClassExplored(clazz)) {
			return;
		}
		try {
			Package clazzPackage = clazz.getPackage();
			if (clazzPackage == null) {
				return;
			}
			if (clazz.getPackage().getName().contains(STR_PACKAGE_FILTER) == false) {
				return;
			}
		} catch (Exception e) {
			return; // damn anonymous types
		}

		// add me to the explored class list
		exploredClasses.add(clazz);

		// expore superclass
		exploreClassTree(index, path, clazz.getSuperclass());

		// explore inner classes
		Field[] fieldList = clazz.getDeclaredFields();
		for (Field field : fieldList) {
			exploreFieldTree(index, path, field);
		}

	}

	// will treat this field as a class too
	private void exploreFieldTree(int index, String path, Field field) {
		if (field == null) {
			return;
		}

		String repath = path + "." + field.getName();

		// explore myself;
		if (isBound(field)) {
			mapAttribute(index, repath, field);
		}

		Class<?> fieldClass = field.getType();

		exploreClassTree(index, repath, fieldClass);

	}

	private void fillGettersSetters() {
		for (AttributeRegister register : mapping) {
			if (register == null) {
				continue;
			}
			String name = register.fieldName;
			Class<?> dataType = register.type;

			String getterPrefix = "get";
			if (dataType.equals(Boolean.class)) {
				getterPrefix = "is";
			}
			String setterPrefix = "set";
			String uppercased = name.substring(0, 1).toUpperCase() + name.substring(1);

			Class<?> clazz = sourceClasses[register.objectIndex];

			try {
				Method getter = clazz.getMethod(getterPrefix + uppercased, (Class<?>[]) null);
				register.getter = getter;
			} catch (Exception e) {
				// no public getter. Who cares.
				register.getter = null;
			}

			try {
				Method setter = clazz.getMethod(setterPrefix + uppercased, dataType);
				register.setter = setter;
			} catch (Exception e) {
				register.setter = null;
			}
		}

	}

	private void mapAttribute(int objectIndex, String path, Field field) {
		AttributeRegister register = new AttributeRegister();
		AtView annot = this.getAnnotationForView(field);
		Integer column = Integer.valueOf(annot.column());

		register.objectIndex = objectIndex;
		register.attributeIndex = column;
		register.fieldName = field.getName();
		register.type = field.getType();
		register.fieldPath = path;

		mapping[column] = register;
	}

}
