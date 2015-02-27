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
package org.squashtest.tm.web.internal.controller.administration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.model.datatable.DataTable10Model;
import org.squashtest.tm.web.internal.model.datatable.DataTable10ModelAdaptor;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;

/**
 * Controller for rendering info list management pages
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/administration/info-lists")
public class InfoListAdministrationController {
	public interface InfoList {
		Long getId();

		String getName();

		String getDescription();

		String getDefaultValue();

		Date getCreatedOn();

		String getCreatedBy();

		Date getLastModifiedOn();

		String getLastModifiedBy();

		int getBoundProjectsCount();
	}

	@ModelAttribute("tablePageSize")
	public long populateTablePageSize() {
		return Pagings.DEFAULT_PAGING.getPageSize();
	}

	/**
	 * Shows the custom fields manager.
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showManager(Model model) {
		List<Object> lists = makeList();

		model.addAttribute("infoLists", lists);
		return "info-list-manager.html";
	}

	private List<Object> makeList() {
		List<Object> lists = new ArrayList<Object>();

		for (int i = 10; i < 20; i++) {
			final long id = i;
			lists.add(new InfoList() {

				@Override
				public Long getId() {
					return (long) id;
				}

				@Override
				public String getName() {
					return "item" + 1;
				}

				@Override
				public String getDescription() {
					return "my fancy " + id;
				}

				@Override
				public String getDefaultValue() {
					return "def" + id;
				}

				@Override
				public Date getCreatedOn() {
					return new Date();
				}

				@Override
				public String getCreatedBy() {
					return "YOUR MOM";
				}

				@Override
				public Date getLastModifiedOn() {
					return new Date();
				}

				@Override
				public String getLastModifiedBy() {
					return "YOUR MOM";
				}

				@Override
				public int getBoundProjectsCount() {
					return (int) (id % 2);
				}

			});
		}
		return lists;
	}

	@RequestMapping(method = RequestMethod.GET, params = "_", headers = AcceptHeaders.CONTENT_JSON)
	@ResponseBody
	public DataTable10Model getCustomFieldsTableModel(@RequestParam("_") String echo,
			final DataTableDrawParameters params, final Locale locale) {
		DataTableModel model = new DataTableModel(echo);
		model.setAaData(makeList());
		return DataTable10ModelAdaptor.adapt(model);
	}
}
