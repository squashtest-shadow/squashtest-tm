package org.squashtest.csp.core.infrastructure.dynamicmanager;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
/**
 * {@link DynamicComponentInvocationHandler} which handles <code>List<ENTITY> findAllByIdList(Collection<Long> id)</code> method. Fetches all entities matching the ids of a collection.
 * @author Gregory Fouquet
 *
 */
public class FindAllByIdListHandler<ENTITY> implements DynamicComponentInvocationHandler { // NOSONAR : I dont choose what JDK interfaces throw
	private final Class<ENTITY> entityType;
	private final SessionFactory sessionFactory;

	/**
	 * @param entityType
	 * @param sessionFactory
	 */
	public FindAllByIdListHandler(@NotNull Class<ENTITY> entityType, @NotNull SessionFactory sessionFactory) {
		super();
		this.entityType = entityType;
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Performs an entity fetch using {@link #entityType} and the first arg as the collection of entities ids. 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		Collection<Long> ids = (Collection<Long>) args[0];
		
		if (ids.isEmpty()) {
			return Collections.emptyList();
		}
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(entityType);
		return crit.add(Restrictions.in("id", ids)).list();
	}

	/**
	 * @return <code>true</code> if method signature is <code>ENTITY findById(long id)</code>
	 */
	@Override
	public boolean handles(Method method) {
		return methodNameMatchesMethodPattern(method) && mehtodParamsMatchMethodParams(method)
				&& methodReturnTypeMatchesMethodPattern(method);
	}

	private boolean mehtodParamsMatchMethodParams(Method method) {
		Class<?>[] params = method.getParameterTypes();
		return params.length == 1 && Collection.class.isAssignableFrom(params[0]);
	}

	public boolean methodNameMatchesMethodPattern(Method method) {
		return "findAllByIdList".equals(method.getName());
	}

	private boolean methodReturnTypeMatchesMethodPattern(Method method) {
		return List.class.isAssignableFrom(method.getReturnType());
	}
}
