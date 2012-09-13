<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2012 Henix, henix.fr

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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>

<s:url var="automatedSuitesUrl" value="/automated-suites">
</s:url>



<div id="execution-info-template" style="display: hidden">
	<div class="display-table-row">
		<div class="executionName display-table-cell"></div>
		<div class="executionStatus display-table-cell"></div>
	</div>
</div>


<!-- *************************POPUP*********************** -->
<pop:popup id="execute-auto-dialog" titleKey="dialog.execute-auto.title"
	isContextual="true" closeOnSuccess="false">
	<jsp:attribute name="buttons">
			
				<f:message var="label" key="label.upper.Close" />
				'${ label }': function() {
					$( this ).dialog( 'close' );
				}		
				
			</jsp:attribute>
			<jsp:attribute name="additionalSetup">
			height: 530
			</jsp:attribute>
	<jsp:attribute name="body">
			<div class="executions-auto-top" style="height:335px; width: 100%; overflow-y: scroll">
				<div id="executions-auto-infos" class="display-table" style="width:100%">
				</div>
			</div>
			<div class="executions-auto-bottom" style="min-height:45px; width: 100%; ">
			
				<div id="execution-auto-progress"
				style="width: 80%; margin: auto; margin-top: 20px">
					<div
					style="width: 80%; display: inline-block; vertical-align: middle">
					<div id="execution-auto-progress-bar"></div>
				</div>
	 				<div id="execution-auto-progress-amount"
					style="width: 10%; display: inline-block"></div>
				</div>
				
				<div class="popup-notification">
				<f:message key="dialog.execute-auto.close.note" />
				</div>
				
				</div>
				
			</jsp:attribute>
</pop:popup>
<script>
	$("#execution-auto-progress-bar").progressbar({
		value : 0
	});
	
	squashtm.automatedSuiteOverviewDialog = new AutomatedSuiteOverviewDialog({
			automatedSuiteBaseUrl : "${automatedSuitesUrl}",
	});
	
</script>
<!-- *************************/POPUP*********************** -->