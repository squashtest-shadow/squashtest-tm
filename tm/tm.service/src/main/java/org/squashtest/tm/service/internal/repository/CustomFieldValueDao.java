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

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.*;

import java.util.Collection;
import java.util.List;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

public interface CustomFieldValueDao extends Repository<CustomFieldValue, Long> {
	String ENTITY_TYPE = "entityType";

	/**
	 * 'nuff said.
	 *
	 * @param newValue
	 */
	@NativeMethodFromJpaRepository
	void save(CustomFieldValue newValue);


	/**
	 * 'nuff said.
	 *
	 * @param value
	 */
	@NativeMethodFromJpaRepository
	void delete(CustomFieldValue value);


	/**
	 * Delete all the CustomFieldValue, given their ids.
	 *
	 * @param ids
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	@Modifying
	@Transactional
	@EmptyCollectionGuard	
	void deleteAll(@Param("ids") List<Long> ids);


	/**
	 * Delete all the CustomFieldValue related to a {@link CustomFieldBinding}, given its id.
	 *
	 * @param bindingId
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	@Modifying
	@Transactional
	void deleteAllForBinding(@Param("bindingId") Long bindingId);


	/**
	 * Delete all the custom field values related to a BoundEntity, identified by its id and BindableEntity
	 *
	 * @param entityId
	 * @param entity
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	@Modifying
	@Transactional
	void deleteAllForEntity(@Param("entityId") Long entityId, @Param("entityType") BindableEntity entity);


	/**
	 * Delete all the custom field values related to a bunch of bound entities
	 *
	 * @param entityIds
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	@Modifying
	@Transactional
	@EmptyCollectionGuard
	void deleteAllForEntities(@Param(ENTITY_TYPE) BindableEntity entityType, @Param(ParameterNames.ENTITY_IDS) List<Long> entityIds);


	/**
	 * 'nuff said.
	 *
	 * @param id
	 * @return
	 */
	// note : uses the Spring JPA dsl
	CustomFieldValue findById(Long id);


	/**
	 * returns the list of {@link CustomFieldValue} for the given entity, sorted according to the
	 * order specified in their respective {@link CustomFieldBinding}.
	 *
	 * @param entityId
	 * @param entityType
	 * @return
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<CustomFieldValue> findAllCustomValues(@Param("entityId") long entityId, @Param("entityType") BindableEntity entityType);


	/**
	 * Same as above, list version.
	 *
	 * @param entityIds
	 * @param entityType
	 * @return
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	@EmptyCollectionGuard
	List<CustomFieldValue> batchedFindAllCustomValuesFor(@Param(ParameterNames.ENTITY_IDS) Collection<Long> entityIds, @Param(ENTITY_TYPE) BindableEntity entityType);


	/**
	 * Same as above, and initialiazes the bindings and custom fields.
	 *
	 * @param entityIds
	 * @param entityType
	 * @return
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	@EmptyCollectionGuard
	List<CustomFieldValue> batchedInitializedFindAllCustomValuesFor(@Param(ParameterNames.ENTITY_IDS) List<Long> entityIds, @Param(ENTITY_TYPE) BindableEntity entityType);


	/**
	 * Same as above, will restrict to the custom fields specified as arguments
	 *
	 * @return
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	@EmptyCollectionGuard	
	List<CustomFieldValue> batchedRestrictedFindAllCustomValuesFor(@Param(ParameterNames.ENTITY_IDS) List<Long> entityIds,
																   @Param(ENTITY_TYPE) BindableEntity entityType,
																   @Param("customFields") Collection<CustomField> customFields);


	/**
	 * returns all the {@link CustomFieldValue} related to a given {@link CustomFieldBinding}, sorted according to
	 * their custom field binding order.
	 *
	 * @param customFieldBindingId
	 * @return
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<CustomFieldValue> findAllCustomValuesOfBinding(@Param("bindingId") long customFieldBindingId);


	/**
	 * returns all the CustomFieldValue related to a list of CustomFieldBinding, the resulting elements will be
	 * returned in unspecified order
	 *
	 * @param customFieldBindingIds
	 * @return
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	@EmptyCollectionGuard
	List<CustomFieldValue> findAllCustomValuesOfBindings(@Param("bindingIds") List<Long> customFieldBindingIds);


	/**
	 * Will return instances of {@link CustomFieldValuesPair}, that will pair two {@link CustomFieldValue} that represents the same
	 * CustomFieldBinding. Those two CustomFieldValue belongs to two BoundEntity as specified by the parameters.
	 * One of them is considered as the original and the other one is the copy.
	 *
	 * @param entity
	 * @param origEntityId
	 * @param copyEntityId
	 * @return
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<CustomFieldValuesPair> findPairedCustomFieldValues(@Param(ENTITY_TYPE) BindableEntity entity,
															@Param("origEntityId") Long origEntityId, @Param("copyEntityId") Long copyEntityId);


	/**
	 * return the custom field value matchine the given params.
	 *
	 * @param customFieldBindingId : the id of the {@link CustomFieldBinding}
	 * @param boundEntityId        : the id of the {@link BoundEntity}
	 * @param bindableEntity       : the type of the {@link BoundEntity}
	 * @return
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<CustomFieldValue> findAllCustomFieldValueOfBindingAndEntity(
		long customFieldBindingId,
		long boundEntityId,
		BindableEntity bindableEntity);


	@UsesANamedQueryInPackageInfoOrElsewhere
	Long findBoundEntityId(@Param("customFieldValueId") Long customFieldValueId);


	@UsesANamedQueryInPackageInfoOrElsewhere
	List<CustomFieldValue> findAllForEntityAndRenderingLocation(@Param("entityId") long entityId, @Param("entityType") BindableEntity entityType, @Param("location") RenderingLocation renderingLocation);

	@UsesANamedQueryInPackageInfoOrElsewhere
	@EmptyCollectionGuard
	List<String> findAllAvailableTagForEntityInProjects(@Param("boundEntityType") BindableEntity boundEntityType, @Param("projectsIds") List<Long> projectIds);



	// **************************** inner classes *********************************

	final class CustomFieldValuesPair {

		private CustomFieldValue original;
		private CustomFieldValue recipient;

		public CustomFieldValuesPair() {
			super();
		}

		public CustomFieldValuesPair(CustomFieldValue original,
									 CustomFieldValue recipient) {
			super();
			this.original = original;
			this.recipient = recipient;
		}


		public void setOriginal(CustomFieldValue original) {
			this.original = original;
		}

		public void setRecipient(CustomFieldValue recipient) {
			this.recipient = recipient;
		}


		public CustomFieldValue getOriginal() {
			return original;
		}

		public CustomFieldValue getRecipient() {
			return recipient;
		}

		/**
		 * copies the value of the original CustomFieldValue into the value of the recipient CustomFieldValue
		 */
		public void copyContent() {
			RawValue rawValue = original.asRawValue();
			rawValue.setValueFor(recipient);
		}
	}


}
