<%--

        This file is part of the Squashtest platform.
        Copyright (C) Henix, henix.fr

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
<%@ tag body-content="empty" description="inserts the content of the connection history tab" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<div id="connection-table-pane" class="table-tab" >
  <div class="table-tab-wrap">
    <table id="connections-table" data-def="deferloading=${ pagedConnectionLogs.totalNumberOfItems }, pagesize=${ connectionsPageSize }" class="unstyled-table">
      <thead>
        <tr>
          <th class="not-displayed">Id(masked)</th>
          <th>#</th>
          <th class="datatable-filterable"><f:message key="label.Login" /></th>
          <th class="datatable-filterable"><f:message key="label.connectionDate" /></th>
          <th class="datatable-filterable"><f:message key="label.successful"/></th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="connection" varStatus="teamStat" items="${ pagedConnectionLogs.pagedItems }">
        <tr>
          <td class="not-displayed">${ connection.id }</td>
          <td >${ teamStat.index + 1 }</td>
          <td>${ connection.login }</td>
          <td><comp:date value="${ connection.connectionDate }" /></td>
          <td>${ connection.success }</td>
        </tr>
        </c:forEach>
        <%-- Will be populated through ajax --%>
      </tbody>
    </table>
  </div>
</div><%-- /div#team-table-pane --%>
