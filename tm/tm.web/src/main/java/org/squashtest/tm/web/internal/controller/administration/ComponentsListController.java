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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/platform/components")
public class ComponentsListController {
	public static class StateHelper {
		public String coerceToString(int state) {
			String res;
			switch (state) {
			case Bundle.ACTIVE:
				res = "ACTIVE";
				break;
			case Bundle.INSTALLED:
				res = "INSTALLED";
				break;
			case Bundle.RESOLVED:
				res = "RESOLVED";
				break;
			case Bundle.STARTING:
				res = "STARTING";
				break;
			case Bundle.STOPPING:
				res = "UNINSTALLED";
				break;
			case Bundle.UNINSTALLED:
				res = "UNINSTALLED";
				break;
			default:
				res = "UNKNOWN STATE";
			}

			return res;
		}
	}

	@Inject
	private BundleContext bundleContext;

	private final Comparator<Bundle> symNameComp = new Comparator<Bundle>() {

		@Override
		public int compare(Bundle b1, Bundle b2) {
			if (b1.getSymbolicName() == null) { // this looks weird yet it does happen
				return -1;
			}
			if (b2.getSymbolicName() == null) { // this looks weird yet it does happen
				return 1;
			}
			return b1.getSymbolicName().compareTo(b2.getSymbolicName());
		}
	};

	private final StateHelper stateHelper = new StateHelper();

	/**
	 * 
	 */
	public ComponentsListController() {
		super();
	}

	@ModelAttribute("stateHelper")
	public StateHelper getStateHelper() {
		return stateHelper;
	}

	@RequestMapping
	public String showList(Model model) {
		Bundle[] bundles = bundleContext.getBundles();
		List<Bundle> squashBundles = new ArrayList<Bundle>();
		List<Bundle> thirdPartyBundles = new ArrayList<Bundle>();

		for (Bundle bundle : bundles) {
			List<Bundle> recipient;
			if (bundle.getSymbolicName() != null && bundle.getSymbolicName().startsWith("org.squashtest")) {
				recipient = squashBundles;
			} else {
				recipient = thirdPartyBundles;
			}

			recipient.add(bundle);
		}

		Collections.sort(squashBundles, symNameComp);
		model.addAttribute("squashBundles", squashBundles);

		Collections.sort(thirdPartyBundles, symNameComp);
		model.addAttribute("thirdPartyBundles", thirdPartyBundles);

		return "components-list.html";
	}
}
