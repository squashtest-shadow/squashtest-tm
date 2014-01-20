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
function SquashEventObject(id, restype) {
	this.obj_id = id;
	this.obj_restype = restype;
}

function SquashEvent(name) {
	this.evt_name = name;
}

// 'paste' event, inheriting base event
function EventPaste(destination) {
	this.base = SquashEvent;
	this.base("paste");
	this.evt_destination = destination;
}
EventPaste.prototype = new SquashEvent();

// 'duplicate' event, inheriting paste
function EventDuplicate(destination, duplicate, source) {
	this.base = EventPaste;
	this.base(destination);
	this.evt_duplicate = duplicate;
	this.evt_source = source;
}
EventDuplicate.prototype = new EventPaste();

// 'rename' event, inheriting base event
function EventRename(target, newName) {
	this.base = SquashEvent;
	this.base('rename');
	this.evt_target = target;
	this.evt_newname = newName;
}

EventRename.prototype = new SquashEvent();

// 'update reference', inheriting base event
function EventUpdateReference(target, newReference) {
	this.base = SquashEvent;
	this.base('update-reference');
	this.evt_target = target;
	this.evt_newref = newReference;
}

EventUpdateReference.prototype = new SquashEvent();

//'update category', inheriting base event
function EventUpdateCategory(target, newCategory) {
	this.base = SquashEvent;
	this.base('update-category');
	this.evt_target = target;
	this.evt_newcat = newCategory;
}

EventUpdateCategory.prototype = new SquashEvent();

//'update-status', inheriting base event
function EventUpdateStatus(target, newStatus) {
	this.base = SquashEvent;
	this.base('update-status');
	this.evt_target = target;
	this.evt_newstatus = newStatus;
}

EventUpdateStatus.prototype = new SquashEvent();

//'update-importance', inheriting base event
function EventUpdateImportance(target, newImportance) {
	this.base = SquashEvent;
	this.base('update-importance');
	this.evt_target = target;
	this.evt_newimpt = newImportance;
}

EventUpdateImportance.prototype = new SquashEvent();

//'update-reqCoverage', inheriting base event
function EventUpdateReqCoverage(target) {
	this.base = SquashEvent;
	this.base('update-reqCoverage');
	this.evt_target = target;
}

EventUpdateReqCoverage.prototype = new SquashEvent();