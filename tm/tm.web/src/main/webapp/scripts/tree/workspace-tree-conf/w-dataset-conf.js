/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
define(["workspace.event-bus"], function (eventBus) {
	"use strict";
	return {
		generate: function () {
			return {
				"types": {
					"max_depth": -2, // unlimited without check
					"max_children": -2, // unlimited w/o check
					"valid_children": ["drive"],

					"types": {
						"global-dataset": {
							"valid_children": 'none'
						},
						"composite-dataset": {
							"valid_children": 'none'
						},
						"dataset-template": {
							"valid_children": 'none'
						},
						"folder": {
							"valid_children": ["global-dataset", "composite-dataset", "dataset-template", "folder"]
						},
						"drive": {
							"valid_children": ["global-dataset", "folder"]
						}
					}
				}
			};
		}
	};
});
