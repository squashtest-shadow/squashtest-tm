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

import javax.inject.Inject;

import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.service.internal.repository.ParameterDao;

public class ParameterModificationServiceImpl implements  ParameterModificationService{

	@Inject
	ParameterDao parameterDao;
	
	@Override
	public List<Parameter> getAllforTestCase(long testCaseId) {
		return parameterDao.findAllByTestCase(testCaseId);
	}

	@Override
	public void persist(Parameter parameter) {
		this.parameterDao.persist(parameter);
	}

	@Override
	public void changeName(long parameterId, String newName) {

		Parameter parameter = this.parameterDao.findById(parameterId);
		parameter.setName(newName);
		
	}

	@Override
	public void changeDescription(long parameterId, String newDescription) {

		Parameter parameter = this.parameterDao.findById(parameterId);
		parameter.setDescription(newDescription);
	}

	@Override
	public void remove(Parameter parameter) {
		
		this.parameterDao.delete(parameter);
	}

	@Override
	public void removeById(long parameterId) {
		
		Parameter parameter = this.parameterDao.findById(parameterId);
		this.parameterDao.delete(parameter);
	}
	
	@Override
	public List<Parameter> checkForParamsInStep(long stepId) {
		// TODO Auto-generated method stub
		return null;
	}

}
