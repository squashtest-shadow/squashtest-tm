/**
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
package org.squashtest.tm.service.internal.repository;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.core.dynamicmanager.annotation.DynamicDao;
import org.squashtest.tm.core.dynamicmanager.annotation.QueryParam;
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
@DynamicDao(entity = DenormalizedFieldValue.class)
public interface DenormalizedFieldValueDao extends CustomDenormalizedFieldValueDao {

	void persist(DenormalizedFieldValue newValue);

	DenormalizedFieldValue findById(long denormalizedFieldHolderId);

	/**
	 * Delete all the denormalized field values related to a DenormalizedFieldHolder, identified by its id and
	 * DenormalizedFieldHolderType
	 * 
	 * @param denormalizedFieldHolderId
	 * @param denormalizedFieldHolderType
	 */
	void deleteAllForEntity(@QueryParam("entityId") long denormalizedFieldHolderId,
			@QueryParam("entityType") DenormalizedFieldHolderType denormalizedFieldHolderType);

	/**
	 * Return all denormalized field values related to the denormalizedFieldHolder matching params. The list is ordered
	 * by position asc.
	 * 
	 * @param denormalizedFieldHolderId
	 * @param denormalizedFieldHolderType
	 * @return the list of corresponding {@link DenormalizedFieldValue} ordered by position asc.
	 */
	List<DenormalizedFieldValue> findDFVForEntity(@QueryParam("entityId") long denormalizedFieldHolderId,
			@QueryParam("entityType") DenormalizedFieldHolderType denormalizedFieldHolderType);

	/**
	 * Return all denormalized field values related to the denormalizedFieldHolder matching params. The list is ordered
	 * by position asc.
	 * 
	 * @param denormalizedFieldHolderId
	 * @param denormalizedFieldHolderType
	 * @param renderingLocation
	 * @return
	 */
	List<DenormalizedFieldValue> findDFVForEntityAndRenderingLocation(
			@QueryParam("entityId") long denormalizedFieldHolderId,
			@QueryParam("entityType") DenormalizedFieldHolderType denormalizedFieldHolderType,
			@QueryParam("renderingLocation") RenderingLocation renderingLocation);


	List<DenormalizedFieldValue> findDFVForEntities(@QueryParam("entityType") DenormalizedFieldHolderType type, @QueryParam("entityIds") Collection<Long> entities);


	List<DenormalizedFieldValue> findDFVForEntitiesAndLocations(
			@QueryParam("entityType") DenormalizedFieldHolderType denormalizedFieldHolderType,
			@QueryParam("entityIds") Collection<Long> entities,
			@QueryParam("locations") Collection<RenderingLocation> locations);

	public long countDenormalizedFields(@QueryParam("entityId") long entityId, @QueryParam("entityType") DenormalizedFieldHolderType entityType);

}
