<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

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
<%@ tag description="definition of the popup that follows the execution of an automated test suite." pageEncoding="utf-8"%>
	
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>

<s:url var="automatedSuitesUrl" value="/automated-suites" />

<f:message var="popupTitle" key='dialog.execute-auto.title'/>
<f:message var="closeLabel" key='label.Close' />
<f:message var="okLabel" 	key='label.Ok' />
<f:message var="cancelLabel" 	key='label.Cancel' />

<div id="execution-info-template" class="not-displayed">
	<div class="display-table-row">
		<div class="execution-name display-table-cell"></div>
		<div class="execution-status display-table-cell"></div>
	</div>
</div>
<!-- *************************POPUP*********************** -->


<div id="execute-auto-dialog" class="popup-dialog not-displayed" title="${popupTitle}" 
	data-def="url=${automatedSuitesUrl}, height=490">

	<div data-def="state=main">
		<div class="executions-auto-top" style="height:335px; width: 100%; overflow-y: scroll">
			<div id="executions-auto-infos" class="display-table" style="width:100%"></div>
		</div>
		
		<div class="executions-auto-bottom" style="min-height:45px; width: 100%; ">
		
			<div id="execution-auto-progress" style="width: 80%; margin: auto; margin-top: 20px">
				<div style="width: 80%; display: inline-block; vertical-align: middle">
					<div id="execution-auto-progress-bar" ></div>
				</div>
				<div id="execution-auto-progress-amount" style="width: 10%; display: inline-block"></div>
			</div>
			
		</div>
	</div>
	
	<div data-def="state=warning">
		
		<span><f:message key='message.CloseAutomatedSuiteOverview'/></span>
	
	</div>

	<div class="popup-dialog-buttonpane">
		<input type="button" value="${closeLabel}" data-def="evt=mainclose, state=main, mainbtn=main"/>
		<input type="button" value="${okLabel}" data-def="evt=warningok, state=warning"/>
		<input type="button" value="${cancelLabel}" data-def="evt=warningcancel, state=warning, mainbtn=warning"/>	
	</div>

</div>

<script type="text/javascript">
require( ["common"], function(){

		require(["jquery","test-automation/automated-suite-overview"], function($,overview){
			$(function(){
				overview.init();
			});
	});
});
</script>

<!-- *************************/POPUP*********************** -->