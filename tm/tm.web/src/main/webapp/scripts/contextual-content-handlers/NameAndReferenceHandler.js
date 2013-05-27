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
define([ 'jquery' ], function($) {

	return function() {

		this.identity = undefined;

		this.nameDisplay = undefined;

		this.nameHidden = undefined;

		this.referenceHidden = undefined;

		this._updateDisplay = function() {

			var name = $(this.nameHidden).text();
			var reference = $.trim($(this.referenceHidden).text());

			if (reference.length > 0) {
				reference += " - ";
			}
			

			$(this.nameDisplay).text(reference + name);
		};

		this._rename = function(newName) {
			$(this.nameHidden).text(newName);
			this._updateDisplay();
		};

		this._updateReference = function(newRef) {
			$(this.referenceHidden).text(newRef);
			this._updateDisplay();
		};

		this.isMe = function(target) {
			return (this.identity.obj_id == target.obj_id)
					&& (this.identity.obj_restype == target.obj_restype);
		};

		this.update = function(evt) {
			switch (evt.evt_name) {
			case "rename":
				if (this.isMe(evt.evt_target)) {
					this._rename(evt.evt_newname);
				}
				
				break;

			case "update-reference":
				if (this.isMe(evt.evt_target)) {
					this._updateReference(evt.evt_newref);
				}
				
				break;
			default:
				break;
			}
		};

	};

});