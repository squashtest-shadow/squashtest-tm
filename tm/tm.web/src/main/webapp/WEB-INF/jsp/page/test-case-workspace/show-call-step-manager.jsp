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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<%--
	parameters :
	
		- testCase : the test case at the calling-side of the call step the user will design in this interface.
		- rootModel : the initial nodes loaded in the tree

 --%>


<c:set var="resourceName" value="test-case" />

<s:url var="postCallStepUrl" value="/test-cases/{id}/call">
	<s:param name="id" value="${testCase.id}"/>
</s:url>




<f:message var="noSelectionError" key="subpage.test-case.callstep.error.noselection.label" />
<layout:tree-page-layout titleKey="squashtm" highlightedWorkspace="${resourceName}" isSubPaged="true">

	<jsp:attribute name="head">
		<comp:sq-css name="squash.green.css" />
		<script type="text/javascript">
		require([ "common" ], function() {
			require([ "domReady", "jquery" ], function(domReady, $) { 
				
			function navigateBackFromCallStepManager(){
				var url = $.cookie('call-step-manager-referer');
				document.location.href=url;
			}
		
		
			domReady(function(){
				$("#call-step-associate-button").click(associationAction);
				$("#call-step-back-button").click(navigateBackFromCallStepManager);
			});
			

			function associationAction(){
				var calledId = getSelectedId();
				
				if (calledId.length != 1){
					$.squash.openMessage("<f:message key='popup.title.error' />", "${noSelectionError}");
					return;
				}
				
				var param = {'called-test-case' : calledId[0]};
				
				<%-- 
					we need that post to be a json just to trigger business exception handlers server side (if an exception occurs).
					also we'll receive the said exception as a json object, which is always cool.
				--%>
				$.post("${postCallStepUrl}", param, function(){},"json").success(navigateBackFromCallStepManager);
	
				
			}		
			
			function getSelectedId(){
				
				var tab =  [];
				var selected = $( "#tabbed-pane" ).tabs('option', 'selected');
				var tree = $("#tree-pane .tree");
				if (selected == 0){
					tree.jstree('get_selected').each(function(index, node){
						if ($( node ).attr('rel') == 'test-case') {
							tab.push($( node ).attr('resId'));
						}
					});
				}
				
				if (selected == 1){
					tab = findIdsOfSelectedSearchRow();
				}
				return tab;
				
			}

			
			});
		});
			
		</script>
		
	</jsp:attribute>

	<jsp:attribute name="titlePane">
		<h2><f:message key="workspace.test-case.title" /></h2>	
	</jsp:attribute>
	
	<jsp:attribute name="subPageTitle">
		<h2>${testCase.name}&nbsp;:&nbsp;<f:message key="subpage.test-case.callstep.title" /></h2>	
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="associateButtonLabel" key="subpage.test-case.callstep.button.call.label" />
		<input id="call-step-associate-button" type="button" class="button" value="${associateButtonLabel}" />
		<f:message var="backButtonLabel" key="label.Back" />
		<input id="call-step-back-button" type="button" class="button" value="${backButtonLabel}" onclick="navigateBackFromCallStepManager();"/> 
	</jsp:attribute>	

	<jsp:attribute name="tree">
		<tree:call-step-tree rootModel="${rootModel}"/>	
	</jsp:attribute>

	<jsp:attribute name="contextualContent">
		<%-- empty --%>
	</jsp:attribute>

	<jsp:attribute name="footer">

	</jsp:attribute>
</layout:tree-page-layout>