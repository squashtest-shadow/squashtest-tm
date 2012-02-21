<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org

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
	<span ><f:message key="test-suite.statistics.nbTestCases" />&nbsp;:&nbsp;${ statisticsEntity.nbTestCases }</span>	<br />
	<span ><f:message key="test-suite.statistics.progression" />&nbsp;:&nbsp;${ statisticsEntity.progression }%&nbsp;(&nbsp;${ statisticsEntity.nbDone }&nbsp;/&nbsp;${ statisticsEntity.nbTestCases }&nbsp;)&nbsp;</span>	<br />
	<span ><f:message key="test-suite.statistics.nbSuccess" />&nbsp;:&nbsp;${ statisticsEntity.nbSuccess }</span>	<br />
	<span ><f:message key="test-suite.statistics.nbFailure" />&nbsp;:&nbsp;${ statisticsEntity.nbFailure }</span>	<br />
	<span ><f:message key="test-suite.statistics.nbRunning" />&nbsp;:&nbsp;${ statisticsEntity.nbRunning }</span>	<br />
	<span ><f:message key="test-suite.statistics.nbBloqued" />&nbsp;:&nbsp;${ statisticsEntity.nbBloqued }</span>	<br />
	<span ><f:message key="test-suite.statistics.nbReady" />&nbsp;:&nbsp;${ statisticsEntity.nbReady }</span>	<br />
	<c:if test="${ statisticsEntity.status.terminatedStatus }">	
			<span ><f:message key="test-suite.statistics.status" />&nbsp;:&nbsp;<b><f:message key="execution.execution-status.DONE" /></b></span>	<br />
	</c:if>
	<c:if test="${ not statisticsEntity.status.terminatedStatus }">	
			<span ><f:message key="test-suite.statistics.status" />&nbsp;:&nbsp;<b><f:message key="${ statisticsEntity.status.i18nKey }" /></b></span>	<br />
	</c:if>
</div>