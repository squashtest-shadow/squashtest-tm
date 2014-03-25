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
<%@ tag body-content="empty" description="the calling test case table" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<%@ attribute name="writable"  required="true" type="java.lang.Boolean"  description="if the user has write permission on this test case" %>


<f:message var="renameDialogTitle" key="dialog.rename-test-case.title"/>
<f:message var="renameButtonLabel" key="dialog.rename-test-case.title"/>
<f:message var="cancelLabel" key="label.Cancel" />


<%---------------------------- Rename test case popup ------------------------------%>

<c:if test="${ writable }">

<div id="rename-test-case-dialog" title="${renameDialogTitle}" class="popup-dialog not-displayed">
	
	<div>
		<label><f:message key="dialog.rename.label" /></label>
		<input type="text" id="rename-test-case-input"  maxlength="255"	size="50" />
		<br />
		<comp:error-message forField="name" />
	</div>
	
	<div class="popup-dialog-buttonpane">
		<input type="button" value="${renameButtonLabel}" data-def="evt=confirm, mainbtn"/>
		<input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
	</div>
</div>

</c:if>

