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
package org.squashtest.tm.domain.customfield;

import java.text.ParseException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.lang.IsoDateUtils;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.exception.customfield.BindableEntityMismatchException;
import org.squashtest.tm.exception.customfield.MandatoryCufException;

@Entity
public class CustomFieldValue implements Identified {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomFieldValue.class);

	@Id
	@GeneratedValue
	@Column(name="CFV_ID")
	private Long id;
	
	private Long boundEntityId;

	@Enumerated(EnumType.STRING)
	private BindableEntity boundEntityType;
	

	@ManyToOne
	@JoinColumn(name="CFB_ID")
	private CustomFieldBinding binding;
		
	@Size(min = 0, max = 255)
	private String value;
	
	
	
	public CustomFieldValue(){
		super();
	}
	
	
	public CustomFieldValue(Long boundEntityId, BindableEntity boundEntityType,
			CustomFieldBinding binding, String value) {
		super();
		this.boundEntityId = boundEntityId;
		this.boundEntityType = boundEntityType;
		this.binding = binding;
		this.value = value;
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		if(getCustomField() != null && !getCustomField().isOptional() && StringUtils.isBlank(value)){
			throw new MandatoryCufException(this);
		}
		this.value = value;
	}

	public CustomFieldBinding getBinding() {
		return binding;
	}
	
	public CustomField getCustomField(){
		if(binding != null){
			return binding.getCustomField();
		}
		return null;
	}


	public void setBinding(CustomFieldBinding binding) {
		this.binding = binding;
	}

	public Long getBoundEntityId(){
		return boundEntityId;
	}

	public BindableEntity getBoundEntityType(){
		return boundEntityType;
	}

	
	public void setBoundEntity(BoundEntity entity){
		if (entity.getBoundEntityType() != binding.getBoundEntity()){
			throw new BindableEntityMismatchException("attempted to bind '"+entity.getBoundEntityType()+"' while expected '"+binding.getBoundEntity()+"'");
		}
		this.boundEntityId=entity.getBoundEntityId();
		this.boundEntityType=entity.getBoundEntityType();
	}
	
	public CustomFieldValue copy(){
		CustomFieldValue copy = new CustomFieldValue();
		copy.setBinding(binding);
		copy.setValue(this.value);
		return copy;
	}
	
	public boolean representsSameBinding(CustomFieldValue otherValue){
		return otherValue.getBinding().getId().equals(binding.getId());
	}
	
	public boolean representsSameCustomField(CustomFieldValue otherValue){
		return otherValue.getCustomField().getId().equals(getCustomField().getId());
	}
	
	public Date getValueAsDate(){
		if(getCustomField() != null && getCustomField().getInputType() == InputType.DATE_PICKER){
			try {
				return IsoDateUtils.parseIso8601Date(value);
			} catch (ParseException e) {
				LOGGER.warn("Unable to parse date '" + value + "' of custom field value #" + id, e);
			}
		}
		return null;
		
	}
}
