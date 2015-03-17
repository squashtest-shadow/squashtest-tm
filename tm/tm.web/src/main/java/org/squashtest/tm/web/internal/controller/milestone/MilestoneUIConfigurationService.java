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
package org.squashtest.tm.web.internal.controller.milestone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneMember;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.milestone.MilestoneFinderService;
import org.squashtest.tm.web.internal.model.json.JsonMilestone;



/**
 * 
 * That service helps other controllers to configure the UI served to the client, by creating
 * configuration beans for that purpose. Those beans are : {@link MilestoneFeatureConfiguration}
 * and {@link MilestonePanelConfiguration}.
 * 
 * @author bsiri
 *
 */
@Component
public class MilestoneUIConfigurationService {

	@Inject
	MilestoneFinderService milestoneFinder;

	@Inject
	FeatureManager featureManager;


	public MilestoneFeatureConfiguration configure(List<Long> activeMilestones, TestCase testCase){
		MilestoneFeatureConfiguration conf = createCommonConf(activeMilestones, testCase);
		Map<String, String> identity = createIdentity(testCase);
		conf.setIdentity(identity);
		return conf;
	}

	public MilestoneFeatureConfiguration configure(List<Long> activeMilestones, RequirementVersion version){
		MilestoneFeatureConfiguration conf = createCommonConf(activeMilestones, version);
		Map<String, String> identity = createIdentity(version);
		conf.setIdentity(identity);
		return conf;
	}

	public MilestoneFeatureConfiguration configure(List<Long> activeMilestones, Campaign campaign){
		MilestoneFeatureConfiguration conf = createCommonConf(activeMilestones, campaign);
		Map<String, String> identity = createIdentity(campaign);
		conf.setIdentity(identity);
		return conf;
	}

	public MilestoneFeatureConfiguration configure(List<Long> activeMilestones, Iteration iteration){
		MilestoneFeatureConfiguration conf = createCommonConf(activeMilestones, iteration);
		Map<String, String> identity = createIdentity(iteration);
		conf.setIdentity(identity);
		return conf;
	}

	public MilestoneFeatureConfiguration configure(List<Long> activeMilestones, TestSuite testSuite){
		MilestoneFeatureConfiguration conf = createCommonConf(activeMilestones, testSuite);
		Map<String, String> identity = createIdentity(testSuite);
		conf.setIdentity(identity);
		return conf;
	}



	// ************************** private stuffs *******************************************


	private MilestoneFeatureConfiguration createCommonConf(List<Long> milestones, MilestoneMember member){

		MilestoneFeatureConfiguration conf = new MilestoneFeatureConfiguration();

		JsonMilestone activeMilestone = new JsonMilestone();
		boolean globallyEnabled = true;
		boolean userEnabled = true;
		boolean locked = false;
		int totalMilestones = 0;

		List<Long> activeMilestones;
		if (milestones != null){
			activeMilestones = milestones;
		}
		else{
			activeMilestones = new ArrayList<>();
		}


		// TODO : test whether the functionality is globally enabled
		globallyEnabled = featureManager.isEnabled(Feature.MILESTONE);
		if (! globallyEnabled){
			conf.setGloballyEnabled(false);
			return conf;
		}

		// checks whether the entity is locked by milestone status
		locked = isMilestoneLocked(member);

		conf.setMilestoneLocked(locked);

		// does the user actually use the feature
		userEnabled = ! (activeMilestones.isEmpty());
		if (! userEnabled){
			conf.setUserEnabled(false);
		}

		// total count of milestones
		totalMilestones = member.getMilestones().size();
		conf.setTotalMilestones(totalMilestones);

		// if both globally and user enabled, fetch the active milestones etc
		if (globallyEnabled && userEnabled && ! activeMilestones.isEmpty()){
			Milestone milestone = milestoneFinder.findById(activeMilestones.get(0));
			activeMilestone.setId(milestone.getId());
			activeMilestone.setLabel(milestone.getLabel());
			conf.setActiveMilestone(activeMilestone);

		}

		return conf;
	}

	private boolean isMilestoneLocked(MilestoneMember member){
		boolean locked = false;
		Collection<Milestone> milestones = member.getMilestones();

		for (Milestone m : milestones){
			MilestoneStatus status = m.getStatus();
			if (! status.isAllowObjectModification()){
				locked = true;
				break;
			}
		}

		return locked;
	}

	private Map<String, String> createIdentity(TestCase testCase){
		Map<String, String> identity = new HashMap<>();
		identity.put("restype", "test-cases");
		identity.put("resid", testCase.getId().toString());
		return identity;
	}

	private Map<String, String> createIdentity(Campaign campaign){
		Map<String, String> identity = new HashMap<>();
		identity.put("restype", "campaigns");
		identity.put("resid",campaign.getId().toString());
		return identity;
	}

	private Map<String, String> createIdentity(RequirementVersion version){
		Map<String, String> identity = new HashMap<>();
		identity.put("restype", "requirements");
		identity.put("resid", version.getRequirement().getId().toString());
		return identity;
	}

	private Map<String, String> createIdentity(Iteration iteration){
		Map<String, String> identity = new HashMap<>();
		identity.put("restype", "iterations");
		identity.put("resid", iteration.getId().toString());
		return identity;
	}

	private Map<String, String> createIdentity(TestSuite testSuite){
		Map<String, String> identity = new HashMap<>();
		identity.put("restype", "test-suites");
		identity.put("resid", testSuite.getId().toString());
		return identity;
	}
}
