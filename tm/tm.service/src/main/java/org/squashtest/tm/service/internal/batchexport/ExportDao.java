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

import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.service.internal.batchexport.ExportModel.CustomField;
import org.squashtest.tm.service.internal.batchexport.ExportModel.ParameterModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestCaseModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestStepModel;
import org.squashtest.tm.service.internal.repository.hibernate.EasyConstructorResultTransformer;

@Repository
public class ExportDao {

	@Inject
	private SessionFactory factory;

	
	public ExportDao(){
		super();
	}
	
	
	public ExportModel findModel(List<Long> tclnIds){
		
		ExportModel model = new ExportModel();
		
		List<TestCaseModel> tclnModels = findTestCaseModels(tclnIds);
		List<TestStepModel> stepModels = findStepsModel(tclnIds);
		List<ParameterModel> paramModels = findParametersModel(tclnIds);
		
		model.setTestCases(tclnModels);
		model.setTestSteps(stepModels);
		model.setParameters(paramModels);
				
		return model;
		
	}
	
	private List<TestCaseModel> findTestCaseModels(List<Long> tclnIds){
		
		Session session = getStatelessSession();
		List<TestCaseModel> models = new ArrayList<TestCaseModel>(tclnIds.size());
		List<TestCaseModel> buffer;
		

		// get the models
		Query q1 = session.getNamedQuery("testCase.excelExportDataFromFolder"); 
		q1.setParameterList("testCaseIds", tclnIds, LongType.INSTANCE);
		q1.setResultTransformer(new EasyConstructorResultTransformer(TestCaseModel.class));
		buffer = q1.list();		
		models.addAll(buffer);
		
		Query q2 = session.getNamedQuery("testCase.excelExportDataFromLibrary"); 
		q2.setParameterList("testCaseIds", tclnIds, LongType.INSTANCE);
		q2.setResultTransformer(new EasyConstructorResultTransformer(TestCaseModel.class));
		buffer = q2.list();		
		models.addAll(buffer);
		
		//get the cufs
		Query q3 = session.getNamedQuery("testCase.excelExportCUF");
		q3.setParameterList("tcIds", tclnIds, LongType.INSTANCE);
		q3.setResultTransformer(new EasyConstructorResultTransformer(CustomField.class));
		List<CustomField> cufModels = q3.list();
		
		// add them to the test case models
		for (TestCaseModel model : models){
			Long id = model.getId();
			ListIterator<CustomField> cufIter = cufModels.listIterator();
			
			while (cufIter.hasNext()){
				CustomField cuf = cufIter.next();
				if (id.equals(cuf.getOwnerId())){
					model.addCuf(cuf);
					cufIter.remove();
				}
			}
		}

		// end
		return models;
	
	}
	
	
	private List<TestStepModel> findStepsModel(List<Long> tcIds){
		
		Session session = getStatelessSession();
		List<TestStepModel> models = new ArrayList<TestStepModel>(tcIds.size());
		List<TestStepModel> buffer;
		
		Query q1 = session.getNamedQuery("testStep.excelExportActionSteps");
		q1.setParameterList("testCaseIds", tcIds, LongType.INSTANCE);
		q1.setResultTransformer(new EasyConstructorResultTransformer(TestStepModel.class));
		buffer = q1.list();
		models.addAll(buffer);
		
		Query q2 = session.getNamedQuery("testStep.excelExportCallSteps"); 
		q2.setParameterList("testCaseIds", tcIds, LongType.INSTANCE);
		q2.setResultTransformer(new EasyConstructorResultTransformer(TestStepModel.class));
		buffer = q2.list();		
		models.addAll(buffer);
		
		//get the cufs
		Query q3 = session.getNamedQuery("testStep.excelExportCUF");
		q3.setParameterList("testCaseIds", tcIds, LongType.INSTANCE);
		q3.setResultTransformer(new EasyConstructorResultTransformer(CustomField.class));
		List<CustomField> cufModels = q3.list();
		
		// add them to the test case models
		for (TestStepModel model : models){
			Long id = model.getId();
			ListIterator<CustomField> cufIter = cufModels.listIterator();
			
			while (cufIter.hasNext()){
				CustomField cuf = cufIter.next();
				if (id.equals(cuf.getOwnerId())){
					model.addCuf(cuf);
					cufIter.remove();
				}
			}
		}	
	
		
		// done
		return models;
	}
	
	private List<ParameterModel> findParametersModel(List<Long> tcIds){
		Query q = getStatelessSession().getNamedQuery("parameter.excelExport");
		q.setParameterList("testCaseIds", tcIds);
		q.setResultTransformer(new EasyConstructorResultTransformer(ParameterModel.class));
		return q.list();
	}
	
	
	private Session getStatelessSession(){
		Session s = factory.getCurrentSession();
		s.setFlushMode(FlushMode.MANUAL);
		return s;
	}
	
}
