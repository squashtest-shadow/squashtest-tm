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

final class Messages {

	static final String ERROR_MALFORMED_PATH = "message.import.log.error.field.malformedPath";
	static final String ERROR_FIELD_MANDATORY = "message.import.log.error.mandatoryColumn";
	static final String ERROR_MANDATORY_CUF = "message.import.log.error.cuf.noValueForMandatoryCuf";
	static final String ERROR_PROJECT_NOT_EXIST = "message.import.log.error.tc.tcPath.projectNotFound";
	static final String ERROR_MAX_SIZE = "message.import.log.error.field.maxSize";
	static final String ERROR_UNPARSABLE_CHECKBOX = "message.import.log.error.field.notBoolean";
	static final String ERROR_UNPARSABLE_DATE = "message.import.log.error.field.wrongDateFormat";
	static final String ERROR_UNPARSABLE_OPTION = "message.import.log.error.listField.wrongValueForListField";
	static final String ERROR_UNKNOWN_CUF_TYPE = "message.import.log.cuf.unknowntype";
	static final String ERROR_TC_ALREADY_EXISTS = "message.import.log.error.tc.alreadyexists";
	static final String ERROR_NO_PERMISSION = "message.import.log.error.unsuficientRight";
	static final String ERROR_INCONSISTENT_PATH_AND_NAME = "message.import.log.error.tc.inconsistentNameAndPath";
	static final String ERROR_TC_NOT_FOUND = "message.import.log.error.tc.notFound";
	static final String ERROR_TC_CANT_RENAME = "message.import.log.error.tc.cantrename";
	static final String ERROR_REMOVE_CALLED_TC = "message.import.log.error.tc.cannotRemoveCalledTestCase";
	static final String ERROR_STEPINDEX_EMPTY = "message.import.log.error.tc.tcStep.empty"; 
	static final String ERROR_STEPINDEX_NEGATIVE = "message.import.log.error.tc.tcStep.negative";
	static final String ERROR_STEPINDEX_OVERFLOW = "message.import.log.error.tc.tcStep.numberOverNextPosition";
	static final String ERROR_NOT_A_CALLSTEP = "message.import.log.error.tc.tcStep.notacallstep";
	static final String ERROR_NOT_AN_ACTIONSTEP = "message.import.log.error.tc.tcStep.notanactionstep";
	static final String ERROR_CYCLIC_STEP_CALLS = "message.import.log.error.tc.callStep.cyclicCalls";
	static final String ERROR_UNEXPECTED_ERROR = "message.import.log.error.unexpectederror";
	static final String ERROR_PARAMETER_ALREADY_EXISTS = "message.import.log.error.tc.param.alreayexists";
	static final String ERROR_PARAMETER_NOT_FOUND = "message.import.log.error.tc.param.notFound";
	
	
	static final String IMPACT_MAX_SIZE = "message.import.log.impact.truncatedValue";
	static final String IMPACT_DEFAULT_VALUE = "message.import.log.impact.useDefaultValue";
	static final String IMPACT_NO_CHANGE = "message.import.log.impact.fieldNotChange";
	static final String IMPACT_TC_WITH_SUFFIX = "message.import.log.impact.tc.renamed";
	static final String IMPACT_TC_CREATED = "message.import.log.impact.tc.created";
	static final String IMPACT_STEP_CREATED_LAST = "message.import.log.impact.testStepAddedAtMaxPosition";
	static final String IMPACT_PARAM_UPDATED = "message.import.log.impact.paramupdate";
	
	private Messages(){
		super();
	}
}
