/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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

define(["jquery", "module", "jquery.squash.squashbutton", "jquery.squash.togglepanel"], function($, module){
	
	function initPreview(){
		
		var conf = module.config();
		
		var stopButton = $("#execute-stop-button");
		var stopIcon = stopButton.data('icon');
		stopButton.removeAttr('data-icon');
		stopButton.squashButton({
			'text' : false,
			'icons' : {
				'primary' : stopIcon
			}
		}).click(function(){
			window.close();
		});
		
		var beginButton = $("#execute-begin-button");
		var beginIcon = beginButton.data('icon');
		beginButton.removeAttr('data-icon');
		beginButton.squashButton({
			'icons' : {
				'secondary' : beginIcon
			}
		});
		
		var informationsPanel = $("#execute-informations-panel");
		var infoTitle = informationsPanel.data('title');
		informationsPanel.removeAttr('data-title');
		informationsPanel.togglePanel({
			title : infoTitle
		});
		
	};
	
	return initPreview;
	
});
	