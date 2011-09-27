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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.requirement.ExportRequirementData;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.csp.tm.domain.requirement.VerificationCriterion;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.RequirementDao;

@Repository
public class HibernateRequirementDao extends HibernateEntityDao<Requirement> implements RequirementDao {
	private static final String REQUIREMENT_ID_PARAM_NAME = "requirementId";

	private static final Map<VerificationCriterion, Object[]> HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION = new HashMap<VerificationCriterion, Object[]>(
			VerificationCriterion.values().length);

	static {
		HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION.put(VerificationCriterion.ANY, new Object[] {
				RequirementLibraryNode.class, null }); // yeah, it's a null.
		HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION.put(VerificationCriterion.SHOULD_BE_VERIFIED, new Object[] {
				Requirement.class, Restrictions.isNotEmpty("verifyingTestCases") });
		HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION.put(VerificationCriterion.SHOULD_NOT_BE_VERIFIED, new Object[] {
				Requirement.class, Restrictions.isEmpty("verifyingTestCases") });

	}

	@Override
	public List<Requirement> findAllByIdList(final List<Long> requirementsIds) {
		if (! requirementsIds.isEmpty()){
			SetQueryParametersCallback setParams = new SetQueryParametersCallback() {
				@Override
				public void setQueryParameters(Query query) {
					query.setParameterList("requirementsIds", requirementsIds);
				}
			};
			return executeListNamedQuery("requirement.findAllByIdList", setParams);
		}else{
			return Collections.emptyList();
		}

	}

	@Override
	public List<TestCase> findAllVerifyingTestCasesById(long requirementId) {
		return executeListNamedQuery("requirement.findAllVerifyingTestCasesById", idParameter(requirementId));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TestCase> findAllVerifyingTestCasesByIdFiltered(final long requirementId, final CollectionSorting filter) {
		Session session = currentSession();

		String sortedAttribute = filter.getSortedAttribute();
		String order = filter.getSortingOrder();

		Criteria crit = session.createCriteria(Requirement.class).add(Restrictions.eq("id", requirementId))
				.createAlias("verifyingTestCases", "TestCase").createAlias("TestCase.project", "Project")
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

		/* add ordering */
		if (sortedAttribute != null) {
			if (order.equals("asc")) {
				crit.addOrder(Order.asc(sortedAttribute).ignoreCase());
			} else {
				crit.addOrder(Order.desc(sortedAttribute).ignoreCase());
			}
		}

		/* result range */
		crit.setFirstResult(filter.getFirstItemIndex());
		crit.setMaxResults(filter.getMaxNumberOfItems());

		List<Map<String, ?>> rawResult = crit.list();

		List<TestCase> testCases = new ArrayList<TestCase>();
		ListIterator<Map<String, ?>> iter = rawResult.listIterator();
		while (iter.hasNext()) {
			Map<String, ?> map = iter.next();
			testCases.add((TestCase) map.get("TestCase"));
		}

		return testCases;
	}

	@Override
	public long countVerifyingTestCasesById(long requirementId) {
		return (Long) executeEntityNamedQuery("requirement.countVerifyingTestCasesById", idParameter(requirementId));
	}

	private SetQueryParametersCallback idParameter(long requirementId) {
		return new SetIdParameter(REQUIREMENT_ID_PARAM_NAME, requirementId);
	}

	@Override
	public List<String> findNamesInFolderStartingWith(final long folderId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("containerId", folderId);
				query.setParameter("nameStart", nameStart + "%");
			}
		};
		return executeListNamedQuery("requirement.findNamesInFolderStartingWith", newCallBack1);
	}

	@Override
	public List<String> findNamesInLibraryStartingWith(final long libraryId, final String nameStart) {
		SetQueryParametersCallback callBack = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("containerId", libraryId);
				query.setParameter("nameStart", nameStart + "%");
			}
		};
		return executeListNamedQuery("requirement.findNamesInLibraryStartingWith", callBack);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementLibraryNode> findAllBySearchCriteria(RequirementSearchCriteria searchCriteria) {
		DetachedCriteria crit = createCriteria(searchCriteria);

		crit.addOrder(Order.asc("name"));

		return crit.getExecutableCriteria(currentSession()).list();
	}

	private DetachedCriteria createCriteria(RequirementSearchCriteria searchCriteria) {
		DetachedCriteria crit = DetachedCriteria.forClass(getCriteriaClass(searchCriteria));

		if (StringUtils.isNotBlank(searchCriteria.getName())) {
			crit.add(Restrictions.ilike("name", searchCriteria.getName(), MatchMode.ANYWHERE));
		}
		if (StringUtils.isNotBlank(searchCriteria.getReference())) {
			crit.add(Restrictions.ilike("reference", searchCriteria.getReference(), MatchMode.ANYWHERE));
		}
		if (!searchCriteria.getCriticalities().isEmpty()) {
			crit.add(Restrictions.in("criticality", searchCriteria.getCriticalities()));
		}

		addVerificationRestriction(searchCriteria, crit);

		return crit;
	}

	Class<?> getCriteriaClass(RequirementSearchCriteria searchCriteria) {
		return (Class<?>) HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION
				.get(searchCriteria.getVerificationCriterion())[0];
	}

	private void addVerificationRestriction(RequirementSearchCriteria searchCriteria, DetachedCriteria criteria) {
		Criterion restriction = (Criterion) HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION.get(searchCriteria
				.getVerificationCriterion())[1];

		if (restriction != null) {
			criteria.add(restriction);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementLibraryNode> findAllBySearchCriteriaOrderByProject(RequirementSearchCriteria searchCriteria) {
		DetachedCriteria crit = createCriteria(searchCriteria);

		crit.createAlias("project", "p").addOrder(Order.asc("p.name"));
		crit.addOrder(Order.asc("name"));

		return crit.getExecutableCriteria(currentSession()).list();
	}

	@Override
	public List<ExportRequirementData> findRequirementToExportFromFolder(List<Long> params) {
		if (! params.isEmpty()){
			
			List <Object[]> listObject;
			List<Long> listReqNodeId;
			List<Long> reqIds;

			List<Object> rootReqs = findRootContentRequirement(params); 
			listReqNodeId = findRequirementIds(params);
			if ( listReqNodeId == null || listReqNodeId.isEmpty()) {
				return   formatExportResult(addRootContentToExportData(rootReqs, new ArrayList<Object[]>()));
			}
			reqIds = findAllRequirementInExportData(listReqNodeId);
			listObject = findRequirementExportData(reqIds);
	
			if (!rootReqs.isEmpty()){
				listObject = addRootContentToExportData(rootReqs, listObject);
			}
			List<ExportRequirementData> exportList = formatExportResult(listObject);
			return exportList;
		}else{
			return Collections.emptyList();
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<Long> findRequirementIds(List<Long> params) {
		if (! params.isEmpty()){
			Session session = currentSession();
			String sql = "select DESCENDANT_ID from RLN_RELATIONSHIP where ANCESTOR_ID in (:list)";
	
			
			List<BigInteger> list;
			List<Long> result = new ArrayList<Long>();
			result.addAll(params);	//the inputs are also part of the output.
			List<Long> local = params;
			
			do {
				Query sqlQuery = session.createSQLQuery(sql);
				sqlQuery.setParameterList("list", local, new LongType());
				list = sqlQuery.list();
				if (!list.isEmpty()) {
					local.clear();
					for (BigInteger bint : list) {
						local.add(bint.longValue());
						result.add(bint.longValue());
					}
				}
			}
			while (!list.isEmpty());
			if (result.isEmpty()){
				return null;
			}
			return result;		
		}else{
			return Collections.emptyList();
		}
		
		
	}
	
	private List<Object[]> findRequirementExportData(final List<Long> requirementIds) {
		if (!requirementIds.isEmpty()){
			SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {
				@Override
				public void setQueryParameters(Query query) {
					query.setParameterList("rIds", requirementIds, new LongType());
				}
			};
			
			List<Object[]> resultList = executeListNamedQuery("requirement.findRequirementExportData", newCallBack1);
			return resultList;
		}else{
			return Collections.emptyList();
		}
	}
	
	private List<Long> findAllRequirementInExportData(final List<Long> requirementIds) {
		if (!requirementIds.isEmpty()){
			SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {
				@Override
				public void setQueryParameters(Query query) {
					query.setParameterList("rIds", requirementIds, new LongType());
				}
			};
			
			List<Object> resultList = executeListNamedQuery("requirement.findRequirementInExportData", newCallBack1);
			List<Long> reqIds = new ArrayList<Long>();
			for (Object obj : resultList) {
				reqIds.add(Long.parseLong(obj.toString()));
			}
			return reqIds;
		}else{
			return Collections.emptyList();
		}
	}
	
	private List<ExportRequirementData> formatExportResult(List<Object[]> list){
		if (!list.isEmpty()){
			List<ExportRequirementData> exportList = new ArrayList<ExportRequirementData>();
			
			for (Object[] tuple : list){
				Requirement req = (Requirement) tuple[0];
				String folder = (String) tuple[1];
				ExportRequirementData erd = new ExportRequirementData(req, folder);
				exportList.add(erd);
			}
	
			return exportList;
		}else{
			return Collections.emptyList();
		}
	}
	
	private List<Object> findRootContentRequirement (final List<Long> params){
		if (! params.isEmpty()){
			SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {
				@Override
				public void setQueryParameters(Query query) {
					query.setParameterList("paramIds", params, new LongType());
				}
			};
			List<Object> resultList = executeListNamedQuery("requirement.findRootContentRequirement", newCallBack1);
			return resultList;
		}else{
			return Collections.emptyList();
		}
	}

	
	@Override
	public List<ExportRequirementData> findRequirementToExportFromLibrary(
		 final List<Long> libIds) {
		
		if (! libIds.isEmpty()){
			SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {
				@Override
				public void setQueryParameters(Query query) {
					query.setParameterList("rIds", libIds, new LongType());
				}
			};
			List<Long> result = executeListNamedQuery("requirement.findAllRootContent", newCallBack1);
			
			return findRequirementToExportFromFolder(result);
		}else{
			return Collections.emptyList();
		}
	}
	

	
	private List <Object[]> addRootContentToExportData(List<Object> rootRequirement, List<Object[]> folderContent){
		for(Object obj : rootRequirement){
			Requirement objReq = (Requirement) obj;
			Object[] tab = {objReq, objReq.getProject().getName()};
			folderContent.add(tab);
		}
		return folderContent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Requirement> findAllRequirementsVerifiedByTestCases(Collection<Long> verifiersIds,
			CollectionSorting sorting) {
		
		if (! verifiersIds.isEmpty()){
		
			String hql = "select Requirement from TestCase as TestCase join TestCase.verifiedRequirements Requirement " +
					    "left join Requirement.project as Project where TestCase.id in (:verifiersIds) group by Requirement ";
			
			String orderBy = " order by " + sorting.getSortedAttribute() + ' ' + sorting.getSortingOrder();
	
			Query query = currentSession().createQuery(hql + orderBy);
			query.setParameterList("verifiersIds", verifiersIds);
	
			query.setMaxResults(sorting.getMaxNumberOfItems());
			query.setFirstResult(sorting.getFirstItemIndex());
	
			return query.list();
		}else{
			return Collections.emptyList();
		}
	}

	@Override
	public long countRequirementsVerifiedByTestCases(Collection<Long> verifiersIds) {
		if (! verifiersIds.isEmpty()){
			Query query = currentSession().getNamedQuery("requirement.countRequirementsVerifiedByTestCases");
			query.setParameterList("verifiersIds", verifiersIds);
			return (Long) query.uniqueResult();
		}else{
			return 0;
		}
	}
}
