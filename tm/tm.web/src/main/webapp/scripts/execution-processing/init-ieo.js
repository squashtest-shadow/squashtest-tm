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
define([ "jquery", "module", "./ieo-manager", "squash.resizer", "./jquery.ieo-control" ], function($, module,
		OptimizedManager, resizer) {

	return function() {

		// init the manager

		var settings = module.config();

		var manager = new OptimizedManager(settings);

		// set it in the context
		squashtm = squashtm || {};
		squashtm.ieomanager = manager;

		// init the control
		var control = $("#ieo-control").ieoControl();

		// wire them
		manager.setControl(control);

		// the right panel
		manager.setRightPane($("#ieo-right-panel"));

		// make the panels resizeable

		resizer.init({
			leftSelector : "#ieo-left-panel",
			rightSelector : "#ieo-right-panel"
		});

	};

});
