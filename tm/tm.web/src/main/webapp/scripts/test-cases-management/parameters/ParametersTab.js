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

define([ "jquery", "backbone", "./ParametersPanel",	"./DatasetsPanel", "jquery.squash.confirmdialog" ],
		function($, Backbone, ParametersPanel, DatasetsPanel) {
			var ParametersTab = Backbone.View.extend({
			
				el : "#parameters-tabs-panel",
				
				initialize : function() {
					var self = this;
					this.settings = this.options.settings;
//					$("div.fragment-tabs").tabs("option" , {cache: false, ajaxOptions : {cache:false}});
					this.parametersPanel = new ParametersPanel({settings : this.settings, parentTab : this});
					this.datasetsPanel = new DatasetsPanel({settings : this.settings, parentTab : this});
				},
				events : {}
			});
			return ParametersTab;
		});