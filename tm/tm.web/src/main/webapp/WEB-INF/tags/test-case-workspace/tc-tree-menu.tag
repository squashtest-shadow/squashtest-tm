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
		<a id="tree-create-button" href="JavaScript:void(0)" 	class="buttonmenu"><f:message key="label.create"/>...</a>
		<ul id="tree-create-menu" class="not-displayed">
			<li id="new-folder-tree-button" 	class="ui-state-disabled"><a href="JavaScript:void(0);"><f:message key="tree.button.new-folder.label" />...</a></li>
			<li id="new-test-case-tree-button"  class="ui-state-disabled"><a href="JavaScript:void(0);"><f:message key="tree.button.new-test-case.label" />...</a></li>
		</ul>
	</div>
	<div class="button-group">
		<a id="copy-node-tree-button"  href="JavaScript:void(0);"><f:message key='tree.button.copy-node.label' /></a>
		<a id="paste-node-tree-button" href="JavaScript:void(0);" ><f:message key="tree.button.paste-node.label" /></a>
	</div>	
	<div class="button-group">
		<a  id="rename-node-tree-button" href="JavaScript:void(0);"  ><f:message key="tree.button.rename-node.label" />...</a>			
		<a  id="tree-import-button" 	 href="JavaScript:void(0);"  class="buttonmenu"><f:message key="squashtm.treemenu.import.label"/>...</a>
		<ul id="tree-import-menu" class="not-displayed">
			<li id="import-excel-tree-button" class="ui-state-disabled"><a href="JavaScript:void(0);"><f:message key="label.Import" />...</a></li>
			<li id="import-links-excel-tree-button" class="ui-state-disabled"><a href="JavaScript:void(0);"><f:message key="tree.button.import.links.label" />...</a></li>
			<li id="export-tree-button" class="ui-state-disabled"><a  href="JavaScript:void(0);"><f:message key='label.Export'/>...</a></li>
		</ul>		
	</div>	
	<div class="button-group">
		<a id="delete-node-tree-button" href="JavaScript:void(0);"><f:message key="tree.button.delete.label" />...</a>
	</div>
</div>
 
 
<script type="text/javascript">
$(function () {
    squashtm.treemenu = {};
    
    require(['tc-workspace'], function($, main){
       
    	main.init();
        
        <%--
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
        

        
        initButton("#tree-action-button", "ui-icon-arrowreturnthick-1-e", false);
        initButton("#copy-node-tree-button", "ui-icon-copy", true);
        initButton("#paste-node-tree-button", "ui-icon-clipboard", true);
        initButton("#rename-node-tree-button", "ui-icon-pencil", true);
        initButton("#delete-node-tree-button", "ui-icon-trash", true);

        var createOption = {
        	"create-folder": ".new-folder-tree-button",
        	"create-file": ".new-leaf-tree-button" 
        	<c:if test = "${ not empty newResourceButtonMessage }" >,
            "create-resource": ".new-resource-tree-button" 
            </c:if>
    	};
    	
        var createOption = {
        	html : $("#tree-create-menu").html(),
        	treeselector : "#tree",
        	buttons : {
        		'.new-folder-tree-button' : function(nodes){ console.log('updating status for new-folder');},
        		'.new-leaf-tree-button' : function(nodes) { console.log('updating status new-leaf');}
        	}
        }
        
    	squashtm.treemenu.create = $('#tree-create-button').treeMenu(createOption);
    		

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


