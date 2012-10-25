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

package org.squashtest.csp.tm.web.internal.controller.administration;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.InputType;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.customfield.CustomFieldManagerService;
import org.squashtest.csp.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableFilterSorter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;
import org.squashtest.tm.core.foundation.collection.DefaultPaging;

/**
 * Controller for the Custom Fields management pages.
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/administration/custom-fields")
public class CustomFieldAdministrationController {
	
	private CustomFieldManagerService customFieldManagerService;
	
	@Inject
	private InternationalizationHelper messageSource;
	
	@ServiceReference
	public void setCustomFieldManagerService(CustomFieldManagerService customFieldManagerService){
		this.customFieldManagerService = customFieldManagerService;
	}
	
	@ModelAttribute("inputTypes")
	public InputType[] populateInputTypes() {
		return InputType.values();
	}
	@ModelAttribute("customFieldsPageSize")
	public long populateCustomFieldsPageSize() {
		return DefaultPaging.FIRST_PAGE.getPageSize();
	}

	/**
	 * Shows the custom fields manager.
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showManager(Model model) {
		
		List<CustomField> customFields = customFieldManagerService.findAllOrderedByName();
		model.addAttribute("customFields", customFields);

		return "custom-field-manager.html";
	}
	
	private final DataTableMapper customFieldTableMapper = new DataTableMapper("unused", CustomField.class).initMapping(6)
			.mapAttribute(CustomField.class, 2, "name", String.class)
			.mapAttribute(CustomField.class, 3, "label", String.class)
			.mapAttribute(CustomField.class, 5, "inputType", String.class);
	
	/**
	 * Return the DataTableModel to display the table of all custom fields.
	 * 
	 * @param params the {@link DataTableDrawParameters} for the custom field table
	 * @param locale the browser selected locale
	 * @return the {@link DataTableModel} with organized {@link CustomField} infos.
	 */
	@RequestMapping(method = RequestMethod.GET, params = "sEcho")
	@ResponseBody
	public DataTableModel getCustomFieldsTableModel(final DataTableDrawParameters params, final Locale locale) {
		CollectionSorting filter =  new DataTableFilterSorter(params, customFieldTableMapper);

		FilteredCollectionHolder<List<CustomField>> holder = customFieldManagerService.findSortedCustomFields(filter);

		return new CustomFieldDataTableModelHelper(locale).buildDataModel(holder, filter.getFirstItemIndex() + 1, params.getsEcho());
	}
	
	private class CustomFieldDataTableModelHelper extends DataTableModelHelper<CustomField>{
		private Locale locale;
		
		private CustomFieldDataTableModelHelper(Locale locale){
			this.locale = locale;
		}

		@Override
		public Map<String, Object> buildItemData(CustomField item) {

			Map<String, Object> res = new HashMap<String, Object>();

			res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put("name", item.getName());
			res.put("label", item.getLabel());
			res.put("raw-input-type", item.getInputType().name());
			res.put("input-type", messageSource.internationalize(item.getInputType().getI18nKey(), locale));
			res.put("empty-delete-holder", " ");
			return res;
		}
	}
	
}
