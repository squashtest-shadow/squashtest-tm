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
<%@ attribute name="newLeafButtonMessage" required="true" %>
<%@ attribute name="newResourceButtonMessage" required="false" %>
<%@ attribute name="workspace" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
	
<div id="tree_element_menu" class="tree-top-toolbar">
<div class="button-group">
	<a id="tree-create-button" href="#tree-create-menu" class="button"><f:message key="label.create"/>...</a>
	<div class="not-displayed" >
	<div id="tree-create-menu" >
	<ul>
		<li><a class="new-folder-tree-button menu-disabled" href="JavaScript:void(0);"><f:message key="tree.button.new-folder.label" />...</a></li>
		<li><a class="new-leaf-tree-button menu-disabled" href="JavaScript:void(0);"><f:message key="${newLeafButtonMessage}" />...</a></li>
		<c:if test="${ workspace == 'campaign' }">
		<li><a class="new-resource-tree-button menu-disabled" href="JavaScript:void(0);"><f:message key="${newResourceButtonMessage}" />...</a></li>
		</c:if>
	</ul>
	</div>
	</div>
</div>
<%--
<div class="button-group">
	<a id="copy-node-tree-button" href="JavaScript:void(0);"><f:message key='tree.button.copy-node.label' /></a>
	<a id="paste-node-tree-button" href="JavaScript:void(0);"><f:message key="tree.button.paste-node.label" /></a>
</div>
<div class="button-group">
	<a id="rename-node-tree-button" href="JavaScript:void(0);"><f:message key="tree.button.rename-node.label" />...</a>	
	<c:if test="${workspace == 'test-case' || workspace == 'requirement' }">
		<a id="tree-import-button" href="#tree-import-menu" class="button"><f:message key="squashtm.treemenu.import.label"/>...</a>
		<div class="not-displayed" >
		<div id="tree-import-menu" >
		<ul>
			<li><a class="import-excel-tree-button menu-disabled" href="JavaScript:void(0);"><f:message key="label.Import" />...</a></li>
			<li><a class="import-links-excel-tree-button menu-disabled" href="JavaScript:void(0);"><f:message key="tree.button.import.links.label" />...</a></li>
			<li><a class="export-tree-button menu-disabled" href="JavaScript:void(0);"><f:message key='label.Export'/>...</a></li>
		</ul>
		</div>
		</div>	
	</c:if>	
</div>

<c:if test="${ not empty wizards }">
<div id="wizard-tree-pane" class="button-group">
	<a id="wizard-tree-button" class="not-displayed" href="JavaScript:void(0);" data-icon="ui-icon-star" data-text="false"><f:message key="label.wizards" />...</a>
	<script id="ws-wizard-tree-menu-template" type="text/x-handlebars-template">
		<div id="ws-wizard-tree-menu"> 
			<ul>
				{{#each wizards}}
				<li><a id="{{this.name}}" class="menu-disabled" href="javascript:void(0)" title="{{this.tooltip}}">{{this.label}}...</a></li>
				{{/each}}
			</ul>
		</div>
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
<div class="button-group">
	<a id="delete-node-tree-button" href="JavaScript:void(0);"><f:message key="tree.button.delete.label" />...</a>
</div>
 --%>
</div>

<script type="text/javascript">
$(function () {
    squashtm.treemenu = {};
    
    require(['jquery', 'treemenu'], function($){
        var initButton = function (bSelector, cssIcon, disabledParam) {
            var opts = {
                disabled: disabledParam,
                text: false,
            };

            if (cssIcon) {
                opts.icons = {
                    primary: cssIcon
                }
            }

            $(bSelector).squashButton(opts);
        };

        initButton("#tree-create-button", "ui-icon ui-icon-plusthick", false);
        
        <%--
        
        initButton("#tree-action-button", "ui-icon-arrowreturnthick-1-e", false);
        initButton("#copy-node-tree-button", "ui-icon-copy", true);
        initButton("#paste-node-tree-button", "ui-icon-clipboard", true);
        initButton("#rename-node-tree-button", "ui-icon-pencil", true);
        initButton("#delete-node-tree-button", "ui-icon-trash", true);

    	--%>

        var createOption = {
        	"create-folder": ".new-folder-tree-button",
        	"create-file": ".new-leaf-tree-button" 
        	<c:if test = "${ not empty newResourceButtonMessage }" >,
            "create-resource": ".new-resource-tree-button" 
            </c:if>
    	};
    	
        var createOption = {
        	html : $("#tree-create-button").html(),
        	treeselector : "#tree",
        	buttons : {
        		'.new-folder-tree-button' : function(nodes){ console.log('updating status for new-folder');},
        		'.new-leaf-tree-button' : function(nodes) { console.log('updating status new-leaf');}
        	}
        }
        
    	squashtm.treemenu.create = $('#tree-create-button').treeMenu(createOption);
    		
        
        <%--
    	var treeButtons = {
    			"copy" : $('#copy-node-tree-button'),
    			"paste" : $('#paste-node-tree-button'),
    			"rename" : $('#rename-node-tree-button'),
    			"delete" : $('#delete-node-tree-button')
    		};
    		
    	squashtm.treeButtons = treeButtons;

    	<c:if test="${workspace == 'test-case' || workspace == 'requirement'}">
    	initButton("#tree-import-button", "ui-icon-transferthick-e-w");		
    	
    	var importOption = {
    		"import-excel" : ".import-excel-tree-button",
    		"import-links-excel" : ".import-links-excel-tree-button",
    		"export" : ".export-tree-button"
    	};
    	
    	squashtm.treemenu.importer = $('#tree-import-button').treeMenu('#tree-import-menu', importOption);
    	</c:if>
    	--%>  	
    });

});
</script>


