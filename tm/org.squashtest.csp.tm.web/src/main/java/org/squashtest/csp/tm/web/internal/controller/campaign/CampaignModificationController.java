/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.web.internal.controller.campaign;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.CampaignModificationService;
import org.squashtest.csp.tm.service.IterationModificationService;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableFilterSorter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;
import org.squashtest.csp.tm.web.internal.utils.DateUtils;

@Controller
@RequestMapping("/campaigns/{campaignId}")
public class CampaignModificationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(CampaignModificationController.class);

	private CampaignModificationService campaignModService;
	private IterationModificationService iterationModService;

	@Inject
	private MessageSource messageSource;

	/*
	 * //TODO since this controller may return two different models of different datatables (the one in the campaign and
	 * the one in the association interface) they should be addressed by two distinct DataTableMappers, especially
	 * because their configuration and content are different !
	 */
	private final DataTableMapper testPlanMapper = new DataTableMapper("irrelevant", TestCase.class, Project.class)
			.initMapping(7).mapAttribute(Project.class, 2, "name", String.class)
			.mapAttribute(TestCase.class, 3, "name", String.class)
			.mapAttribute(TestCase.class, 5, "importance", TestCaseImportance.class)
			.mapAttribute(TestCase.class, 6, "executionMode", TestCaseExecutionMode.class);

	@ServiceReference
	public void setIterationModificationService(IterationModificationService iterationModificationService) {
		this.iterationModService = iterationModificationService;
	}

	@ServiceReference
	public void setCampaignModificationService(CampaignModificationService service) {
		campaignModService = service;
	}

	// will return the Campaign in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView showCampaignInfo(@PathVariable long campaignId) {

		Campaign campaign = campaignModService.findById(campaignId);

		ModelAndView mav = new ModelAndView("page/campaign-libraries/show-campaign");

		mav.addObject("campaign", campaign);

		return mav;
	}

	// will return the fragment only
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showCampaign(@PathVariable long campaignId) {
		Campaign campaign = campaignModService.findById(campaignId);

		ModelAndView mav = new ModelAndView("fragment/campaigns/edit-campaign");
		mav.addObject("campaign", campaign);

		return mav;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=campaign-description", "value" })
	public @ResponseBody
	String updateDescription(@RequestParam("value") String newDescription, @PathVariable long campaignId) {

		campaignModService.changeDescription(campaignId, newDescription);
		LOGGER.trace("Campaign " + campaignId + ": updated description to " + newDescription);
		return newDescription;

	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	public @ResponseBody
	Object rename(HttpServletResponse response, @RequestParam("newName") String newName, @PathVariable long campaignId) {
		LOGGER.info("Renaming Campaign " + campaignId + " as " + newName);

		campaignModService.rename(campaignId, newName);
		final String reNewName = newName;
		return new Object() {
			public String newName = reNewName; // NOSONAR : field is actually read by JSON marshaller
		};

	}

	@RequestMapping(value = "/remove-campaigns", method = RequestMethod.POST, params = "isIteration=1")
	@ResponseBody
	public String removeIterations(@RequestParam("tab[]") String[] data) {
		String retour = "";
		for (int i = 0; i < data.length; i++) {
			LOGGER.info("Deleting Iteration " + data[i] + " Long : " + (Long.parseLong(data[i])));
			retour = iterationModService.delete(Long.parseLong(data[i]));
			LOGGER.info("Deleting Iteration " + data[i]);
		}
		return retour;

	}

	@RequestMapping(value = "/general", method = RequestMethod.GET)
	public ModelAndView refreshGeneralInfos(@PathVariable long libraryId, @PathVariable long campaignId) {

		Campaign campaign = campaignModService.findById(campaignId);

		ModelAndView mav = new ModelAndView("fragment/generics/general-information-fragment");

		mav.addObject("auditableEntity", campaign);
		mav.addObject("entityContextUrl", "/campaigns/" + campaignId);

		return mav;
	}

	/*
	 * *************************************** planning *********************************
	 */

	// returns null if the string is empty, or a date otherwise. No check regarding the actual content of strDate.
	private Date strToDate(String strDate) {
		return DateUtils.millisecondsToDate(strDate);
	}

	private String dateToStr(Date date) {
		return DateUtils.dateToMillisecondsAsString(date);
	}

	@RequestMapping(value = "/planning", params = { "scheduledStart" })
	public @ResponseBody
	String setScheduledStart(HttpServletResponse response, @PathVariable long campaignId,
			@RequestParam(value = "scheduledStart") String strDate) {

		Date newScheduledStart = strToDate(strDate);
		String toReturn = dateToStr(newScheduledStart);

		LOGGER.info("Setting scheduled start date for campaign " + campaignId + ", new date : " + newScheduledStart);

		campaignModService.changeScheduledStartDate(campaignId, newScheduledStart);

		return toReturn;

	}

	@RequestMapping(value = "/planning", params = { "scheduledEnd" })
	@ResponseBody
	String setScheduledEnd(HttpServletResponse response, @PathVariable long campaignId,
			@RequestParam(value = "scheduledEnd") String strDate) {

		Date newScheduledEnd = strToDate(strDate);
		String toReturn = dateToStr(newScheduledEnd);

		LOGGER.info("Setting scheduled start date for campaign " + campaignId + ", new date : " + newScheduledEnd);

		campaignModService.changeScheduledEndDate(campaignId, newScheduledEnd);

		return toReturn;

	}

	/** the next functions may receive null arguments : empty string **/

	@RequestMapping(value = "/planning", params = { "actualStart" })
	@ResponseBody
	String setActualStart(HttpServletResponse response, @PathVariable long campaignId,
			@RequestParam(value = "actualStart") String strDate) {

		Date newActualStart = strToDate(strDate);
		String toReturn = dateToStr(newActualStart);

		LOGGER.info("Setting scheduled start date for campaign " + campaignId + ", new date : " + newActualStart);

		campaignModService.changeActualStartDate(campaignId, newActualStart);

		return toReturn;

	}

	@RequestMapping(value = "/planning", params = { "actualEnd" })
	@ResponseBody
	String setActualEnd(HttpServletResponse response, @PathVariable long campaignId,
			@RequestParam(value = "actualEnd") String strDate) {

		Date newActualEnd = strToDate(strDate);
		String toReturn = dateToStr(newActualEnd);

		LOGGER.info("Setting scheduled start date for campaign " + campaignId + ", new date : " + newActualEnd);

		campaignModService.changeActualEndDate(campaignId, newActualEnd);

		return toReturn;

	}

	@RequestMapping(value = "/planning", params = { "setActualStartAuto" })
	@ResponseBody
	String setActualStartAuto(HttpServletResponse response, @PathVariable long campaignId,
			@RequestParam(value = "setActualStartAuto") Boolean auto) {

		LOGGER.info("Autosetting actual start date for campaign " + campaignId + ", new value " + auto.toString());

		campaignModService.changeActualStartAuto(campaignId, auto);
		Campaign campaign = campaignModService.findById(campaignId);

		String toreturn = dateToStr(campaign.getActualStartDate());

		return toreturn;
	}

	@RequestMapping(value = "/planning", params = { "setActualEndAuto" })
	@ResponseBody
	String setActualEndAuto(HttpServletResponse response, @PathVariable long campaignId,
			@RequestParam(value = "setActualEndAuto") Boolean auto) {
		LOGGER.info("CampaignModificationController : autosetting actual end date for campaign " + campaignId
				+ ", new value " + auto.toString());

		campaignModService.changeActualEndAuto(campaignId, auto);
		Campaign campaign = campaignModService.findById(campaignId);

		String toreturn = dateToStr(campaign.getActualEndDate());

		return toreturn;

	}

	// ****************************** Test Plan **********************************

	@RequestMapping(value = "/test-cases-table", params = "sEcho")
	public @ResponseBody
	DataTableModel getTestCasesTableModel(@PathVariable("campaignId") long campaignId,
			final DataTableDrawParameters params, final Locale locale) {
		CollectionSorting filter = createCollectionSorting(params, testPlanMapper);

		FilteredCollectionHolder<List<CampaignTestPlanItem>> holder = campaignModService.findTestPlanByCampaignId(
				campaignId, filter);

		return new DataTableModelHelper<CampaignTestPlanItem>() {
			@Override
			public Object[] buildItemData(CampaignTestPlanItem item) {
				TestCase testCase = item.getReferencedTestCase();
				String user = (item.getUser() != null) ? item.getUser().getLogin() : formatNoData(locale); 
				
				return new Object[] { item.getId(), getCurrentIndex(), testCase.getProject().getName(),
						testCase.getName(),
						user,
						formatImportance(testCase.getImportance(), locale),
						formatExecutionMode(testCase.getExecutionMode(), locale), "" };
			}
		}.buildDataModel(holder, filter.getFirstItemIndex() + 1, params.getsEcho());
	}

	@RequestMapping(value = "/linkable-test-cases-table", params = "sEcho")
	public @ResponseBody
	DataTableModel getLinkableTestCasesTableModel(@PathVariable("campaignId") long campaignId,
			final DataTableDrawParameters params, final Locale locale) {
		CollectionSorting filter = createCollectionSorting(params, testPlanMapper);

		FilteredCollectionHolder<List<CampaignTestPlanItem>> holder = campaignModService.findTestPlanByCampaignId(
				campaignId, filter);

		return new DataTableModelHelper<CampaignTestPlanItem>() {
			@Override
			public Object[] buildItemData(CampaignTestPlanItem item) {
				TestCase testCase = item.getReferencedTestCase();
				return new Object[] { testCase.getId(), getCurrentIndex(), testCase.getProject().getName(),
						testCase.getName(), formatImportance(testCase.getImportance(), locale),
						formatExecutionMode(testCase.getExecutionMode(), locale), "" };
			}
		}.buildDataModel(holder, filter.getFirstItemIndex() + 1, params.getsEcho());
	}

	private CollectionSorting createCollectionSorting(final DataTableDrawParameters params, DataTableMapper mapper) {
		return new DataTableFilterSorter(params, mapper);
	}

	/* ************************************** formatting code ****************************** */

	private String formatExecutionMode(TestCaseExecutionMode mode, Locale locale) {
		return messageSource.getMessage(mode.getI18nKey(), null, locale);
	}

	private String formatNoData(Locale locale) {
		return messageSource.getMessage("squashtm.nodata", null, locale);
	}

	private String formatImportance(TestCaseImportance importance, Locale locale) {
		return messageSource.getMessage(importance.getI18nKey(), null, locale);
	}
}
