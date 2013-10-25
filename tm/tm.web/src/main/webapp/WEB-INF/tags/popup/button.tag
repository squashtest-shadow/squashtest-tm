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
<%@ tag  description="adds a button to a popup." body-content="empty" %>
<%@ attribute name="labelKey" required="true" description="key of this button's label" %>
<%@ attribute name="closePopup" description="This button closes the popup when clicked. Default is false" %>
<%@ attribute name="handler" required="true" description="Name of javascript function which handles this button." %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<f:message var="label" key="${ labelKey }" />
'${ label }': function() {
	${ handler }();
	<c:if test="${ closePopup }">$( this ).dialog( 'close' );</c:if>
},
