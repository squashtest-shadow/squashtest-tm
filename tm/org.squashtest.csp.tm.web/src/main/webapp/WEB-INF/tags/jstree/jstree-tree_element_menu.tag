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
<%@ tag description="Holds the html and javascript code necessary to display the tree element toolbar and bind it to events" %>
<%@ attribute name="newLeafButtonMessage" required="true" %>
<%@ attribute name="newResourceButtonMessage" required="false" %>
<%@ attribute name="workspace" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
	
<div id="tree_element_menu" class="tree-top-toolbar">
	<a id="tree-create-button" href="#tree-create-menu" class="button"><fmt:message key="squashtm.treemenu.create.label"/>...</a>
	<a id="tree-action-button" href="#tree-action-menu" class="button" ><fmt:message key="squashtm.treemenu.action.label"/>...</a> 

	<div class="not-displayed" >
	<div id="tree-create-menu" >
	<ul>
		<li><a class="new-folder-tree-button menu-disabled" href="#"><fmt:message key="tree.button.new-folder.label" />...</a></li>
		<li><a class="new-leaf-tree-button menu-disabled" href="#"><fmt:message key="${newLeafButtonMessage}" />...</a></li>
		<c:if test="${ workspace == 'campaign' }">
		<li><a class="new-resource-tree-button menu-disabled" href="#"><fmt:message key="${newResourceButtonMessage}" />...</a></li>
		</c:if>
	</ul>
	</div>
	</div>
	

	<div class="not-displayed" >
	<div id="tree-action-menu" >
	<ul>
		<li><a class="copy-node-tree-button menu-disabled" href="#"><fmt:message key="tree.button.copy-node.label" /></a></li>
		<li><a class="paste-node-tree-button menu-disabled" href="#"><fmt:message key="tree.button.paste-node.label" /></a></li>
		<li><a class="rename-node-tree-button menu-disabled" href="#"><fmt:message key="tree.button.rename-node.label" />...</a></li>
		<li><a class="delete-node-tree-button menu-disabled" href="#"><fmt:message key="tree.button.delete.label" />...</a></li>
	</ul>
	</div>
	</div>	
	
<c:if test="${workspace == 'test-case' }">
	<a id="tree-import-button" href="#tree-import-menu" class="button"><fmt:message key="squashtm.treemenu.import.label"/>...</a>
	
	<div class="not-displayed" >
	<div id="tree-import-menu" >
	<ul>
		<li><a class="import-excel-tree-button" href="#"><fmt:message key="tree.button.import-excel.label" /></a></li>
	</ul>
	</div>
	</div>	
	
</c:if>	
	
</div>


<script type="text/javascript">
	$(function(){
		squashtm.treemenu = {};
		
		var initButton = function(bSelector, cssIcon){
			$(bSelector).button({
				disabled : false,
				text : false,
				icons : {
					primary : cssIcon
				}
			});
		};

		initButton("#tree-create-button", "ui-icon ui-icon-plusthick");
		initButton("#tree-action-button", "ui-icon-arrowreturnthick-1-e");		
		
		var createOption = {
			"create-folder" : ".new-folder-tree-button",
			"create-file" : ".new-leaf-tree-button"
			<c:if test="${ not empty newResourceButtonMessage }">
			,"create-resource" : ".new-resource-tree-button"
			</c:if>
		};
		
		squashtm.treemenu.create = $('#tree-create-button').treeMenu("#tree-create-menu", createOption);
			
		var actionOption = {
				"copy" : ".copy-node-tree-button",
				"paste" : ".paste-node-tree-button",
				"rename" : ".rename-node-tree-button",
				"delete" : ".delete-node-tree-button"
			};
			
		squashtm.treemenu.action = $('#tree-action-button').treeMenu("#tree-action-menu", actionOption, 320);


		<c:if test="${workspace == 'test-case'}">
		initButton("#tree-import-button", "ui-icon-transferthick-e-w");		
		
		var importOption = {
			"import-excel" : ".import-excel-tree-button"
		};
		
		squashtm.treemenu.importer = $('#tree-import-button').treeMenu('#tree-import-menu', importOption);
		
		</c:if>
		
		
	});

</script>


