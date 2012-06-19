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
<%-- 
	a tree page layout specialize the layout by adding a tabbed pane on the left part of the page.
	
	Default is plain page, however it may be configured to use a sub-page-layout. You may then call the tag with isSubPaged=true.


	dev note : 
	
	the code is duplicated, one for the standard layout, and one for the sub page layout. they are exactly similar safe for
	the name of the tag file. The reason is that the servlet compiler complains if you branch to one or the other using jstl
	while keeping the content unique.

 --%>



<%@ tag description="Layout for a page with a left tree and a right contextual-content. Declare ajax-errors handling and renders nice buttons" body-content="empty" %>
<%@ attribute name="titleKey" required="true" %>
<%@ attribute name="highlightedWorkspace" required="true" %>

<%-- boiling plate here --%>

<%-- from common-import-outer-frame-layout --%>

<%@ attribute name="head" fragment="true" description="Additional html head fragment" %>
<%@ attribute name="titlePane" fragment="true" description="the title pane" %>
<%@ attribute name="foot" fragment="true" description="Pseudo html foot fragment where one can put inlined script and js libraries imports" %>
<%@ attribute name="footer" fragment="true" description="Optional page foot" %>
<%@ attribute name="linkable" %>
 
<%@ attribute name="tree" fragment="true" required="true" description="Tree definition" %>
<%@ attribute name="contextualContent" fragment="true" description="Optional contextual content" %>


<%-- from sub-page-layout --%>

<%@ attribute name="subPageTitle" fragment="true" description="the sub page has its own title. Define it there."%>
<%@ attribute name="subPageButtons" fragment="true" description="Unlike a regular workspace, a sub page exists to perform 
																	a one shot operation. Those operations are proposed in a dedicated action panel.
																	That action panel already propose a 'go back' button."	 %>



<%-- tree-page-layout speecifics --%>

<%@ attribute name="isSubPaged" required="false"  description="boolean. if set to true, the layout will be applied in a sub-paged form. Basically
it will insert sub-page-layout.tag between the top template and this one." %>
<%@ attribute name="isRequirementPaneSearchOn" required="false" description="boolean. If set to true, activate the test plan manager pane which allows to search test cases by requirement" %>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sq" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"  %>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>



<c:set var="tabbedPaneSCript" >
	<script type="text/javascript">
			
			<%--  
				In test plan management, there's the classic search interface and the possibility to find test plan by requirement.
				In this case, there's two div used to display the results. 
				Here we define which tab is currently used with selectedTab.
				selectedTab is used in show-iteration-test-plan-manager, search-panel*...
			--%>
			//The selected pane number. Always the first one (0) by default
			selectedTab = 0;
	
			$(function() {
				$( "#tabbed-pane" ).tabs();
				makePanelResizable();
				resizeRightPanel();
				refreshOnResize();
				/*  */
				$( "#tabbed-pane" ).bind( "tabsselect", function(event, ui) {
					  //change the number of the selected pane 
					 selectedTab =  ui.index;
					});
			});
			

			function makePanelResizable(){
				$("#tree-panel-left").resizable({
					minWidth: 270,
					helper: "ui-resizable-helper",
					alsoResize: "#contextual-content",
					start: function(){ 
						$("#contextual-content").css('visibility','hidden');
						$("#tree-panel-left").css('visibility','hidden');
					},
					stop: function(event, ui) {
						$("#tree-panel-left").css('height', '');
						$("#contextual-content").css('height', '');
						$("#tree-panel-left").resize();
						resizeRightPanel();
					}
				});

				$("#contextual-content").resizable();
			}
			
			
			function refreshOnResize() {
				$(parent.window).resize(function() {
					$("#tree-panel-left").css('height', '');
					$("#contextual-content").css('height', '');
					$("#tree-panel-left").resize();
					resizeRightPanel();
				});
			}
			
			
			function resizeRightPanel() {
				var body = document.getElementById("tree-panel-left").parentNode;
				var bodyWidth = body.offsetWidth;
				var marginLeft = 10;
				var leftPanelSize = $("#tree-panel-left").width();
				var rightPanelWidth = bodyWidth - (marginLeft + leftPanelSize);
				$("#contextual-content").width(rightPanelWidth);
				$("#contextual-content").css('visibility','visible');
				$("#tree-panel-left").css('visibility','visible');
			}
		
		</script>	

</c:set>


<c:choose>
<c:when test="${not empty isSubPaged and isSubPaged }">
<layout:sub-page-layout highlightedWorkspace="${ highlightedWorkspace }" titleKey="${ titleKey }" >
	<jsp:attribute name="head" >	
		<jsp:invoke fragment="head"/>
		
		<%-- tabed tree panel specific code --%>
		${tabbedPaneSCript}
	
		
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">	
		<jsp:invoke fragment="titlePane"/>
	</jsp:attribute>		
	
	<jsp:attribute name="foot">	
		<jsp:invoke fragment="foot"/>
	</jsp:attribute>
	
	<jsp:attribute name="footer">	
		<jsp:invoke fragment="footer"/>
	</jsp:attribute>		
	

	<jsp:attribute name="subPageTitle">
		<jsp:invoke fragment="subPageTitle" />
	</jsp:attribute>
		
	<jsp:attribute name="subPageButtons">
		<jsp:invoke fragment="subPageButtons" />	
	</jsp:attribute>
		
	<jsp:attribute name="content">
	<%-- now the actual specifics for the tree-page-layout itself --%>
	
			<div id="tree-panel-left">
				<div id="tabbed-pane">
				
					<ul>
						<li class="tab" > <a href="#tree-pane"><f:message key="tabbed_panel.tree.pane.label"/></a></li>
						<li class="tab"> <a href="#search-pane"><f:message key="tabbed_panel.search.pane.label"/></a></li>
						<c:if test="${ isRequirementPaneSearchOn eq 'true'}">
							<li class="tab"> <a href="#requirement-search-pane"><f:message key="tabbed_panel.requirement.pane.label"/></a></li>
						</c:if>						
					</ul>
					
					
					<div id="tree-pane" <c:if test="${ highlightedWorkspace == 'requirement'}"> class="requirement-tree-pane"</c:if> >
						<jsp:invoke fragment="tree" />
					</div>
					
					<div id="search-pane">
						<c:choose>
						<c:when test="${not empty linkable}">
						<layout:search-panel workspace="${ highlightedWorkspace }" linkable="${ linkable }" />
						</c:when>
						<c:otherwise>
						<layout:search-panel workspace="${ highlightedWorkspace }"/>
						</c:otherwise>
						</c:choose>
					</div>
					<c:if test="${ isRequirementPaneSearchOn eq 'true'}">
						<div id="requirement-search-pane">
							<layout:search-panel-by-requirement />
						</div>
					</c:if>
					

				</div>
			</div>
			
			
		
			<script type="text/javascript">
				$(function(){
					squashtm.contextualContent = $('#contextualContent').contextualContent();
				});		
			</script>
			
			<div id="contextual-content">
				<jsp:invoke fragment="contextualContent" />
			</div>
	</jsp:attribute>
</layout:sub-page-layout>
</c:when>

<c:otherwise>
<layout:common-import-outer-frame-layout highlightedWorkspace="${ highlightedWorkspace }" titleKey="${ titleKey }" >
	<jsp:attribute name="head" >	
		<jsp:invoke fragment="head"/>
		
		${tabbedPaneSCript}
		
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">	
		<jsp:invoke fragment="titlePane"/>
	</jsp:attribute>		
	
	<jsp:attribute name="foot">	
		<jsp:invoke fragment="foot"/>
	</jsp:attribute>
	
	<jsp:attribute name="footer">	
		<jsp:invoke fragment="footer"/>
	</jsp:attribute>		
		

	<jsp:attribute name="content">
	<%-- now the actual specifics for the tree-page-layout itself --%>
	
			<div id="tree-panel-left">
				<div id="tabbed-pane">
				
					<ul>
						<li class="tab" > <a href="#tree-pane"><f:message key="tabbed_panel.tree.pane.label"/></a></li>
						<li class="tab"> <a href="#search-pane"><f:message key="tabbed_panel.search.pane.label"/></a></li>
						<c:if test="${ isRequirementPaneSearchOn eq 'true'}">
							<li class="tab"> <a href="#requirement-search-pane"><f:message key="tabbed_panel.requirement.pane.label"/></a></li>
						</c:if>
					</ul>

					<div id="tree-pane" <c:if test="${ highlightedWorkspace == 'requirement'}"> class="requirement-tree-pane"</c:if> >
						<jsp:invoke fragment="tree" />
					</div>
					
					<div id="search-pane">
						<c:choose>
						<c:when test="${not empty linkable}">
						<layout:search-panel workspace="${ highlightedWorkspace }" linkable="${ linkable }" />
						</c:when>
						<c:otherwise>
						<layout:search-panel workspace="${ highlightedWorkspace }"/>
						</c:otherwise>
						</c:choose>
					</div>
					<c:if test="${ isRequirementPaneSearchOn eq 'true'}">
						<div id="requirement-search-pane">
							<layout:search-panel-by-requirement />
						</div>
					</c:if>
					<c:if test="${highlightedWorkspace == 'requirement'}">
					<a href="javascript:void(0)" id="export-link" class="export-requirement-link"><f:message key='tree.button.export.label'/>...</a>
					</c:if>					
				</div>
			</div>
			
			

			<div id="contextual-content">
				<script type="text/javascript">
					$(function(){
						squashtm.contextualContent = $('#contextual-content').contextualContent();
					});		
				</script>
				<jsp:invoke fragment="contextualContent" />
			</div>
	</jsp:attribute>
</layout:common-import-outer-frame-layout>
</c:otherwise>
</c:choose>