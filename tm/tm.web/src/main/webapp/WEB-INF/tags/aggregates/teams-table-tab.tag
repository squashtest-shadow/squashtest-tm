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
<%@ tag body-content="empty" description="inserts the content of the team tab" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<div id="team-table-pane" class="table-tab" >
  <div class="toolbar">
    <button id="new-team-button"><f:message key="label.addTeam"/></button>
  </div>
  <div class="table-tab-wrap">
    <table id="teams-table" data-def="deferloading=${ pagedTeams.totalNumberOfItems }, pagesize=${ teamsPageSize }"  class="unstyled-table">
    	<thead>
    		<tr>
    			<th class="not-displayed">Id(masked)</th>
    			<th>#</th>
    			<th class="datatable-filterable"><f:message key="label.Name" /></th>
    			<th><f:message key="label.Description"/></th>
    			<th><f:message key="label.numberOfAssociatedUsers"/></th>
    			<th><f:message key="label.CreatedOn" /></th>
    			<th class="datatable-filterable"><f:message key="label.createdBy" /></th>
    			<th><f:message key="label.modifiedOn"/></th>
    			<th class="datatable-filterable"><f:message key="label.modifiedBy"/></th>
    			<th>&nbsp;</th>	
    		</tr>
    	</thead>
    	<tbody>
        <c:forEach var="team" varStatus="teamStat" items="${ pagedTeams.pagedItems }">
        <tr>
          <td class="not-displayed">${ team.id }</td>
          <td >${ teamStat.index + 1 }</td>
          <td>${ team.name }</td>
          <td>${ team.description }</td>
          <td>${ fn:length(team.members) }</td>
          <td><comp:date value="${ team.createdOn }" /></td>
          <td>${ team.createdBy }</td>
          <td><comp:date value="${ team.lastModifiedOn }" /></td>
          <td>${ team.lastModifiedBy }</td>
          <td>&nbsp;</td>
        </tr>
        </c:forEach>
    		<%-- Will be populated through ajax --%>
    	</tbody>
    </table>
  </div>
</div><%-- /div#team-table-pane --%>
<div id="add-team-dialog" class="not-displayed popup-dialog form-horizontal" title="<f:message key='title.addTeam' />">
  <table class="form-horizontal">
    <tr class="control-group">
      <td>
        <label class="control-label" for="add-team-name"><f:message key="label.Name" /></label>
      </td>
      <td class="controls">
        <input id="add-team-name" name="add-team-name" type="text" size="50" maxlength="255"
        style="display: block;" /><%-- inlined style as quickfix for issue #2206 --%>
        <comp:error-message forField="name" />
      </td>
    </tr>
    <tr class="control-group">
      <td>
        <label class="control-label" for="add-team-description"><f:message key="label.Description" /></label>
      </td>
      <td class="controls">
        <textarea id="add-team-description" name="add-team-description"></textarea>
        <span class="help-inline">&nbsp;</span>
      </td>
    </tr>
  </table>
  
  <div class="popup-dialog-buttonpane">
    <input class="confirm" type="button" value="<f:message key='label.Add' />" />
    <input class="cancel" type="button" value="<f:message key='label.Cancel' />" />
  </div>  
</div>