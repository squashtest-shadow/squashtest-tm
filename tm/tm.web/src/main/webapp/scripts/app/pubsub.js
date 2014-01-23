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
/**
 * Provides the publisher / subscriber pattern 
 * 
 * Adds the publish, subscribe and unsubscribe methods to the global
 * namespace.
 * 
 * 
 * Needs to be bootstrapped by loading the pubsub-boot.js file *before* require kicks in.
 */
define([ "jquery" ], function($) {
	var proxy = $({});

	window.publish = function() {
		proxy.trigger.apply(proxy, arguments);
	};

	window.subscribe = function(event) {
		proxy.on.apply(proxy, arguments);
		
		$(document.eventsQueue).each(function(index) {
			if (this[0] === event) {
				proxy.trigger.apply(proxy, this);
				document.eventsQueue.splice(index, 1);
				return false;
			}
		});
	};

	window.unsubscribe = function() {
		proxy.off.apply(proxy, arguments);
	};

	return {
		publish : window.publish,
		subscribe : window.subscribe,
		unsubscribe : window.unsubscribe
	};
});
