/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
package org.squashtest.tm.domain.dataset

import org.squashtest.tm.domain.parameter.GlobalParameter
import org.squashtest.tm.domain.testcase.DatasetParamValue
import spock.lang.Specification

/**
 * @author aguilhem
 */
class GlobalDatasetTest extends Specification {

	def "should copy globalDataset"(){
		when:
		GlobalDataset source = new GlobalDataset()
		source.name = "source"
		source.description = "source description"
		source.reference = "SC1"
		GlobalParameter param = new GlobalParameter()
		param.name = "param"
		param.description = "param description"
		DatasetParamValue paramValue = new DatasetParamValue()
		paramValue.parameter = param
		paramValue.dataset = source
		paramValue.paramValue = "value"
		GlobalParameter param2 = new GlobalParameter();
		param2.name = "param2"
		param2.description = "param2 description"
		DatasetParamValue paramValue2 = new DatasetParamValue()
		paramValue2.parameter = param2
		paramValue2.dataset = source
		paramValue2.paramValue = "value"

		source.addGlobalParameter(param)
		source.addParameterValue(paramValue2)
		source.addGlobalParameter(param2)
		source.addParameterValue(paramValue)

		GlobalDataset copy = source.createCopy()
		GlobalParameter copiedParam = copy.globalParameters.get(0)
		DatasetParamValue copiedParamValue = copy.getParameterValues().first()

		then:
		copy.name == source.name
		copy.description == source.description
		copy.reference == source.reference
		copy.globalParameters.size() == 2
		copy.parameterValues.size() == 2

		copiedParam.name == param.name
		copiedParam.description == param.description

		copiedParamValue.paramValue == paramValue.paramValue
		copiedParamValue.dataset == copy


	}
}
