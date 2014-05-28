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
<%@ tag description="test automation panel (project level)" body-content="empty"%>

<%@ attribute name="project" type="java.lang.Object" required="true" description="the TM Project"%>
<%@ attribute name="availableTAServers" type="java.util.Collection" required="true"
  description="the list of the available TA servers"%>

<%@ tag language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json"%>


<f:message var="confirmLabel" key="label.Confirm" />
<f:message var="cancelLabel" key="label.Cancel" />
<f:message var="noServerLabel" key="label.NoServer" />

<c:url var="listRemoteProjectsURL" value="/test-automation/servers/projects-list" />

<s:url var="projectUrl" value="/generic-projects/{projectId}">
  <s:param name="projectId" value="${project.id}" />
</s:url>

<s:url var="localProjectsURL" value="/generic-projects/{projectId}/test-automation-projects">
  <s:param name="projectId" value="${project.id}" />
</s:url>


<c:set var="inputSize" value="50" />

<comp:toggle-panel id="test-automation-management-panel" titleKey="project.testauto.panel.title" open="true">

  <jsp:attribute name="body">
		<div class="ta-main-div">
		
			<%-- =================================== server block =============================================================== --%>	
		
			<fieldset class="ta-server-block ta-block">
				<legend>
					<f:message key="project.testauto.serverblock.title" />
				</legend>
				
				<div id="selected-ta-server-span" class="std-margin-top std-margin-bottom">${(not empty project.testAutomationServer) ? project.testAutomationServer.name : noServerLabel }</div>
								
			</fieldset> 
			<%-- =================================== /server block =============================================================== --%>	
		
			
			<%-- =================================== projects block =============================================================== --%>
			
			<f:message var="addTAProjectLabel" key="project.testauto.projectsblock.add.button.label" />
			<fieldset class="ta-projects-block  ta-block">
				<legend>
					<f:message key="project.testauto.projectsblock.title" />
			        <button id="ta-projects-bind-button" title="${addTAProjectLabel}" class="sq-icon-btn btn-sm">
			          <span class="ui-icon ui-icon-plus"></span>
			        </button>
				</legend>

				
				<table id="ta-projects-table" class="ta-projects-table"
          data-def="ajaxsource=${localProjectsURL}, hover, deferloading=${fn:length(project.testAutomationProjects)}">
					<thead>
						<tr>
							<th data-def="map=entity-index,narrow, select">#</th>
							<th data-def="map=name">
                <f:message key="project.testauto.projectsblock.table.headers.name" />
              </th>
							<th data-def="map=server-url">
                <f:message key="project.testauto.projectsblock.table.headers.serverurl" />
              </th>
							<th data-def="map=server-kind">
                <f:message key="project.testauto.projectsblock.table.headers.serverkind" />
              </th>
							<th data-def="delete-button=#ta-projects-unbind-popup">&nbsp;</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${project.testAutomationProjects}" var="taproj" varStatus="status">
						<tr>
							<td>${taproj.id}</td>
							<td>${status.index}</td>
							<td>${taproj.name}</td>
							<td>${taproj.server.baseURL}</td>
							<td>${taproj.server.kind}</td>
							<td> </td>
						</tr>
						</c:forEach>
					</tbody>
				</table>
				<br />
		</fieldset>
		<%-- =================================== /projects block =============================================================== --%>	
		
	</div>
	</jsp:attribute>

</comp:toggle-panel>


<%-- ================================================

	Change TA server confirmation popup
	

 ================================================= --%>

<div id="ta-server-confirm-popup" class="popup-dialog" title="CONFIRM">

  <!-- _____________CASE 1_______________ -->
  <div data-def="state=case1">
    <p>
      <f:message key="message.testAutomationBinding.removeJobs" />
    </p>
    <p>
      <label>
        <f:message key="label.warning" />
      </label>
      <f:message key="message.testAutomationServer.noExecution.warning" />
    </p>
    <p>
      <f:message key="message.testAutomationServer.change.confirm" />
    </p>
  </div>
  <!-- _____________CASE 2_______________ -->
  <div data-def="state=case2">
    <p>
      <f:message key="message.testAutomationBinding.removeJobs" />
    </p>
    <p>
      <label>
        <f:message key="label.warning" />
      </label>
      <f:message key="message.testAutomationServer.withExecution.warning" />
    </p>
    <p>
      <f:message key="message.testAutomationServer.change.confirm" />
    </p>
  </div>
  <!-- _____________Progression_______________ -->
  <div data-def="state=pleasewait">
    <comp:waiting-pane />
  </div>
  <div class="popup-dialog-buttonpane">
    <input class="confirm" type="button" value="${confirmLabel}"
      "
                data-def="evt=confirm,  state=case1, mainbtn" />
    <input class="confirm" type="button" value="${confirmLabel}"
      "
                data-def="evt=confirm,  state=case2, mainbtn" />
    <input class="cancel" type="button" value="${cancelLabel}" data-def="evt=cancel" />
  </div>
</div>


<%-- ================================================
	Project add popup. 
	
	Dumb definition here, the code is elsewhere 
================================================= --%>


<f:message var="bindProjectPopup" key="project.testauto.projectsblock.add.popup.title" />
<div id="ta-projects-bind-popup" title="${bindProjectPopup}" class="popup-dialog not-displayed">

  <div data-def="state=pleasewait">
    <comp:waiting-pane />
  </div>

  <div data-def="state=fatalerror">
    <span> </span>
  </div>

  <div data-def="state=error">
    <span> </span>
  </div>

  <div data-def="state=main" class="ta-projects-bind-maindiv">
    <label>
      <f:message key="project.testauto.projectsblock.add.popup.caption" />
    </label>
    <table class="ta-project-bind-listdiv">

      <%--
				!!!!!!!!!!!!!!! CONSEILS DEVELOPPEMENT !!!!!!!!!!!!!!
	
				TOUT LE CSS UTILISE PAR CETTE POPUP DEVRAIT RESPECTER 
				LA CONVENTION 'ta-projects-bind-X' ET DECLARE DANS LE
				FICHIER 'components.css' A COTE DE LA CLASSE 
				'ta-projects-bind-maindiv'
				
				!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!			
			
			 --%>

    </table>

  </div>
  <div class="ta-projectsadd-fatalerror">
    <span> </span>
  </div>

  <div class="ta-projectsadd-error">
    <span> </span>
  </div>
  <div class="popup-dialog-buttonpane">
    <input type="button" value="${confirmLabel}" data-def="mainbtn=main, evt=confirm" />
    <input type="button" value="${cancelLabel}" data-def="evt=cancel" />

  </div>

</div>


<%-- the project unbind confirmation popup (STUB) --%>

<f:message var="unbindPopupTitle" key="dialog.unbind-ta-project.tooltip" />
<div id="ta-projects-unbind-popup" class="popup-dialog not-displayed" title="${unbindPopupTitle}">

  <div>
    <f:message key="dialog.unbind-ta-project.message" />
  </div>
  <script id="default-item-tpl" type="text/x-handlebars-template" th:inline="text">
        <tr class="listdiv-item"> <td><input type="checkbox" value="{{jsonItem.name}}"/><td> <td>{{jsonItem.name}}</td><td class="ta-project-tm-label"><label th:text="#{label.taProjectTmLabel}">Libellé dans Squash TM</label></td></tr>
	</script>
  <div class="popup-dialog-buttonpane">
    <input type="button" value="${confirmLabel}" data-def="evt=confirm" />
    <input type="button" value="${cancelLabel}" data-def="evt=cancel, mainbtn" />
  </div>

</div>


<%-- ===================================
	Js initialization
==================================== --%>

<script type="text/javascript">
require(["common"], function() {
	require(["jquery", "projects-manager/project-info/automation-panel", "squashtable"], function($, automationBlock){
		$(function(){
			//************************** manager setup ********************
			
			var automationSettings = {
				tmProjectURL : "${projectUrl}",
				availableServers: ${json:serialize(availableTAServers)},
				TAServerId : ${(empty project.testAutomationServer) ? 0 : project.testAutomationServer.id}
			};
			
			automationBlock.init(automationSettings);
			

		});
		
	});
});

	

	

	
</script>