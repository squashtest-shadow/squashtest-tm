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

		_create : function() {
			var self = this;

			var parent = this.element.eq(0).parent();

			function cancelOnEsc(event) {
				if (event.keyCode === $.ui.keyCode.ESCAPE) {
					self.cancel(event);
					event.preventDefault();
				}
			}

			this._readDomConf();
			
			// creates the widget
			self._super();

			// declares custom events
			self._on({
				"click .ui-dialog-buttonpane :input" : self._triggerCustom,
				"click .ui-dialog-titlebar-close" : self.cancel,
				"keydown" : cancelOnEsc
			});

			// autoremove when parent container is removed
			parent.on('remove', function() {
				self.element.confirmDialog('destroy');
				self.element.remove();
			});
			
			this._on('formdialogopen', this._cleanup);
			
		},
		
		_cleanup : function(){
			this.element.find(':input,textarea,.error-message').each(function(){
				$(this).val('');
			})
		},

		_createButtons : function(buttons) {

			//ripped from jquery-ui 1.8.13. It might change some day, be careful.
			
			var buttonpane = $(this.element).find('.popup-dialog-buttonpane');
			buttonpane.addClass('ui-dialog-buttonpane ui-widget-content ui-helper-clearfix').wrapInner('<div class="ui-dialog-buttonset"></div>');
			buttonpane.find('input:button').button().appendTo('.ui-dialog-buttonset', buttonpane);
			
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
			this._super();
		},
		
		_readDomConf : function(){
			var $dialog = this;
			var $elt = $(this.element);		
			var handlers = $.squash.formDialog.domconf;
			
			$elt.find('[data-def]').each(function(){
				
				var $elt = $(this);
				var raw = $elt.data('def');
				var conf = attrparser.parse(raw);
				
				var handler;
				for (var key in conf){
					handler = handlers[key];
					if (handler!==undefined){
						handler.call($dialog, $elt, conf[key]);
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
			this.element.keypress(function(evt){
				if (evt.which=='13'){
					$elt.click();
				}
			});
		},
		'evt' : function($elt, value){
			$elt.data('evt', value);
		}
	}
});