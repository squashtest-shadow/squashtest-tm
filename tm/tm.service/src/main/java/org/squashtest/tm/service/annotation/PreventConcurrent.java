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
package org.squashtest.tm.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods with this annotation should not be run concurrently when they access the same entity.
 * <p/>
 * The method arg which conveys the entity's id has to be annotated with @Id
 * <p/>
 * This annotation is processed at runtime using Spring AOP so it can only be used on spring beans. It means it should
 * be put on the concrete class and @Id should be put on the interface (yeah, that sucks) => best thing is to put it on
 * both the interface and the concrete class.
 *
 * @author Gregory Fouquet
 * @since 1.11.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreventConcurrent {
	/**
	 * Type of the entity which should be locked
	 */
	Class<?> entityType();
}
