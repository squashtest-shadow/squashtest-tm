/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
define([ "jquery", "backbone", "jqueryui", "jquery.squash.togglepanel",
		"jeditable.simpleJEditable", "jeditable.selectJEditable"  ], function($, Backbone) {
	var cfMod = squashtm.app.cfMod;
	/*
	 * Defines the controller for the custom fields table.
	 */
	var CustomFieldModificationView = Backbone.View.extend({
		el : "#information-content",
		initialize : function() {
			// back button
			$("#back").button().click(function() {
				document.location.href = cfMod.backUrl;
			});
			// information toggle panel
			var settings = {
				initiallyOpen : true,
				title : "Information panel",
				cssClasses : "is-contextual",
			}
			this.$("#cuf-info-panel").togglePanel(settings);
			// jeditable
			
			new SimpleJEditable({
				language: {
					richEditPlaceHolder : cfMod.richEditPlaceHolder,
					okLabel: cfMod.okLabel,
					cancelLabel: cfMod.cancelLabel,
				},
				targetUrl : cfMod.customFieldUrl,
				componentId : "cuf-label",
				jeditableSettings : {}
			});
			
			new SelectJEditable({
				language: {
					richEditPlaceHolder : cfMod.richEditPlaceHolder,
					okLabel: cfMod.okLabel,
					cancelLabel: cfMod.cancelLabel,
				},
				targetUrl : cfMod.customFieldUrl,
				componentId : "cuf-inputType",
				jeditableSettings : {data : JSON.stringify(cfMod.inputTypes)},
			});
		},
	// events: {
	// "click #add-cf": "showNewCfPanel"
	// },
	// showNewCfPanel: function(event) {
	// var self = this;
	//
	// var discard = function() {
	// self.newCfPanel.off("cancel confirm");
	// self.newCfPanel.undelegateEvents();
	// self.newCfPanel = null;
	// $(event.target).button("enable");
	// self.table.squashTable().fnDraw();
	// };
	//				
	// $(event.target).button("disable");
	// self.newCfPanel = new NewCustomFieldPanelView({ model: new
	// NewCustomFieldModel() });
	// self.newCfPanel.on("cancel confirm", discard);
	// }
	});
	return CustomFieldModificationView;
});