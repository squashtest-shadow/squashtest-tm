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

import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus;
import org.squashtest.tm.validation.ValidatorFactoryBean;

class TestCaseValidator {

	private Model model;
	private Validator validator = ValidatorFactoryBean.getInstance().getValidator();

	Model getModel() {
		return model;
	}

	void setModel(Model model) {
		this.model = model;
	}


	/**
	 *  those checks are performed for a test case for any type of operations.
	 *  
	 *  It checks : 
	 *  - the path is well formed (failure)
	 *  - the test case has a name (failure)
	 *  - the project exists (failure)
	 *  - the size of fields that are restricted in size  (warning)
	 *  - the format of the custom fields (lists, dates and checkbox) (warning)
	 * 
	 * @param target
	 * @param testCase
	 * @param cufValues
	 * @return
	 */
	LogTrain basicTestCaseChecks(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues){
		
		LogTrain logs = new LogTrain();
		
		// 1 - path must be supplied and and well formed
		if (! target.isWellFormed()){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.MALFORMED_PATH));
		}
		
		// 2 - name must be supplied
		if (StringUtils.isBlank(testCase.getName())){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.NO_NAME));
		}
		
		// 3 - the project actually exists
		TargetStatus projectStatus = model.getProjectStatus(target.getProject()); 
		if (projectStatus.getStatus() != Existence.EXISTS){
			logs.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.PROJECT_DO_NOT_EXIST));
		}
		
		// 4 - fields with restricted size are indeed restricted
		
		/*
		 * L'utilisation d'un validateur va masquer l'origine des problèmes et simplement 
		 * renvoyer un message d'erreur. Par exemple impossible de savoir si un champ est 
		 * malformé à cause de sa taille, ou s'il est null etc.
		 * 
		 * De deux choses l'une :
		 * 	- soit on code en dur la validation sur la taille (actuellement de 0 à 255), 
		 *  - soit on retourne les messages de validation tel quel 
		 */
		
		Set<ConstraintViolation<TestCase>> validationErrors= validator.validate(testCase);
		for (ConstraintViolation<TestCase> violation : validationErrors){
			violation.getConstraintDescriptor().
		}
		
		
		return logs;
	}
	
}
