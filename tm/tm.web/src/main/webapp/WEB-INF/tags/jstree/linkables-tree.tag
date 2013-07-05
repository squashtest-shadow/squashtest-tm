<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2013 Henix, henix.fr

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
<%@ attribute name="id" required="true" description="id of the tree component" %>
<%@ attribute name="rootModel" required="false" type="java.lang.Object" description="JSON serializable model of root of tree. 
	If not set, the tree won't be initialized until initLinkableTree(json) is explicitely called with a valid json argument" %>
<%@ attribute name="workspaceType" required="false" description="if set, will override the default icons"%>


<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>
<%@ taglib prefix="su" uri="http://org.squashtest.tm/taglib/string-utils" %>


<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>


<tree:_html-tree treeId="${ id }">
</tree:_html-tree> 
<script type="text/javascript">
	
	<c:if test="${not empty rootModel}">
	$(function(){
		var jsondata = ${ json:serialize(rootModel) };
		if (jsondata!=null){
			initLinkableTree(jsondata);
		}
	});
	</c:if>
	
	function initLinkableTree(jsonData) {
		$("#${ id }").linkableTree({ 
			contextPath: "${ pageContext.servletContext.contextPath }",
			jsonData: jsonData, 
			workspaceType: "${ workspaceType }"
		});
	};
</script>
