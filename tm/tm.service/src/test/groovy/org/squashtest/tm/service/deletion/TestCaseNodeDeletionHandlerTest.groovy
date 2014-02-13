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
package org.squashtest.tm.service.deletion


import org.squashtest.tm.service.internal.deletion.TestCaseNodeDeletionHandlerImpl;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;

import spock.lang.Specification;

class TestCaseNodeDeletionHandlerTest extends Specification {

	TestCaseDao tcDao = Mock();
	TestCaseFolderDao fDao = Mock();
	
	TestCaseNodeDeletionHandlerImpl handler = new TestCaseNodeDeletionHandlerImpl();
	
	
	def setup(){
		handler.leafDao = tcDao;
		handler.folderDao = fDao;
	}
	
	
	private toListOfArray(List<List<Object>> inputList){
		def result = new ArrayList<Object[]>();
		
		for (List<Object> list  : inputList){
			def array = new Object[4]
			array[0]=list[0]
			array[1]=list[1]
			array[2]=list[2]
			array[3]=list[3]
			result.add(array)
		}
		
		return result
	}
	
	//if there is a groovy way to do that please tell me
	private boolean containsValue(List<Object[]> list, Object[] value){
		for (Object[] item : list){
			boolean match = true;
			for (int i=0;i<value.length;i++){
				if ( item[i] != value[i]){
					match=false;
					break;
				}
			}
			if (match) return true;
		}
		return false;
		
	}
	
	
	/*
	 * structure : 1 calls 11 and 12,
	 * 			   11 calls 21, 22 and 23,
	 * 			   12 calls 22, 23 and 24
	 * 			   23 calls 24
	 * 			   
	 * 	21, 22, 24 and 25 call no one.
	 *  1 and 25 are called by no one.
	 * 
	 */
	
	def "should find all the caller/called pairs"(){
		
	
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]
			def layer2 = [ 
							[11l, "11", 21l , "21"   ].toArray(), [11l, "11", 22l, "22"   ].toArray(), [11l, "11", 23l, "23"   ].toArray(),
							[12l, "12", 22l , "22"   ].toArray(), [12l, "12", 23l, "23"   ].toArray(), [12l, "12", 24l, "24"   ].toArray(),
							[23l, "23", 24l, "24"].toArray(),
							[null, null, 25l, "25" ].toArray()
						 ]
		and :
			tcDao.findTestCasesHavingCallerDetails( [21l, 22l, 23l, 24l, 25l]) >> layer2
			tcDao.findTestCasesHavingCallerDetails( [11l, 12l]) >> layer1
			tcDao.findTestCasesHavingCallerDetails( [1l]) >> layer0
			
		and :
			def expected = layer0 + layer1 + layer2 
			List.metaClass.containsValue = { Object[] arg -> return containsValue(delegate, arg)  }
			
		
		when :
			def result = handler.getAllCallerCalledPairs([21l, 22l, 23l, 24l, 25l])
		
		then :
			 for (Object[] item in expected){
				 result.containsValue(item)
			 }
		
		
	}
	
	
	

	
	
	
	
	def "should return a report about which nodes cannot be deleted and why (#1)"(){
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]
			def layer2 = [ 
							[11l, "11", 21l , "21"   ].toArray(), [11l, "11", 22l, "22"   ].toArray(), [11l, "11", 23l, "23"   ].toArray(),
							[12l, "12", 22l , "22"   ].toArray(), [12l, "12", 23l, "23"   ].toArray(), [12l, "12", 24l, "24"   ].toArray(),
							[23l, "23", 24l, "24"].toArray(),
							[null, null, 25l, "25" ].toArray()
						 ]
		and :
			tcDao.findTestCasesHavingCallerDetails( [21l, 22l, 23l, 24l, 25l]) >> layer2
			tcDao.findTestCasesHavingCallerDetails( [11l, 12l]) >> layer1
			tcDao.findTestCasesHavingCallerDetails( [1l]) >> layer0
		
		when :
			def report = handler.previewLockedNodes([21l, 22l, 23l, 24l, 25l]);
		
		then :
			report.nodeNames == ["21", "22", "23", "24"] as Set
			report.why == ["11", "12", "1"] as Set
		
		
	}
	
	
	def "should return a report about which nodes cannot be deleted and why (#2)"(){
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]

		and :
			tcDao.findTestCasesHavingCallerDetails( [11l, 12l]) >> layer1
			tcDao.findTestCasesHavingCallerDetails( [1l]) >> layer0
		
		when :
			def report = handler.previewLockedNodes([11l, 12l]);
		
		then :
			report.nodeNames == ["11", "12"] as Set
			report.why == ["1"] as Set
		
		
	}
	
	
	def "should return no report about which nodes cannot be deleted and why (#3)"(){
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]

		and :
			tcDao.findTestCasesHavingCallerDetails( [11l, 12l, 1l]) >> layer1 + layer0
		
		when :
			def report = handler.previewLockedNodes([11l, 12l, 1l]);
		
		then :
			report == null
	}
	
	
	def "should return really non deletable nodes (#1)"(){
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]
			def layer2 = [
						[11l, "11", 21l , "21"   ].toArray(), [11l, "11", 22l, "22"   ].toArray(), [11l, "11", 23l, "23"   ].toArray(),
						[12l, "12", 22l , "22"   ].toArray(), [12l, "12", 23l, "23"   ].toArray(), [12l, "12", 24l, "24"   ].toArray(),
						[23l, "23", 24l, "24"].toArray(),
						[null, null, 25l, "25" ].toArray()
					 ]
		and :
			tcDao.findTestCasesHavingCallerDetails( [21l, 22l, 23l, 24l, 25l]) >> layer2
			tcDao.findTestCasesHavingCallerDetails( [11l, 12l]) >> layer1
			tcDao.findTestCasesHavingCallerDetails( [1l]) >> layer0
			
			
		when :
			def reallyNonDeletables = handler.detectLockedNodes([21l, 22l, 23l, 24l, 25l])
			
		then :
			reallyNonDeletables == [21l, 22l, 23l, 24l]
		
		
	}
	
	
	def "should return really non deletable nodes (#2)"(){
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]

		and :
			tcDao.findTestCasesHavingCallerDetails( [11l, 12l]) >> layer1
			tcDao.findTestCasesHavingCallerDetails( [1l]) >> layer0
		
		when :
			def reallyNonDeletables = handler.detectLockedNodes([11l, 12l])
			
		then :
			reallyNonDeletables == [11l, 12l]
		
	}
	
	
	def "should return really non deletable nodes (#3)"(){
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]

		and :
			tcDao.findTestCasesHavingCallerDetails( [11l, 12l, 1l]) >> layer1 + layer0
		
		when :
			def reallyNonDeletables = handler.detectLockedNodes([11l, 12l, 1l])
			
		then :
			reallyNonDeletables == []
	}

	
	
}
