/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
define(['jquery', 'tree', 'workspace/workspace.import-popup'], function($, zetree){
	
	$.widget("squash.reqimportDialog", $.squash.importDialog, {
		
		createSummary : function(json){
			var panel = this.element.find('.import-summary');
			
			//basic infos			
			$(".total-import", panel).text(json.total);
			$(".success-import", panel).text(json.success);
			$(".rejected-import", panel).text(json.rejected);
			
			var failSpan = $(".failures-import", panel).text(json.failures);
			if (json.failures===0){ failSpan.removeClass("span-red"); }else{	failSpan.addClass("span-red"); }
			
			//notes
			if ((json.renamed===0) && (json.modified===0) && (json.milestoneFailures ===0) && (json.milestoneNotActivatedFailures === 0)){
				$(".import-excel-dialog-note", panel).hide();
			}else{
				$(".import-excel-dialog-note", panel).show();
				
				var renamedDialog = $(".import-excel-dialog-renamed", panel);
				if (json.renamed>0) { renamedDialog.show(); } else { renamedDialog.hide(); }
				
				var milestoneFailuresDialog = $(".import-excel-dialog-milestoneFailures", panel);
				if (json.milestoneFailures>0) { milestoneFailuresDialog.show(); } else { milestoneFailuresDialog.hide(); }
				
				var milestoneNotActivatedDialog = $(".import-excel-dialog-milestoneNotActivated", panel);
				if (json.milestoneNotActivatedFailures>0) { milestoneNotActivatedDialog.show(); } else { milestoneNotActivatedDialog.hide(); }
				
			}
		},
		
		bindEvents : function(){
			this._super();
			var self = this;
			
			this.onOwnBtn('ok', function(){
				var tree = zetree.get();
				var projectId = self.element.find('select[name="projectId"]').val();
				var lib = tree.jstree('findNodes', {rel : 'drive', resid : projectId});
				if (lib.size()>0){
					tree.jstree('refresh', lib);
				}
			});
			
			this.element.on('change', 'select[name="projectId"]', function(){
				var projectname = $(':selected', this).text(); 
				self.element.find('.confirm-project').text(projectname);
			});
			
			this.element.on('change', 'input[type="file"]', function(){
				var filename = /([^\\]+)$/.exec(this.value)[1]; 
				self.element.find('.confirm-file').text(filename);
			});
		}
	
	});
	
	function init(){
		
		var dialog = $("#import-excel-dialog").reqimportDialog({
			formats : ['xls', 'xlsx']
		});
		
		

	}
	
	return {
		init : init
	};

});