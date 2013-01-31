/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.tm.web.internal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.testcase.ActionStepCollector;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.customfield.CustomFieldValueManagerService;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldJsonConverter;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldModel;
import org.squashtest.tm.web.internal.service.CustomFieldHelperService.Helper;



/**
 * Read the definition of {@link Helper} instead
 * 
 * 
 * @author bsiri
 *
 */

@Component
public class CustomFieldHelperService {

	private CustomFieldBindingFinderService cufBindingService;

	private CustomFieldValueManagerService cufValuesService;
	

	@Inject
	private CustomFieldJsonConverter converter;
	
	@ServiceReference
	public void setCustomFieldBindingFinderService(CustomFieldBindingFinderService service){
		this.cufBindingService=service;
	}
	
	@ServiceReference
	public void setManagerService(CustomFieldValueManagerService cufValuesService) {
		this.cufValuesService = cufValuesService;
	}
	



	// *************************** the public helper class backed by this service *****************************
	
	
	/**
	 * The goal of a helper is to hide all the logic regarding the custom fields, so as to make the task for 
	 * the controller as light as possible. 
	 */
	public class Helper<X extends BoundEntity> {
		
		private Collection<X> entities;
		private Collection<RenderingLocation> locations;
		
		private CustomFieldDefinitionStrategy addingStrategy = CustomFieldDefinitionStrategy.INTERSECTION;
		
		private List<CustomField> customFields;
		private List<CustomFieldValue> values;
		
		
		public Helper(X entity){
			this.entities = new ArrayList<X>();
			this.entities.add(entity);
		}
		
		public Helper(Collection<X> entities){
			this.entities = entities;
		}
		
		public Helper<X> setRenderingLocations(RenderingLocation... locations){
			this.locations = Arrays.asList(locations);
			return this;
		}
		
		public Helper<X> setRenderingLocations(Collection<RenderingLocation> locations){
			this.locations = locations;
			return this;
		}
		
		
		/**
		 * tells the helper to retain only the custom fields that are common to all the entities (in case they come from mixed projects, or are 
		 * of mixed concrete classes)
		 * 
		 * @return this object
		 */
		public Helper<X> restrictToCommonFields(){
			addingStrategy = CustomFieldDefinitionStrategy.INTERSECTION;
			return this;
		}
		
		/**
		 * tells the helper to include every custom fields it finds.
		 * 
		 * @return
		 */
		public Helper<X> includeAllCustomFields(){
			addingStrategy = CustomFieldDefinitionStrategy.UNION;
			return this;
		}
		

		/**
		 * sorted by position, filtered by location.
		 * 
		 * @return
		 */
		public List<CustomFieldModel> getCustomFieldConfiguration(){
			if (! isInited()){
				init();
			}
			
			return convertToJson(customFields);
			
		}
		
		
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
			
			CollectionUtils.filter(bindings, new BindingLocationFilter(optionalLocations));
			
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
			
			List<CustomFieldValue> values = cufValuesService.findAllCustomFieldValues(entities, customFields);

			return values;
		}

		

		private List<CustomFieldModel> convertToJson(Collection<CustomField> customFields){
			List<CustomFieldModel> models = new ArrayList<CustomFieldModel>(customFields.size());
			for (CustomField field : customFields){
				models.add(converter.toJson(field));
			}
			return models;
		}
		
		
		private <Y>  void retainUniques(Collection<Y> argument){
			Set<Y> set = new LinkedHashSet<Y>(argument);
			argument.clear();
			argument.addAll(set);
		}
		
	}
	
	
	// ************************** service methods **********************************
	
	
	public boolean hasCustomFields(BoundEntity entity){
		return cufValuesService.hasCustomFields(entity);
	}
	
	
	public <X extends BoundEntity> Helper<X> newHelper(X entity){
		return new Helper<X>(entity);
	}
	
	public <X extends BoundEntity> Helper<X> newHelper(List<X> entities){
		return new Helper<X>(entities);
	}
	
	public Helper<ActionTestStep> newStepsHelper(List<TestStep> steps){
		return new Helper<ActionTestStep> (new ActionStepCollector().collect(steps));
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
		
	}
	
	
	private static final class BindingSorter implements Comparator<CustomFieldBinding>{
		@Override
		public int compare(CustomFieldBinding o1, CustomFieldBinding o2) {
			return o1.getPosition() - o2.getPosition();
		}
	}
	
	private static final class BindingLocationFilter implements Predicate{

		private Collection<RenderingLocation> locations;
		
		BindingLocationFilter( Collection<RenderingLocation>  locations){
			this.locations = locations;
		}
		
		@Override
		public boolean evaluate(Object arg0) {
			CustomFieldBinding binding = (CustomFieldBinding)arg0;
			return (CollectionUtils.containsAny(locations, binding.getRenderingLocations()));
		}
		
	}
	
	private static final class ValueLocationFilter implements Predicate{

		private Collection<RenderingLocation> locations;
		
		ValueLocationFilter( Collection<RenderingLocation>  locations){
			this.locations = locations;
		}
		
		
		@Override
		public boolean evaluate(Object arg0) {
			CustomFieldValue value = (CustomFieldValue)arg0;
			return (CollectionUtils.containsAny(locations, value.getBinding().getRenderingLocations()));
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
}
