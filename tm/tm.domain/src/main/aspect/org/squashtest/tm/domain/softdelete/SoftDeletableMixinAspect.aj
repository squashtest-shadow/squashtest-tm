/**
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
package org.squashtest.tm.domain.softdelete;

import org.squashtest.tm.domain.softdelete.SoftDeletable;
import org.hibernate.annotations.Filter;
import javax.persistence.Entity;
import java.util.Collection;

/**
 * This aspects adds the {@link SoftDeletableMixin} mixin and soft-deletion related filters to entities maked as @SoftDeletable
 * @author Gregory Fouquet
 *
 */
public aspect SoftDeletableMixinAspect {
	// provide default mixin implementation to @SoftDeletable entities
	declare parents : @SoftDeletable  @Entity * implements SoftDeletableMixin;

// sets soft-delete related filters on entities and fields
declare @type : @SoftDeletable @Entity * : @Filter(name="filter.entity.deleted", condition = "DELETED_ON is null");

declare @field : private (@SoftDeletable @Entity *) (@Entity *).* : @Filter(name="filter.entity.deleted", condition = "DELETED_ON is null");

declare @field : private (Collection<@SoftDeletable @Entity *>+) (@Entity *).* : @Filter(name="filter.entity.deleted", condition = "DELETED_ON is null");
}
