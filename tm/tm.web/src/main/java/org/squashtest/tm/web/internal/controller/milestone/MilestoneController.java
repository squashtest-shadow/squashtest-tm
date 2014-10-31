package org.squashtest.tm.web.internal.controller.milestone;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/milestones")
public class MilestoneController {
	
	@Inject
	private Provider<MilestoneStatusComboDataBuilder> statusComboDataBuilderProvider;
	
	@RequestMapping(value = "/status-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public String buildStatusComboData(Locale locale) {
		return statusComboDataBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

}
