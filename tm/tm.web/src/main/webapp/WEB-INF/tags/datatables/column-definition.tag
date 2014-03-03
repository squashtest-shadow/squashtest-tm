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
<%@ tag body-content="empty" %>
<%@ attribute name="targets" required="true" description="comma separated list of 0-index columns targeted by this definition" %>
<%@ attribute name="visible" type="java.lang.Boolean" %>
<%@ attribute name="sortable" type="java.lang.Boolean" description="the column is sortable. defaults to false" %>
<%@ attribute name="width" type="java.lang.String" %>
<%@ attribute name="datatype" type="java.lang.String" %>
<%@ attribute name="cssClass" type="java.lang.String" %>
<%@ attribute name="dataProp" type="java.lang.String" %>
<%@ attribute name="lastDef" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${ not empty visible }"><c:set var="visibleToken" value="'bVisible': ${ visible }, " /></c:if>
<c:set var="sortableToken" value="'bSortable': ${ not empty sortable and sortable }, " />
<c:if test="${ not empty width }"><c:set var="widthToken" value="'sWidth': '${ width }', " /></c:if>
<c:if test="${ not empty datatype }"><c:set var="dataTypeToken" value="'sSortDataType': '${ datatype }', " /></c:if>
<c:if test="${ not empty cssClass }"><c:set var="classToken" value="'sClass': '${ cssClass }', " /></c:if>
<c:if test="${ not empty dataProp }"><c:set var="dataPropToken" value="'mDataProp': '${ dataProp }', " /></c:if>
{${ visibleToken }${ sortableToken }${ widthToken }${dataTypeToken}${ classToken }${ dataPropToken }'aTargets': [${targets}]}<c:if test="${ empty lastDef or not lastDef }">,</c:if>
