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
function AutomatedSuiteOverviewDialog(settings){
	
	//------------------------public -----------------------------
	this.open = openDialog;
	this.popup = $("#execute-auto-dialog");
		
	//------------------------initialize---------------------------------
	var self = this;
	initialize();
	var automatedSuiteBaseUrl = settings.automatedSuiteBaseUrl;
	var executionRowTemplate = $("#execution-info-template .display-table-row");
	var executionAutoInfos = $("#executions-auto-infos");
	
	function initialize(){
		self.popup.bind("dialogclose", function(event, ui) {
			clearInterval(autoUpdate);
			executionAutoInfos.empty();
			$("#execution-auto-progress-bar").progressbar("value", 0);
			$("#execution-auto-progress-amount").text(0 + "/" + 0);
			refreshTestPlans();
			refreshStatistics();
		});
	}
		
	//---------------------------private -----------------------------------------
		
		var autoUpdate ; 
		var suiteId ; 
		
		function openDialog(suiteView) {
			suiteId = suiteView.suiteId;
			//update progress bar values
			updateProgress(suiteView);
			//fill execution-info content
			fillContent(suiteView);
			
			self.popup.dialog('open');
			
			if (suiteView.percentage < 100) {
				autoUpdate = setInterval(function() {
					refreshContent();
				}, 5000);
			}
		}
		
		function updateProgress(suiteView) {
			var executions = suiteView.executions;
			var progress = suiteView.percentage;
			var executionTerminated = progress / 100 * executions.length
			$("#execution-auto-progress-bar").progressbar("value", progress);
			$("#execution-auto-progress-amount").text(
					executionTerminated + "/" + executions.length);
		}
		
		
		
		function fillContent(suiteView){
				
			var executions = suiteView.executions;
			
			var template = executionRowTemplate.clone()
			for (i = 0; i < executions.length; i++) {
				var execution = executions[i];
				var executionHtml = template.clone();
				
				//NAME
				executionHtml.attr('id', "execution-info-" + execution.id);
				executionHtml.find(".executionName").html(execution.name);
				//STATUS
				var executionStatus = executionHtml.find(".executionStatus");
				var statusHtml = squashtm.statusFactory.getHtmlFor(execution.localizedStatus, execution.status);
				executionStatus.html(statusHtml);
				
				
				executionAutoInfos.append(executionHtml);
				
			}
		}
		
		function refreshContent() {
		$.ajax({
			type : 'GET',
			url : automatedSuiteBaseUrl+"/" + suiteId + "/executions",
			dataType : "json"
		}).done(
				function(suiteView) {
					//find executions in json
					var executions = suiteView.executions;
					for (i = 0; i < executions.length; i++) {
						//FIND EXEC
						var execution = executions[i];
						var executionHtml = $("#execution-info-" + execution.id);
						
						//CHANGE STATUS
						var executionStatus = executionHtml.find(".executionStatus");
						var statusHtml = squashtm.statusFactory.getHtmlFor( execution.localizedStatus, execution.status);
						executionStatus.html(statusHtml);
						

					}
					updateProgress(suiteView)
					if (suiteView.percentage == 100) {
						clearInterval(autoUpdate);
					}
				});
	}
		
}