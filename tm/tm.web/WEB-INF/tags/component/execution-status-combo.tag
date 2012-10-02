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
<%@ tag body-content="empty" description="Outputs a combobox of execution statuses with no selection" %>
<%@ attribute name="id" required="true" description="The html id of the combo" %>
<%@ attribute name="name" required="true" description="The name attribute of the combo" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<select id="${ id }" name="${ name }" class="execution-status-combo-class">
	<option value="UNTESTABLE" class="executions-status-bloqued-icon"><f:message key="execution.execution-status.UNTESTABLE" /></option>
	<option value="BLOCKED" class="executions-status-bloqued-icon"><f:message key="execution.execution-status.BLOCKED" /></option>
	<option value="FAILURE" class="executions-status-failure-icon"><f:message key="execution.execution-status.FAILURE" /></option>
	<option value="SUCCESS" class="executions-status-success-icon"><f:message key="execution.execution-status.SUCCESS" /></option>
	<option value="READY" class="executions-status-ready-icon"><f:message key="execution.execution-status.READY" /></option>
</select >				
