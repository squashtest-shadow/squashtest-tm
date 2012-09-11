/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.web.internal.controller.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.web.internal.helper.JsonHelper;

public class AutomatedExecutionViewUtils {
 private AutomatedExecutionViewUtils(){
	 
 }
 public static  String buildExecInfo(List<Execution> executions, Locale locale, MessageSource messageSource) {
		List<Map<String, Object>> executionInfos = new ArrayList<Map<String, Object>>(executions.size());
		for(Execution execution : executions){
			Map<String, Object> infos = new HashMap<String, Object>(4);
			infos.put("id", execution.getId());
			infos.put("name", execution.getName());
			infos.put("status", execution.getExecutionStatus());
			infos.put("localizedStatus", messageSource.getMessage(execution.getExecutionStatus().getI18nKey(),null, locale));
			executionInfos.add(infos);
		}
		return JsonHelper.serialize(executionInfos);
	}
 
}
