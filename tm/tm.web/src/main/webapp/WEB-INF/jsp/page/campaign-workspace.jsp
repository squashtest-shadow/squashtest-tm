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
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="treepopup" tagdir="/WEB-INF/tags/treepopups" %>
<%@ taglib prefix="json"  uri="http://org.squashtest.tm/taglib/json" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<layout:workspace-page-layout resourceName="campaign">
	<jsp:attribute name="head">
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.purple.css" />
		<script type="text/javascript">
			var squashtm = squashtm || {};
			squashtm.app = squashtm.app || {};
			squashtm.app.campaignWorkspace = {
				wizards: ${ json:marshall(wizards) }<%-- that was a JSP expression --%>
			}
			
			require( ["common"], function(){
				require(["campaign-workspace"], function() {
				});
			});
		</script>		
	</jsp:attribute>
	
	<jsp:attribute name="footer">
		<treepopup:add-nameable-node-dialog resourceName="iteration" treeNodeButton="squashtm.treemenu.create.buttons['create-resource']" />
	</jsp:attribute>
</layout:workspace-page-layout>