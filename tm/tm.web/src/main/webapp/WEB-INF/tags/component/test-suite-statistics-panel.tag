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
<%@ tag description="general information panel for an auditable entity. Client can add more info in the body of this tag" body-content="scriptless" %>
<%@ attribute name="statisticsEntity" required="true" type="java.lang.Object" description="The entity which general information we want to show" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="test-suite-statistics-panel">
	
	<span ><f:message key="test-suite.statistics.progression" />&nbsp;:&nbsp;<b>${ statisticsEntity.progression }</b>%&nbsp;(&nbsp;${ statisticsEntity.nbDone }&nbsp;/&nbsp;${ statisticsEntity.nbTestCases }&nbsp;)&nbsp;&nbsp;&nbsp;</span>
	<c:if test="${ statisticsEntity.status.terminatedStatus }">	
			<span ><f:message key="test-suite.statistics.status" />&nbsp;:&nbsp;<b><f:message key="execution.execution-status.DONE" /></b></span>	<br />
	</c:if>
	<c:if test="${ not statisticsEntity.status.terminatedStatus }">	
			<span ><f:message key="test-suite.statistics.status" />&nbsp;:&nbsp;<b><f:message key="${ statisticsEntity.status.i18nKey }" /></b></span>	<br />
	</c:if>
	
	<br />
	<div id="table-div" class="dataTables_wrapper">
		<table id="stats-table" class="is-contextual">
			<thead>
				<tr>
					<th class="ui-state-default" rowspan="1" colspan="1"><f:message key="test-suite.statistics.nbTestCases" /></th>
					<th class="ui-state-default" rowspan="1" colspan="1"><f:message key="test-suite.statistics.nbSuccess" /></th>
					<th class="ui-state-default" rowspan="1" colspan="1"><f:message key="test-suite.statistics.nbFailure" /></th>
					<th class="ui-state-default" rowspan="1" colspan="1"><f:message key="test-suite.statistics.nbRunning" /></th>
					<th class="ui-state-default" rowspan="1" colspan="1"><f:message key="test-suite.statistics.nbBloqued" /></th>
					<th class="ui-state-default" rowspan="1" colspan="1"><f:message key="test-suite.statistics.nbReady" /></th>
				</tr>
			</thead>
			<tbody>
				<tr id="stats:1" class="odd ui-state-highlight">
					<td><center>${ statisticsEntity.nbTestCases }</center></td>
					<td><center>${ statisticsEntity.nbSuccess }</center></td>
					<td><center>${ statisticsEntity.nbFailure }</center></td>
					<td><center>${ statisticsEntity.nbRunning }</center></td>
					<td><center>${ statisticsEntity.nbBloqued }</center></td>
					<td><center>${ statisticsEntity.nbReady }</center></td>
				</tr>
			</tbody>
		</table>
	</div>
</div>