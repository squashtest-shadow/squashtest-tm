package org.squashtest.csp.tm.internal.repository;

import java.util.Collection;
import java.util.List;

import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;

public interface CustomRequirementVersionDao {
	/**
	 * Returns paged and sorted collection of requirement versions verified by any of the given verifiers.
	 *
	 * @param verifiersIds
	 * @param sorting
	 * @return
	 */
	List<RequirementVersion> findAllVerifiedByTestCases(Collection<Long> verifiersIds, PagingAndSorting sorting);
	/**
	 * Counts the number of requirements verified by any of the given verifiers.
	 *
	 * @param verifiersIds
	 * @return
	 */
	long countVerifiedByTestCases(Collection<Long> verifiersIds);

}
