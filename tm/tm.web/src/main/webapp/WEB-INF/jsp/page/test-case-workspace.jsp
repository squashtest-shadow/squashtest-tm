<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2015 Henix, henix.fr

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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>
<%@ taglib prefix="wkp" tagdir="/WEB-INF/tags/test-case-workspace" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<layout:tree-page-layout titleKey="squashtm" highlightedWorkspace="test-case">
	
	<jsp:attribute name="head">
		<comp:sq-css name="squash.green.css" />
		
		<script type="text/javascript">
			var squashtm = squashtm || {};
			squashtm.app = squashtm.app || {};
			squashtm.app.testCaseWorkspace = {
				wizards: ${ json:marshall(wizards) }<%-- that was a JSP expression --%>
			}
		</script>	
				
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2><f:message key="workspace.test-case.title" /></h2>	
	</jsp:attribute>	
	
	<jsp:attribute name="tree">
		<wkp:tc-tree-menu/>
		<tree:_html-tree treeId="tree" />
	</jsp:attribute>
	
	<jsp:attribute name="contextualContent">
		<%-- empty --%>
	</jsp:attribute>	
	
	<jsp:attribute name="footer">
		
		<wkp:tc-tree-popups importableLibraries="${editableLibraries}"/>
		
		<script type="text/javascript">
		require( ["common"], function(){
			require(["jquery", "tc-workspace",'jquery.cookie'], function($, initWkp) {
				var conf = {
					tree : {
						model : ${ json:serialize(rootModel) },
						workspace : "test-case",
						treeselector : "#tree",
						selectedNode : "${selectedNode}"
					}
				};
							
				$(function(){
					$.cookie("workspace-prefs", null, {path:'/'});
					initWkp.init(conf);		
				});
			});						
		});
		</script>
	
	</jsp:attribute>
	
</layout:tree-page-layout>