/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.service.internal.batchexport.ExportModel.CoverageModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.CustomField;
import org.squashtest.tm.service.internal.batchexport.ExportModel.DatasetModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.ParameterModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestCaseModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestStepModel;
import org.squashtest.tm.service.internal.batchexport.RequirementExportModel.RequirementModel;
import org.squashtest.tm.service.internal.library.HibernatePathService;
import org.squashtest.tm.service.internal.library.PathService;
import org.squashtest.tm.service.internal.repository.hibernate.EasyConstructorResultTransformer;

@Repository
public class ExportDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExportDao.class);
	
	@Inject
	private SessionFactory factory;

	@Inject
	private PathService pathService;
	
	public ExportDao(){
		super();
	}
	
	
	public RequirementExportModel findAllRequirementModel(List<Long> versionIds){
		RequirementExportModel model = new RequirementExportModel();
		List<RequirementModel> requirementsModel = findRequirementModel(versionIds);
		model.setRequirementsModels(requirementsModel);
		return model;
	}
	
	public ExportModel findModel(List<Long> tclnIds){
		
		ExportModel model = new ExportModel();
		
		List<TestCaseModel> tclnModels = findTestCaseModels(tclnIds);
		List<TestStepModel> stepModels = findStepsModel(tclnIds);
		List<ParameterModel> paramModels = findParametersModel(tclnIds);
		List<DatasetModel> datasetModels = findDatasetsModel(tclnIds);
		List<CoverageModel> coverageModels = findCoverageModel(tclnIds);
		
		setPathForCoverage(coverageModels);
	
		model.setCoverages(coverageModels);
		model.setTestCases(tclnModels);
		model.setTestSteps(stepModels);
		model.setParameters(paramModels);
		model.setDatasets(datasetModels);		
		
		return model;
		
	}
	
	private void setPathForCoverage(List<CoverageModel> coverageModels) {
		for (CoverageModel model : coverageModels){
			model.setReqPath(getRequirementPath(model.getRequirementId(), model.getRequirementProjectName()));
			model.setTcPath(pathService.buildTestCasePath(model.getTcId()));
		}
		
	}


	private List<CoverageModel> findCoverageModel(List<Long> tcIds) {
		return findModels(getStatelessSession(), "testCase.excelExportCoverage", tcIds, CoverageModel.class);
	}


	private List<TestCaseModel> findTestCaseModels(List<Long> tclnIds){
		
		Session session = getStatelessSession();
		List<TestCaseModel> models = new ArrayList<TestCaseModel>(tclnIds.size());
		List<TestCaseModel> buffer;
		

		// get the models
		buffer = findModels(session, "testCase.excelExportDataFromFolder", tclnIds, TestCaseModel.class);
		models.addAll(buffer);
		
		buffer = findModels(session, "testCase.excelExportDataFromLibrary", tclnIds, TestCaseModel.class);
		models.addAll(buffer);
		
		//get the cufs
		List<CustomField> cufModels = findModels(session, "testCase.excelExportCUF", tclnIds, CustomField.class);
		
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
		
		
		buffer = findModels(session, "testStep.excelExportActionSteps", tcIds, TestStepModel.class);
		models.addAll(buffer);
		
		buffer = findModels(session, "testStep.excelExportCallSteps", tcIds, TestStepModel.class);	
		models.addAll(buffer);
		
		//get the cufs
		List<CustomField> cufModels = findModels(session, "testStep.excelExportCUF", tcIds, CustomField.class);
		
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
		return findModels(getStatelessSession(), "parameter.excelExport", tcIds, ParameterModel.class);
	}
	
	
	private List<DatasetModel> findDatasetsModel(List<Long> tcIds){
		return findModels(getStatelessSession(), "dataset.excelExport", tcIds, DatasetModel.class);
	}
	
	
	@SuppressWarnings("unchecked")
	private <R> List<R> findModels(Session session, String query, List<Long> tcIds, Class<R> resclass){
		Query q = session.getNamedQuery(query);
		q.setParameterList("testCaseIds", tcIds, LongType.INSTANCE);
		q.setResultTransformer(new EasyConstructorResultTransformer(resclass));
		return q.list();
	}
	
	private Session getStatelessSession(){
		Session s = factory.getCurrentSession();
		s.setFlushMode(FlushMode.MANUAL);
		return s;
	}
	
	@SuppressWarnings("unchecked")
	private List<RequirementModel> findRequirementModel(List<Long> versionIds) {
		Session session = getStatelessSession();
		Query q = session.getNamedQuery("requirement.findVersionsModels");
		q.setParameterList("versionIds", versionIds, LongType.INSTANCE);
		q.setResultTransformer(new EasyConstructorResultTransformer(RequirementModel.class));
		List<RequirementModel> requirementModels = q.list();
		getOtherProperties(requirementModels);
		return requirementModels;
	}
	
	private void getOtherProperties(List<RequirementModel> requirementModels){
		for (RequirementModel requirementModel : requirementModels) {
			requirementModel.setPath(getPathAsString(requirementModel));
			getModelRequirementPosition(requirementModel);
			getModelRequirementCUF(requirementModel);
		}
	}
	

	@SuppressWarnings("unchecked")
	private void getModelRequirementCUF(RequirementModel requirementModel) {
		Session session = getStatelessSession();
		Query q = session.getNamedQuery("requirement.excelRequirementExportCUF");
		q.setLong("requirementVersionId", requirementModel.getId());
		q.setResultTransformer(new EasyConstructorResultTransformer(CustomField.class));
		requirementModel.setCufs(q.list());
	}

	private void getModelRequirementPosition(RequirementModel requirementModel) {
			Long reqId = requirementModel.getRequirementId();
			int index = getRequirementPositionInLibrary(reqId);
			if (index == 0) {
				index = getRequirementPositionInFolder(reqId);
			}
			if (index == 0) {
				index = getPositionChildrenRequirement(reqId);
			}
			requirementModel.setRequirementIndex(index);
	}


	private int getPositionChildrenRequirement(Long reqId) {
		Session session = getStatelessSession();
		Query qFolder = session.getNamedQuery("requirement.findVersionsModelsIndexChildrenRequirement");
		qFolder.setLong("requirementId",reqId);
		Object result =  qFolder.uniqueResult();
		if (result!=null) {
			return (int) result;
		}
		return 0;
	}


	private int getRequirementPositionInFolder(Long reqId) {
		Session session = getStatelessSession();
		Query qFolder = session.getNamedQuery("requirement.findVersionsModelsIndexInFolder");
		qFolder.setLong("requirementId",reqId);
		Object result =  qFolder.uniqueResult();
		if (result!=null) {
			return (int) result;
		}
		return 0;
	}

	
	private int getRequirementPositionInLibrary(Long reqId){
		Session session = getStatelessSession();
		Query qLibrary = session.getNamedQuery("requirement.findVersionsModelsIndexInLibrary");
		qLibrary.setLong("requirementId",reqId);
		Object result =  qLibrary.uniqueResult();
		if (result!=null) {
			return (int) result;
		}
		return 0;
	}
	

	public String getPathAsString(RequirementModel exportedRequirement) {	
		return getRequirementPath(exportedRequirement.getRequirementId(), exportedRequirement.getProjectName());
	}

	
	private String getRequirementPath(Long requirementId, String requirementProjectName){
		StringBuffer sb = new StringBuffer(HibernatePathService.PATH_SEPARATOR);
		sb.append(requirementProjectName);
		sb.append(HibernatePathService.PATH_SEPARATOR);
		String pathFromFolder = getPathFromFolder(requirementId);
		String pathFromParents = getPathFromParentsRequirements(requirementId);
		sb.append(pathFromFolder);
		sb.append(pathFromParents);
		return HibernatePathService.escapePath(sb.toString());
		
		
	}

	private String getPathFromParentsRequirements(Long requirementId) {
		Session session = getStatelessSession();
		Query q = session.getNamedQuery("requirement.findReqParentPath");
		q.setParameter("requirementId", requirementId);
		Object result = q.uniqueResult();
		if (result!=null) {
			return result.toString();
		}
		return "";
	}


	private String getPathFromFolder(Long requirementId) {
		Session session = getStatelessSession();
		Query q = session.getNamedQuery("requirement.findReqFolderPath");
		q.setParameter("requirementId", requirementId);
		Object result = q.uniqueResult();
		if (result!=null) {
			return result.toString()+HibernatePathService.PATH_SEPARATOR;
		}
		return "";
	}
	
}
