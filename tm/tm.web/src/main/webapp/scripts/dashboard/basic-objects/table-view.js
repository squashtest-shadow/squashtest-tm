/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
 * This is your plain table. When its model is updated, will render again. Note that 
 * it must be subclassed and implement getData. Also, if there is one row in the tbody when
 * the widget initialize, this line will be used as a template for when the model is 
 * empty (no data).
 * 
 * DOM options (set on the <table> tag) : 
 * 	- model-attribute : (optional) If set, will consider only this attribute of the model and not the whole model.
 */

define(["jquery", "backbone", "squash.attributeparser", "handlebars"], 
		function($, Backbone, attrparser, Handlebars){
	
	
	return Backbone.View.extend({

		_datarowtemplate : Handlebars.compile(
				"{{#each this}}" +
				"<tr>" +
				"{{#each this}}" +
				"<td>{{this}}</td>" +
				"{{/each}}" +
				"</tr>" +
				"{{/each}}"
		),
		
		getData : function(){
			throw "must be override. Must return [][] (array of array).";
		},
		
		initialize : function(){
			this._readDOM();
			this.render();
			this._bindEvents();
		},
		
		render : function(){
			
			if (! this.model.isAvailable()){
				return;
			}
			
			var body = this.$el.find('tbody');
			body.empty();
			
			var data = this.getData();
			
			if (data.length===0){
				body.append(this.emptyRowTemplate.clone());
			}
			else{
				var rows = this._datarowtemplate(data);
				body.append(rows);
			}
		},
		
		_readDOM : function(){
			// empty row template
			var emptyrow = this.$el.find('tbody tr:first')
			this.emptyRowTemplate = (emptyrow.length>0) ? emptyrow : $('<tr></tr>');

			
			var strconf = this.$el.data('def');
			var conf = attrparser.parse(strconf);
			if (conf['model-attribute']!==undefined){
				this.modelAttribute = conf['model-attribute'];
			}
		},
		
		_bindEvents : function(){
			var self = this;
			var modelchangeevt = "change";
			if (this.modelAttribute!==undefined){
				modelchangeevt+=":"+this.modelAttribute;
			}
			this.listenTo(this.model, modelchangeevt, this.render);
		}
		
	});
	
	
});