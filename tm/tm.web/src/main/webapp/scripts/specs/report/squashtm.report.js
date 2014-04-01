/*
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
define([ "app/report/squashtm.report" ], function(report) {
	describe("Report", function() {
		describe("when validating simple data boundary", function() {
			it("should not validate form without a boundary", function() {
				report.init({});

				var formState = {
					foo : [ {
						selected : false,
						type : "OPTION"
					} ],
					bar : [ {
						selected : false,
						type : "TEXT",
						value : []
					} ]
				};

				expect(report._private.isPerimeterValid(formState)).toBe(false);
			});

			it("should validate form with selected project", function() {
				report.init({});

				var formState = {
					foo : [ {
						selected : true,
						type : "PROJECT_PICKER"
					} ]
				};

				expect(report._private.isPerimeterValid(formState)).toBe(true);
			});

			it("should validate form with selected nodes", function() {
				report.init({});

				var formState = {
					foo : [ {
						type : "TREE_PICKER",
						value : "10"
					} ]
				};

				expect(report._private.isPerimeterValid(formState)).toBe(true);
			});

			it("should validate form with 'everything' item selected", function() {
				report.init({});

				var formState = {
					foo : [ {
						type : "RADIO_BUTTONS_GROUP",
						selected : true,
						value : "EVERYTHING"
					} ]
				};

				expect(report._private.isPerimeterValid(formState)).toBe(true);
			});
		});

		describe("when validating boundary mode", function() {
			it("should not validate project mode when only nodes are picked", function() {
				report.init({});

				var formState = {
					boundaryMode : [ {
						type : "RADIO_BUTTONS_GROUP",
						selected : true,
						value : "PROJECT_PICKER"
					}, {
						type : "RADIO_BUTTONS_GROUP",
						selected : false,
						value : "TREE_PICKER"
					} ],
					treePicker : [ {
						type : "TREE_PICKER",
						value : "10"
					} ],
					projectPicker : [ {
						type : "PROJECT_PICKER",
						selected : false,
						value : "foo"
					}, {
						type : "PROJECT_PICKER",
						selected : false,
						value : "bar"
					} ]
				};

				expect(report._private.isPerimeterValid(formState)).toBe(false);
			});

			it("should not validate tree mode when only projects are picked", function() {
				report.init({});

				var formState = {
					boundaryMode : [ {
						type : "RADIO_BUTTONS_GROUP",
						selected : true,
						value : "TREE_PICKER"
					}, {
						type : "RADIO_BUTTONS_GROUP",
						selected : false,
						value : "TREE_PICKER"
					} ],
					treePicker : [ {
						type : "TREE_PICKER",
						value : ""
					} ],
					projectPicker : [ {
						type : "PROJECT_PICKER",
						selected : true,
						value : "foo"
					}, {
						type : "PROJECT_PICKER",
						selected : false,
						value : "bar"
					} ]
				};

				expect(report._private.isPerimeterValid(formState)).toBe(false);
			});

			it("should validate against project picker", function() {
				report.init({});

				var formState = {
					boundaryMode : [ {
						type : "RADIO_BUTTONS_GROUP",
						selected : true,
						value : "PROJECT_PICKER"
					}, {
						type : "RADIO_BUTTONS_GROUP",
						selected : false,
						value : "TREE_PICKER"
					} ]
				};

				expect(report._private.reduceToSelectedPicker(formState)).toBe("PROJECT_PICKER");
			});

			it("should validate against treepicker", function() {
				report.init({});

				var formState = {
					boundaryMode : [ {
						type : "RADIO_BUTTONS_GROUP",
						selected : false,
						value : "PROJECT_PICKER"
					}, {
						type : "RADIO_BUTTONS_GROUP",
						selected : true,
						value : "TREE_PICKER"
					} ]
				};

				expect(report._private.reduceToSelectedPicker(formState)).toBe("TREE_PICKER");
			});

			it("should not validate against any picker", function() {
				report.init({});

				var formState = {
					boundaryMode : [ {
						type : "RADIO_BUTTONS_GROUP",
						selected : false,
						value : "TREE_PICKER"
					}, {
						type : "RADIO_BUTTONS_GROUP",
						selected : false,
						value : "TREE_PICKER"
					} ]
				};

				expect(report._private.reduceToSelectedPicker(formState)).not.toBeDefined();
			});
		});
	});
});