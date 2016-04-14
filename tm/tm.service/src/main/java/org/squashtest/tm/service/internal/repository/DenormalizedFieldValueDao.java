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

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolderType;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

/**
 * Data access methods for {@link DenormalizedFieldValue}. Methods are all dynamically generated: see
 * {@link DynamicDaoFactoryBean}.
 *
 * @author mpagnon
 *
 */

public interface DenormalizedFieldValueDao extends Repository<DenormalizedFieldValue, Long> {

	public static final String PARAM_ENTITY_TYPE = "entityType";
	public static final String PARAM_ENTITY_ID = "entityId";

	@NativeMethodFromJpaRepository
	void save(DenormalizedFieldValue newValue);

	// note : uses the Spring JPA dsl
	DenormalizedFieldValue findById(long denormalizedFieldHolderId);

	/**
	 * Delete all the denormalized field values related to a DenormalizedFieldHolder, identified by its id and
	 * DenormalizedFieldHolderType
	 *
	 * @param denormalizedFieldHolderId
	 * @param denormalizedFieldHolderType
	 * @deprecated does not seem to be used as per 1.14
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	@Modifying
	@Transactional
	@Deprecated
	void deleteAllForEntity(@Param(PARAM_ENTITY_ID) long denormalizedFieldHolderId,
							@Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType denormalizedFieldHolderType);

	/**
	 * Return all denormalized field values related to the denormalizedFieldHolder matching params. The list is ordered
	 * by position asc.
	 *
	 * @param denormalizedFieldHolderId
	 * @param denormalizedFieldHolderType
	 * @return the list of corresponding {@link DenormalizedFieldValue} ordered by position asc.
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<DenormalizedFieldValue> findDFVForEntity(@Param(PARAM_ENTITY_ID) long denormalizedFieldHolderId,
												  @Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType denormalizedFieldHolderType);

	/**
	 * Return all denormalized field values related to the denormalizedFieldHolder matching params. The list is ordered
	 * by position asc.
	 *
	 * @param denormalizedFieldHolderId
	 * @param denormalizedFieldHolderType
	 * @param renderingLocation
	 * @return
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<DenormalizedFieldValue> findDFVForEntityAndRenderingLocation(
		@Param(PARAM_ENTITY_ID) long denormalizedFieldHolderId,
		@Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType denormalizedFieldHolderType,
		@Param("renderingLocation") RenderingLocation renderingLocation);


	@UsesANamedQueryInPackageInfoOrElsewhere
	@EmptyCollectionGuard
	List<DenormalizedFieldValue> findDFVForEntities(@Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType type, @Param(ParameterNames.ENTITY_IDS) Collection<Long> entities);


	@UsesANamedQueryInPackageInfoOrElsewhere
	@EmptyCollectionGuard
	List<DenormalizedFieldValue> findDFVForEntitiesAndLocations(
		@Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType denormalizedFieldHolderType,
		@Param(ParameterNames.ENTITY_IDS) Collection<Long> entities,
		@Param("locations") Collection<RenderingLocation> locations);

	@UsesANamedQueryInPackageInfoOrElsewhere
	public long countDenormalizedFields(@Param(PARAM_ENTITY_ID) long entityId, @Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType entityType);

}
