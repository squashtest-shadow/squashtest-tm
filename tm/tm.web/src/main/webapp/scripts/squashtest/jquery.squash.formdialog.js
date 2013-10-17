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
 *		<div>
 *			<p>this is my main content. Because it isn't a 'state' div (read below), it will always be displayed. 
 *				It is basically equivalent state=default
 *			</p>
 * 
 *			<textarea data-def="isrich">will be turned into rich editable</textarea>
 * 
 *			<textarea>will remain a regular textarea</textarea>
 *		</div>
 * 
 *		<div data-def="state=content-1">
 *			<p>either this is displayed</p>
 *		</div>
 * 
 *		<div data-def="state=content-2">
 *			<p>or that</p>
 *		</div>
 *
 *		<div class="popup-dialog-buttonpane">
 *			<input type="button" value="ok"						data-def="evt=confirm, mainbtn"/>
 *			<input type="button" value="cancel"					data-def="evt=cancel" />
 *			<input type="button" value="specific to content1"	data-def="state=content-1" />
 *			<input type="button" value="specific to content2"	data-def="state=content-2" />
 *		</div>
 * 
 *	</div>
 * 
 * 
 *	========== behaviour ================
 * 
 *	1/ the buttons aren't created like regular popup : the same dom objects are litteraly reused. This can help you
 *	with the logic of the popup. HOWEVER, they are moved around, which may lead to the loss of callbacks on those buttons
 *	if they were bound before the dialog was initialized.
 * 
 *	2/ the popup will be automatically destroyed and removed whenever the container it was initially declared in
 *	is removed. Not more 'is-contextual' bullshit !
 * 
 *	3/ all the inputs defined in this dialog will be cleaned up automatically whenever the dialog is opened again.
 * 
 *	4/ a popup can define several alternative content that are displayed one at a time, representing a state. 
 *	Displaying one will automatically hide the other alternatives. 
 *	Those elements are declared using data-def="state=<state-id>", or directly using class="popup-dialog-state-<state-id>". 
 *	See the API for details (setState()) and configuration for details.
 * 
 *	============= API ====================
 * 
 *	1/ cleanup : force the cleanup of the controls
 * 
 *	2/ setState(id) : will show anything configured 'state=<state id>' and hide the other ones.
 *
 *  3/ onOwnBtn(evtname, handler) : for inheritance purposes. This allows subclasses of this dialog
 *					to listen to their own button event defined using evt=<eventname> (see DOM configuration). 
 *  
 *	========= configuration ==============
 * 
 *	1/ basic : all basic options of jQuery dialog are valid here, EXCEPT for the buttons.
 *
 *
 *	2/ DOM conf : reads parts of the conf from the datatable, see the handlers at the end of the document for details.
 *	for now, supports : 
 * 
 *	- isrich : for textarea. If set, will be turned into a ckeditor.
 *	- evt=<eventname> : for buttons. If set, clicking on that button will trigger <eventname> on the dialog.
 *	- state=<state id>[ <stat id> ...] : for any elements in the popup. Multiple elements can declare the same <state-id> and they'll
 *						be logically bound when setState(<state-id>) is invoked. Note that a single element can belong
 *						to multiple state either by a space-separated list of states,  either declaring this 'state' clause multiple times.
 *
 *	- mainbtn[=<state-id>] : for buttons. If set, pressing <ENTER> inside the dialog will trigger 'click' on that button if the popup is in that
 *						current state. the <state-id> is optional : if left blank, the button will be triggered if the popup is in the default 
 *						state.
 * 
 */

define([ 'jquery', 'squash.attributeparser', 'squash.configmanager', 'jqueryui' ], function($, attrparser, confman) {

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
			_internalEvents : {},
			_state : "default",
			_richeditors : [],
			_mainBtns : {}
		},

		_triggerCustom : function(event) {
			var evtname = $(event.target).data('evt');
			this._trigger(evtname);
			this._triggerInternal(evtname);
			return this;
		},
		
		_triggerInternal : function(evtname){
			var listeners = this.options._internalEvents[evtname];
			if (listeners!==undefined){
				for (var i=0,len = listeners.length; i<len;i++){
					listeners[i]();
				}
			}
		},
		
		onOwnBtn : function(evtname, handler){
			var listeners = this.options._internalEvents[evtname];
			if (listeners === undefined){
				listeners = this.options._internalEvents[evtname] = [];
			}
			listeners.push(handler);
		},

		cancel : function(event) {
			if (!this.close()) {
				return;
			}

			this._trigger("cancel");
			return this;
		},

		// if the argument is unknown, will default to state "default"
		setState : function(state) {
			this.uiDialog.find('[class*="popup-dialog-state"]').hide();

			var tobedisplayed = this.uiDialog.find('.popup-dialog-state-' + state);
			tobedisplayed.show();

			this.options._state = (tobedisplayed.length === 0) ? "default" : state;
		},
		
		getState : function(){
			return this.options._state;
		},

		_create : function() {
			var self = this;

			var parent = this.element.eq(0).parent();

			function keyshortcuts(event) {
				switch (event.keyCode) {
				case $.ui.keyCode.ESCAPE:
					self.cancel(event);
					event.preventDefault();
					break;

				case $.ui.keyCode.ENTER:
					var state = self.options._state;
					var btn = self.options._mainBtns[state];

					if (btn !== undefined) {
						btn.click();
					}
					break;

				default:
					return;
				}
			}

			// creates the widget
			self._super();
			
			// extend the conf with the domconf on the root element
			var def = this.element.data('def');
			if (!!def){
				var conf = attrparser.parse(def);
				$.extend(this.options, conf);
			}

			// read and apply dom conf
			this._readDomConf();

			// declares custom events
			self._on({
				"click .ui-dialog-buttonpane :input" : self._triggerCustom,
				"click .ui-dialog-titlebar-close" : self.cancel
			});

			// autoremove when parent container is removed
			parent.on('remove', function() {
				self.destroy('destroy');
				self.element.remove();
			});

			this.uiDialog.keydown(keyshortcuts);

		},

		open : function() {
			this.cleanup();
			this._super();

		},

		cleanup : function() {
			this.element.find(':input,textarea').each(function() {
				$(this).val('');
			});
			this.element.find('.error-message').each(function(){
				$(this).text('');
			});
		},

		_createButtons : function() {

			// ripped from jquery-ui 1.8.13. It might change some day, be careful.
			var buttonpane = this.uiDialog.find('.popup-dialog-buttonpane');
			buttonpane.addClass('ui-dialog-buttonpane ui-widget-content ui-helper-clearfix').wrapInner(
					'<div class="ui-dialog-buttonset"></div>');
			var buttons = buttonpane.find('input:button').button();
			buttonpane.find('.ui-dialog-buttonset').append(buttons);

			// the following line will move the buttonpane after the body of the popup.
			buttonpane.appendTo(this.uiDialog);

		},

		// negation of the above. Untested yet.
		_destroyButtons : function() {
			var buttonpane = this.uiDialog.find('.popup-dialog-buttonpane');
			buttonpane.removeClass('ui-dialog-buttonpane ui-widget-content ui-helper-clearfix');
			buttonpane.find('input:button').button('destroy').appendTo(buttonPane);
			buttonpane.find('div.ui-dialog-buttonset').remove();

			// move the buttonpane back to the body.
			this.element.append(buttonpane);

		},

		_setOption : function(key, value) {
			// In jQuery UI 1.8, you have to manually invoke the _setOption method from the base widget
			$.Widget.prototype._setOption.apply(this, arguments);
		},

		_destroyCked : function() {
			var i = 0, len = this._richeditors.length;
			for (i = 0; i < len; i++) {
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
			this._destroyCked();
			this._destroyButtons();
			this._super();
		},

		_readDomConf : function() {
			var $widget = this;
			var handlers = $.squash.formDialog.domconf;

			$widget.uiDialog.find('[data-def]').each(function() {

				var $elt = $(this);
				var raw = $elt.data('def');
				var conf = attrparser.parse(raw);

				var handler;
				for ( var key in conf) {

					handler = handlers[key];
					if (handler !== undefined) {
						handler.call($widget, $elt, conf[key]);
					}
				}
			});
		}

	});

	$.squash.formDialog.domconf = {
		'isrich' : function($elt, value) {
			this.options._richeditors.push($elt);
			var conf = confman.getStdChkeditor();
			$elt.ckeditor(function() {
			}, conf);
		},

		'mainbtn' : function($elt, value) {
			if (value === true){
				this.options._mainBtns["default"] = $elt;
			}
			else{
				var values = $.trim(value).split(' ');
				for (var i=0, len = values.length; i<len;i++){
					this.options._mainBtns[values] = $elt;
				}
			}
		},

		'evt' : function($elt, value) {
			$elt.data('evt', value);
		},

		'state' : function($elt, value) {
			var values = $.trim(value).split(' ');
			for (var i=0,len = values.length; i<len;i++){
				$elt.addClass('popup-dialog-state-' + values[i]);
			}
		}
	};
});