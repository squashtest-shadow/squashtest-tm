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
define(["jquery", "backbone", "underscore", "app/squash.handlebars.helpers"],
	function($, backbone, _, Handlebars) {
	"use strict";

	var abstractStepView = Backbone.View.extend({
		el : "#current-step",

	
		updateModel : function (){
			//do in sub class
		},
	
		_initialize : function(data) {
			this.render(data);
		},
		render : function(data, tmpl) {	
				var src = $(this.tmpl).html();
				this.template = Handlebars.compile(src);

			this.$el.append(this.template(data));

			return this;
		},
		
		destroy_view: function() {

		    this.undelegateEvents();
		    this.$el.removeData().unbind(); 
		    this.remove();  
		    Backbone.View.prototype.remove.call(this);
		}
		
	});
	
	abstractStepView.extend = function(child) {
		var view = Backbone.View.extend.apply(this, arguments);
		view.prototype.events = _.extend({}, this.prototype.events, child.events);
		return view;
	};
	

	return abstractStepView;

});