package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.core.infrastructure.hibernate.PagingUtils;
import org.squashtest.csp.core.infrastructure.hibernate.SortingUtils;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.internal.repository.CustomRequirementVersionDao;
/**
 * 
 * @author Gregory Fouquet
 *
 */
@Repository("CustomRequirementVersionDao")
public class HibernateRequirementVersionDao implements CustomRequirementVersionDao {
	@Inject
	private SessionFactory sessionFactory;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersion> findAllVerifiedByTestCases(Collection<Long> verifiersIds,
			PagingAndSorting pagingAndSorting) {
		if (verifiersIds.isEmpty()) {
			return Collections.emptyList();
		}
		
		Criteria crit = currentSession().createCriteria(RequirementVersion.class, "RequirementVersion");
		crit.createAlias("verifyingTestCases", "TestCase");
		crit.createAlias("requirement.project", "Project", Criteria.LEFT_JOIN);
		crit.add(Restrictions.in("TestCase.id", verifiersIds)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		PagingUtils.addPaging(crit, pagingAndSorting);
		SortingUtils.addOrder(crit, pagingAndSorting);
		
		return crit.list();
	}

	@Override
	public long countVerifiedByTestCases(Collection<Long> verifiersIds) {
		if (verifiersIds.isEmpty()){
			return 0;
		}
		
		Query query = currentSession().getNamedQuery("requirementVersion.countVerifiedByTestCases");
		query.setParameterList("verifiersIds", verifiersIds);
		return (Long) query.uniqueResult();
	}

	private Session currentSession() {
		return sessionFactory.getCurrentSession();
	}

}
