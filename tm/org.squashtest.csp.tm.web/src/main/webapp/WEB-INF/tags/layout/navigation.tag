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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="lay" tagdir="/WEB-INF/tags/layout" %>
<%@ attribute name="highlighted" %>
<%-- fix for the different library id names--%>

<c:if test="${not empty library }">
		<c:set var="libraryId" value="${library.id}" />
</c:if>

<script type="text/javascript">
	<c:if test="${ not empty highlighted }">
	$(function() {
		navLinkHighlight('${ highlighted }-link');
	});
	</c:if>

	var squashtest_nav_button_active;

	/* init. Todo : find a better name */
	function navLinkHighlight(link_name){
		squashtest_nav_button_active=link_name;
		navLinkOn(link_name);
	}
	
	
	function navLinkOn(link_name){
		var thisLink=$('#'+link_name);
		var imgName=$(thisLink).find('img').attr("src");
		
		imgName=imgName.replace("_off", "_on");
		
		$(thisLink).find('img').attr("src",imgName);		
	}
	
	
	function navLinkOff(link_name){
		var thisLink=$('#'+link_name);
		var imgName=$(thisLink).find('img').attr("src");
		
		imgName=imgName.replace("_on", "_off");
		
		$(thisLink).find('img').attr("src",imgName);			
	}
	
	
	$(function(){
		$(".nav_btn").hover(
			function(){
				var link_name=$(this).attr("id");
				
				if (link_name!=squashtest_nav_button_active)					
					navLinkOn(link_name);
			},
			function(){
				var link_name=$(this).attr("id");
				if (link_name!=squashtest_nav_button_active)	
					navLinkOff(link_name);				
				
			}
		
		);
	});
	

</script>

<div id="navigation">
	<div id="test_mgt_nav">
		<lay:_workspace-button imageName="Button_Nav_Home_off.png" resourceName="home" />
		<lay:_workspace-button imageName="Button_Nav_Requirement_off.png" resourceName="requirement" />
		<lay:_workspace-button imageName="Button_Nav_TestCase_off.png" resourceName="test-case" />
		<lay:_workspace-button imageName="Button_Nav_Campaign_off.png" resourceName="campaign" />
		<lay:_workspace-button imageName="Button_Nav_Reporting_off.png" resourceName="report" />

	</div>
	
	<div id="nav_logo">
		<img src="${ pageContext.servletContext.contextPath }/images/logo_squash30.png" alt="logo_squash" style="width:30px;"/>
	</div>
</div>
