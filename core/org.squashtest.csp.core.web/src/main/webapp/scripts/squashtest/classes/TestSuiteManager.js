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

 /*
  * TODO : document this
  *
  */
 

function TestSuiteManagerControl(settings){
	
	this.manager=settings.manager;
	this.defaultMessage=settings.defaultMessage;
	this.panel=settings.panel;
	this.action=settings.action;
	
	this.input=$("input[type='text']",settings.panel);
	this.button=$("input[type='button']", settings.panel);
	
	var self=this;
	
	/* ********* public ************ */
	
	this.reset = function(){
		defaultState();
		this.input.addClass('manager-control-ready');
		this.input.removeClass('manager-control-disabled');
	};
	
	this.deactivate = function(){
		defaultState();
		this.input.attr('disabled','disabled');
		this.input.removeClass('manager-control-ready');
		this.input.addClass('manager-control-disabled');
	};
	
	
	/* ************* private ******* */
	
	var defaultState=$.proxy(function(){
		this.button.button("disable");
		this.input.removeAttr('disabled');
		this.input.val(this.defaultMessage);
	}, self);
	
	var editState=$.proxy(function(){
		this.input.removeClass('manager-control-ready');
		this.input.val('');
		this.input.change();
		this.button.button('disable');
	}, self);
	
	
	/* ************* handlers ******** */	
	
			
	this.button.click(function(){
		self.action();
	});

	//we're in competition here with the default 'enter' event bound to the close button
	this.input.keypress(function(evt){
		self.manager.instance.find('.error-message').html('');
		if(evt.which=='13'){
			evt.stopImmediatePropagation();
			var disabledStatus = self.button.button("option", "disabled");
			if (disabledStatus===false){
				self.button.click();
			}
		}		
	});

	//that one is better than change()
	this.input.keyup(function(evt){
		var button = self.button;
		if (this.value.length>0){
			button.button('enable');
		}else{
			button.button('disable');
		}	
	});
	
	this.input.focus(editState);
	
	//this.input.focusout(defaultState);
		
}
 
 
function TestSuiteManager(settings){


	/* **************** private state management methods ******************** */

	var self=this;
			
	var deselectAllSuites = $.proxy(function(){
		$(".suite-div", this.display.panel).removeClass("suite-selected ui-widget-header ui-state-default");
	}, self);
	
	var updatePopupState = $.proxy(function(){		

		var allItems = $(".suite-div.suite-selected", this.display.panel);
		
		switch(allItems.size()){
			case 0 :
				this.rename.control.deactivate();
				this.remove.button.button('disable');
				break;
			case 1 : 
				this.rename.control.reset();
				this.remove.button.button('enable');
				break;
			default : 
				this.rename.control.deactivate();
				break;
		}
		
	}, self);
	

	/* ******************** DOM management ************************* */

	var sortSuiteList = function(){
		var allSuites = $('.suite-div', self.display.panel);
		
		var sorted = allSuites.sort(function(a,b){
			if (a.firstElementChild!==undefined){
				return (a.firstElementChild.textContent < b.firstElementChild.textContent) ? -1 : 1;		
			}else{
				return (a.firstChild.innerText < b.firstChild.innerText) ? -1 : 1;
			}
				
		});
		self.display.panel.append(sorted);
	};

	
	var appendNewSuite = $.proxy(function(jsonSuite){	
		
		var newSuite = $("<div/>", {'class' : 'suite-div ui-corner-all' } );
		var spanSuite = $("<span/>", {'data-suite-id' : jsonSuite.id, 'text' : jsonSuite.name});
		
		newSuite.append(spanSuite);
		this.display.panel.append(newSuite);
		
	}, self);
	
	var renameSuite = $.proxy(function(jsonSuite){
		var spanSuite = $(".suite-selected span[data-suite-id='"+jsonSuite.id+"']", this.display.panel);
		spanSuite.text(jsonSuite.name);
	}, self);
	
	
	/* ******************** actions ************************* */
	
	/* ----- suite creation ------- */
	
	var postNewSuite = $.proxy(function(){
		var url = this.baseCreateUrl+"/new";
		var name = this.create.control.input.val();
		
		var defer = $.Deferred();
	
		$.ajax({
			'url' : url,
			type : 'POST',
			data : { 'name' : name },
			dataType : 'json'
		})
		.success(function(json){
			appendNewSuite(json);
			sortSuiteList();
			defer.resolve();
		})
		.error(defer.reject);
		
		return defer.promise();
		
	}, self);
	
	/* ------- suite renaming -------- */
	
	var postRenameSuite = $.proxy(function(){
		
		var suiteId = $('.suite-selected span', this.display.panel).data('suite-id');
		var url = this.baseUpdateUrl+"/"+suiteId+"/rename";
		var newName = this.rename.control.input.val();
		
		var defer = $.Deferred();
		
		$.ajax({
			'url' : url,
			type : 'POST',
			data : { 'suiteId' : suiteId, 'newName' : newName },
			dataType : 'json'
		})
		.success(function(json){
			renameSuite(json);
			sortSuiteList();
			defer.resolve();
		})
		.error(defer.reject);
		
		return defer.promise();
		
	}, self);
	
	
	/* ------ item selection --------- */

	var bindSelectSuite = $.proxy(function(){
		this.display.panel.delegate('.suite-div', 'click', function(){
			$(this).toggleClass('suite-selected ui-widget-header ui-state-default');
			updatePopupState();
		});	
	}, self);
		
	
	/* ******************** init code ****************************** */
	
	
	this.init = function(){	
		deselectAllSuites();
		this.create.control.reset();
		updatePopupState();
	}
	
	
	this.instance = settings.instance;
	this.baseCreateUrl = settings.baseCreateUrl;
	this.baseUpdateUrl = settings.baseUpdateUrl;
	
	this.create = {};
	this.rename = {};
		
	this.display = {};
	this.display.panel = $(".display-suites-section", this.instance);
	
	this.remove = {};
	this.remove.button = $(".remove-suites-section input", this.instance);

	var createControlSettings = {
		manager : self,
		defaultMessage : settings.defaultMessage,
		panel : $(".create-suites-section", this.instance),
		action : postNewSuite	
	}
	
	var renameControlSettings = {
		manager : self,
		defaultMessage: settings.defaultMessage,
		panel : this.rename.panel = $(".rename-suites-section", this.instance),
		action : postRenameSuite
	}
	
	this.create.control = new TestSuiteManagerControl(createControlSettings);
	this.rename.control = new TestSuiteManagerControl(renameControlSettings);

	
	sortSuiteList();
	bindSelectSuite();
	
	/* TODO : */
	this.remove.button.click(function(){alert("not implemented yet");});
	
}
 
