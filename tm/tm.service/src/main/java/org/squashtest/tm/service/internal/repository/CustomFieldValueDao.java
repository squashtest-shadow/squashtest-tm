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
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;

@DynamicDao(entity = CustomFieldValue.class)
public interface CustomFieldValueDao extends CustomCustomFieldValueDao{
	String ENTITY_TYPE = "entityType";

	/**
	 * 'nuff said.
	 * @param newValue
	 */
	void persist(CustomFieldValue newValue);



	/**
	 * 
	 * 'nuff said.
	 * @param value
	 */
	void delete(CustomFieldValue value);


	/**
	 * Delete all the CustomFieldValue, given their ids.
	 * 
	 * @param ids
	 */
	void deleteAll(@QueryParam("ids") List<Long> ids);



	/**
	 * Delete all the CustomFieldValue related to a {@link CustomFieldBinding}, given its id.
	 * 
	 * @param bindingId
	 */
	void deleteAllForBinding(@QueryParam("bindingId") Long bindingId);


	/**
	 * Delete all the custom field values related to a BoundEntity, identified by its id and BindableEntity
	 * 
	 * @param entityId
	 * @param entity
	 */
	void deleteAllForEntity(@QueryParam("entityId") Long entityId, @QueryParam("entityType") BindableEntity entity);


	/**
	 * Delete all the custom field values related to a bunch of bound entities
	 * 
	 * @param entityTpe
	 * @param entityIds
	 */
	void deleteAllForEntities(@QueryParam(ENTITY_TYPE) BindableEntity entityType, @QueryParam("entityIds") List<Long> entityIds);



	/**
	 * 'nuff said.
	 * @param id
	 * @return
	 */
	CustomFieldValue findById(Long id);



	/**
	 * returns the list of {@link CustomFieldValue} for the given entity, sorted according to the
	 * order specified in their respective {@link CustomFieldBinding}.
	 * 
	 * @param entityId
	 * @param entityType
	 * @return
	 */
	List<CustomFieldValue> findAllCustomValues(long entityId, BindableEntity entityType);


	/**
	 * Same as above, list version.
	 * 
	 * @param entityIds
	 * @param entityType
	 * @return
	 */
	List<CustomFieldValue> batchedFindAllCustomValuesFor(@QueryParam("entityIds") Collection<Long> entityIds, @QueryParam(ENTITY_TYPE) BindableEntity entityType);


	/**
	 * Same as above, and initialiazes the bindings and custom fields.
	 * 
	 * @param entityIds
	 * @param entityType
	 * @return
	 */
	List<CustomFieldValue> batchedInitializedFindAllCustomValuesFor(@QueryParam("entityIds") List<Long> entityIds, @QueryParam(ENTITY_TYPE) BindableEntity entityType);
	
	
	/**
	 * Same as above, will restrict to the custom fields specified as arguments
	 * 
	 * @return
	 */
	List<CustomFieldValue> batchedRestrictedFindAllCustomValuesFor(@QueryParam("entityIds") List<Long> entityIds,
			@QueryParam(ENTITY_TYPE) BindableEntity entityType,
			@QueryParam("customFields") Collection<CustomField> customFields);


	/**
	 * returns all the {@link CustomFieldValue} related to a given {@link CustomFieldBinding}, sorted according to
	 * their custom field binding order.
	 * 
	 * @param customFieldBindingId
	 * @return
	 */
	List<CustomFieldValue> findAllCustomValuesOfBinding(long customFieldBindingId);


	/**
	 * returns all the CustomFieldValue related to a list of CustomFieldBinding, the resulting elements will be
	 * returned in unspecified order
	 * @param customFieldBindingIds
	 * @return
	 */
	List<CustomFieldValue> findAllCustomValuesOfBindings(@QueryParam("bindingIds") List<Long>customFieldBindingIds);


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
	List<CustomFieldValuesPair> findPairedCustomFieldValues(@QueryParam(ENTITY_TYPE) BindableEntity entity,
			@QueryParam("origEntityId") Long origEntityId, @QueryParam("copyEntityId") Long copyEntityId);


	/**
	 * return the custom field value matchine the given params.
	 * 
	 * @param customFieldBindingId : the id of the {@link CustomFieldBinding}
	 * @param boundEntityId : the id of the {@link BoundEntity}
	 * @param bindableEntity : the type of the {@link BoundEntity}
	 * @return
	 */
	List<CustomFieldValue> findAllCustomFieldValueOfBindingAndEntity(
			long customFieldBindingId,
			long boundEntityId,
			BindableEntity bindableEntity);


	public static final class CustomFieldValuesPair{

		private CustomFieldValue original;
		private CustomFieldValue recipient;

		public CustomFieldValuesPair(){
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
		public void copyContent(){
			recipient.setValue(original.getValue());
		}
	}


	Long findBoundEntityId(@QueryParam("customFieldValueId") Long customFieldValueId);


	List<CustomFieldValue> findAllForEntityAndRenderingLocation(long entityId, BindableEntity entityType, RenderingLocation renderingLocation);


}
