/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
define(["jquery", "squash.dateutils", "./general-information-panel-controller"], function($, dateutils, Controller){
	"use strict";
	function updateDateInformations(options, infos){
		infos = infos || defaults();

		var newCreatedOn = (infos.createdOn !== null && infos.createdOn.length>0) ? dateutils.format(infos.createdOn, options.format) : "";
		var newCreatedBy = (infos.createdBy !== null && infos.createdBy.length>0) ? '('+infos.createdBy+')' : options.never;

		var newModifiedOn = (infos.modifiedOn !== null && infos.modifiedOn.length>0) ? dateutils.format(infos.modifiedOn, options.format) : "";
		var newModifiedBy = (infos.modifiedBy !== null && infos.modifiedBy.length>0) ? '('+infos.modifiedBy+')' : options.never;

		$("#created-on > .datetime").text(newCreatedOn);
		$("#created-on > .author").text(newCreatedBy);

		$("#last-modified-on > .datetime").text(newModifiedOn);
		$("#last-modified-on > .author").text(newModifiedBy);

	}

	function defaults() {
		return {
			createdOn : $("#created-on > .datetime").text(),
			createdBy : $("#created-on > .author").text(),
			modifiedOn : $("#last-modified-on > .datetime").text(),
			modifiedBy : $("#last-modified-on > .author").text()
		};
	}

	return new Controller(updateDateInformations);
});