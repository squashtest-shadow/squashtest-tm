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
<%@ attribute name="rootModel" required="true" type="java.lang.Object" description="JSON serializable model of root of tree." %>

<%@ attribute name="workspaceType" required="false" description="if set, will override the default icons"%>


<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>


<tree:_html-tree treeId="${ id }" />
<script type="text/javascript">

	var conf = {
		model : ${ json:serialize(rootModel) },
		workspace : "${workspaceType}",
		treeselector : "#${id}"
	}
	
	$(function(){
		require( ["tree"], function(initTree){
			initTree.initLinkableTree(conf);				
		});				
	});
	
</script>
