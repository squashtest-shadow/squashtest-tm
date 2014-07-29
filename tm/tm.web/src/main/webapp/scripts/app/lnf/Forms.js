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
 * This module handles form messages.
 */
define([ "jquery" ], function($) {
	function clearState(help, controlGroup) {
		return function() {
			help.hide().addClass("not-displayed").html("&nbsp;");
			controlGroup.removeClass("error").removeClass("warning");

			return this;
		};
	}

	/**
	 * input has 2 methods : clearState setState(cssClass, messageKey)
	 */
	function input($dom) {
		var $input = $dom, 
			$controlGroup = $input.closest(".control-group"), 
			$help = $input.closest(".controls").find(".help-inline");

		/**
		 * Shows the message read from squashtm.app.messages using the given css class
		 */
		var setState = function(state, messageKey) {
			var message = messageKey;
			if (!! squashtm.app.messages) {
				message = squashtm.app.messages[messageKey] || messageKey;
			}

			$controlGroup.removeClass("error").removeClass("warning").addClass(state);

			$help.html(message).hide().fadeIn("slow", function() {
				$(this).removeClass("not-displayed");
			});

			return this;
		};

		return {
			$el : $input,
			clearState : clearState($help, $controlGroup),
			setState : setState,
			hasHelp : $help.length !== 0
		};
	}

	function form($dom) {
		var $form = $dom, $controlGroup = $form.find(".control-group"), $help = $form.find(".help-inline");

		return {
			clearState : clearState($help, $controlGroup),
			input : input
		};
	}

	return {
		form : form,
		input : input
	};
});