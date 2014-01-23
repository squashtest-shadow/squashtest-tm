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
/* https://github.com/requirejs/example-multipage-shim */
({
	appDir : "${project.basedir}/src/main/webapp", // root of webapp files
	mainConfigFile: "${project.basedir}/src/main/webapp/scripts/common.js",
	baseUrl : "scripts", // where the js files are supposed to be inside appDir
	dir : "${rjs.outputDirectory}", // output of optimizer
	optimize: "uglify2",
	skipDirOptimize: true,  // only want to minify the build layers specified in modules options and not the rest of the JS files in the build output directory
	generateSourceMaps : true, // maps enables modern browsers to fetch pretty source files from ugly ones 
	preserveLicenseComments: false,
	// for each module, we have to tell which are the top level dependencies in the "include" field
	modules : [ {
		name : "common", 
		include: [
		  				// shimmed
		  				"jqueryui",
		  				"datatables",
		  				"handlebars",
		  				"underscore",
		  				"backbone",
		  				"ckeditor",
		  				"jquery.ckeditor",
		  				"jeditable",
		  				"jeditable.ckeditor",
		  				"jeditable.datepicker",
		  				"jeditable.simpleJEditable",
		  				"jeditable.selectJEditable",
		  				"jstree",
		  				"jform",
		  				"jquery.generateId",
		  				"jquery.hotkeys",
		  				"jquery.timepicker",
		  				"jquery.squash",
		  				"jquery.squash.fragmenttabs",
		  				"jquery.squash.togglepanel",
		  				"jquery.squash.messagedialog",
		  				"jquery.squash.confirmdialog",
		  				"jquery.squash.oneshotdialog",
		  				"jquery.squash.squashbutton",
		  				"jquery.squash.jeditable",
		  				"jquery.squash.projectpicker",
		  				"jquery.cookie",
		  				"jquery.tagit",
		  				"jquery.switchButton",
		  				"jqplot-core",
		  				"jqplot-pie",
		  				"squash.KeyEventListener",
		  				"squash.events",
		  				// AMD
		  				"domReady",
		  				"jquery",
		  				"squash.cssloader",
		  				"squash.translator",
		  				"squash.resizer",
		  				"squash.session-pinger",
		  				"jquery.squash.tagit",
		  				"workspace.contextual-content",
		  				]
	}, {
		name: "login-page", 
		include: ["jquery",
		          "app/ws/squashtm.notification","jqueryui","jquery.squash.squashbutton"],
		exclude: [
			"common"
		] 
	} ]
})