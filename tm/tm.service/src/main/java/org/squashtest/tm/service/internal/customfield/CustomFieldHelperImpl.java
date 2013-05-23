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
package org.squashtest.tm.service.internal.customfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.customfield.CustomFieldHelper;
import org.squashtest.tm.service.customfield.CustomFieldValueManagerService;

public class CustomFieldHelperImpl<X extends BoundEntity> implements CustomFieldHelper<X> {

	
	// *********** backing services *************
	
	private CustomFieldBindingFinderService cufBindingService;
	
	private CustomFieldValueManagerService cufValuesService;
	
	// ************ attributes ******************
	
	private Collection<X> entities;
	private Collection<RenderingLocation> locations;
	
	private CustomFieldDefinitionStrategy addingStrategy = CustomFieldDefinitionStrategy.INTERSECTION;
	
	private List<CustomField> customFields;
	private List<CustomFieldValue> values;
	
	
	
	// ************* code ************************
	
	public CustomFieldHelperImpl(X entity){
		this.entities = new ArrayList<X>();
		this.entities.add(entity);
	}
	
	public CustomFieldHelperImpl(Collection<X> entities){
		this.entities = entities;
	}
	
	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.internal.customfield.CustomFieldHelper#setRenderingLocations(org.squashtest.tm.domain.customfield.RenderingLocation)
	 */
	@Override
	public CustomFieldHelper<X> setRenderingLocations(RenderingLocation... locations){
		this.locations = Arrays.asList(locations);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.internal.customfield.CustomFieldHelper#setRenderingLocations(java.util.Collection)
	 */
	@Override
	public CustomFieldHelper<X> setRenderingLocations(Collection<RenderingLocation> locations){
		this.locations = locations;
		return this;
	}
	
	
	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.internal.customfield.CustomFieldHelper#restrictToCommonFields()
	 */
	@Override
	public CustomFieldHelper<X> restrictToCommonFields(){
		addingStrategy = CustomFieldDefinitionStrategy.INTERSECTION;
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.internal.customfield.CustomFieldHelper#includeAllCustomFields()
	 */
	@Override
	public CustomFieldHelper<X> includeAllCustomFields(){
		addingStrategy = CustomFieldDefinitionStrategy.UNION;
		return this;
	}
	

	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.internal.customfield.CustomFieldHelper#getCustomFieldConfiguration()
	 */
	@Override
	public List<CustomField> getCustomFieldConfiguration(){
		if (! isInited()){
			init();
		}
		
		return customFields;
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.internal.customfield.CustomFieldHelper#getCustomFieldValues()
	 */
	@Override
	public List<CustomFieldValue> getCustomFieldValues(){
		
		if (! isInited()){
			init();
		}
		
		if (values == null){
			
			values = findRestrictedCustomFieldValues(entities, customFields);
			
		}
		
		return values;
		
	}
	
	// ******************* utilities **************************

	private boolean isInited(){
		return customFields!=null;
	}
	
	private void init(){
		if (! entities.isEmpty()){			
			
			//restrict the number of queries we must perform : 1 per pair of (project, bindableentity)
			Collection<BindingTarget> targets = CollectionUtils.collect(entities, new BindingTargetCollector());
			retainUniques(targets);
			
			customFields = new ArrayList<CustomField>(); 
			
			//collect the result
			for (BindingTarget target : targets){
				customFields = addingStrategy.add(customFields,  
									findCustomFields(target.getProjectId(), target.getBindableEntity(), locations)
								);

			}
			
			//eliminate multiple occurences
			retainUniques(customFields);
		}
		else{
			customFields = Collections.emptyList();
		}			
	}
	
	
	/**
	 * Return the CustomFields referenced by the CustomFieldBindings for the given project and BindableEntity type, ordered by their position. 
	 * The location argument is optional, if set then only the custom fields that are rendered in at least one of these locations will be returned.
	 * 
	 * @param projectId
	 * @param entityType
	 * @return
	 */
	private List<CustomField> findCustomFields(long projectId, BindableEntity entityType, Collection<RenderingLocation> optionalLocations){
		
		List<CustomFieldBinding> bindings = cufBindingService.findCustomFieldsForProjectAndEntity(projectId, entityType);
		
		Collections.sort(bindings, new BindingSorter());
		
		if (optionalLocations!=null && ! optionalLocations.isEmpty()){
			CollectionUtils.filter(bindings, new BindingLocationFilter(optionalLocations));
		}
		
		return (List<CustomField>) CollectionUtils.collect(bindings, new BindingFieldCollector());

	}
	

	
	/**
	 * returns the flattened collection of custom fields associated to all the entities in arguments, restricted to only the supplied customfields.
	 * 
	 * @param entities
	 * @param optionalLocations
	 * @return
	 */
	private List<CustomFieldValue> findRestrictedCustomFieldValues(Collection<? extends BoundEntity> entities,  
																   Collection<CustomField> customFields){
		
		if (entities.isEmpty() || customFields.isEmpty()){
			return Collections.emptyList();
		}
		
		List<CustomFieldValue> cufValues = cufValuesService.findAllCustomFieldValues(entities, customFields);

		return cufValues;
	}


	
	private <Y>  void retainUniques(Collection<Y> argument){
		Set<Y> set = new LinkedHashSet<Y>(argument);
		argument.clear();
		argument.addAll(set);
	}
	
	
	

	// *************************** utility classes **********************************
	
	
	private enum CustomFieldDefinitionStrategy{
		
		INTERSECTION(){
			@Override
			List<CustomField>  add(List<CustomField> orig, List<CustomField> addition) {
				if (orig.isEmpty()){
					return addition;
				}
				else{
					return new ArrayList<CustomField>(CollectionUtils.intersection(orig, addition));
				}
			}
		},
		UNION(){
			@Override
			List<CustomField>  add(List<CustomField> orig, List<CustomField> addition) {
				orig.addAll(addition);
				return orig;
			}
		};
		
		abstract List<CustomField> add(List<CustomField> orig, List<CustomField> addition);
	}
	
	
	private static class BindingTarget{
	
		private Long projectId;
		private BindableEntity bindableEntity;
		
		BindingTarget(BoundEntity entity){
			this.projectId = entity.getProject().getId();
			this.bindableEntity=entity.getBoundEntityType();
		}
		
		public Long getProjectId() {
			return projectId;
		}
		
		public BindableEntity getBindableEntity() {
			return bindableEntity;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((bindableEntity == null) ? 0 : bindableEntity.hashCode());
			result = prime * result
					+ ((projectId == null) ? 0 : projectId.hashCode());
			return result;
		}

		@Override//NOSONAR  code generation, assumed to be safe
		public boolean equals(Object obj) {//NOSONAR
			if (this == obj)//NOSONAR
				return true;//NOSONAR
			if (obj == null)//NOSONAR
				return false;//NOSONAR
			if (getClass() != obj.getClass())//NOSONAR
				return false;//NOSONAR
			BindingTarget other = (BindingTarget) obj;//NOSONAR
			if (bindableEntity != other.bindableEntity)//NOSONAR
				return false;//NOSONAR
			if (projectId == null) {//NOSONAR
				if (other.projectId != null)//NOSONAR
					return false;//NOSONAR
			} else if (!projectId.equals(other.projectId))//NOSONAR
				return false;//NOSONAR
			return true;//NOSONAR
		}
		
		
		
		
	}
	
	
	private static final class BindingSorter implements Comparator<CustomFieldBinding>{
		@Override
		public int compare(CustomFieldBinding o1, CustomFieldBinding o2) {
			return o1.getPosition() - o2.getPosition();
		}
	}
	
	private static final class BindingLocationFilter implements Predicate{

		private Collection<RenderingLocation> locations;
		private boolean automaticallyPassed = false;
		
		BindingLocationFilter( Collection<RenderingLocation>  locations){
			this.locations = locations;
			automaticallyPassed = (locations == null || locations.isEmpty());
		}
		
		@Override
		public boolean evaluate(Object binding) {
			return automaticallyPassed ||			
				  (CollectionUtils.containsAny(locations, ((CustomFieldBinding)binding).getRenderingLocations()));
		}
		
	}

	private static final class BindingFieldCollector implements Transformer{

		@Override
		public Object transform(Object arg0) {
			CustomFieldBinding binding = (CustomFieldBinding) arg0;
			return binding.getCustomField();
		}
		
	}
	
	private static final class BindingTargetCollector implements Transformer{
		@Override
		public Object transform(Object arg0) {
			return new BindingTarget((BoundEntity) arg0);
		}
	}

	void setCufBindingService(CustomFieldBindingFinderService cufBindingService) {
		this.cufBindingService = cufBindingService;
	}

	void setCufValuesService(CustomFieldValueManagerService cufValuesService) {
		this.cufValuesService = cufValuesService;
	}
	
	
	
	
}
