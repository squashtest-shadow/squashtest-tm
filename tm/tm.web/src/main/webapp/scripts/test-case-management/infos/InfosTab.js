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
			this.identity = { obj_id : this.settings.testCaseId, obj_restype : "test-cases"  };
			this.prerequisitePanel = new PrerequisitePanel({
				settings : this.settings,
				parentTab : this
			});
			
			this.verifiedRequirementsPanel = new TestCaseVerifiedRequirementsPanel();
			this.listenTo(this.verifiedRequirementsPanel.table, "verifiedrequirementversions.refresh", this.generalInfosPanel.refreshImportanceIfAuto);
			this.listenTo(this.verifiedRequirementsPanel.table, "verifiedrequirementversions.tableDrawn", this.sendUpdateReqToTree);
			eventBus.onContextual("testStepsTable.removedSteps", this.verifiedRequirementsPanel.table.refresh);
		},
		
		events : {},
		
		_sendUpdateReqToTree : function(){
			var rows = this.verifiedRequirementsPanel.table.getRowNumber();
			var reqCoverStatus = "ko";
			if(rows){
				reqCoverStatus = "ok";
			}
			var evt = new EventUpdateReqCoverage(this.identity, reqCoverStatus);
			squashtm.workspace.eventBus.fire(null, evt);
		}
	});
	return ParametersTab;
});