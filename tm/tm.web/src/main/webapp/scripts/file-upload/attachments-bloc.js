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
define(["jquery", "./should-downgrade-upload-dialog", "./jquery.squash.attachmentsDialog"], function($, shouldDowngrade){
	
	function reloadAttachments(settings){
		var container =$("#attachment-container"); 
		
		container.load(settings.baseURL+"/display");
	}
	
	
	function initRegularPopup(settings){
		
		// init the dialog
		var dialog = $("#add-attachments-dialog").attachmentsDialog({
			url : settings.baseURL+"/upload"
		});
		
		dialog.on('attachmentsdialogdone', function(){
			reloadAttachments(settings);
		});
		
		
		// bind the buttons
		$("#manage-attachment-bloc-button").on('click', function(){
			document.location.href = settings.baseURL+"/manager?workspace="+settings.workspace;
		});
		
	
		$("#upload-attachment-button").on('click', function(){
			$("#add-attachments-dialog").attachmentsDialog('open');
		});
		
		
	}
	
	function initDowngradedPopup(settings){
		
		// no attachment popup here : the downgraded upload form is downloaded from the server
		
		
		// event binding now
		$("#manage-attachment-bloc-button").on('click', function(){
			document.location.href = settings.baseURL+"/manager?workspace="+settings.workspace;
		});
		
		$("#upload-attachment-button").on('click', function(){
			window.open(settings.baseURL+'/form', 'uploader', "height=400, width=450, resizable=yes");
			
			// we must publish the function reloadAttachments so that the
			// window we just opened can invoke it
			squashtm.api = squashtm.api || {};
			squashtm.api.reloadAttachments = function(){
				reloadAttachments(settings);
			};
		});
	}
	


	function init(settings){
		if (shouldDowngrade()){
			initDowngradedPopup(settings);
		}
		else{
			initRegularPopup(settings);
		}
		
	}
	
	return {
		init : init
	};
});