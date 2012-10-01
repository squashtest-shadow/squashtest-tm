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
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>


<fmt:message var="bTitle" key="workspace.bugtracker.button.label" />
<a id="bugtracker-link" class="nav_btn" href="#bugtrackers"><img
	src="${ pageContext.servletContext.contextPath }/images/Button_Nav_Bugtracker_off.png"
	alt="${ bTitle }" title="${ bTitle }" border="0" /> </a>
	
	
<div id="bugtrackers" style="display: none">
	<ul>
		<c:forEach var="bugtracker" items="${bugtrackers}">
			<li>
				<c:choose>
					<c:when test="${ bugtracker.iframeFriendly }">
					<s:url var="workspaceUrl" value="/bugtracker/{bugtrackerId}/workspace">
						<s:param name="bugtrackerId" value="${bugtracker.id}" />
					</s:url>
						<a id="bugtracker-${bugtracker.id }" href="${ workspaceUrl }" >${bugtracker.name}</a>
					</c:when>
					<c:otherwise>
						<a id="bugtracker-${bugtracker.id }" href="javascript:open${bugtracker.id }()"  >${bugtracker.name}</a>
						<script>function open${bugtracker.id}(){
							window.open("${ bugtracker.URL }", "_newtab");
						}</script>
					</c:otherwise>
				</c:choose>
			</li>
		</c:forEach>
	</ul>
</div>
<script> 
			$(function() {
				$("#bugtracker-link").fgmenu({
					content : $('#bugtracker-link').next().html(),
					showSpeed : 0,
					width : 130
				});

			});
		</script>