package org.squashtest.tm.web.internal.controller.users;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.service.user.PartyPreferenceService;

import javax.inject.Inject;
import javax.validation.Valid;

/**
 * Created by jthebault on 11/10/2016.
 */
@Controller
@RequestMapping("/user-prefs")
public class PartyPreferenceController {

	@Inject
	PartyPreferenceService partyPreferenceService;

	@RequestMapping(value="/update", method= RequestMethod.POST)
	@ResponseBody
	public void changeUserPreference (@RequestBody @Valid PartyPreferenceModel partyPreferenceModel){
		partyPreferenceService.addOrUpdatePreferenceForCurrentUser(partyPreferenceModel.getKey(),partyPreferenceModel.getValue());
	}

}
