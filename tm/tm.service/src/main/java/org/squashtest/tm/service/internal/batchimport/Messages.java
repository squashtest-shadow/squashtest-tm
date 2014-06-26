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

public final class Messages {

	public static final String ERROR_MALFORMED_PATH = "message.import.log.error.field.malformedPath";
	public static final String ERROR_FIELD_MANDATORY = "message.import.log.error.mandatoryColumn";
	public static final String ERROR_MANDATORY_CUF = "message.import.log.error.cuf.noValueForMandatoryCuf";
	public static final String ERROR_PROJECT_NOT_EXIST = "message.import.log.error.tc.tcPath.projectNotFound";
	public static final String ERROR_MAX_SIZE = "message.import.log.error.field.maxSize";
	public static final String ERROR_CALLED_TC_NOT_FOUND = "message.import.log.error.tc.callStep.calledTcNotFound";
	public static final String ERROR_CALLED_STEP_WRONG_FORMAT = "message.import.log.error.tc.callStep.wrongFormat";

	public static final String ERROR_UNPARSABLE_CHECKBOX = "message.import.log.error.field.notBoolean";
	public static final String ERROR_UNPARSABLE_DATE = "message.import.log.error.field.wrongDateFormat";
	public static final String ERROR_UNPARSABLE_OPTION = "message.import.log.error.listField.wrongValueForListField";
	public static final String ERROR_UNPARSABLE_INTEGER = "message.import.log.error.field.notInteger";
	public static final String ERROR_GENERIC_UNPARSABLE = "message.import.log.error.cannotParse";
	public static final String ERROR_FUNKY_CELL_TYPE = "message.import.log.error.unexpectedCellType";

	public static final String ERROR_UNKNOWN_CUF_TYPE = "message.import.log.cuf.unknowntype";
	public static final String ERROR_TC_ALREADY_EXISTS = "message.import.log.error.tc.alreadyexists";
	public static final String ERROR_NO_PERMISSION = "message.import.log.error.unsuficientRight";
	public static final String ERROR_INCONSISTENT_PATH_AND_NAME = "message.import.log.error.tc.inconsistentNameAndPath";
	public static final String ERROR_TC_NOT_FOUND = "message.import.log.error.tc.notFound";
	public static final String ERROR_TC_CANT_RENAME = "message.import.log.error.tc.cantrename";
	public static final String ERROR_REMOVE_CALLED_TC = "message.import.log.error.tc.cannotRemoveCalledTestCase";
	public static final String ERROR_STEPINDEX_EMPTY = "message.import.log.error.tc.tcStep.empty";
	public static final String ERROR_STEPINDEX_NEGATIVE = "message.import.log.error.tc.tcStep.negative";
	public static final String ERROR_STEPINDEX_OVERFLOW = "message.import.log.error.tc.tcStep.numberOverNextPosition";
	public static final String ERROR_STEP_NOT_EXISTS = "message.import.log.error.tc.tcStep.notexists";
	public static final String ERROR_NOT_A_CALLSTEP = "message.import.log.error.tc.tcStep.notacallstep";
	public static final String ERROR_NOT_AN_ACTIONSTEP = "message.import.log.error.tc.tcStep.notanactionstep";
	public static final String ERROR_CYCLIC_STEP_CALLS = "message.import.log.error.tc.callStep.cyclicCalls";
	public static final String ERROR_UNEXPECTED_ERROR = "message.import.log.error.unexpectederror";
	public static final String ERROR_PARAMETER_OWNER_NOT_FOUND = "message.import.log.error.tc.tcOwnerNotFound";
	public static final String ERROR_DATASET_PARAM_OWNER_NOT_FOUND = "message.import.log.error.tc.paramValueParamOwnerNotFound";
	public static final String ERROR_PARAMETER_ALREADY_EXISTS = "message.import.log.error.tc.param.alreadyexists";
	public static final String ERROR_PARAMETER_CONTAINS_FORBIDDEN_CHARACTERS = "message.import.log.error.tc.param.wrongFormat";
	public static final String ERROR_PARAMETER_NOT_FOUND = "message.import.log.error.tc.param.notFound";
	public static final String ERROR_DATASET_NOT_FOUND = "message.import.log.error.tc.dataset.notFound";
	public static final String ERROR_DATASET_PARAMETER_MISMATCH = "message.import.log.error.tc.dataset.paramOwnerNotFound";
	public static final String ERROR_TC_USER_NOT_FOUND = "message.import.log.error.tc.userNotFound";
	public static final String ERROR_CALL_NOT_READABLE = "message.import.log.error.tc.callStep.calledTcNotReadable";

	public static final String IMPACT_MAX_SIZE = "message.import.log.impact.truncatedValue";
	public static final String IMPACT_DEFAULT_VALUE = "message.import.log.impact.useDefaultValue";
	public static final String IMPACT_USE_CURRENT_DATE ="message.import.log.impact.useCurrentDate";
	public static final String IMPACT_NO_CHANGE = "message.import.log.impact.fieldNotChange";
	public static final String IMPACT_TC_WITH_SUFFIX = "message.import.log.impact.tc.renamed";
	public static final String IMPACT_TC_CREATED = "message.import.log.impact.tc.created";
	public static final String IMPACT_STEP_CREATED_LAST = "message.import.log.impact.testStepAddedAtMaxPosition";
	public static final String IMPACT_PARAM_UPDATED = "message.import.log.impact.paramupdate";
	public static final String IMPACT_PARAM_CREATED = "message.import.log.impact.paramcreated";
	public static final String IMPACT_DATASET_CREATED = "message.import.log.impact.dscreated";
	public static final String IMPACT_CALL_AS_ACTION_STEP = "message.import.log.impact.callStepImportedAsActionStep";
	public static final String IMPACT_USE_CURRENT_LOGIN = "message.import.log.impact.useCurrentLogin";
	public static final String IMPACT_FIELD_NOT_CHANGED = "message.import.log.impact.fieldNotChange";
	private Messages(){
		super();
	}
}
