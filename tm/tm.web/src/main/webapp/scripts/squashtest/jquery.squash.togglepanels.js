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
(function($) {
	// the über-new toggle-panel: each .sq-tg is automatically turned into a toggle panel
	$(document).on("click", ".sq-tg .tg-head", function(event) {
		var $target = $(event.target);

		if ($target.parents(".tg-toolbar").length > 0) {
			// click from within the toolbar -> bail out
			return;
		} // else do the toggling.

		event.stopImmediatePropagation();

		var $panel = $target.parents(".sq-tg");
	
		$panel.find(".tg-body").toggle("blind", 500, function() {
			$panel.toggleClass("collapse");
			$panel.toggleClass("expand");

			$panel.find(".tg-toolbar .sq-btn").prop("disabled", $panel.is(".collapse"));
		});

	});

	// turns .sq-tl into toggle lists
	$(document).on("click", ".sq-tl .tl-head", function(event) {
		var $target = $(event.target);
		event.stopImmediatePropagation();
		var $panel = $target.parent(".sq-tl");

		$panel.find(".tl-body").toggle("blind", 500, function() {
			$panel.toggleClass("collapse");
			$panel.toggleClass("expand");
		});

	});

	$.widget("ui.togglePanel",{

		options : {
			initiallyOpen : true,
			title : undefined
		},

		_create : function() {
			var widget = this;

			this.element.each(function(){
				var $elt = $(this);

				if ($elt.hasClass('toggle-panel-initialized')){
					return true;	//AKA 'continue'
				}

				var prerendered = $elt.data('prerendered');
				if (prerendered !== true){
					widget._createDom($elt);
				}

				var panelHead = $elt.prev(),
					wrapper = $elt.parent('div.toggle-panel');

				// buttons
				panelHead.find('.snap-right')
							.children()
							.filter('input.button')
							.squashButton()
							.click(function(event) {
								event.stopPropagation();
							});


				// click event
				panelHead.click(function(event) {
					var $target = $(event.target);
					// don't toggle if the click was targetted at some kind of button
					// TODO maybe better, enforce a .btn-group panel and check that instead
					if ($target.is("input[type='button'], button, a")) {
						return;
					}

					event.stopImmediatePropagation();
					widget.toggleContent();
				});

				$elt.addClass('toggle-panel-initialized');

			});

		},

		_createDom : function($maindiv){
			var title = $maindiv.attr('title') || this.options.title;

			var initiallyOpen = $maindiv.data('init-open');
			if (initiallyOpen===undefined){
				initiallyOpen = this.options.initiallyOpen;
			}

			// build the necessary components
			var panelHead = $('<h3/>',{
				'class' : "ui-accordion-header ui-helper-reset ui-state-default"
			});

			var titlepanel = $('<div/>', {
				'style' : "overflow:hidden;"
			});
			var snapleft = $('<div class="snap-left"><a class="tg-link">'+ title +'</a></div>');
			var snapright = $('<div/>', {
				'class' : "snap-right"
			});

			/*
			 * find the wrapper or create it if not exists. It's
			 * best if the wrapper exists, because inserting the
			 * content into it won't be necessary. This will
			 * prevent
			 * the double javascript execution bug, see #1291
			 */
			var wCreate;
			var wrapper = $maindiv.parent('div.toggle-panel');
			if (wrapper.length > 0) {
				wrapper.addClass("ui-accordion ui-widget ui-helper-reset ui-accordion-icons");
				wCreate = false;
			} else {
				wrapper = $('<div/>', {
					'class' : "toggle-panel ui-accordion ui-widget ui-helper-reset ui-accordion-icons"
				});
				wCreate = true;
			}

			// finish the creation of the structure
			$maindiv.addClass("ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom ui-accordion-content-active");

			titlepanel.append(snapleft).append(snapright);
			panelHead.append(titlepanel);

			var buttons = wrapper.find('.toggle-panel-buttons').children();
			panelHead.find('.snap-right').append(buttons);

			if (wCreate) {
				$maindiv.wrap(wrapper);
			}

			panelHead.insertBefore($maindiv);

			if (initiallyOpen){
				panelHead.addClass('tg-open ui-state-focus ui-corner-top');
			}
			else{
				$maindiv.addClass('not-displayed');
				panelHead.addClass('ui-state-active ui-corner-all');
			}


		},



		toggleContent : function() {

			// skip if already being toggled
			if (this.element.parent().hasClass('ui-effects-wrapper')) {
				return;
			}

			var panelHead = this.element.prev();

			this.element.toggle('blind', 500);
			panelHead.toggleClass("ui-state-focus ui-state-active ui-corner-top ui-corner-all tg-open");

			// now disable or enable the buttons.
			var disabled = (panelHead.hasClass('tg-open')) ? false : true;
			panelHead.find(':button, .button').squashButton("option", "disabled", disabled);
		},

		openContent : function() {
			var panelHead = this.element.prev();
			if (!panelHead.hasClass('tg-open')) {
				this.toggleContent();
			}
		},

		closeContent : function() {
			var panelHead = this.element.prev();
			if (panelHead.hasClass('tg-open')) {
				this.toggleContent();
			}
		}
	});
}(jQuery));
