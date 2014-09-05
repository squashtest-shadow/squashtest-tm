<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ tag dynamic-attributes="paramsBindings" description="convert a map of param binding / selector for jq component into a map param binding / value of component" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
{<c:forEach var="paramBinding" varStatus="status" items="${ paramsBindings }">${ paramBinding.key }: $( '${ paramBinding.value }' ).val()<c:if test="${ not status.last }">,</c:if></c:forEach>}
