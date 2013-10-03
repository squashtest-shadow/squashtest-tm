/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
define([ "jquery", "app/ws/squashtm.navbar", "app/ws/squashtm.projectfilter",
		"app/ws/squashtm.menubar", "app/ws/squashtm.notification",
		"squash.session-pinger"], function($, NavBar, ProjectFilter, MenuBar,
		WTF, SSP) {
	function init(highlightedWorkspace) {
		/* navigation tag */
		NavBar.init(highlightedWorkspace);


		ProjectFilter.init(squashtm.app.projectFilterConf);
		MenuBar.init(squashtm.app.menuBarConf);

		/* wtf */
		WTF.init(squashtm.app.notificationConf);

		/* Try to prevent FOUCs */
		$(".unstyled").fadeIn("fast", function() {
			$(this).removeClass("unstyled");
		});
		
		/*session ping*/
		new SSP();
	}

	return {
		init : init
	};
});