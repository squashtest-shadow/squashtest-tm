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
<%@ tag body-content="empty" description="inserts the html table of verified resquirements" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<table id="verified-requirements-table">
	<thead>
		<tr>
			<th>Id</th>
			<th>#</th>
			<th><f:message key="verified-requirements.table.column-header.project.label" /></th>
			<th><f:message key="requirement.reference.label"/></th>
			<th><f:message key="requirement.name.label" /></th>
			<th><f:message key="requirement.criticality.label"/></th>
			<th>&nbsp;</th>			
			<th>isDirectlyVerified(masked)</th>	
		</tr>
	</thead>
	<tbody>
		<%-- Will be populated through ajax --%>
	</tbody>
</table>
<div id="verified-requirement-row-buttons" class="not-displayed">
	<a id="delete-verified-requirement-button" href="#"	class="delete-verified-requirement-button">
		<f:message key="test-case.verified_requirement_item.remove.button.label" />
	</a>
</div>