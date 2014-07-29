/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
/**
 * projectPicker JQuery ui widget. Should be used with the appropriate dom component (project-picker.frag.html)
 * 
 * Configuration : { 
 *	url: "the url where to get the projects", required 
 *  confirm : function() on confirm, required 
 *  cancel: function() on cancel,  optional, defaults to close 
 *  loadOnce: true|false|"never", loads projects only once, defaults to false, loads on each open, "never" never loads. 
 *  
 *  }
 * 
 * It also forwards additional configuration to the internal popup dialog.
 * 
 * Methods : open, close
 */
(function($) {
	var dialogConfig = {
		autoOpen : false,
		resizable : false,
		modal : true,
		width : 600
	};

	function eachCheckbox(domPicker, eachCallback) {
		var boxes = $(domPicker).parents(".project-picker").find(".project-filter-list .project-checkbox");
		boxes.each(eachCallback);
	}

	function selectAllProjects() {
		eachCheckbox(this, function() {
			this.checked = true;
		});
	}

	function deselectAllProjects() {
		eachCheckbox(this, function() {
			this.checked = false;
		});
	}

	function invertAllProjects() {
		eachCheckbox(this, function() {
			this.checked = !this.checked;
		});
	}

	function zcssClass() {
		var current = "even";
		return {
			swap : function() {
				current = (current === "even" ? "odd" : "even");
				return current;
			}
		};
	}

	/**
	 * Code managing the loading phase of the popup. It expects the server to send the data as a json object, see
	 * tm.web.internal.model.jquery.FilterModel
	 * 
	 * note : each project in the array is an array made of the following : { Long , String , Boolean )
	 */

	function appendProjectItem(jqPicker, projectItemData, cssClass) {
		var jqNewItem = jqPicker.find(".project-item-template .project-item").clone();
		jqNewItem.addClass(cssClass);

		var jqChkBx = jqNewItem.find(".project-checkbox");
		jqChkBx.attr('id', 'project-checkbox-' + parseInt(projectItemData[0], 10));
		jqChkBx.attr("value", projectItemData[0]);

		var jqName = jqNewItem.find(".project-name");
		jqName.append(projectItemData[1]);

		// get() returns an array even id jqChkBx is not one
		jqChkBx.get()[0].checked = projectItemData[2];
		// initializes the previous state of each project checkbox
		jqChkBx.data("previous-checked", projectItemData[2]);

		jqPicker.find(".project-filter-list").append(jqNewItem);
	}

	function populateFilterProject(jqPicker, jsonData) {
		var cssClass = zcssClass();

		$.each(jsonData.projectData, function() {
			appendProjectItem(jqPicker, this, cssClass.swap());
		});
	}

	function itemToDataMapper() {
		var item = $(this), jqCbx = item.find(".project-checkbox"), cbx = jqCbx.get()[0];
		var $name = item.find(".project-name").text();

		return {
			id : cbx.value,
			name : $name,
			selected : cbx.checked
		};
	}

	$.widget("squash.projectPicker",$.squash.confirmDialog, {
		options : {
			url : "",
			loadOnce : false
		},

		_create : function() {
			this._super();
			var self = this, 
				opt = self.options, 
				elem = self.element, 
				projectList = elem.find(".project-filter-list"), 
				selAll = elem.find(".project-picker-selall"), 
				unselAll = elem.find(".project-picker-deselall"), 
				invSel = elem.find(".project-picker-invsel");

			elem.addClass("popup-dialog");
			self.projectList = projectList;

			selAll.click(selectAllProjects);
			unselAll.click(deselectAllProjects);
			invSel.click(invertAllProjects);

			// on dialog close we reset checkboxes to last
			// stored state. Ok handler insures state has been
			// stored beforehand.
			self.uiDialog.on('click', '.ui-dialog-buttonpane button:first',  $.proxy(self._okWrapper, self));
		},

		/**
		 * Returns a function which should be used as the ok handler for this widget's dialog.
		 */
		_okWrapper : function() {
			this._commitState.apply(this, arguments);
			if (!! this.options.confirm){
				this.options.confirm.call(this, arguments);
			}
		},

		/**
		 * Commits the current state of project checkboxes ie stores the current state.
		 */
		_commitState : function() {
			this.element.find(".project-filter-list .project-checkbox").each(function() {
				$(this).data("previous-checked", this.checked);
			});
		},

		/**
		 * Sets the project checkboxes back in their previously saved state.
		 */
		_resetState : function() {
			this.element.find(".project-filter-list .project-checkbox").each(function() {
				var previous = $(this).data("previous-checked");
				this.checked = previous;
			});
		},

		_setOption : function(key, value) {
			$.Widget.prototype._setOption.apply(this, arguments);
		},

		destroy : function() {
			$.Widget.prototype.destroy.call(this);
		},

		open : function() {
			var _super = this._super;
			var self = this;
			self._load().done(function() {
				_super.call(self);
				self._resetState();
			});
		},
		
		_load : function() {
			var self = this, picker = this.element, opt = self.options, deferred = $.Deferred();

			if (opt.loadOnce === "never"){
				deferred.resolve();
			}			
			else if (opt.loadOnce && self.projectsLoaded) {
				deferred.resolve();
			} 
			else {
				self.projectList.empty();

				$.getJSON(opt.url).done(function(data) {
					populateFilterProject(picker, data);
					self.projectsLoaded = true;
					deferred.resolve();

				}).fail(function() {
					deferred.reject();
				});

			}

			return deferred.promise();
		},

		data : function() {
			var self = this, projectList = self.projectList;
			return projectList.find(".project-item").map(itemToDataMapper);
		}
	});
})(jQuery);
