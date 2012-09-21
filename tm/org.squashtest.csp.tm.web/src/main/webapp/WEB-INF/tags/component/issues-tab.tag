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
<%@ tag description="javascript to load a new issue tab with ajax" %>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ attribute name="btEntityUrl" required="true"
	description="the url to get the tab"%>
<%--
	this section is loaded asynchronously, and in this case as a tab. The bugtracker might be out of reach indeed. Nothing will be loaded if no bugtracker was defined.
 --%>	
 <f:message key="tabs.label.issues" var="tabIssueLabel"/>
 <div id="bugtracker-section-div">
 <script type="text/javascript">
 	$(function(){
 				<%-- first : add the tab entry --%>
	 			$("div.fragment-tabs").tabs( "add" , "#bugtracker-section-div" , "${tabIssueLabel}");
	 			<%-- second : load the bugtracker section --%>
	 			var btDiv = $("#bugtracker-section-div");
	 			btDiv.load("${btEntityUrl}?style=fragment-tab", function(){btDiv.addClass("table-tab");}); 			
 	});
 </script>
</div>
