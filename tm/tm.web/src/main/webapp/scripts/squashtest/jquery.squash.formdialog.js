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
 * Documentation  : 
 * 
 * ======== structure ===========
 * 
 * Contrary to regular jQuery dialogs, the button pane is declared explicitely. Here is a sample formdialog, note 
 * the use of css classes and data-def :
 * 
 * <div id="mydialog" class="popup-dialog">
 * 
 * 		<div>
 * 			<p>this is my main content. Because it isn't a xor-content div (read below), it will always be displayed.</p>
 * 
 * 			<textarea data-def="isrich">will be turned into rich editable</textarea>
 * 
 * 			<textarea>will remain a regular textarea</textarea>
 * 		</div>
 * 
 * 		<div data-def="xor-content=content-1">
 * 			<p>either this is displayed</p></div>
 * 
 * 		<div data-def="xor-content=content-2">
 * 			<p>or that</p>
 * 		</div>
 * 	
 * 		<div class="popup-dialog-buttonpane">
 * 			<input type="button" value="ok" 					data-def="evt=confirm, mainbtn"/>
 * 			<input type="button" value="cancel" 				data-def="evt=cancel" />
 * 			<input type="button" value="specific to content1" 	data-def="xor-content=content-1" />
 * 			<input type="button" value="specific to content2" 	data-def="xor-content=content-2" />
 * 		</div>
 * 
 * </div>
 * 
 * 
 * ========== behaviour ================
 * 
 * 1/ the buttons aren't created like regular popup : the same dom objects are litteraly reused. This can help you
 * with the logic of the popup. HOWEVER, they are moved around, which may lead to the loss of callbacks on those buttons
 * if they were bound before the dialog was initialized.
 * 
 * 2/ the popup will be automatically destroyed and removed whenever the container it was initially declared in
 * is removed. Not more 'is-contextual' bullshit !
 * 
 * 3/ all the inputs defined in this dialog will be cleaned up automatically whenever the dialog is opened again.
 * 
 * 4/ a popup can define several alternative content that are displayed one at a time. Displaying one will automatically hide the other alternatives. 
 * 	  Those elements are declared using data-def="xor-content=<content-id>", or directly using class="popup-dialog-xor-content-<content-id>". 
 *    See the API for details (showContent) and configuration for details.
 * 
 * ============= API ====================
 * 
 * 1/ cleanup : force the cleanup of the controls
 * 
 * 2/ showContent(id) : will show anything configured 'xor-content=<content id>' and hide the other ones.
 *  
 * ========= configuration ==============
 * 
 * 1/ basic : all basic options of jQuery dialog are valid here, EXCEPT for the buttons.
 * 
 * 2/ more conf :{
 * 		extender : {
 * 			an object that will be merged to 'this' when the popup is created. Can be used to override some functions too.
 * 		}
 *  }
 * 
 * 
 * 3/ DOM conf : reads parts of the conf from the datatable, see the handlers at the end of the document for details.
 * for now, supports : 
 * - isrich : for textarea. If set, will be turned into a ckeditor.
 * - evt=<eventname> : for buttons. If set, clicking on that button will trigger <eventname> on the dialog.
 * - mainbtn : for buttons. If set, pressing <ENTER> inside the dialog will trigger 'click' on that button.
 * - xor-content=<content id> : for any elements in the popup. Multiple elements can declare the same <content-id> and they'll
 * 								be logically bound when showContent(<content-id>) is invoked. Note that a single element can belong
 * 								to multiple xor-content.
 * 
 * 
 */

define(['jquery', 'squash.attributeparser', 'squash.configmanager', 'jqueryui'], function($, attrparser, confman){

	if (($.squash !== undefined) && ($.squash.formDialog !== undefined)) {
		// plugin already loaded
		return;
	}
	
	
	$.widget("squash.formDialog", $.ui.dialog, {
		
		options : {
			autoOpen : false,
			resizable : false,
			modal : true,
			width : 600,
			position : [ 'center', 100 ],
			extender : {}
		},
		

		_richeditors : [],

		_triggerCustom : function(event) {
			var evtname = $(event.target).data('evt');
			this._trigger(evtname);
			return this;
		},

		cancel : function(event) {
			if (!this.close()) {
				return;
			}

			this._trigger("cancel");
			return this;
		},
		
		showContent : function(contentId){
			this.uiDialog.find('[class*="popup-dialog-xor-content"]').hide().end().find('.popup-dialog-xor-content-'+contentId).show();
		},

		_create : function() {
			var self = this;

			var parent = this.element.eq(0).parent();

			function cancelOnEsc(event) {
				if (event.keyCode === $.ui.keyCode.ESCAPE) {
					self.cancel(event);
					event.preventDefault();
				}
			}
			// creates the widget
			self._super();
			
			//read and apply dom conf
			this._readDomConf();
			

			// declares custom events
			self._on({
				"click .ui-dialog-buttonpane :input" : self._triggerCustom,
				"click .ui-dialog-titlebar-close" : self.cancel,
				"keydown" : cancelOnEsc
			});

			// autoremove when parent container is removed
			parent.on('remove', function() {
				self.element.formDialog('destroy');
				self.element.remove();
			});
			
			//show the first xor-content if any.
			this._activateFirstContent();
			
			//extensions/overrides
			var extender = this.options.extender;
			$.extend(this, extender);

		},
	
		open : function(){
			this.cleanup();
			this._super();
			
		},

		cleanup : function(){
			this.element.find(':input,textarea,.error-message').each(function(){
				$(this).val('');
			})
		},
		
		_activateFirstContent : function(){
			var elt = this.uiDialog.find('[class*="popup-dialog-xor-content"]:first');
			if (elt.length===0){
				return ;
			}
			var classes = elt.attr('class');
			var contentId = /popup-dialog-xor-content-(\S*)/.exec(classes);
			this.showContent(contentId);
		},

		_createButtons : function() {

			//ripped from jquery-ui 1.8.13. It might change some day, be careful.
			var buttonpane = this.uiDialog.find('.popup-dialog-buttonpane');
			buttonpane.addClass('ui-dialog-buttonpane ui-widget-content ui-helper-clearfix').wrapInner('<div class="ui-dialog-buttonset"></div>');
			var buttons = buttonpane.find('input:button').button();
			buttonpane.find('.ui-dialog-buttonset').append(buttons);
			
			//the following line will move the buttonpane after the body of the popup.
			buttonpane.appendTo(this.uiDialog);
			
		},
		
		//negation of the above. Untested yet.
		_destroyButtons : function(){
			var buttonpane = this.uiDialog.find('.popup-dialog-buttonpane');
			buttonpane.removeClass('ui-dialog-buttonpane ui-widget-content ui-helper-clearfix');
			buttonpane.find('input:button').button('destroy').appendTo(buttonPane);
			buttonpane.find('div.ui-dialog-buttonset').remove();
			
			//move the buttonpane back to the body.
			this.element.append(buttonpane);
			
		},

		_setOption : function(key, value) {
			// In jQuery UI 1.8, you have to manually invoke the _setOption method from the base widget
			$.Widget.prototype._setOption.apply(this, arguments);
		},
		
		_destroyCked : function(){
			var i = 0, len= this._richeditors.length;
			for (i=0;i<len;i++){
				var domelt = this._richeditors[i].get(0);
				var ckInstance = CKEDITOR.instances[domelt.id];
				if (ckInstance) {
					ckInstance.destroy(true);
				}				
			}
		},

		_destroy : function() {
			this._off($(".ui-dialog-buttonpane button"), "click");
			this._off($(".ui-dialog-titlebar-close"), "click");
			this._off(this.element, 'keypress');
			this._destroyCked();
			this._destroyButtons();
			this._super();
		},
		
		_readDomConf : function(){
			var $widget = this;		
			var handlers = $.squash.formDialog.domconf;
			
			$widget.uiDialog.find('[data-def]').each(function(){
				
				var $elt = $(this);
				var raw = $elt.data('def');
				var conf = attrparser.parse(raw);
				
				var handler;
				for (var key in conf){
					handler = handlers[key];
					if (handler!==undefined){
						handler.call($widget, $elt, conf[key]);
					}
				}
				
			});
		}
		
	});
	
	$.squash.formDialog.domconf = {
		'isrich' : function($elt, value){
			this._richeditors.push($elt);
			var conf = confman.getStdChkeditor();
			$elt.ckeditor(function(){}, conf);
		},
		'mainbtn' : function($elt, value){
			function callback(evt){
				if (evt.which=='13'){
					$elt.click();
				}
			};
			this.uiDialog.keypress(callback);
		},
		'evt' : function($elt, value){
			$elt.data('evt', value);
		},
		'xor-content' : function($elt, value){
			$elt.addClass('popup-dialog-xor-content-'+value);
		}
	}
});