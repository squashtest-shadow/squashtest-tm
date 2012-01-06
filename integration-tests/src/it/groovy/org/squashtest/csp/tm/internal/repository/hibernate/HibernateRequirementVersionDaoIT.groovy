package org.squashtest.csp.tm.internal.repository.hibernate

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.core.infrastructure.collection.SortOrder;
import org.squashtest.csp.tm.internal.repository.RequirementVersionDao;
import org.squashtest.csp.tools.unittest.assertions.CollectionAssertions;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;

@UnitilsSupport
class HibernateRequirementVersionDaoIT extends DbunitDaoSpecification {
	@Inject RequirementVersionDao versionDao
	
	def setup() {
		CollectionAssertions.declareContainsExactlyIds()
	}

	@DataSet("HibernateRequirementVersionDaoIT.should count requirements verified by list of test cases.xml")
	def "should find all requirements verified by list of test cases sorted by id"() {
		PagingAndSorting sorting = Mock()
		sorting.firstItemIndex >> 0
		sorting.pageSize >> 10
		sorting.sortedAttribute >> "RequirementVersion.id"
		sorting.sortingOrder >> SortOrder.ASCENDING

		when:
		def reqs = versionDao.findAllVerifiedByTestCases([100L, 200L], sorting)

		then:
		reqs.collect { it.id } == [10L, 20L, 30L]
	}
	@DataSet("HibernateRequirementVersionDaoIT.should count requirements verified by list of test cases.xml")
	def "should find all requirements verified by list of test cases sorted by desc name"() {
		PagingAndSorting sorting = Mock()
		sorting.firstItemIndex >> 0
		sorting.pageSize >> 10
		sorting.sortedAttribute >> "RequirementVersion.name"
		sorting.sortingOrder >> SortOrder.DESCENDING

		when:
		def reqs = versionDao.findAllVerifiedByTestCases([100L, 200L], sorting)

		then:
		reqs.collect { it.name } == ["vingt", "30", "10"]
	}

	@DataSet("HibernateRequirementVersionDaoIT.should count requirements verified by list of test cases.xml")
	def "should find paged list of requirements verified by list of test cases"() {
		PagingAndSorting sorting = Mock()
		sorting.firstItemIndex >> 1
		sorting.pageSize >> 1
		sorting.sortedAttribute >> "RequirementVersion.id"
		sorting.sortingOrder >> SortOrder.ASCENDING

		when:
		def reqs = versionDao.findAllVerifiedByTestCases([100L, 200L], sorting)

		then:
		reqs.collect { it.id } == [20L]
	}

	@DataSet("HibernateRequirementVersionDaoIT.should count requirements verified by list of test cases.xml")
	def "should count requirements verified by list of test cases"() {
		when:
		def count = versionDao.countVerifiedByTestCases([100L, 200L])

		then:
		count == 3
	}

	@DataSet("HibernateRequirementVersionDaoIT.should find all requirements versions by id.xml")
	def "should find all requirements versions by id"() {
		when:
		def res = versionDao.findAllByIdList([10L, 20L])
		
		then: 
		res.containsExactlyIds([10L, 20L])
	}
}
