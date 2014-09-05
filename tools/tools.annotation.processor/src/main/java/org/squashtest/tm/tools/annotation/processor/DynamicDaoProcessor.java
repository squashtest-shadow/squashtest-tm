/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.tools.annotation.processor;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;

import org.squashtest.tm.core.dynamicmanager.annotation.DynamicDao;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;

/**
 * Consumes {@link DynamicManager} annotated interfacrs and produces spring configuration accordlingly.
 * 
 * @author Gregory Fouquet
 * 
 */
@SupportedAnnotationTypes(value = { "org.squashtest.tm.core.dynamicmanager.annotation.DynamicDao" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class DynamicDaoProcessor extends DynamicComponentProcessor<DynamicDao> {
	private static final String DYNAMIC_DAO_BEAN_FACTORY = "org.squashtest.tm.core.dynamicmanager.factory.DynamicDaoFactoryBean";

	/**
	 * @see org.squashtest.tm.tools.annotation.processor.DynamicComponentProcessor#annotationClass()
	 */
	@Override
	protected Class<DynamicDao> annotationClass() {
		return DynamicDao.class;
	}

	/**
	 * @see org.squashtest.tm.tools.annotation.processor.DynamicComponentProcessor#entityClass(java.lang.annotation.Annotation)
	 */
	@Override
	protected Class<?> entityClass(DynamicDao componentDefinition) {
		return componentDefinition.entity();
	}

	/**
	 * @see org.squashtest.tm.tools.annotation.processor.DynamicComponentProcessor#beanName(java.lang.annotation.Annotation)
	 */
	@Override
	protected String beanName(DynamicDao componentDefinition) {
		return componentDefinition.name();
	}

	/**
	 * @see org.squashtest.tm.tools.annotation.processor.DynamicComponentProcessor#beanFactoryClass()
	 */
	@Override
	protected String beanFactoryClass() {
		return DYNAMIC_DAO_BEAN_FACTORY;
	}

	/**
	 * @see org.squashtest.tm.tools.annotation.processor.DynamicComponentProcessor#generatedFileName()
	 */
	@Override
	protected String generatedFileName() {
		return "dynamicdao-context.xml";
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.tools.annotation.processor.DynamicComponentProcessor#lookupCustomImplementation(java.lang.
	 * annotation.Annotation)
	 */
	@Override
	protected boolean lookupCustomImplementation(DynamicDao definition) {
		return definition.hasCustomImplementation();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.squashtest.tm.tools.annotation.processor.DynamicComponentProcessor#sessionFactoryName(java.lang.annotation
	 * .Annotation)
	 */
	@Override
	protected CharSequence sessionFactoryName(DynamicDao definition, Element component) {
		String name = definition.sessionFactoryName();
		checkSessionFactoryName(name, component);
		return name;
	}

}
