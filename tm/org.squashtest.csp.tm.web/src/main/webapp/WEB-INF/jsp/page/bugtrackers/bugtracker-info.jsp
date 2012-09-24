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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>

<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%------------------------------------- URLs et back button ----------------------------------------------%>
<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="bugtrackerUrl" value="/bugtracker/{bugtrackerId}">
	<s:param name="bugtrackerId" value="${bugtracker.id}" />
</s:url>
<s:url var="bugtrackersUrl" value="/bugtrackers" />


<layout:info-page-layout titleKey="workspace.bugtracker.info.title">
	<jsp:attribute name="head">	
		<link rel="stylesheet" type="text/css"
			href="${ pageContext.servletContext.contextPath }/styles/master.blue.css" />	
	</jsp:attribute>

	<jsp:attribute name="titlePane">
		<h2>
			<f:message key="workspace.bugtracker.info.title" />
		</h2>	
	</jsp:attribute>

	<jsp:attribute name="informationContent">

		<div id="bugtracker-name-div"
			class="ui-widget-header ui-corner-all ui-state-default fragment-header">

			<div style="float: left; height: 3em">
				<h2>
					<label for="bugtracker-name-header"><f:message
							key="label.Bugtracker" />
					</label><a id="bugtracker-name-header" href="javascript:void(0);"><c:out
							value="${ bugtracker.name }" escapeXml="true" />
					</a>
				</h2>
			</div>

			<div style="float: right;">
				<f:message var="back" key="label.Back" />
				<input id="back" type="button" value="${ back }" />
				
			</div>

			<div style="clear: both;"></div>

		</div>
	
		<div class="fragment-body">
			<%------------------------------------------------ BODY -----------------------------------------------%>
	
			<div id="bugtracker-toolbar" classes="toolbar-class ui-corner-all">
				<%--- Toolbar ---------------------%>
				
			<div class="toolbar-button-panel">
				<f:message var="rename" key="rename" />
				<input type="button" value="${ rename }" id="rename-bugtracker-button"
							class="button" />
			</div>
			</div>
			<%--------End Toolbar ---------------%>
		
			<%----------------------------------- INFORMATION PANEL -----------------------------------------------%>
			<br />
			<br />
			<comp:toggle-panel id="bugtracker-info-panel"
				titleKey="label.BugtrackerInformations" isContextual="true" open="true"
				classes="information-panel">
	
				<jsp:attribute name="body">
					<div id="bugtracker-description-table" class="display-table">
					<div class="display-table-row">
							<label for="bugtracker-kind" class="display-table-cell">
							<f:message key="label.Kind" />
							</label>
							<div class="display-table-cell" id="bugtracker-kind">${ bugtracker.kind }</div>
						<comp:select-jeditable componentId="bugtracker-kind" jsonData="${bugtrackerKinds}" targetUrl="${bugtrackerUrl}" />
						</div>
						<div class="display-table-row">
							<label for="bugtracker-url" class="display-table-cell">
							<f:message key="label.Url" />
							</label>
							<div class="display-table-cell" id="bugtracker-url">${ bugtracker.url }</div>
							<comp:simple-jeditable targetUrl="${ bugtrackerUrl }" componentId="bugtracker-url" submitCallback="changeBugTrackerUrlCallback"/>
						</div>
						<div class="display-table-row">
							<label for="bugtracker-iframeFriendly" class="display-table-cell">
							<f:message key="label.DisplaysInIframe" />
							</label>
							<div class="display-table-cell" id="bugtracker-iframeFriendly" style="cursor:pointer">
								<input id="bugtracker-iframeFriendly-checkbx" type="checkbox" 
								<c:if test="${bugtracker.iframeFriendly}">
								checked="checked"
								</c:if>
								/>
							</div>
						</div>
					</div>
				</jsp:attribute>
			</comp:toggle-panel>
			
			
			<%-----------------------------------END INFORMATION PANEL -----------------------------------------------%>
			</div>
		<%---------------------------------------------------------------END  BODY -----------------------------------------------%>
	<comp:decorate-buttons />
	</jsp:attribute>
</layout:info-page-layout>
<script type="text/javascript">

//*****************Back button
	$(function() {
		$("#back").button().click(clickBugtackerBackButton);
		$("#bugtracker-iframeFriendly-checkbx").change(clickBugTrackerIframeFriendly);
	
	});
	
	function clickBugtackerBackButton(){
		document.location.href = "${bugtrackersUrl}";
	}
	
	function clickBugTrackerIframeFriendly(){
		
		var isIframeFriendlyPostParam = $("#bugtracker-iframeFriendly-checkbx").attr("checked");
	 	
		$.ajax({
			type : 'post',
			data : {
				'isIframeFriendly' : isIframeFriendlyPostParam
			},
			dataType : "json",
			url : "${ bugtrackerUrl }"
		}).done(function(){
				//Update navigation menu
						updateBugTrackerMenu(false);
			});
	 }
	
	function changeBugTrackerUrlCallback(){
		updateBugTrackerMenu(false);
	}

</script>

<!-- --------------------------------RENAME POPUP--------------------------------------------------------- -->

	<comp:popup id="rename-bugtracker-dialog"
		titleKey="dialog.rename-bugtracker.title" isContextual="true"
		openedBy="rename-bugtracker-button">
		<jsp:attribute name="buttons">
		<f:message var="label" key="rename" />
			'${ label }': function() {
				var url = "${ bugtrackerUrl }";
				<jq:ajaxcall url="url" dataType="json" httpMethod="POST"
					useData="true" successHandler="renameBugtrackerSuccess">					
					<jq:params-bindings newName="#rename-bugtracker-input" />
				</jq:ajaxcall>					
			},			
			<pop:cancel-button />
		</jsp:attribute>
		<jsp:body>
			<script type="text/javascript">
				$("#rename-bugtracker-dialog").bind("dialogopen", function(event, ui) {
					var name = $.trim($('#bugtracker-name-header').text());
					$("#rename-bugtracker-input").val($.trim(name));
			
				});
				/* renaming success handler */
				function renameBugtrackerSuccess(data) {
					updateBugTrackerMenu(false);
					$('#bugtracker-name-header').html(data.newName);
					$('#rename-bugtracker-dialog').dialog('close');
				}
			</script>
			<label><f:message key="dialog.rename.label" /></label>
			<input type="text" id="rename-bugtracker-input" maxlength="255" size="50" />
			<br />
			<comp:error-message forField="name" />
		</jsp:body>
	</comp:popup>
<!-- ------------------------------------END RENAME POPUP------------------------------------------------------- -->