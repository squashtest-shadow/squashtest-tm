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
<%@ tag description="Holds the html and javascript code necessary to display the tree element toolbar and bind it to events" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
	
<div id="tree_element_menu" class="tree-top-toolbar unstyled-pane">
	<div class="button-group">
		<a id="tree-create-button"  	class="buttonmenu"><f:message key="label.create"/>...</a>
		<ul id="tree-create-menu" class="not-displayed">
			<li id="new-folder-tree-button" 	  class="ui-state-disabled cursor-pointer"><a ><f:message key="tree.button.new-folder.label" />...</a></li>
			<li id="new-requirement-tree-button"  class="ui-state-disabled cursor-pointer"><a ><f:message key="tree.button.new-requirement.label" />...</a></li>
		</ul>
	</div>
	
	<div class="button-group">
		<a id="copy-node-tree-button"   ><f:message key='tree.button.copy-node.label' /></a>
		<a id="paste-node-tree-button"  ><f:message key="tree.button.paste-node.label" /></a>
	</div>	
	
	<div class="button-group">
		<a  id="rename-node-tree-button"   ><f:message key="tree.button.rename-node.label" />...</a>			
		<a  id="tree-import-button" 	   class="buttonmenu"><f:message key="squashtm.treemenu.import.label"/>...</a>
		<ul id="tree-import-menu" class="not-displayed">
			<li id="import-excel-tree-button" class="ui-state-disabled cursor-pointer"><a ><f:message key="label.Import" />...</a></li>
			<li id="import-links-excel-tree-button" class="ui-state-disabled cursor-pointer"><a ><f:message key="tree.button.import.links.label" />...</a></li>
			<li id="export-tree-button" class="ui-state-disabled cursor-pointer"><a  ><f:message key='label.Export'/>...</a></li>
		</ul>		
	</div>

	<div class="button-group">
		<a id="search-tree-button" ><f:message key="tree.button.search.label"/>...</a>
	</div>
	
	<c:if test="${ not empty wizards }">
	<div id="wizard-tree-pane" class="button-group">
		<a id="wizard-tree-button" class="buttonmenu"  data-icon="ui-icon-star" data-text="false"><f:message key="label.wizards" />...</a>
		<script id="ws-wizard-tree-menu-template" type="text/x-handlebars-template">		
		<ul id="ws-wizard-tree-menu">
			{{#each wizards}}
			<li id="{{this.name}}" class="ui-state-disabled cursor-pointer"><a  title="{{this.tooltip}}">{{this.label}}...</a></li>
			{{/each}}
		</ul>	
	</script>
	<script id="start-ws-wizard-form-template" type="text/x-handlebars-template">
		<form id="start-ws-wizard-form" action="{{url}}" method="post"> 
			{{#each nodes}}
			<input type="hidden" name="{{this.type}}" value="{{this.id}}" />
			{{/each}}
		</form>
	</script>
		<div id="start-ws-wizard-container" class="not-displayed">
		</div>
	</div>
	</c:if>	
	
	<div class="button-group snap-right">
		<a id="delete-node-tree-button" ><f:message key="tree.button.delete.label" />...</a>
	</div>
</div>
