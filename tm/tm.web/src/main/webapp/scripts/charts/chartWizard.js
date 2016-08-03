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
define([ "jquery", "backbone", "app/ws/squashtm.workspace", "workspace.routing", "./wizardRouter", "./wizardView", "./chartWizardModel" ], function($, Backbone, workspace, router, WizardRouter, WizardView, WizardModel) {

	function init() {
		$("#back-popup").confirmDialog().on('confirmdialogconfirm', function(){
			var url = router.buildURL('custom-report-base');
			window.location.href = url;
			});
		
		$("#back").on('click', function() {
			$("#back-popup").confirmDialog('open');
		});	
		
		workspace.init();
		
		$.ajax({
			url: router.buildURL('chart.wizard.data')	
		}).done(function(data){
			
			data.parentId = squashtm.chart.parentId;
			data.defaultProject = squashtm.chart.defaultProject;
			data.chartDef = JSON.parse(squashtm.chart.chartDef);
			
			var model = new WizardModel(data);
			
			var wizardView = new WizardView ({
				model: model
			});
			
			new WizardRouter({
				wizardView : wizardView
			});
			
			Backbone.history.start();
		});
	}
	
	return {
		init : init
	};

});
 