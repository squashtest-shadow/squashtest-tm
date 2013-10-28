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
 * Event bus used for communications between different parts of the workspace.
 * 
 * At the moment this documentation was written you can listen and publish events in two ways :
 * - using the old school API : see addPermanentListener(), addContextualListener(), fire()
 * - using the new API : onContextual() + the rest of jquery API
 * 
 * 
 * 
 * A method is of special significance : clearContextualListeners(). When the contextual content is flushed, 
 * every listeners living in that part of the document must be unregistered. When clearContextualListeners() 
 * is invoked, the bus will trigger for all listeners the event 'contextualcontent.clear', 
 * then remove the contextual listeners. A contextual listener is a listener that had been registered
 * using addContextualListener() or onContextual() .
 * 
 * See code for the rest.
 * 
 * 
 * TODO : migrate the shitty old API I designed some times ago 
 * to jQuery or Backbone events. Also, the Backbone object is a fine candidate
 * for event bus-ing as it extends Backbone.Event. 
 * 
 */

define([ 'jquery' ], function($) {

	squashtm = squashtm || {};
	squashtm.workspace = squashtm.workspace || {};

	if (squashtm.workspace.eventBus !== undefined) {
		return squashtm.workspace.eventBus;
	} 
	else {

		squashtm.workspace.eventBus = $.extend($({}), {

			// *********** for old school API : *************
			
			oldschool_listeners : [],
			oldschool_permanentListeners : [],
			
			// listeners registered here stay as long as the page
			addPermanentListener : function(listener) {
				this.oldschool_listeners.push(listener);
				this.oldschool_permanentListeners.push(listener);
			},
			
			// listeners registered here are wiped when event 'contextualcontent.clear'
			// is triggered because they don't belong to the permanent listeners
			addContextualListener : function(listener){
				this.oldschool_listeners.push(listener);
			},
						
			fire : function(origin, event) {
				
				//jquery event
				this.trigger(event.evt_name, event);
				
				// all school events
				this._fire(origin, event);
				
			},
			
			_fire : function(origin, event){				
				
				for ( var i=0, len = this.oldschool_listeners.length; i<len;i++) {
					var listener = this.oldschool_listeners[i];
					if (listener !== origin) {
						listener.update(event);
					}
				}
			},
			
			// ********************* new API ***********
			

			newschool_contextualListeners : [],
			
			// Registers listeners that will be removed when 'contextualcontent.clear' is fired.
			// All but the 'data' parameter of the method call will be saved for later reference.
			// The 'data' parameter will be omitted because it never appears in the signature of jquery .off()
			onContextual : function(){
				
				// prepare to register the parameters of the future call to .off();
				var dataParamIndex = this._findDataIndex.apply(this, arguments);			
				var offParams = Array.prototype.slice.call(arguments, 0);
				if (dataParamIndex > -1){
					offParams.splice(dataParamIndex, 1);
				}

				this.newschool_contextualListeners.push(offParams);	
				
				// now register the event
				this.on.apply(this, arguments);
				
	
			},
			
			/* 
			 * jquery .on() signature : .on(events [, selector ] [, data ], handler(eventObject))
			 * 
			 * Finding if there is a 'data' parameter is tricky because it can be in second or third position in the parameter list, 
			 * depending on things like what are the other parameters and their types. The following code is adapted from jQuery 1.8.3. 
			 */
			_findDataIndex : function(types, selector, data, fn){
				var index = -1;
				
				//case : the events is a hash
				if (typeof types === "object"){
					
					if (!! data ){
						// case (hash, selector/null/undefined, data) 
						index = 2;
					}
					else if (typeof selector !== "string"){
						// case (hash, data)
						index = 1;
					}
					
				}
				
				else if ( !! data && fn === undefined && selector !== "string" ){
					// case (types, data, fn)
					index = 1;
				}
				
				else if (!! fn && !! data ){
					// case (types, selector, data, handler)
					index = 2;
				}
				
				return index;
			},
			
			clearContextualListeners : function(){
						
				// notify then wipe the oldschool contextual listeners, this is done by reassigning 
				// the permanent listeners only.
				this._fire(null, { evt_name : 'contextualcontent.clear'} );
				
				this.oldschool_listeners = this.oldschool_permanentListeners.slice(0);
				
				// notify then wipe the jquery style contextual listeners, then empty the list of contextual listeners :
				this.trigger('contextualcontent.clear');
				
				var offparams = this.newschool_contextualListeners;
				for (var i=0;i<offparams.length;i++){
					var params = offparams[i]; 
					this.off.apply(this, params);
				}
				
				this.newschool_contextualListeners = [];
			}
			
		});

		
		return squashtm.workspace.eventBus;

	}

});
