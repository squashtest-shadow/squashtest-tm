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
<%@ tag language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="httpMethod" 		description="http method used, default is ajax default" %>
<%@ attribute name="useData"		 	description="if set, the data field will be filled with the body of the tag."%>
<%@ attribute name="successHandler"		description="if set, will call the said script as a success handler"%>
<%@ attribute name="errorHandler"		description="if set, will call the said script as a failure handler"%>
<%@ attribute name="dataType"			description="if set, will specify the datatype" %>
<%@ attribute name="url" required="true" rtexprvalue="true"	 description="the type of this field is a javascript string variable"%>
$.ajax({
		<c:if test="${not empty httpMethod}">
		type : '${httpMethod}',
		</c:if>		
		<c:if test="${not empty useData}">
		data : <jsp:doBody/>,
		</c:if>
		<c:if test="${not empty successHandler}">
		success : function(data, textStatus, XMLHttpRequest){${successHandler}(data, textStatus, XMLHttpRequest);},
		</c:if>		
		<c:if test="${not empty errorHandler}">
		error : function(XMLHttpRequest, textStatus, errorThrown){${errorHandler}(XMLHttpRequest, textStatus, errorThrown);},
		</c:if>
		<c:if test="${not empty dataType}">
		dataType : "${dataType}",
		</c:if>		
		url : ${url}			//	do not surround with "". Read the description
});	