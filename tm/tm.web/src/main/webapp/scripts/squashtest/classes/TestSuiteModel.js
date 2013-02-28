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
 
 /* ********************
	This object somewhat implements a distributed MVC. Locally, within the contextual content, it represents the model. However, when modifications happen it must warn it's listeners, but also
	the object squashtm.contextualContent. It will relay the information to other models (ie, the tree).
	
	As such a model object is both master (of its listeners) and slave (of the contextual content).
 
 ********************* */

function TestSuiteModel(settings) {

	this.createUrl = settings.createUrl;
	this.baseUpdateUrl = settings.baseUpdateUrl;
	this.getUrl = settings.getUrl;
	this.removeUrl = settings.removeUrl;
	
	if (settings.initData !==undefined){
		this.data=settings.initData;
	}else{
		this.data = [];
	}
	
	this.listeners = [];

	var self = this;

	/* ************** private ************* */
	
	//we have to reimplement indexOf because IE8 doesn't support it
	//returns -1 if not found
	var indexById = $.proxy(function (id){
		for (var i=0;i<this.data.length;i++){
			if (this.data[i].id==id){
				return i;
			}
		}
		return -1;
	}, self);
	
	var renameSuite = $.proxy(function (json) {
		var index = indexById(json.id);
		if (index!=-1){
			this.data[index].name=json.name;
		}
	}, self);
	
	var removeSuites = $.proxy(function (list) {
		for (var i in list){
			var index = indexById(list[i]);
			if (index!=-1){
				this.data.splice(index, 1);
			}
		}
	}, self);
	
	var _getModel = function (){
		return $.ajax({
			'url' : self.getUrl,
			type : 'GET',
			dataType : 'json'
		}).success(function (json){
			this.data=json;
		});
	}

	var notifyListeners = $.proxy(function (evt) {
		for ( var i = 0; i < this.listeners.length; i++) {
			this.listeners[i].update(evt);
		}
	}, self);
	
	
	var notifyContextualContent = $.proxy(function (evt){
		if (squashtm.contextualContent !== undefined){
			squashtm.contextualContent.fire(this, evt);
		}
	}, self);

	/* ************** public interface (slave) **************** */
	
	
	this.update = function (event){
		//in any case we refetch the data. Perhaps we will refine this later.
		this.getModel();
	}	
	
	
	/* ************** public interface (master) *************** */

	this.addListener = function (listener) {
		this.listeners.push(listener);
	}

	this.getData = function () {
		return this.data;
	}


	this.postNew = function (name) {

		return $.ajax({
			'url' : self.createUrl,
			type : 'POST',
			data : {
				'name' : name
			},
			dataType : 'json'
		}).success(function (json) {
			self.data.push(json);
			var evt = { evt_name : "add" , newSuite : json};
			notifyListeners(evt);
			notifyContextualContent(evt);
		})
	}

	this.postRename = function (toSend) {

		var url = this.baseUpdateUrl + "/" + toSend.id + "/rename";

		return $.ajax({
			'url' : url,
			type : 'POST',
			data : toSend,
			dataType : 'json'
		}).success(function (json) {
			renameSuite(json);
			var evt = { 
				evt_name : "rename",
				evt_target : {
					obj_id : toSend.id,
					obj_restype : "test-suites"
				},
				evt_newname : toSend.name
			};
			notifyListeners(evt);
			notifyContextualContent(evt);
		})
	}
	
	this.postRemove = function (toSend) {

		var url = this.removeUrl;

		return $.ajax({
			'url' : url,
			type : 'POST',
			data : toSend,
			dataType : 'json'
		}).success(function (json) {
			removeSuites(json);
			var evt = { evt_name : "remove" } ;
			notifyListeners(evt);
			notifyContextualContent(evt);
		})
	}

	this.postBind = function (toSend) {
		var url = this.baseUpdateUrl +"/test-cases";

		return $.ajax({
			'url' : url,
			type : 'POST',
			data : toSend,
			dataType : 'json'
		}).success(function (json) {
			var evt = { evt_name : "bind" };
			notifyListeners(evt);
			notifyContextualContent(evt);
		});
	}
	

	this.getModel = function (){
		_getModel().success(function () {
			notifyListeners({ evt_name : "refresh"});
		});
	}
	
	//register to the contextual content manager if exists
	
	if (squashtm.contextualContent !== undefined){
		squashtm.contextualContent.addListener(this);
	}	

}