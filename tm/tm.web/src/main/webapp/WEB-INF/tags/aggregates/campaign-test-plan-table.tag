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
<%@ tag body-content="empty" description="inserts the html table of test cases" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<table id="test-cases-table">
	<thead>
		<tr>
			<th>Item Id</th>
			<th>#</th>
			<th><f:message key="label.project" /></th>
			<th><f:message key="label.Reference"/></th>
			<th><f:message key="test-case.name.label" /></th>
			<th><f:message key="test-case.user.combo.label" /></th>
			<th>assigned user id (masked)</th>
			<th><f:message key="test-case.importance.combo.label" /></th>
			<th><f:message key="label.Mode" /></th>
			<th>&nbsp;</th>				
			<th>TC Id</th>							
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>
	<div id="test-case-row-buttons" class="not-displayed">
	<a id="delete-test-case-button"  class="delete-test-case-button"><f:message key="test-case.verified_requirement_item.remove.button.label" /></a>
</div> 
