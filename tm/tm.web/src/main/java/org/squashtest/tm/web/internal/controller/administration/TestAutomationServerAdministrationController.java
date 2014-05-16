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
package org.squashtest.tm.web.internal.controller.administration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.testautomation.TestAutomationServerManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

/**
 * Controller for the Test automation servers management pages.
 * 
 * @author mpagnon
 * 
 */
@Controller
@RequestMapping("/administration/test-automation-servers")
public class TestAutomationServerAdministrationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationServerAdministrationController.class);
	private static final String BASE_URL = "baseURL";
	private static final String TEST_AUTOMATION_SERVERS = "testAutomationServers";


	@Inject
	private TestAutomationServerManagerService testAutomationServerService;

	@Inject
	private InternationalizationHelper messageSource;

	@ModelAttribute("testAutomationServerPageSize")
	public long populateCustomFieldsPageSize() {
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

		// TODO replace following lines
		TestAutomationServer tas = getStubTAS();
		List<TestAutomationServer> testAutomationServers = Arrays.asList(tas);
		//List<TestAutomationServer> testAutomationServers = testAutomationServerService.findAllOrderedByName();
		model.addAttribute(TEST_AUTOMATION_SERVERS, testAutomationServers);

		return "test-automation-servers-manager.html";
	}

	//TODO delete stub method
	private TestAutomationServer getStubTAS() {
		TestAutomationServer tas = null;
		try {
			tas = new TestAutomationServer("tas", new URL("http://localhost:9080/jenkins"), "login",
					"password");
		} catch (MalformedURLException e) {
		}
		AuditableMixin mixin = (AuditableMixin) tas;
		mixin.setCreatedBy("mpagnon");
		mixin.setCreatedOn(new Date());
		mixin.setLastModifiedOn(new Date());
		mixin.setLastModifiedBy("bsiri");
		return tas;
	}

	/**
	 * A Mapping for ta servers table sortable columns : maps the table column index to an entity property. NB:
	 * column index is of all table's columns (displayed or not)
	 */
	private final DatatableMapper<String> testAutomationServerTableMapper = new NameBasedMapper(3)
	.mapAttribute(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, TestAutomationServer.class)
	.mapAttribute(BASE_URL, BASE_URL, TestAutomationServer.class)
	.mapAttribute(DataTableModelConstants.DEFAULT_CREATED_ON_KEY, DataTableModelConstants.DEFAULT_CREATED_ON_KEY, TestAutomationServer.class)
	.mapAttribute(DataTableModelConstants.DEFAULT_CREATED_BY_KEY,DataTableModelConstants.DEFAULT_CREATED_BY_KEY, TestAutomationServer.class)
	.mapAttribute(DataTableModelConstants.DEFAULT_LAST_MODIFIED_ON_KEY, DataTableModelConstants.DEFAULT_LAST_MODIFIED_ON_KEY, TestAutomationServer.class)
	.mapAttribute(DataTableModelConstants.DEFAULT_LAST_MODIFIED_BY_KEY, DataTableModelConstants.DEFAULT_LAST_MODIFIED_BY_KEY, TestAutomationServer.class);

	/**
	 * Return the DataTableModel to display the table of all ta servers.
	 * 
	 * @param params
	 *            the {@link DataTableDrawParameters} for the ta servers table
	 * @param locale
	 *            the browser selected locale
	 * @return the {@link DataTableModel} with organized {@link TestAutomationServer} infos.
	 */
	@RequestMapping(method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getTestAutomationServersTableModel(final DataTableDrawParameters params, final Locale locale) {

		//TODO replace following lines
		PagedCollectionHolder<List<TestAutomationServer>> holder  = new PagedCollectionHolder<List<TestAutomationServer>>() {

			@Override
			public long getFirstItemIndex() {
				return 0;
			}

			@Override
			public long getTotalNumberOfItems() {
				return 1;
			}

			@Override
			public List<TestAutomationServer> getPagedItems() {
				TestAutomationServer tas = getStubTAS();
				return Arrays.asList(tas);
			}
		};
		//		PagingAndSorting filter = new DataTableSorting(params, testAutomationServerTableMapper);
		//		PagedCollectionHolder<List<TestAutomationServer>> holder = testAutomationServerService.findSortedTestAutomationServers(filter);

		return new TestAutomationServerDataTableModelHelper(locale).buildDataModel(holder, params.getsEcho());
	}

	/**
	 * Will help to create the {@link DataTableModel} to fill the data-table of test automation servers
	 * 
	 */
	private class TestAutomationServerDataTableModelHelper extends DataTableModelBuilder<TestAutomationServer> {
		private Locale locale;

		private TestAutomationServerDataTableModelHelper(Locale locale) {
			this.locale = locale;
		}

		@Override
		public Map<String, Object> buildItemData(TestAutomationServer item) {
			AuditableMixin auditable = (AuditableMixin) item;
			Map<String, Object> res = new HashMap<String, Object>();

			res.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, item.getName());
			res.put(BASE_URL, item.getBaseURL().toString());
			res.put(DataTableModelConstants.DEFAULT_CREATED_BY_KEY, formatUsername(auditable.getCreatedBy()));
			res.put(DataTableModelConstants.DEFAULT_LAST_MODIFIED_BY_KEY, formatUsername(auditable.getLastModifiedBy()));
			res.put(DataTableModelConstants.DEFAULT_LAST_MODIFIED_ON_KEY, messageSource.localizeDate(auditable.getLastModifiedOn(), locale));
			res.put(DataTableModelConstants.DEFAULT_CREATED_ON_KEY, messageSource.localizeDate(auditable.getCreatedOn(), locale));
			res.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
			return res;
		}
	}

	@RequestMapping(value = "/{testAutomationServerId}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteTestAutomationServer(@PathVariable long testAutomationServerId) {
		LOGGER.info("Delete test automation server of id #{}", testAutomationServerId);
		//TODO
		//		testAutomationServerService.delete(testAutomationServerId);
	}
}
