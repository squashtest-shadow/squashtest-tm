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
package org.squashtest.tm.service.internal.batchexport;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.internal.batchexport.ExportModel.CustomField;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestCaseModel;

@Component
public class ExportDao {

	@Inject
	private SessionFactory factory;

	
	public ExportDao(){
		super();
	}
	
	
	public ExportModel findModel(List<Long> tclnIds){
		
		ExportModel model = new ExportModel();
		
		List<TestCaseModel> tclnModels = findTestCaseModels(tclnIds);
		
		model.setTestCases(tclnModels);
		
		
		return model;
		
	}
	
	private List<TestCaseModel> findTestCaseModels(List<Long> tclnIds){
		
		Session session = factory.getCurrentSession();
		List<TestCaseModel> models = new ArrayList<TestCaseModel>(tclnIds.size());
		List<TestCaseModel> buffer;
		
		
		// get the models
		Query q = session.getNamedQuery("testCase.excelExportDataFromFolder"); 
		q.setParameterList("testCaseIds", tclnIds, LongType.INSTANCE);
		buffer = q.list();		
		models.addAll(buffer);
		
		q = session.getNamedQuery("testCase.excelExportDataFromLibrary"); 
		q.setParameterList("testCaseIds", tclnIds, LongType.INSTANCE);
		buffer = q.list();		
		models.addAll(buffer);
		
		//get the cufs
		q = session.getNamedQuery("testCase.excelExportCUF");
		q.setParameterList("tcIds", tclnIds, LongType.INSTANCE);
		List<CustomField> cufModels = q.list();
		
		// add them to the test case models
		for (TestCaseModel model : models){
			Long id = model.getId();
			ListIterator<CustomField> cufIter = cufModels.listIterator();
			
			while (cufIter.hasNext()){
				CustomField cuf = cufIter.next();
				if (id == cuf.getOwnerId()){
					model.addCuf(cuf);
					cufIter.remove();
				}
			}
		}

		// end
		return models;
	
	}
	
 
	
}
