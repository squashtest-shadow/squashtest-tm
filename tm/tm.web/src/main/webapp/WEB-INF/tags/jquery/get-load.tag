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
<%@ tag description="loads a page fragment through a GET request" %>
<%@ attribute name="url" description="url used to get html fragment. Either use this or the 'urlExpression' attribute" %>
<%@ attribute name="urlExpression" description="javascript expression to build the url. Either use this or the 'url' attribute" %>
<%@ attribute name="targetSelector" required="true" description="jquery selector expression which selects the area to load" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${ not empty url }">
	<c:set var="urlExpression" value="'${ url }'" />
</c:if>
$.get(${ urlExpression }, function(data) {
	  $('${ targetSelector }').html(data);
}, "html");				
