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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.bugtracker.Bugged;
import org.squashtest.csp.tm.domain.bugtracker.Issue;
import org.squashtest.csp.tm.domain.bugtracker.IssueList;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.IssueDao;

@Repository
public class HibernateIssueDao extends HibernateEntityDao<Issue> implements IssueDao {


	@Override
	@SuppressWarnings("unchecked")
	public <X extends Bugged> X findBuggedEntity(Long entityId, Class<X> entityClass) {
		Session session = currentSession();

		X bugged = (X)session.createCriteria(entityClass)
							.add(Restrictions.eq("id", entityId))
							.uniqueResult();
		
		return bugged;
	}





	@Override
	public Integer countIssuesfromIssueList(final List<Long> issueListIds) {
		
		if (! issueListIds.isEmpty()){			
			Query query = currentSession().getNamedQuery("issueList.countIssues");
			query.setParameterList("issueListIds", issueListIds);
			Long result = (Long)query.uniqueResult();
			
			return result.intValue();
		}
		else{
			return 0;
		}
		
	}

	
	/*
	 * the process is the following :
	 *  
	 *  - 1 : get the sorted/filtered issues from them. Those issues are now sorted regardless of the owning entity.
	 *  - 2 : from the result of 1, get which ones are from the Execution and which ones are from its steps.
	 *  - 3 : from the two list resulting of 2, merge them in a single one and re-sort them again. The result should be ordered
	 *  just like the list in 1.
	 * 
	 * 
	 * Note : that code may sound weird and hairy, but preferable to 1/ get all the data from the db and 2/ sort them here. In the current
	 * implementation do most of the sorting job, and the list we sort here was already narrowed.
	 * 
	 * **********************************************************************************************************************************
	 * 
	 * TODO : this whole code is stupid and I apologize for it. Just use the damn hibernate abilities : cycle through all bugged entities, merge their list, 
	 * sort the result, be done with it !
	 * 
	 */
	@Override
	public List<IssueOwnership<Issue>> findIssuesWithOwner(Bugged entity,
			CollectionSorting sorter) {
		
		List<Long> issueListIds = entity.getAllIssueListId();
		List<Class<? extends Bugged>> possibleOwnerClasses = getActualClasses(entity.getAllBuggeds());
		
		//step 1
		List<Issue> sortedOrphanIssues = findIssuesFromIssueList(issueListIds, sorter);
		
		//step 2
		List<Long> sortedOprhanIds = collectIds(sortedOrphanIssues);
		
		List<IssueOwnership<Issue>> pairedIssues = new LinkedList<IssueOwnership<Issue>>();
		
		for (Class<? extends Bugged> clazz : possibleOwnerClasses){
			pairedIssues.addAll(findIssueOwnerOfClass(sortedOprhanIds, clazz));
		}
		
		//step 3
		
		Comparator<IssueOwnership<Issue>> comparator = buildComparator(sorter);
		
		Collections.sort(pairedIssues, comparator);
			
		//done
		return pairedIssues;
		
	}

	


	
	/* **************************** private code *********************************** */

	private Comparator<IssueOwnership<Issue>> buildComparator(CollectionSorting sorter){
		String sortingOrder = sorter.getSortingOrder();
		
		if (sortingOrder.equals("asc")){
			return new Comparator<IssueOwnership<Issue>>(){
				@Override
				public int compare(IssueOwnership<Issue> arg0,
						IssueOwnership<Issue> arg1) {
					return (int) (arg0.getIssue().getId() - arg1.getIssue().getId());
				}				
			};
		}
		else{
			return new Comparator<IssueOwnership<Issue>>(){
				@Override
				public int compare(IssueOwnership<Issue> arg0,
						IssueOwnership<Issue> arg1) {
					return (int) (arg1.getIssue().getId() - arg0.getIssue().getId());
				}				
			};			
		}
	}
	
	
	/*
	 * returns the list of classes represented by a given list of Bugged entities .
	 * the list will contain each class only once. 
	 */
	
	private List<Class<? extends Bugged>> getActualClasses(List<Bugged> buggeds){
		List<Class<? extends Bugged>> classes = new LinkedList<Class<? extends Bugged>>();
		
		for (Bugged bugged : buggeds){
			Class<? extends Bugged> clazz = bugged.getClass();
			if (! classes.contains(clazz)){
				classes.add(clazz);
			}
		}
		
		return classes;
	}
	
	private List<Long> collectIds(List<Issue> issues){
		List<Long> ids = new LinkedList<Long>();
		for (Issue issue : issues){
			ids.add(issue.getId());
		}
		return ids;
	}


	/*
	 * will fetch all the issues that belong to the IssueList(s) which id is within the id list.
	 * 
	 * 
	 * @param issueListIds the list of the ids of the IssueList
	 * @param sorter will sort and filter the result set
	 * @return a non-null but possibly empty list of Issue.
	 */
	protected List<Issue> findIssuesFromIssueList(final List<Long> issueListIds,
			final CollectionSorting sorter) {

		List<Issue> result = new LinkedList<Issue>();
		
		if (! issueListIds.isEmpty()){
			
			Criteria crit = currentSession().createCriteria(IssueList.class, "IssueList")
											.createAlias("IssueList.issues", "Issue")
											.add(Restrictions.in("IssueList.id", issueListIds))
											.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
			
			String sortBy = sorter.getSortedAttribute();
			String sortDirection = sorter.getSortingOrder();
			
			if (sortDirection.equals("asc")){
				crit = crit.addOrder(Order.asc(sortBy));
			}
			else{
				crit = crit.addOrder(Order.desc(sortBy));
			}
			
			crit = crit.setFirstResult(sorter.getFirstItemIndex())
						.setMaxResults(sorter.getMaxNumberOfItems());
			
			
			List<Map<String,?>> rawResult = crit.list();
			
			
			ListIterator<Map<String, ?>> iterator = rawResult.listIterator();
			
			while ( iterator.hasNext() ) {
			    Map<String,?> map = iterator.next();
			    Issue issue = (Issue)map.get("Issue");
			    
			    result.add(issue);
			}
		}
		
		return result;
		
	}
	
	
	/*
	 * Will fetch all the issues which is is within the given id list, and pair them with their owner. The issue will be part of the 
	 * result set if and only if its owner is of the given class. 
	 * 
	 * @param <X> the actual implementing class of Bugged that own the issues 
	 * @param buggedIds the list of id of the issues we look for
	 * @param concreteClass the Class object representing <X>
	 * @return a non null but possibly empty list of IssueOwnership, each of them existing if and only if an instance of <X> was found.
	 */
	protected <X extends Bugged> List<IssueOwnership<Issue>> findIssueOwnerOfClass(
			List<Long> issueIds, Class<X> concreteClass) {
		
		String shortName = concreteClass.getSimpleName();


		List<IssueOwnership<Issue>> result = new LinkedList<IssueOwnership<Issue>>();
		
		//due to a bug of of MySqlDialect regarding Restrictions.in(Collection)
		//(see bug http://opensource.atlassian.com/projects/hibernate/browse/HHH-2776)
		//we have to handle the case where the issueIds list is empty separately. The best is 
		//not to perform the operation at all if we aren't looking for any issue (ie, empty list)
		
		if (issueIds.size() > 0){
				
			Criteria crit = currentSession().createCriteria(concreteClass, shortName)
											.createAlias("issueList", "issueList")
											.createAlias("issueList.issues", "issue")
											.add(Restrictions.in("issue.id", issueIds))
											.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
			
			
			List<Map<String,?>> rawResult = crit.list();
			
			
			ListIterator<Map<String, ?>> iterator = rawResult.listIterator();
			
			while ( iterator.hasNext() ) {
			    Map<String,?> map = iterator.next();
			    Issue issue = (Issue)map.get("issue");
			    Bugged bugged = (Bugged)map.get(shortName);
			    
			    IssueOwnership<Issue> ownership = new IssueOwnership<Issue>(issue, bugged);
			    result.add(ownership);
			}
		}
		return result;
		
	}
	


	
}
