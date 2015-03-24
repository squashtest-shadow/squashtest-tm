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
define(['jquery', 'workspace.event-bus', 'workspace/workspace.import-popup'], function($, eventBus){
	
	$.widget("squash.linksimportDialog", $.squash.importDialog, {
		
		createSummary : function(json){
			var panel = this.element.find('.import-summary');
			
			//basic infos			
			$(".success-import", panel).text(json.success);
			
			var failSpan = $(".failures-import", panel).text(json.failures);
			if (json.failures===0){ failSpan.removeClass("span-red"); }else{	failSpan.addClass("span-red"); }
			
			// display the errors if any
			if (! json.criticalErrors && json.failures===0){
				$(".import-links-excel-dialog-note", panel).hide();
			}
			else if (json.criticalErrors){
				this.showImportCriticalErrors(panel, json);
			}
			else{
				this.showImportNormalErrors(panel, json);			
			}			
			
		},	
	
		showImportNormalErrors : function(panel, response){
			$(".import-links-excel-dialog-note", panel).show();
			$(".import-links-excel-dialog-critical-errors", panel).hide();
			$(".import-links-excel-dialog-normal-errors", panel).show();
				
			var obsoleteDialog = $(".import-links-excel-dialog-obsolete", panel);
			if ($.trim(response.obsolete) !== "") { 
				$(".obsolete-import", panel).text(response.obsolete);
				obsoleteDialog.show(); 
				} else { obsoleteDialog.hide(); }
			
			var reqAccessDeniedDialog = $(".import-links-excel-dialog-req-access-denied", panel);
			if ($.trim(response.requirementAccessRejected) !== "") { 
				$(".req-access-denied-import", panel).text(response.requirementAccessRejected);
				reqAccessDeniedDialog.show(); 
				} else { reqAccessDeniedDialog.hide(); }
			
			var requirementNotFoundDialog = $(".import-links-excel-dialog-req-not-found", panel);
			if ($.trim(response.requirementNotFound) !== "") { 
				$(".req-not-found-import", panel).text(response.requirementNotFound);
				requirementNotFoundDialog.show(); 
				} else { requirementNotFoundDialog.hide(); }
			
			var testCaseAccessRejectedDialog = $(".import-links-excel-dialog-tc-access-denied", panel);
			if ($.trim(response.testCaseAccessRejected) !== "") { 
				$(".tc-access-denied-import", panel).text(response.testCaseAccessRejected);
				testCaseAccessRejectedDialog.show(); 
				} else { testCaseAccessRejectedDialog.hide(); }
			
			var testCaseNotFoundDialog = $(".import-links-excel-dialog-tc-not-found", panel);
			if ($.trim(response.testCaseNotFound) !== "") { 
				$(".tc-not-found-import", panel).text(response.testCaseNotFound);
				testCaseNotFoundDialog.show(); 
				} else { testCaseNotFoundDialog.hide(); }
			
			var versionNotFoundDialog = $(".import-links-excel-dialog-version-not-found", panel);
			if ($.trim(response.versionNotFound) !== "") { 
				$(".version-not-found-import", panel).text(response.versionNotFound);
				versionNotFoundDialog.show(); 
				} else { versionNotFoundDialog.hide(); }
			
			var linkAlreadyExistDialog = $(".import-links-excel-dialog-link-already-exist", panel);
			if ($.trim(response.linkAlreadyExist) !== "") { 
				$(".link-already-exist-import", panel).text(response.linkAlreadyExist);
				linkAlreadyExistDialog.show(); 
				} else { linkAlreadyExistDialog.hide(); }
			
		},
		
		showImportCriticalErrors : function (panel, response){
			$(".import-links-excel-dialog-note", panel).show();
			$(".import-links-excel-dialog-critical-errors", panel).show();
			$(".import-links-excel-dialog-normal-errors", panel).hide();
			
			var missingHeadersDialog = $(".import-links-excel-dialog-missing-headers", panel);
			if ($.trim(response.missingColumnHeaders) !== "") { 
				$(".file-missing-headers", panel).text(response.missingColumnHeaders);
				missingHeadersDialog.show(); 
			} 
			else { 
				missingHeadersDialog.hide(); 
			}		
		},
		
		bindEvents : function(){
			this._super();
			var self = this;

			this.element.on('change', 'input[type="file"]', function(){
				var filename = /([^\\]+)$/.exec(this.value)[1]; 
				self.element.find('.confirm-file').text(filename);
			});
		}
		
	});
	
	function init(){
		var dialog = $("#import-links-excel-dialog").linksimportDialog({
			formats : ['xls', 'xlsx']
		});
		
		
		dialog.on('linksimportdialogok', function(){
			eventBus.trigger('tc-req-links-updated');
		});
		
		
	}
	
	return {
		init : init
	};

});