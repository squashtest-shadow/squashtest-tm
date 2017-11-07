/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
 Backbone things used by bugtracker-info


*/


define(['jquery', 'backbone', 'handlebars'], function($, Backbone, Handlebars){
	
	
	// ****************** auth conf details ***************
	
	var CredentialManagerView = Backbone.View.extend({
		
		el: "#bugtracker-auth",
		
		initialize: function(options){
			
			// select the right subview to apply for the credentials form
			var subviewCtor = null;
			var authMode = options.model.get('selectedProto');
			
			switch(authMode){
				case 'BASIC_AUTH' :  subviewCtor = BasicAuthView; break;
				
				default : console.log('unsupported mode : '+authMode); 
							subviewCtor = new Backbone.View();	// default, empty view if unsupported 
							break;
			}
			
			// prepare the options for the subview
			// note that content can legally be empty, the subview is required to deal with it
			var subviewModel = new Backbone.Model(options.model.get('credentials'));
			var credsOptions = {
				model : subviewModel
			}
			
			// also make the main model listen to the subview model
			options.model.set('credentials', subviewModel);
			
			// init, if defined
			this.subview = new subviewCtor(credsOptions);
			
			// now render
			this.render();
			
		},
		
		events : {
			'click #auth-policy-user' : 'disablePanel',
			'click #auth-policy-application' : 'enablePanel'
		},
		
		render: function(){
			switch(this.model.get('authPolicy')){
				case 'USER': $("#auth-policy-user").click(); break;
				case 'APP_LEVEL' : $("#auth-policy-application").click(); break;
				default: this.enablePanel(); break;
			}
			
			this.subview.render();
			
			return this;
		},
		
		disablePanel: function(){
			
		},
	
		enablePanel : function(){
			console.log('enabling panel');
		}
	});
	
	
	
	
	var BasicAuthView = Backbone.View.extend({
		
		el: '.bt-auth-variable-template',
		
		events: {
			'change #bt-auth-basic-login' : 'setUsername',
			'change #bt-auth-basic-pwd' : 'setPassword'
		},
		
		initialize : function(options){
			// deal with the case of undefined model 
			if (options.model.get('usename') === undefined){
				this.model = new Backbone.Model({
					"username" : "",
					"password" : ""
				});
			}
		},
		
		template: Handlebars.compile(
			'<div class="display-table">' +
				'<div class="display-table-row" style="line-height:3.5">' +
					'<label class="display-table-cell">login</label>' +
					'<input id="bt-auth-basic-login" type="text" class="display-table-cell" value="{{this.username}}">' + 
				'</div>' +
				'<div class="display-table-row" style="line-height:3.5">' +
					'<label class="display-table-cell">mot de passe</label> ' +
					'<input id="bt-auth-basic-pwd" class="display-table-cell" type="password" value="{{this.password}}"> ' +
				'</div>' +
			'</div>'
		),
		
		render: function(){
			this.$el.html(this.template(this.model.attributes));		
		},
		
		enable: function(){
			this.$el.find('input').addAttr('disabled');
		},
		
		disable: function(){
			this.$el.find('input').removeAttr('disabled');
		},
		
		setUsername: function(){
			console.log(args);
		},
		
		setPassword: function(){
			console.log(args);
		}
		
		
	});
	
	
	return {
		CredentialManagerView : CredentialManagerView
	}
	
});