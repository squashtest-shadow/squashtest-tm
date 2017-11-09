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
 Backbone things used by bugtracker-info.
 
 There is a view and a subview. The code is fairly light so there wont 
 be much comments here, beside the indications stated below. 
 
 1/ Master view
 
 The master view is named CredentialManagerView and handles the following :
  
  * its main div : #bugtracker-auth
  * the radio buttons : #bt-auth-policy-user and #bt-auth-policy-application
  * in the stored credentials panel (.bt-auth-credentials-section) 
  		- the authentication protocol dopdown list : #bt-auth-proto
  		- the test button : #bt-auth-test
  		- the save button : #bt-auth-save
  		- the error message pane : #bt-auth-messagezone

The part of the stored credentials panel between the dropdown and the button is 
handled by the subview.

2/ Subview

The subview depends on the authentication protocol selected by #bt-auth-proto :  
the implementation changes when the dropdown changes. Each implementation comes 
with its own template. 

 *template insertion div : #bt-auth-cred-template
 
 
3/ Configuration

The configuration expected by the CredentialManagerView is :

	{ 
		btUrl : the bugtracker url 	
		model : {
			authPolicy :  either 'USER' and 'APP_LEVEL'
			availableProtos : the list of available authentication protocols
			selectedProto : the selected protocol chosen among the above
			failureMessage : error message if an unrecoverable error was encountered
			warningMessage: error message if a recoverable error was encountered
			credentials : the actual credential object, which is variable. See the various subview implementations for insights. May be null if none were set yet.
		}
	}
	
This serves as the Backbone model for the master view. An important thing to 
note is that the attribute 'credentials' will itself be turned into a Backbone
model and then passed to the subview : this element is shared between the 
master and the subview. It entails that the subview implementation MUST NEVER 
instantiate its model, instead it must work with what was given to it. The 
content may however change at will.
	
	
4/ States

	* If #bt-auth-policy-user is selected, the stored credentials panel is disabled
	* If #bt-auth-policy-application is enabled, the stored credentials panel is enabled
	* When #bt-auth-proto changes value, the subview is flushed and replaced by the requested implementation
	* When pages load, the subview is given the credentials object from the configuration as its model
	* Subsequent refreshing of the subview alone will use a blank model 

*/


define(['jquery', 'backbone', 'underscore', 'handlebars', 'app/ws/squashtm.notification', 'app/squash.handlebars.helpers'], 
		function($, Backbone, _, Handlebars, notification){
	
	

	// ***************** communication channel ***************************
	
	var radio = $.extend({}, Backbone.Events);
	
	
	// ************************ The main view ****************************
	
	var CredentialManagerView = Backbone.View.extend({
		
		el: "#bugtracker-auth",
		
		// the following are created at initialization
		$credPane : null,
		$msgPane : null,
		$usrPolRadio : null,
		$appPolRadio : null,
		
		initialize : function(options){
			this.$credPane = new MainView(options);
			this.$msgPane = new MessageView();
			
			this.$usrPolRadio = $("#bt-auth-policy-user");
			this.$appPolRadio = $("#bt-auth-policy-application");
			this.btUrl = options.btUrl;
			
			this.render();
		},
		
		events : {
			'change #bt-auth-policy-user' : 'setPolicyUser',
			'change #bt-auth-policy-application' : 'setPolicyApp'
		},
	
		render : function(){
			var failure = this.model.get('failureMessage');
		
			if (!! failure){
				// ooooh this is bad. Do not show the app credentials panel 
				// and just display the message
				this.fail(failure);
			}
			else{
				this.renderCreds();
			}
			
			return this;
		},
		
		fail : function(failure){
			this.$appPolRadio.prop('disabled', true);
			this.$credPane.hide();
			radio.trigger('bt-auth-failure', failure);
		},
		
		renderCreds: function(){
			this.$appPolRadio.prop('disabled', false);
			
			/*
			 * Most of the time the message pane and the cred pane work together
			 * but here we really mean to handle the cred pane alone because 
			 * the msg pane must be rendered no matter what (and already has)
			 */
			this.$credPane.render();
			this.$credPane.show();
			
			
			switch(this.model.get('authPolicy')){
				case 'USER': this.setPaneStatus('disable'); break;
				case 'APP_LEVEL' : this.setPaneStatus('enable'); break;
				default: this.$credPane.enable(); break;
			}
			
			this.$credPane.render();
		},
		
		setPolicyUser : function(evt){
			var view = this;
			this.setPolicy('USER').done(function(){
				view.setPaneStatus('disable');
			});
		},
		
		setPolicyApp : function(evt){
			var view = this;
			this.setPolicy('APP_LEVEL').done(function(){
				view.setPaneStatus('enable');
			});
		},
		
		setPaneStatus : function(status){
			this.$credPane[status]();
			this.$msgPane[status]();
		},
		
		setPolicy : function(policy){
			this.model.set('authPolicy', policy);
			var url = this.btUrl;
			return $.ajax({
				url : url,
				type : 'POST',
				data : {id: 'bugtracker-auth-policy', value : policy}
			});
		},
		
		
	});

	
	// ************ message pane *************************
	
	
	var MessageView = Backbone.View.extend({
		
		el : '#bt-auth-main-messagezone',
		
		$failPane : null,
		$warnPane : null,
		$succPane : null,

		
		initialize : function(){
			this.$failPane = $("#bt-auth-failure");
			this.$warnPane = $("#bt-auth-warning");
			this.$succPane = $("#bt-auth-info");
			
			this.listenTo(radio, 'bt-auth-success', this.showSuccess);
			this.listenTo(radio, 'bt-auth-warning', this.showWarning);
			this.listenTo(radio, 'bt-auth-failure', this.showFailure);
		},
		
		disable : function(){
			this.$el.addClass('disabled-transparent');
		},
	
		enable : function(){
			this.$el.removeClass('disabled-transparent');
		},
		
		allPanes : function(){
			return [this.$failPane, this.$warnPane, this.$succPane];
		},
		
		showPane : function(paneName){
			_.without(this.allPanes(), this[paneName])
			.forEach(function(pane){
				pane.addClass('not-displayed');
			});
			this[paneName].removeClass('not-displayed');
		},
		
		showSuccess : function(){
			this.showPane('$succPane');
		},
		
		showWarning : function(msg){
			this.$warnPane.find('.generic-warning-main').text(msg);
			this.showPane('$warnPane');
		},
		
		showFailure : function(msg){
			this.$failPane.find('.generic-warning-main').text(msg);
			this.showPane('$failPane');
		}
	});
	
	
	// ********************* The application credentials view **************************

	
	// TODO : actually handle the dropdown list when more protocols are supported
	var MainView = Backbone.View.extend({
		
		el: "#bt-auth-creds-main",
		
		// following attributes are initialized in the init function
		$dropdown : null,
		$btnpane : null,
		btUrl : null,
		
		initialize: function(options){
			
			var $el = this.$el;
			this.$dropdown = $el.find('#bt-auth-proto');
			this.$btnpane = $el.find('#bt-auth-creds-buttonpane');
			this.btUrl = options.btUrl;
			
			this.getButtons().button();
			
			// select the right subview to apply for the credentials form
			var SubView = null;
			var authMode = options.model.get('selectedProto');
			
			switch(authMode){
				case 'BASIC_AUTH' :  SubView = BasicAuthView; break;
				
				default : console.log('unsupported mode : '+authMode); 
							SubView = new Backbone.View();	// default, empty view if unsupported 
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
			this.subview = new SubView(credsOptions);

			
		},
		
		events : {
			'click #bt-auth-test' : 'test',
			'click #bt-auth-save' : 'save'
		},
		
		render: function(){
			this.subview.render();

			var warning = this.model.get('warningMessage');
			if (!!warning){
				radio.trigger('bt-auth-warning', warning);
			}
			
			return this;
		},
		
		show : function(){
			this.$el.removeClass('not-displayed');
		},
		
		hide : function(){
			this.$el.addClass('not-displayed');
		},
		
		
		disable : function(){
			this.$el.addClass('disabled-transparent');
			this.$dropdown.prop('disabled', true);
			this.getButtons().button('disable');
			this.subview.disable();
		},
	
		enable : function(){
			this.$el.removeClass('disabled-transparent');
			this.$dropdown.prop('disabled', false);
			this.getButtons().button('enable');
			this.subview.enable();
		},
		
		setAjaxMode : function(){
			this.getButtons().css('visibility', 'hidden');
			this.$btnpane.addClass('waiting-loading');
		},
		
		unserAjaxMode : function(){
			this.getButtons().css('visibility', 'visible');
			this.$btnpane.removeClass('waiting-loading');
		},
		
		getButtons : function(){
			return this.$btnpane.find(':button');
		},
		
		test : function(){
			this.postCredentials('/credentials/validator');
		},
		
		save : function(){
			this.postCredentials('/credentials');
		},
		
		postCredentials : function(urlSuffix){
			var self = this;
			var url = this.btUrl + urlSuffix;
			var creds = this.model.get('credentials').attributes;
			
			// mixin with the type because it's required for deserialization
			var type = this.model.get('selectedProto');
			var payload = $.extend({type: type}, creds, true);
			
			this.setAjaxMode();
			
			return $.ajax({
				url : url,
				type : 'POST',
				data : JSON.stringify(payload),
				contentType : 'application/json'
			})
			.done(function(){
				radio.trigger('bt-auth-success');
			})
			.fail(function(xhr){
				xhr.errorIsHandled = true;
				radio.trigger('bt-auth-warning', notification.getErrorMessage(xhr));
			})
			.always(function(){
				self.unserAjaxMode();
			});
		}
	});
	


	
	// ************ implementations for the subviews *****************************
	
	var BasicAuthView = Backbone.View.extend({
		
		el: '#bt-auth-cred-template',
		
		events: {
			'change #bt-auth-basic-login' : 'setUsername',
			'change #bt-auth-basic-pwd' : 'setPassword'
		},
		
		initialize : function(options){
			// deal with the case of undefined model 
			if (_.isEmpty(options.model.attributes)){
				options.model.set('username', '');
				options.model.set('password', '');
			}
		},
		
		template: Handlebars.compile(
			'<div class="display-table">' +
				'<div class="display-table-row" style="line-height:3.5">' +
					'<label class="display-table-cell">{{i18n "label.Login"}}</label>' +
					'<input id="bt-auth-basic-login" type="text" class="display-table-cell" value="{{this.username}}">' + 
				'</div>' +
				'<div class="display-table-row" style="line-height:3.5">' +
					'<label class="display-table-cell">{{i18n "label.Password"}}</label> ' +
					'<input id="bt-auth-basic-pwd" class="display-table-cell" type="password" value="{{this.password}}"> ' +
				'</div>' +
			'</div>'
		),
		
		render: function(){
			this.$el.html(this.template(this.model.attributes));	
			return this;
		},
		
		enable: function(){
			this.$el.find('input').prop('disabled', false);
		},
		
		disable: function(){
			this.$el.find('input').prop('disabled', true);
		},
		
		setUsername: function(evt){
			this.model.set('username', evt.target.value);
		},
		
		setPassword: function(evt){
			this.model.set('password', evt.target.value);
		}
		
		
	});
	
	
	
	return CredentialManagerView;

	
});