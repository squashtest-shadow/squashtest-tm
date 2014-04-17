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
/*
 that file will compile every little random plugins/redefinitions etc we need to stuff jQuery with.

 */

var squashtm = squashtm || {};

(function($) {
	// custom selectors, eg $(tree).find(":folder") will select all the nodes
	// corresponding to folders.

	$.extend($.expr[':'], {
		library : function(a) {
			return $(a).is("[rel='drive']");
		},
		folder : function(a) {
			return $(a).is("[rel='folder']");
		},
		'test-case' : function(a) {
			return $(a).is("[rel='test-case']");
		},
		requirement : function(a) {
			return $(a).is("[rel='requirement']");
		},
		campaign : function(a) {
			return $(a).is("[rel='campaign']");
		},
		iteration : function(a) {
			return $(a).is("[rel='iteration']");
		},
		'test-suite' : function(a) {
			return $(a).is("[rel='test-suite']");
		},
		// ***************** legacy *****************
		node : function(a) {
			return $(a).is(":folder, :test-case, :requirement, :campaign");
		},
		resource : function(a) {
			return $(a).is("[rel='iteration']");
		},
		view : function(a) {
			return $(a).is("[rel='test-suite']");
		},
		// ****************** /legacy *****************
		editable : function(a) {
			return $(a).attr('editable') === 'true';
		},
		creatable : function(a) {
			return $(a).attr('creatable') === 'true';
		},
		deletable : function(a) {
			return $(a).attr('deletable') === 'true';
		},
		exportable : function(a){
			return $(a).attr('exportable') === 'true';
		}
	});

	// convenient function to gather data of a jQuery object.
	$.fn.collect = function(fnArg) {
		var res = [];
		if (this.length > 0) {
			this.each(function(index, elt) {
				res.push(fnArg(elt));
			});
		}
		return res;

	};

	$.fn.contains = function(domElt) {
		var vThis = this.collect(function(e) {
			return e;
		});

		for ( var e in vThis) {
			if (vThis[e] === domElt) {
				return true;
			}
		}

		return false;

	};

	/*
	 * thanks to
	 * http://stackoverflow.com/questions/2360655/jquery-event-handlers-always-execute-in-order-they-were-bound-any-way-around-t
	 * for fixing me
	 */
	$.fn.bindFirst = function(name, selector, data, fn) {

		this.on(name, selector, data, fn);

		this.each(function() {
			var handlers = $._data(this, 'events')[name.split('.')[0]];
			var handler = handlers.pop();
			handlers.splice(0, 0, handler);
		});
	};

	/* defines functions in the jQuery namespace */
	$.extend({
		/**
		 * Opens a "popup" window containing the result of a POST. Plain window.open() can only GET
		 * 
		 * @param url
		 *            the url to POST
		 * @param data
		 *            the post data as a javascript object
		 * @param windowDef
		 *            definition of the window to open : { name: "name of window", features: "features string as per
		 *            window.open" }
		 * @return reference to the new window
		 */
		open : function(url, data, windowDef) {
			var postData = '';

			for ( var attr in data) {
				postData += '<input type=\"hidden\" name=\"' + attr + '\" value=\"' + data[attr] + '\" />';
			}

			var form = '<form id=\"postForm\" style=\"display:none;\" action=\"' + url + '\" method=\"post\">' +
					'<input type=\"submit\" name=\"postFormSubmit\" value=\"\" />' + postData + '</form>';

			var win = window.open("about:blank", windowDef.name, windowDef.features);
			win.document.write(form);
			win.document.forms.postForm.submit();

			return win;
		}
	});
	/**
	 * Creates a preconfigured popup dialog from the selector. settings definition : see squashtm.popup(settings)
	 * function
	 */
	$.fn.createPopup = function(settings) {
		var target = this;
		target.addClass("popup-dialog");

		var defaults = {
			autoOpen : false,
			resizable : false,
			modal : true,
			width : 600,
			position : [ 'center', 100 ],
			usesRichEdit : true
		};

		// merge the settings into the defaults;
		$.extend(true, defaults, settings);

		// add default open, close and create behaviour. The user
		// defined callbacks will be invoked as a last operation if
		// any.
		var userOpen = defaults.open;
		var userClose = defaults.close;
		var userCreate = defaults.create;

		defaults.open = function() {
			// // cleanup
			// squashtm.popup.cleanup.call(target);
			// forcible styling of the buttons
			var buttons = target.eq(0).next().find('button');
			buttons.filter(':last').addClass('ui-state-active');
			buttons.filter(':first').removeClass('ui-state-active');
			// user code
			if (!!userOpen) {
				userOpen.call(this);
			}
		};

		defaults.close = function() {
			// cleanup
			squashtm.popup.cleanup.call(target);
			// usercode
			if (!!userClose) {
				userClose.call(this);
			}
		};

		defaults.create = function() {
			if (defaults.usesRichEdit) {
				target.find('textarea').each(function() {
					var jqT = $(this);
					if (settings.isContextual) {
						jqT.addClass('is-contextual');
					}
					jqT.ckeditor(function() {
					}, {
						// in this context
						// 'this' is the
						// defaults
						// object
						// the following
						// properties will
						// appear
						// once we merged with
						// the user-provided
						// settings
						customConfig : settings.ckeditor.styleUrl || "/styles/ckeditor/ckeditor-config.js",
						language : settings.ckeditor.lang || "en"
					});
				});
				if (!!userCreate) {
					userCreate.call(this);
				}
			}
		};

		// popup invokation
		target.dialog(defaults);

		if (defaults.closeOnSuccess === undefined || defaults.closeOnSuccess) {
			target.ajaxSuccess(function() {
				if (target.dialog('isOpen') === true) {
					target.dialog('close');
				}
			});
		}

		target.keypress(function(event) {
			if (event.which == '13') {
				var buttonPane = target.eq(0).next();
				buttonPane.find('button').filter(':first').click();
			}
		});

		if (defaults.openedBy) {
			$(defaults.openedBy).click(function() {
				target.dialog('open');
				return false;
			});
		}

		if (defaults.isContextual) {
			target.addClass('is-contextual');
		}

		// hook on remove : remove the instances of CKEditor
		if (defaults.usesRichEdit) {
			target.on('remove', function() {
				target.find('textarea').each(function() {
					var ckInstance = CKEDITOR.instances[this.id];
					if (ckInstance) {
						ckInstance.destroy(true);
					}
				});
			});
		}

		return this;
	};

	/*
	 * Squash TM domain name : variable $.fn.squashtm
	 * 
	 */
	/**
	 * squashtm.popup(settings) : creates a popup dialog from the given settings
	 * 
	 * popup settings : - all normal $.ui.dialog valid options - selector : jquery selector of the dom element we are
	 * targetting (mandatory) - openedBy : selector for a clickable element that will open the popup (optional) - title :
	 * the title of the popup (mandatory) - isContextual : boolean telling if the said popup should be added the special
	 * class 'is-contextual', that will mark him as a removable popup when the context changes, - closeOnSuccess :
	 * boolean telling if the popup should be closed if an ajax request succeeds (optional) - ckeditor : { - lang : the
	 * desired language for the ckeditor (optional) - styleUrl : the url for the ckeditor style. } - buttons : the
	 * button definition (mandatory)
	 * 
	 * 
	 */
	squashtm.popup = {
		// begin popup.create
		create : function(settings) {
			var target = $(settings.selector);
			target.createPopup(settings);
		}, // end popup.create
		// begin popup.cleanup
		cleanup : function() {
			// clear all error-message even ones that are not in this popup
			// because others might have been filled with error handle.
			$('.error-message').text('');
			// clear inputs
			this.find('input:text').val('');
			this.find('textarea').val('');
		}
	// end popup.cleanup
	};



	$.extend({
		
		/*
		 * inhibits navigation to previous page when pressing backspace, as requested in issue
		 * https://ci.squashtest.org/mantis/view.php?id=2069
		 * 
		 * Solution credited to erikkallen, at
		 * http://stackoverflow.com/questions/1495219/how-can-i-prevent-the-backspace-key-from-navigating-back
		 * 
		 * Kudos my good sir.
		 * 
		 */
		
		noBackspaceNavigation : function() {
			$(document).bind('keydown', function(event) {
				var doPrevent = false;
				if (event.keyCode === 8) {
					var d = event.srcElement || event.target;
					if ((d.tagName.toUpperCase() === 'INPUT' && (d.type.toUpperCase() === 'TEXT' || d.type
							.toUpperCase() === 'PASSWORD')) ||
							d.tagName.toUpperCase() === 'TEXTAREA') {
						doPrevent = d.readOnly || d.disabled;
					} else {
						doPrevent = true;
					}
				}

				if (doPrevent) {
					event.preventDefault();
				}
			});
		}
	});

})(jQuery);