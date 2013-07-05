/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */


define(['jquery', './utils', './permissions-rules'
        'jquery.squash.buttonmenu', 
        'jquery.squash.squashbutton',], function($, utils, permissions){
	

	function createWidgets(){
		
		var createconf = utils.btnconf("ui-icon ui-icon-plusthick");
		var copyconf = utils.btnconf("ui-icon-copy");
		var pasteconf = utils.btnconf("ui-icon-clipboard");
		var renameconf = utils.btnconf("ui-icon-pencil");
		var importconf = utils.btnconf("ui-icon-transferthick-e-w");
		var deleteconf = utils.btnconf("ui-icon-trash");
		
		
		$("#tree-create-button").buttonmenu({
			button : createconf
		});	
		
		$("#copy-node-tree-button").squashButton(copyconf);
		
		$("#paste-node-tree-button").squashButton(pasteconf);
		
		$("#rename-node-tree-button").squashButton(renameconf);
		
		$("#tree-import-button").buttonmenu({
			button : importconf
		});		
		
		$("#delete-node-tree-button").squashButton(renameconf);
				
	}
	
	
	function bindTreeEvents(){
		
		var btnselector =   "#new-folder-tree-button, #new-test-case-tree-button, #copy-node-tree-button, #paste-node-tree-button, "+
							"#rename-node-tree-button, #import-excel-tree-button, #import-links-excel-tree-button, #export-tree-button";
		
		var buttons = [];
		$(btnselector).each(function(){
			buttons.push($(this));
		})

		var tree = $("#tree");	
		
		function loopupdate(){
			
			var nodes = tree.jstree('get_selected');
			var i=0,len = buttons.length;
			
			for (i=0;i<len;i++){
				var btn = buttons[i];
				var id = btn.attr('id');
				var rule = permissions.buttonrules[id];
				if (rule(nodes)){
					btn.removeClass('ui-state-disabled');
				}else{
					btn.addClass('ui-state-disabled');
				}				
			}
			
		}
		
		tree.on('select_node.jstree', loopupdate);
		tree.on('deselect_node.jstree', loopupdate);

	}
	
	
	function init(){
		createWidgets();
		bindEvents();			
	}
	
	
	
	return {
		
		init : init
		
	}
	
});