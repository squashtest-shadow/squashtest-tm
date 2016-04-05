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

import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.core.dynamicmanager.factory.DynamicDaoFactoryBean;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolderType;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;

/**
 * Data access methods for {@link DenormalizedFieldValue}. Methods are all dynamically generated: see
 * {@link DynamicDaoFactoryBean}.
 * 
 * @author mpagnon
 * 
 */
@RepositoryDefinition(domainClass=DenormalizedFieldValue.class, idClass = Long.class)
public interface DenormalizedFieldValueDao {

	public static final String PARAM_ENTITY_TYPE = "entityType";
	public static final String PARAM_ENTITY_ID = "entityId";

	// note : native method from JPA repositorie
	void save(DenormalizedFieldValue newValue);

	// note : uses the Spring JPA dsl 
	DenormalizedFieldValue findById(long denormalizedFieldHolderId);

	/**
	 * Delete all the denormalized field values related to a DenormalizedFieldHolder, identified by its id and
	 * DenormalizedFieldHolderType
	 * 
	 * @param denormalizedFieldHolderId
	 * @param denormalizedFieldHolderType
	 */
	// note : uses a named query in package-info or elsewhere
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
	// note : uses a named query in package-info or elsewhere
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
	// note : uses a named query in package-info or elsewhere
	List<DenormalizedFieldValue> findDFVForEntityAndRenderingLocation(
			@Param(PARAM_ENTITY_ID) long denormalizedFieldHolderId,
			@Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType denormalizedFieldHolderType,
			@Param("renderingLocation") RenderingLocation renderingLocation);


	// note : uses a named query in package-info or elsewhere
	List<DenormalizedFieldValue> findDFVForEntities(@Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType type, @Param(ParameterNames.ENTITY_IDS) Collection<Long> entities);


	// note : uses a named query in package-info or elsewhere
	List<DenormalizedFieldValue> findDFVForEntitiesAndLocations(
			@Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType denormalizedFieldHolderType,
			@Param(ParameterNames.ENTITY_IDS) Collection<Long> entities,
			@Param("locations") Collection<RenderingLocation> locations);

	// note : uses a named query in package-info or elsewhere
	public long countDenormalizedFields(@Param(PARAM_ENTITY_ID) long entityId, @Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType entityType);

}
