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
package org.squashtest.tm.service.internal.batchimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;

import net.sf.cglib.core.CollectionUtils;
import net.sf.cglib.core.Transformer;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService;


@Repository
@Scope("prototype")
public class Model {

	@Inject
	private SessionFactory sessionFactory;
	
	@Inject
	private TestCaseLibraryFinderService finderService;
	
	
	private Map<TestCaseTarget, Long> testCaseIdsByTarget = new HashMap<TestCaseTarget, Long>();
	

	/**
	 * returns null if not found
	 * 
	 * @param target
	 * @return
	 */
	public Long getTestCaseId(TestCaseTarget target){
		
		if (! testCaseIdsByTarget.containsKey(target)){
			try{
				String path = target.getPath();
				Long tcId = finderService.findNodeIdByPath(path);
				testCaseIdsByTarget.put(target, tcId);
			}
			catch (NoSuchElementException ex){
				testCaseIdsByTarget.put(target, null);
			}
		}
			
		return testCaseIdsByTarget.get(target);
		
	}
	
	
	public TestCase getTestCase(TestCaseTarget target){
		
		Long id = getTestCaseId(target);
		if (id == null) {
			return null;
		}
		else{
			return (TestCase)sessionFactory.getCurrentSession().load(TestCase.class, id);
		}
		
	}
	
	public boolean testCaseExists(TestCaseTarget target){
		Long id = getTestCaseId(target);
		return (id != null);
	}
	
	public void updateTarget(TestCaseTarget target, Long id){
		testCaseIdsByTarget.put(target, id);
	}
	
	
	/**
	 * When you have a batch of test cases you might need later on, it's better 
	 * to load them all at once. That's what this method is for.
	 * 
	 * @param targets
	 */
	@SuppressWarnings("unchecked")
	public void loadTargets(List<TestCaseTarget> targets){
		
		List<TestCaseTarget> uniques = uniqueList(targets);
		
		List<String> paths = CollectionUtils.transform(uniques, new Transformer() {
			@Override
			public Object transform(Object value) {
				return ((TestCaseTarget)value).getPath();
			}
		});
		
		List<Long> ids = finderService.findNodeIdsByPath(paths);
		for (int i=0; i< uniques.size(); i++){
			TestCaseTarget t = uniques.get(i);
			testCaseIdsByTarget.put(t, ids.get(i));
		}
	}
	
	private <O extends Object> List<O> uniqueList(Collection<O> orig){
		Set<O> filtered =  new LinkedHashSet(orig);
		return new ArrayList<O>(filtered);
	}
	
}
