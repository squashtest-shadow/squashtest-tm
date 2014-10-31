package org.squashtest.tm.web.internal.controller.milestone;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.service.milestone.MilestoneManagerService;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;

@Controller
@RequestMapping("/milestone/{milestoneId}")
public class MilestoneModificationController {

	@Inject
	private MilestoneManagerService milestoneManager;

	@Inject
	private Provider<MilestoneStatusComboDataBuilder> statusComboDataBuilderProvider;
	
	@Inject
	private Provider<LevelLabelFormatter> levelLabelFormatterProvider;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MilestoneModificationController.class);
	
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView getProjectInfos(@PathVariable long milestoneId, Locale locale) {
		Milestone milestone = milestoneManager.findById(milestoneId);
		ModelAndView mav = new ModelAndView("page/milestones/milestone-info");
		mav.addObject("milestoneStatus", statusComboDataBuilderProvider.get().useLocale(locale).buildMarshalled());
		mav.addObject("milestone", milestone);
		mav.addObject("milestoneStatusLabel", formatStatus(locale, milestone.getStatus()));
		return mav;
	}
	
	private String formatStatus(Locale locale, MilestoneStatus status){
		return levelLabelFormatterProvider.get().useLocale(locale).formatLabel(status);
	}
	@RequestMapping(method = RequestMethod.POST, params = { "id=milestone-description", VALUE })
	@ResponseBody
	public String changeDescription(@PathVariable long milestoneId, @RequestParam(VALUE) String newDescription) {
		milestoneManager.changeDescription(milestoneId, newDescription);
		LOGGER.debug("Milestone modification : change milestone {} description = {}", milestoneId, newDescription);
		return  newDescription;
	}
	
	@RequestMapping(method = RequestMethod.POST, params = { "id=milestone-status", VALUE })
	@ResponseBody
	public String changeStatus(@PathVariable long milestoneId, @RequestParam(VALUE) MilestoneStatus newStatus, Locale locale) {
		milestoneManager.changeStatus(milestoneId, newStatus);
		LOGGER.debug("Milestone modification : change milestone {} Status = {}", milestoneId, newStatus);
		return  formatStatus(locale, newStatus);
	}
	
	@RequestMapping(method = RequestMethod.POST, params = { "newEndDate" })
	@ResponseBody
	public Date changeEndDate(@PathVariable long milestoneId, @RequestParam @DateTimeFormat(pattern= "yy-MM-dd") Date newEndDate, Locale locale) {
		milestoneManager.changeEndDate(milestoneId, newEndDate);
		LOGGER.debug("Milestone modification : change milestone {} end date = {}", milestoneId, newEndDate);
		return  newEndDate;
	}
	
	
	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object changeName(HttpServletResponse response, @PathVariable long milestoneId, @RequestParam String newName) {
		milestoneManager.changeLabel(milestoneId, newName);
		LOGGER.debug("Milestone modification : change milestone {} label = {}", milestoneId, newName);
		return new RenameModel(newName);
	}
	
}
