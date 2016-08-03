/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

public interface CustomFieldBindingDao extends Repository<CustomFieldBinding, Long>, CustomCustomFieldBindingDao {

	@UsesTheSpringJpaDsl
	CustomFieldBinding findById(long bindingId);
	
	/**
	 * returns the bindings grouped by project and entity, sorted by position
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	// cannot really use the dsl findAllByIdIn(collection) because of the presence of grouping in the desired output
	@EmptyCollectionGuard
	List<CustomFieldBinding> findAllByIds(Collection<Long> ids);
	
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<CustomFieldBinding> findAllForGenericProject(long projectId);

	@UsesANamedQueryInPackageInfoOrElsewhere
	List<CustomFieldBinding> findAllForProjectAndEntity(@Param("projectId") long projectId, @Param("entityType") BindableEntity boundEntity);
	
	@UsesTheSpringJpaDsl
	List<CustomFieldBinding> findAllByCustomFieldIdOrderByPositionAsc(long customFieldId);

	@NativeMethodFromJpaRepository
	void save(CustomFieldBinding binding);
	
	@UsesANamedQueryInPackageInfoOrElsewhere
	Long countAllForProjectAndEntity(long projectId, BindableEntity boundEntity);

	/**
	 * Given an id, returns the list of all the entities binding the same project to the same entity.
	 * 
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<CustomFieldBinding> findAllAlike(long id);

	/**
	 * Given a bound entity, find which custom field bindings are effectively honored.
	 * Indeed there is no constraints on the database that forces an entity to perfectly
	 * match the custom field bindings that were defined at the project level for that entity.
	 * 
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<CustomFieldBinding> findEffectiveBindingsForEntity(@Param("entityId") long entityId,
			@Param("entityType") BindableEntity entityType);


	/**
	 * batched version of {@link #findEffectiveBindingsForEntity(long, BindableEntity)}.
	 * 
	 * The result set is a bit different as it returns a tuple-2 : [ entityId, CustomFieldBinding].
	 * The first element of the array is an entityId, and the second is a CustomFieldBinding.
	 * If an entity (of a given id) has multiple binding actually honored, multiple tuples will be
	 * returned for that entity.
	 * 
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	@EmptyCollectionGuard
	List<Object[]> findEffectiveBindingsForEntities(@Param(ParameterNames.ENTITY_IDS) List<Long> entityIds,
			@Param("entityType") BindableEntity entityType);



}
