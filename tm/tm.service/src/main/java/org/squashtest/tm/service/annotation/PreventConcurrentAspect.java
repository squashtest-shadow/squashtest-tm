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
package org.squashtest.tm.service.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

	public static class LockManager {
		private static class EntityRef {
			private final Class type;
			private final Serializable id;

			@Override
			public String toString() {
				return "EntityRef{" +
					"type=" + type +
					", id=" + id +
					'}';
			}

			private EntityRef(@NotNull Class type, Serializable id) {
				this.type = type;
				this.id = id;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;

				EntityRef entityRef = (EntityRef) o;

				if (!type.equals(entityRef.type)) return false;
				return id.equals(entityRef.id);

			}

			@Override
			public int hashCode() {
				int result = type.hashCode();
				result = 31 * result + id.hashCode();
				return result;
			}
		}

		private static final Map<EntityRef, WeakReference<ReentrantLock>> locks = new ConcurrentHashMap<EntityRef, WeakReference<ReentrantLock>>();

		public static synchronized ReentrantLock getLock(Class type, Serializable id) {
			EntityRef ref = new EntityRef(type, id);
			WeakReference<ReentrantLock> wr = locks.get(ref);
			ReentrantLock lock;

			if (wr == null) {
				lock = createLock(ref);
			} else {
				LOGGER.trace("Retrieved lock for entity {}", ref);
				lock = wr.get();

				if (lock == null) {
					LOGGER.trace("Previous lock was GC'd");
					lock = createLock(ref);
				}
			}

			LOGGER.debug("Gotten lock for {}", ref);
			return lock;
		}

		private static ReentrantLock createLock(EntityRef ref) {
			ReentrantLock lock;
			LOGGER.trace("Creating new weak reference and lock for entity {}", ref);
			lock = new ReentrantLock();
			locks.put(ref, new WeakReference<ReentrantLock>(lock));
			return lock;
		}
	}

//	@Pointcut(value = "execution(@org.squashtest.tm.service.annotation.PreventConcurrent * *(..)) && @annotation(pc)", argNames = "pc")
//	public void entityLockingMethod(PreventConcurrent pc) {
//		// NOOP
//	}

	@Around(value = "execution(@org.squashtest.tm.service.annotation.PreventConcurrent * *(..)) && @annotation(pc)", argNames = "pc")
	public Object lockEntity(ProceedingJoinPoint pjp, PreventConcurrent pc) throws Throwable {
		ReentrantLock lock = LockManager.getLock(pc.entityType(), findEntityId(pjp));
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
		MethodSignature sig = (MethodSignature) pjp.getSignature();
		Method meth = sig.getMethod();
		Annotation[][] annotations = meth.getParameterAnnotations();
		LOGGER.trace("Advising method {}{}.", pjp.getSignature().getDeclaringTypeName(), meth.getName());

		Serializable entityId = null;

		argsLoop:
		for (int iArg = 0; iArg < annotations.length; iArg++) {
			Annotation[] curArg = annotations[iArg];

			annLoop:
			for (int jAnn = 0; jAnn < curArg.length; jAnn++) {
				if (curArg[jAnn].annotationType().equals(Id.class)) {
					LOGGER.trace("Found required @Id on arg #{} of method {}", iArg, meth.getName());
					entityId = (Serializable) pjp.getArgs()[iArg];
					break argsLoop;
				}
			}
		}

		if (entityId == null) {
			throw new IllegalArgumentException("I coult not find any arg annotated @Id in @PreventConcurrent method '" +
				pjp.getSignature().getDeclaringTypeName() + '.' + meth.getName() + "' This must be a structural programming error");

		}
		return entityId;
	}
}
