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

define([ "jquery", "backbone", "underscore", "./GeneralInfosPanel", "./PrerequisitePanel", "../../verified-requirements/TestCaseVerifiedRequirementsPanel","workspace.event-bus" ], function($,
		Backbone, _, GeneralInfosPanel, PrerequisitePanel, TestCaseVerifiedRequirementsPanel, eventBus) {
	var ParametersTab = Backbone.View.extend({

		el : "#tab-tc-informations",

		initialize : function() {
			var self = this;
			this.settings = this.options.settings;
			this.sendUpdateReqToTree = $.proxy(this._sendUpdateReqToTree, this);
			this.generalInfosPanel = new GeneralInfosPanel({
				settings : this.settings,
				parentTab : this
			});
			this.prerequisitePanel = new PrerequisitePanel({
				settings : this.settings,
				parentTab : this
			});
			
			this.verifiedRequirementsPanel = new TestCaseVerifiedRequirementsPanel();
			this.listenTo(this.verifiedRequirementsPanel.table, "verifiedrequirementversions.refresh", this.generalInfosPanel.refreshImportanceIfAuto);
			this.listenTo(this.verifiedRequirementsPanel.table, "verifiedrequirementversions.refresh", this.sendUpdateReqToTree);
			//eventBus.onContextual("testStepsTable.removedSteps", this.verifiedRequirementsPanel.table.refresh);
			//todo remove below and add event when delete row in test step if called step
			eventBus.onContextual("testStepsTable.pastedCallSteps", this.verifiedRequirementsPanel.table.refreshRestore);
			eventBus.onContextual("testStepsTable.deletedCallSteps", this.verifiedRequirementsPanel.table.refreshRestore);
		},
		
		events : {},
		
		_sendUpdateReqToTree : function(){
			eventBus.trigger("node.update-reqCoverage", {targetIds : [this.settings.testCaseId]});
		}
	});
	return ParametersTab;
});