/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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


function TestSuiteMenuControl(){
	var self=this;
	
	var makeControl = $.proxy(function (){
		var node = $("<div/>");
		
		var input = $("<input/>", { 'type' : 'text', 'class' : 'suite-manager-menu-input'} );
		node.append(input);
		
		var button = $("<button/>", { 'role' : 'button', 'class' : 'button suite-manager-menu-button'});
		button.button({ 
						'text': false, 
						icons: {
							primary:'ui-icon-circle-plus'
							}
					  });
					  
		node.append(button);
		
		var superDiv = $("<div/>");
		superDiv.append(node);
		
		this.control=superDiv;
		
	}, this);
	
	this.getControlHtml = function (){
		return this.control.html();
	}
	
	makeControl();

}

/*
  this version of fg-menu is able to replace the content dynamically, recreating the internal structure when needed.
*/

function TestSuiteMenu(settings){

	/* **************** private ************** */
	
	var self = this;
	
	var makeList = $.proxy(function (){
		var list=$("<ul/>", { 'class' : 'suite-manager-menu-mainlist' });
		return list;
	}, this);
	
	var makeItem = $.proxy(function (json){
		var node=$("<li/>" );
		var a = $("<a/>", { 'href' : '#', 'data-suite-id' : json.id });
		node.append(a);
		a.text(json.name);
		return node;
	}, this);

	var getItemDomText = function (elt){		
		if (elt.firstElementChild!==undefined){
			return elt.firstElementChild.textContent;		
		}else{
			return elt.firstChild.innerText;
		}	
	};
	
	var getItemDomId = function (elt){
		if (elt.firstElementChild!==undefined){
			return elt.firstElementChild.getAttribute('data-suite-id');		
		}else{
			return elt.firstChild.getAttribute('data-suite-id');		
		}			
	}
	
	var getSpanDomId = function (elt){
		return elt.getAttribute('data-suite-id');
	}
	
	var getItemId = function (jqElt){
		return $('span', jqElt).data('suite-id');
	}
	
	
	var initializeContent = $.proxy(function (){	
		//generate new content
		var model = this.model.getData();		
		var list = makeList();
		
		for (var i in model){
			var node = makeItem(model[i]);
			list.append(node);
		}
		
		//sort new content
		var sorted = $('li', list).sort(function (a, b){
			var textA = getItemDomText(a);
			var textB = getItemDomText(b);
			return (textA < textB) ? -1 : 1;
		});
		
		list.append(sorted);
		
		//the horizontal rule + the control
		var hr = $('<hr/>');
		var control = this.control.getControlHtml();
			
		//now set the content
		var container = $("<div>").append(list);
		container.append(hr);
		container.append(control);
		this.menu.content=container.html();		
		
		
	}, this);
	
	/*
	 * if the menu was open by the time we (re)defined the content, we must create it now 
	 * (and hope that the refresh will be fast enough to the eyes of the user)
	 * else, we simply reset the 'menuExists' flag, and the popup will redraw itself next time 
	 * we open it.
	 */
	var redrawIfNeeded = $.proxy(function (wasOpen){
		if (wasOpen){
			this.menu.create();
		}else{
			this.menu.menuExists=false;
		}
	}, this);
	
	var getDatatableSelected = $.proxy(function (){
		var table = $(this.datatableSelector).dataTable( {'bRetrieve' : true});
		return getIdsOfSelectedTableRows(table, getTestPlansTableRowId);
	}, this);
	
	var displayAddSuiteError = $.proxy(function (xhr, text){
		try{
			var errContent = jQuery.parseJSON(xhr.responseText);
			var message = $("<div/>", { 'margin-top' : 'auto', 'margin-bottom' : 'auto'});
			
			if (errContent.fieldValidationErrors!==undefined){
				var errors=errContent.fieldValidationErrors;
				for (var i=0;i<errors.length;i++){
					message.append("<div>"+errors[0].errorMessage+"</div>");
				}
			}else{
				message.append('<div>could not add your suite : unexpected error</div>');
			}
			
			oneShotDialog('Information', message);
			
		}catch(wtf){
			//non json error : it must be handled by the generic handler (see the 
			//red thing showing up in the view right now)
			//anyway, job done here
		}
	}, this);
	
	/* **************************** public ****************************** */
	
	this.update = function (evt){
		//the only event ignored is "bind"
		if ((evt===undefined) || 
			(evt.evt_name=="rename") || 
			(evt.evt_name=="remove") ||
			(evt.evt_name=="add")	||
			(evt.evt_name=="refresh")
			){
			var wasOpen = this.menu.menuOpen;
			initializeContent();
			redrawIfNeeded(wasOpen);
		}			
	};
	
	/* *********************** handlers ***************** */
	
	
	
	var addSuite = $.proxy(function (){
		var self=this;
		var name = this.menu.getContainer().find('.suite-manager-menu-input').val();
		this.model.postNew(name).error(displayAddSuiteError).success(function (json) {
			bindSuiteItemsGeneral(json.id);
		});			
	}, this);
	
	var bindSuiteItemsGeneral = $.proxy(function (itemId){
		var self=this;
		var toSend = {};
		toSend.id = itemId;
		toSend['test-cases[]'] = getDatatableSelected();
		
		self.model.postBind(toSend)
		.success(function (){
			self.menu.kill();
		});
	},this);
	var bindSuiteItems = $.proxy(function (){
		this.menu.chooseItem = function (item){
			var toSendId = getSpanDomId(item);
			
			if(getDatatableSelected().length ==  0) {
				$(settings.emptySelectionMessageSelector).openMessage();
			} else {
				bindSuiteItemsGeneral(toSendId);				
			}
		}
	}, this);
	
	var bindButton = $.proxy(function (){
		var container = this.menu.getContainer();
		container.delegate('.suite-manager-menu-button', 'click', function (evt){
			evt.stopImmediatePropagation();
			addSuite();
		});
	},this);
	
	var bindInput = $.proxy(function (){
		var container = this.menu.getContainer();
		container.delegate('.suite-manager-menu-input', 'click', function (evt){		
			evt.stopImmediatePropagation();
		});
		container.delegate('.suite-manager-menu-input', 'keypress', function (evt){
			evt.stopImmediatePropagation();
			if (evt.which == '13' ){
				addSuite();
			}
		});
	}, this);
	
	var initHandlerBinding = $.proxy(function (){
		bindSuiteItems();
		bindButton();
		bindInput();
	}, this);
	
	var setContextual = $.proxy(function (){
		var oldCreate = this.menu.create;
		this.menu.create = function (){
			oldCreate.call(this);
			this.getContainer().parent().addClass('is-contextual');
		}
	}, this);
		
	/* *********************** init ********************* */
	//the goal is to init the menu to get a handler on it.
	var initMenu = $.proxy(function (){
		
		this.instance.fgmenu({
			content : '',
			showSpeed : 0,
			width: 190
		});
		
		this.menu = allUIMenus[allUIMenus.length-1];
		
	}, this);
	
	
	this.instanceSelector = settings.instanceSelector;
	this.model = settings.model;
	this.datatableSelector = settings.datatableSelector;
	
	if (settings.isContextual!==undefined) this.isContextual = settings.isContextual; 

	this.instance = $(settings.instanceSelector);
	this.control = new TestSuiteMenuControl();

	this.model.addListener(this);
	
	
	initMenu();
	initHandlerBinding();
	if (this.isContextual){
		setContextual();
	}
	initializeContent();	
	
}

