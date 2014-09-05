/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.widget;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Gregory Fouquet
 *
 */
@Controller
public class DataTablesController {
	@Inject
	private MessageSource messageSource;

	@RequestMapping("/datatables/messages")
	@ResponseBody
	public Map<String, Object> getInternationalizedMessages(Locale locale) {
		Map<String, Object> messages = new HashMap<String, Object>();

		messages.put("sLengthMenu", messageSource.getMessage("generics.datatable.lengthMenu", null, locale));
		messages.put("sZeroRecords", messageSource.getMessage("generics.datatable.zeroRecords", null, locale));
		messages.put("sInfo", messageSource.getMessage("generics.datatable.info", null, locale));
		messages.put("sInfoEmpty", messageSource.getMessage("generics.datatable.infoEmpty", null, locale));
		messages.put("sInfoFiltered", messageSource.getMessage("generics.datatable.infoFiltered", null, locale));
		messages.put("sSearch", messageSource.getMessage("generics.datatable.search", null, locale));

		Map<String, Object> pagination = new HashMap<String, Object>();
		messages.put("oPaginate", pagination);

		pagination.put("sFirst", messageSource.getMessage("generics.datatable.paginate.first", null, locale));
		pagination.put("sPrevious", messageSource.getMessage("generics.datatable.paginate.previous", null, locale));
		pagination.put("sNext", messageSource.getMessage("generics.datatable.paginate.next", null, locale));
		pagination.put("sLast", messageSource.getMessage("generics.datatable.paginate.last", null, locale));

		return messages;
	}
}
