<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2013 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ tag
	description="general information panel for an auditable entity. Client can add more info in the body of this tag"
	pageEncoding="utf-8"%>
<%@ attribute name="testPlanTableId" required="true"%>
<%@ attribute name="url" required="true"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>


<!-- *************************INITIALISATION*********************** -->
<script type="text/javascript">
		
	function executeAll() {
		var ids = [];
		executeAuto(ids);

	}

	function executeSelection() {
		var table = $('#${testPlanTableId}').dataTable();
		var ids = getIdsOfSelectedTableRows(table, getTestPlansTableRowId);
		if (ids.length == 0) {
			$.squash
					.openMessage("<f:message key='popup.title.error' />",
							"<f:message	key='message.EmptyTableSelection'/>");
		} else {
			executeAuto(ids);
		}
	}
	function executeAuto(ids) {
		$.ajax({
			type : 'POST',
			url : "${url}",
			dataType : "json",
			data : {
				"id" : "execute-auto",
				"testPlanItemsIds" : ids
			}
		}).done(function(suiteView) {
			if(suiteView.executions.length == 0){
				$.squash
				.openMessage("<f:message key='popup.title.Info' />",
						"<f:message	key='dialog.execution.auto.overview.error.none'/>");
			}else{
				squashtm.automatedSuiteOverviewDialog.open(suiteView);
			}
		});
	}
	
	
</script>

<!-- *************************/INITIALISATION*********************** -->
<!-- *************************BUTTON*********************** -->
<div id="iteration-suite-auto-execution-button"
	style="display: inline-block;">

	<%--
	<a tabindex="0" href="#execute-auto" class="button run-menu"
		id="execute-auto-button" class="button"><f:message
			key="iteration.suite.execution.auto.label" /> </a>
	 --%>
	 <f:message var="autoExecLabel" key="iteration.suite.execution.auto.label"/>
	<input id="execute-auto-button" class="button run-menu" type="button" value="${autoExecLabel}" />
	<ul class="not-displayed">
		<li>
			<a id="execute-auto-execute-all" href="javascript:void(0)">
				<f:message	key="iteration.suite.execution.auto.all.label" /> 
			</a>
		</li>
		<li>
			<a class="execute-auto-execute-selection" href="javascript:void(0)">
				<f:message	key='iteration.suite.execution.auto.selection.label' /> 
			</a>
		</li>
	</ul>
	

	<script>
	
		$(function() {
			require(['jquery', 'jquery.squash.buttonmenu'], function($){
				
				$("#execute-auto-button").buttonmenu();

				$("#execute-auto-execute-all").on('click', executeAll);
				$("#execute-auto-execute-selection").on('click', executeSelection);
			});			
		});
	</script>
</div>
<!-- *************************/BUTTON*********************** -->
