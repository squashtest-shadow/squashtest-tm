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

function TestSuiteModel(settings) {

	this.createUrl = settings.createUrl;
	this.baseUpdateUrl = settings.baseUpdateUrl;
	this.data = [];
	this.listeners = [];

	var self = this;

	/* ************* private ************* */
	var renameSuite = $.proxy(function(json) {
		for ( var i = 0; i < this.data.length; i++) {
			if (this.data[i].id == json.id) {
				this.data[i].name = json.name;
			}
		}
	}, self);
	var removeSuites = $.proxy(function(json) {
		for ( var i = 0; i < this.data.length; i++) {
			var idp = this.data[i].id;
			if ($.inArray(parseInt(idp, 10), json) >= 0) {
				delete this.data[i];
			}
		}
	}, self);

	var notifyListeners = $.proxy(function(evt) {
		for ( var i = 0; i < this.listeners.length; i++) {
			this.listeners[i].update(evt);
		}
	}, self);

	/* ************** public *************** */

	this.addListener = function(listener) {
		this.listeners.push(listener);
	}

	this.getData = function() {
		return this.data;
	}

	this.postNew = function(name) {

		return $.ajax({
			'url' : self.createUrl,
			type : 'POST',
			data : {
				'name' : name
			},
			dataType : 'json'
		}).success(function(json) {
			self.data.push(json);
			notifyListeners("add");
		})
	}

	this.postRename = function(toSend) {

		var url = this.baseUpdateUrl + "/" + toSend.id + "/rename";

		return $.ajax({
			'url' : url,
			type : 'POST',
			data : toSend,
			dataType : 'json'
		}).success(function(json) {
			renameSuite(json);
			notifyListeners("rename");
		})
	}
	this.postRemove = function(toSend) {

		var url = this.baseUpdateUrl + "/remove";

		return $.ajax({
			'url' : url,
			type : 'POST',
			data : toSend,
			dataType : 'json'
		}).success(function(json) {
			removeSuites(json);
			notifyListeners("remove");
		})
	}

	this.postBind = function(toSend) {
		var url = this.baseUpdateUrl + "/" + toSend.id + "/test-cases";

		return $.ajax({
			'url' : url,
			type : 'POST',
			data : toSend,
			dataType : 'json'
		}).success(function(json) {
			notifyListeners("bind");
		});
	}

	this.getModel = function(){
		$.ajax({
			'url' : settings.testSuiteListUrl,
			type : 'GET',
			dataType : 'json'
		}).then(function(json) {
			this.data = json;
			notifyListeners("add");
		});
	}

}