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



define (["require","squash.cssloader", "./panel", "./table", "./popup"],function(require, cssloader, Panel, Table, Popup){
	
	var cssUrl = require.toUrl("./cuf-binding-styles.css");
	cssloader.cssFromUrl(cssUrl);

	function getPanelConf(settings){
		return { 
			'selector' : settings.mainSelector+" .cuf-binding-panel",
			'initiallyOpen' : true, 
			'title' : settings.panelTitle
		};
	}
	
	function getTableGetURL(settings){
		var url = settings.baseURL;
		url = url+"?projectId="+settings.projectId;
		url = url+"&bindableEntity="+settings.entityType;
		return url;
	}
	
	function getTableDeleteURL(settings){
		return settings.baseURL;
	}
	
	function getTableMoveURL(settings){
		return settings.baseURL;
	}
	
	function getTableConf(settings){
		return {
			selector : settings.mainSelector+" .cuf-binding-table",
			languageUrl : settings.tableLanguageUrl,
			getUrl : getTableGetURL(settings),
			deleteUrl : getTableDeleteURL(settings),
			moveUrl : getTableMoveURL(settings),
			deferLoading : settings.tableDeferLoading,
			oklabel : settings.oklabel,
			cancellabel : settings.cancellabel,
			deleteMessage : settings.tableDeleteMessage,
			deleteTooltip : settings.tableDeleteTooltip
		};
	}
	
	function getPopupGetURL(settings){
		var url = settings.baseURL+"/available";
		url = url+"?projectId="+settings.projectId;
		url = url+"&bindableEntity="+settings.entityType;
		return url;
	}
	
	function getPopupPostURL(settings){
		return settings.baseURL+"/new-batch";
	}
	
	
	function getPopupConf(settings){
		return {
			projectId : settings.projectId,
			bindableEntity : settings.entityType, 
			getURL : getPopupGetURL(settings),
			postURL : getPopupPostURL(settings),
			selector : settings.mainSelector+" .cuf-binding-popup",
			title : settings.popupTitle,
			oklabel : settings.oklabel,
			cancellabel : settings.cancellabel
		};		
	}
	
	return function(settings){
		var self=this;
		
		
		
		var panelConf = getPanelConf(settings);
		this.panel = new Panel(panelConf);
		
		var tableConf = getTableConf(settings);
		this.table = new Table(tableConf);
		
		var popupConf = getPopupConf(settings);
		this.popup = new Popup(popupConf);
		
		this.panel.getButton().setPopup(this.popup);
		this.popup.addPostSuccessListener({
			update : function(){
				self.table.refresh();
			}
		});

	};

});