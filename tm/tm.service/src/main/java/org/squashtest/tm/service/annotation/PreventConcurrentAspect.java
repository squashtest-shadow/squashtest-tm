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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.concurrent.EntityLockManager;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This aspect manages @PreventConcurrentAspect annotations.
 *
 * @author Gregory Fouquet
 * @since 1.11.6
 */
@Component
@Aspect
public class PreventConcurrentAspect implements Ordered {
	private static final Logger LOGGER = LoggerFactory.getLogger(PreventConcurrentAspect.class);

	@Override
	public int getOrder() {
		return 2;
	}

//	@Pointcut(value = "execution(@org.squashtest.tm.service.annotation.PreventConcurrent * *(..)) && @annotation(pc)", argNames = "pc")
//	public void entityLockingMethod(PreventConcurrent pc) {
//		// NOOP
//	}

	@Around(value = "execution(@org.squashtest.tm.service.annotation.PreventConcurrent * *(..)) && @annotation(pc)", argNames = "pc")
	public Object lockEntity(ProceedingJoinPoint pjp, PreventConcurrent pc) throws Throwable {
		ReentrantLock lock = EntityLockManager.getLock(pc.entityType(), findEntityId(pjp));
		lock.lock();
		LOGGER.warn("Acquired lock on {}", lock);

		try {
			return pjp.proceed();

		} finally {
			LOGGER.warn("Releasing lock on {}", lock);
			lock.unlock();

		}
	}

	private Serializable findEntityId(ProceedingJoinPoint pjp) {
		return findAnnotatedParam(pjp, Id.class);
	}

	@Around(value = "execution(@org.squashtest.tm.service.annotation.BatchPreventConcurrent * *(..)) && @annotation(pc)", argNames = "pc")
	public Object lockEntities(ProceedingJoinPoint pjp, BatchPreventConcurrent pc) throws Throwable {
		Collection<? extends Serializable> sourceIds = findEntityIds(pjp);
		IdsCoercer coercer = pc.coercer().newInstance();
		Collection<Lock> locks = EntityLockManager.lock(pc.entityType(), coercer.coerce(sourceIds));

		try {
			return pjp.proceed();

		} finally {
			EntityLockManager.release(locks);

		}
	}

	private Collection<? extends Serializable> findEntityIds(ProceedingJoinPoint pjp) {
		return findAnnotatedParam(pjp, Ids.class);
	}

	private <T> T findAnnotatedParam(ProceedingJoinPoint pjp, Class<? extends Annotation> expected) {
		MethodSignature sig = (MethodSignature) pjp.getSignature();
		Method meth = sig.getMethod();
		Annotation[][] annotations = meth.getParameterAnnotations();
		LOGGER.trace("Advising method {}{}.", pjp.getSignature().getDeclaringTypeName(), meth.getName());

		T annotatedParam = null;

		argsLoop:
		for (int iArg = 0; iArg < annotations.length; iArg++) {
			Annotation[] curArg = annotations[iArg];

			annLoop:
			for (int jAnn = 0; jAnn < curArg.length; jAnn++) {
				if (curArg[jAnn].annotationType().equals(expected)) {
					LOGGER.trace("Found required @{} on arg #{} of method {}", new Object[]{expected.getSimpleName(), iArg, meth.getName()});
					annotatedParam = (T) pjp.getArgs()[iArg];
					break argsLoop;
				}
			}
		}

		if (annotatedParam == null) {
			throw new IllegalArgumentException("I coult not find any arg annotated @" + expected.getSimpleName() + " in @PreventConcurrent method '" +
				pjp.getSignature().getDeclaringTypeName() + '.' + meth.getName() + "' This must be a structural programming error");

		}
		return annotatedParam;
	}

}
