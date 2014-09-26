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
package org.squashtest.tm.web.internal.controller.campaign;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.CampaignModificationService;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.statistics.campaign.CampaignStatisticsBundle;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseImportanceJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseModeJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.json.JsonGeneralInfo;
import org.squashtest.tm.web.internal.model.json.JsonIteration;

@Controller
@RequestMapping("/campaigns/{campaignId}")
public class CampaignModificationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CampaignModificationController.class);

	private static final String PLANNING_URL = "/planning";
	private static final String NEW_DATE_ = ", new date : ";

	@Inject
	private CampaignModificationService campaignModService;

	@Inject
	private IterationModificationService iterationModService;

	@Inject
	private CustomFieldValueFinderService cufValueService;

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentHelper;

	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;

	@Inject
	private Provider<TestCaseModeJeditableComboDataBuilder> modeComboBuilderProvider;

	@Inject
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	@RequestMapping(value = "/statistics", method = RequestMethod.GET)
	public ModelAndView refreshStats(@PathVariable long campaignId) {

		TestPlanStatistics campaignStatistics = campaignModService.findCampaignStatistics(campaignId);
		Campaign campaign = campaignModService.findById(campaignId);
		ModelAndView mav = new ModelAndView("fragment/generics/statistics-fragment");
		mav.addObject("allowsSettled",
				campaign.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.SETTLED));
		mav.addObject("allowsUntestable",
				campaign.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.UNTESTABLE));
		mav.addObject("statisticsEntity", campaignStatistics);

		return mav;
	}

	// will return the Campaign in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String showCampaignInfo(@PathVariable long campaignId, Model model) {
		populateCampaignModel(campaignId, model);
		return "page/campaign-libraries/show-campaign";
	}

	// will return the fragment only
	@RequestMapping(method = RequestMethod.GET)
	public String showCampaign(@PathVariable long campaignId, Model model) {
		populateCampaignModel(campaignId, model);
		return "fragment/campaigns/edit-campaign";
	}

	private Model populateCampaignModel(long campaignId, Model model) {

		Campaign campaign = campaignModService.findById(campaignId);
		TestPlanStatistics statistics = campaignModService.findCampaignStatistics(campaignId);
		boolean hasCUF = cufValueService.hasCustomFields(campaign);
		DataTableModel attachments = attachmentHelper.findPagedAttachments(campaign);

		model.addAttribute("campaign", campaign);
		model.addAttribute("statistics", statistics);
		model.addAttribute("hasCUF", hasCUF);
		model.addAttribute("attachmentsModel", attachments);
		model.addAttribute("assignableUsers", getAssignableUsers(campaignId));
		model.addAttribute("weights", getWeights());
		model.addAttribute("modes", getModes());
		model.addAttribute("allowsSettled",
				campaign.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.SETTLED));
		model.addAttribute("allowsUntestable",
				campaign.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.UNTESTABLE));

		return model;
	}

	private Map<String, String> getAssignableUsers(long campaignId) {

		Locale locale = LocaleContextHolder.getLocale();

		Campaign campaign = campaignModService.findById(campaignId);

		String unassignedLabel = messageSource.internationalize("label.Unassigned", locale);

		Set<User> usersSet = new HashSet<User>();

		for (Iteration iteration : campaign.getIterations()) {
			usersSet.addAll(iterationTestPlanManagerService.findAssignableUserForTestPlan(iteration.getId()));
		}

		List<User> usersList = new ArrayList<User>(usersSet.size());
		usersList.addAll(usersSet);
		Collections.sort(usersList, new UserLoginComparator());

		Map<String, String> jsonUsers = new LinkedHashMap<String, String>(usersList.size());
		jsonUsers.put(User.NO_USER_ID.toString(), unassignedLabel);
		for (User user : usersList) {
			jsonUsers.put(user.getId().toString(), user.getLogin());
		}

		return jsonUsers;
	}

	private Map<String, String> getWeights() {
		Locale locale = LocaleContextHolder.getLocale();
		return importanceComboBuilderProvider.get().useLocale(locale).buildMap();
	}

	private Map<String, String> getModes() {
		Locale locale = LocaleContextHolder.getLocale();
		return modeComboBuilderProvider.get().useLocale(locale).buildMap();
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=campaign-description", VALUE })
	public @ResponseBody
	String updateDescription(@RequestParam(VALUE) String newDescription, @PathVariable long campaignId) {

		campaignModService.changeDescription(campaignId, newDescription);
		LOGGER.trace("Campaign " + campaignId + ": updated description to " + newDescription);
		return newDescription;

	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	public @ResponseBody
	Object rename(HttpServletResponse response, @RequestParam("newName") String newName, @PathVariable long campaignId) {
		LOGGER.info("Renaming Campaign " + campaignId + " as " + newName);

		campaignModService.rename(campaignId, newName);
		return new RenameModel(newName);

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

	@RequestMapping(value = "/general", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonGeneralInfo refreshGeneralInfos(@PathVariable long campaignId) {
		Campaign campaign = campaignModService.findById(campaignId);
		return new JsonGeneralInfo((AuditableMixin) campaign);

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

	@RequestMapping(value = PLANNING_URL, params = { "scheduledStart" })
	public @ResponseBody
	String setScheduledStart(HttpServletResponse response, @PathVariable long campaignId,
			@RequestParam(value = "scheduledStart") String strDate) {

		Date newScheduledStart = strToDate(strDate);
		String toReturn = dateToStr(newScheduledStart);

		LOGGER.info("Setting scheduled start date for campaign " + campaignId + NEW_DATE_ + newScheduledStart);

		campaignModService.changeScheduledStartDate(campaignId, newScheduledStart);

		return toReturn;

	}

	@RequestMapping(value = PLANNING_URL, params = { "scheduledEnd" })
	@ResponseBody
	String setScheduledEnd(HttpServletResponse response, @PathVariable long campaignId,
			@RequestParam(value = "scheduledEnd") String strDate) {

		Date newScheduledEnd = strToDate(strDate);
		String toReturn = dateToStr(newScheduledEnd);

		LOGGER.info("Setting scheduled start date for campaign " + campaignId + NEW_DATE_ + newScheduledEnd);

		campaignModService.changeScheduledEndDate(campaignId, newScheduledEnd);

		return toReturn;

	}

	/** the next functions may receive null arguments : empty string **/

	@RequestMapping(value = PLANNING_URL, params = { "actualStart" })
	@ResponseBody
	String setActualStart(HttpServletResponse response, @PathVariable long campaignId,
			@RequestParam(value = "actualStart") String strDate) {

		Date newActualStart = strToDate(strDate);
		String toReturn = dateToStr(newActualStart);

		LOGGER.info("Setting scheduled start date for campaign " + campaignId + NEW_DATE_ + newActualStart);

		campaignModService.changeActualStartDate(campaignId, newActualStart);

		return toReturn;

	}

	@RequestMapping(value = PLANNING_URL, params = { "actualEnd" })
	@ResponseBody
	String setActualEnd(HttpServletResponse response, @PathVariable long campaignId,
			@RequestParam(value = "actualEnd") String strDate) {

		Date newActualEnd = strToDate(strDate);
		String toReturn = dateToStr(newActualEnd);

		LOGGER.info("Setting scheduled start date for campaign " + campaignId + NEW_DATE_ + newActualEnd);

		campaignModService.changeActualEndDate(campaignId, newActualEnd);

		return toReturn;

	}

	@RequestMapping(value = PLANNING_URL, params = { "setActualStartAuto" })
	@ResponseBody
	String setActualStartAuto(HttpServletResponse response, @PathVariable long campaignId,
			@RequestParam(value = "setActualStartAuto") Boolean auto) {

		LOGGER.info("Autosetting actual start date for campaign " + campaignId + ", new value " + auto.toString());

		campaignModService.changeActualStartAuto(campaignId, auto);
		Campaign campaign = campaignModService.findById(campaignId);

		String toreturn = dateToStr(campaign.getActualStartDate());

		return toreturn;
	}

	@RequestMapping(value = PLANNING_URL, params = { "setActualEndAuto" })
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

	@RequestMapping(value = "/iterations", produces = ContentTypes.APPLICATION_JSON, method = RequestMethod.GET)
	@ResponseBody
	public List<JsonIteration> getIterations(@PathVariable("campaignId") long campaignId) {
		List<Iteration> iterations = campaignModService.findIterationsByCampaignId(campaignId);
		return createJsonIterations(iterations);
	}

	// for now, handles the scheduled dates only
	@RequestMapping(value = "/iterations/planning", consumes = ContentTypes.APPLICATION_JSON, method = RequestMethod.POST)
	@ResponseBody
	public void setIterationsPlanning(@RequestBody JsonIteration[] iterations) throws ParseException {
		Date date;
		for (JsonIteration iter : iterations) {
			date = (iter.getScheduledStartDate() != null) ? DateUtils.parseIso8601DateTime(iter
					.getScheduledStartDate()) : null;
			iterationModService.changeScheduledStartDate(iter.getId(), date);
			date = (iter.getScheduledEndDate() != null) ? DateUtils.parseIso8601DateTime(iter.getScheduledEndDate())
					: null;
			iterationModService.changeScheduledEndDate(iter.getId(), date);
		}
	}

	// *************************** statistics ********************************

	// URL should have been /statistics, but that was already used by another method in this controller
	@RequestMapping(value = "/dashboard-statistics", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	public @ResponseBody
	CampaignStatisticsBundle getStatisticsAsJson(@PathVariable("campaignId") long campaignId) {
		return campaignModService.gatherCampaignStatisticsBundle(campaignId);
	}

	@RequestMapping(value = "/dashboard", method = RequestMethod.GET, produces = ContentTypes.TEXT_HTML)
	public ModelAndView getDashboard(Model model, @PathVariable("campaignId") long campaignId) {

		Campaign campaign = campaignModService.findById(campaignId);
		CampaignStatisticsBundle bundle = campaignModService.gatherCampaignStatisticsBundle(campaignId);

		ModelAndView mav = new ModelAndView("fragment/campaigns/campaign-dashboard");
		mav.addObject("campaign", campaign);
		mav.addObject("dashboardModel", bundle);

		return mav;

	}

	// **************************** private stuffs ***************************

	private List<JsonIteration> createJsonIterations(List<Iteration> iterations) {
		List<JsonIteration> jsonIters = new ArrayList<JsonIteration>(iterations.size());
		for (Iteration iter : iterations) {

			JsonIteration jsonIter = new JsonIteration(iter.getId(), iter.getName(), iter.getScheduledStartDate(),
					iter.getScheduledEndDate());

			jsonIters.add(jsonIter);
		}
		return jsonIters;
	}

	private static final class UserLoginComparator implements Comparator<User> {
		@Override
		public int compare(User u1, User u2) {
			return u1.getLogin().compareTo(u2.getLogin());
		}

	}
}
