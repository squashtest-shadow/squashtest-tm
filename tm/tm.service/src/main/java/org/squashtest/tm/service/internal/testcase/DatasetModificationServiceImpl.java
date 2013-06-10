/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.service.internal.testcase;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.service.internal.repository.DatasetDao;
import org.squashtest.tm.service.internal.repository.DatasetParamValueDao;
import org.squashtest.tm.service.internal.repository.ParameterDao;
import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.squashtest.tm.service.testcase.ParameterModificationService;

@Service("squashtest.tm.service.DatasetModificationService")
public class DatasetModificationServiceImpl implements DatasetModificationService {

	@Inject
	private DatasetDao datasetDao;

	@Inject
	private ParameterDao parameterDao;
	
	@Inject 
	private DatasetParamValueDao datasetParamValueDao;

	@Inject 
	private ParameterModificationService parameterModificationService;
	
	
	@Override
	public void persist(Dataset dataset) {	

		updateDatasetParameters(dataset);
		datasetDao.persist(dataset);
	}

	
	@Override
	public void remove(Dataset dataset) {
		this.datasetDao.delete(dataset);
	}


	@Override
	public void removeById(long datasetId) {
	
		Dataset dataset = this.datasetDao.findById(datasetId);
		this.datasetDao.delete(dataset);
	}
	
	@Override
	public void changeName(long datasetId, String newName) {
		
		Dataset dataset = this.datasetDao.findById(datasetId);
		dataset.setName(newName);

	}

	@Override
	public void changeParamValue(long datasetId, long paramId, String value) {
		
		Dataset dataset = this.datasetDao.findById(datasetId);
		Parameter parameter = this.parameterDao.findById(paramId);
		DatasetParamValue datasetParamValue = findOrAddParameter(dataset, parameter);
		datasetParamValue.setDataset(dataset);
		datasetParamValue.setParameter(parameter);
		datasetParamValue.setParamValue(value);
		updateDatasetParameters(dataset);
	}
	
	public List<Dataset> getAllDatasetByTestCase(long testCaseId){
		return this.datasetDao.findAllDatasetsByTestCase(testCaseId);
	}
	
	private DatasetParamValue findDatasetParamValue(Dataset dataset, Parameter parameter){
		
		DatasetParamValue result = null;
		Set<DatasetParamValue> datasetParamValues = dataset.getParameterValues();
		for(DatasetParamValue datasetParamValue : datasetParamValues){
			if(datasetParamValue.getParameter().equals(parameter)){
				result = datasetParamValue;
			}
		}
		return result;
	}
	
	private DatasetParamValue findOrAddParameter(Dataset dataset, Parameter parameter){
		DatasetParamValue datasetParamValue = findDatasetParamValue(dataset, parameter);
		if(datasetParamValue == null){
			datasetParamValue = new DatasetParamValue(parameter, dataset);
		}
		return datasetParamValue;
	}
	private void updateDatasetParameters(Dataset dataset){
		Long testCaseId = dataset.getTestCase().getId();
		List<Parameter> parameters = parameterModificationService.getAllforTestCase(testCaseId);
		for(Parameter parameter : parameters){
			findOrAddParameter(dataset, parameter);
		}
	}
}
