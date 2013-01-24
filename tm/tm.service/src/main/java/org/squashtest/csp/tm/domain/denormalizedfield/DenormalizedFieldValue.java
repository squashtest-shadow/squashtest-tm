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
package org.squashtest.csp.tm.domain.denormalizedfield;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.domain.customfield.CustomFieldValue;
import org.squashtest.csp.tm.domain.customfield.InputType;
import org.squashtest.csp.tm.domain.customfield.RenderingLocation;

@Entity
public class DenormalizedFieldValue {

	@Id
	@GeneratedValue
	@Column(name = "DFV_ID")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "CFV_ID", nullable = true)
	private CustomFieldValue customFieldValue;

	@NotBlank
	@Size(min = CustomField.MIN_CODE_SIZE, max = CustomField.MAX_CODE_SIZE)
	@Pattern(regexp = CustomField.CODE_REGEXP)
	private String code = "";

	private Long denormalizedFieldHolderId;

	@Enumerated(EnumType.STRING)
	private DenormalizedFieldHolderType denormalizedFieldHolderType;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(updatable = false)
	private InputType inputType;

	@NotBlank
	@Size(min = 0, max = 255)
	private String label = "";

	private int position;

	private String value;

	@ElementCollection
	@CollectionTable(name = "DENORMALIZED_FIELD_RENDERING_LOCATION", joinColumns = @JoinColumn(name = "DFV_ID"))
	@Enumerated(EnumType.STRING)
	@Column(name = "RENDERING_LOCATION")
	private Set<RenderingLocation> renderingLocations = new HashSet<RenderingLocation>(5);

	/**
	 * For ORM purposes.
	 */
	protected DenormalizedFieldValue() {
		super();

	}

	/**
	 * Copies the attributes of the given customFieldValue and it's associated customField and customFieldBinding
	 * 
	 * @param customFieldValue
	 *            : must be bound to it's customField
	 * @param denormalizedFieldHolderId
	 * @param denormalizedFieldHolderType
	 */
	public DenormalizedFieldValue(CustomFieldValue customFieldValue, Long denormalizedFieldHolderId,
			DenormalizedFieldHolderType denormalizedFieldHolderType) {
		super();
		this.customFieldValue = customFieldValue;
		CustomField cuf = customFieldValue.getCustomField();
		this.code = cuf.getCode();
		this.inputType = cuf.getInputType();
		this.label = cuf.getLabel();
		this.value = customFieldValue.getValue();
		this.position = customFieldValue.getBinding().getPosition();
		this.renderingLocations = customFieldValue.getBinding().copyRenderingLocations();
		this.denormalizedFieldHolderId = denormalizedFieldHolderId;
		this.denormalizedFieldHolderType = denormalizedFieldHolderType;
	}

	/**
	 * Create DenormalizedFieldValue with value param. All positions params are copied from the given binding.
	 * 
	 * @param value
	 * @param binding
	 * @param denormalizedFieldHolderId
	 * @param denormalizedFieldHolderType
	 */
	public DenormalizedFieldValue(String value, CustomFieldBinding binding, Long denormalizedFieldHolderId,
			DenormalizedFieldHolderType denormalizedFieldHolderType) {
		super();
		CustomField cuf = binding.getCustomField();
		this.code = cuf.getCode();
		this.inputType = cuf.getInputType();
		this.label = cuf.getLabel();
		this.value = value;
		this.position = binding.getPosition();
		this.renderingLocations = binding.copyRenderingLocations();
		this.denormalizedFieldHolderId = denormalizedFieldHolderId;
		this.denormalizedFieldHolderType = denormalizedFieldHolderType;
	}

	/**
	 * Will create a DenormalizedFieldValue with the value param. The position will be valorized with the given param. No rendering location is added.
	 * 
	 * @param remainingCufValue
	 * @param newBindingPosition
	 */
	public DenormalizedFieldValue(CustomFieldValue customFieldValue, int newBindingPosition,
			Long denormalizedFieldHolderId, DenormalizedFieldHolderType denormalizedFieldHolderType) {
		super();
		this.customFieldValue = customFieldValue;
		CustomField cuf = customFieldValue.getCustomField();
		this.code = cuf.getCode();
		this.inputType = cuf.getInputType();
		this.label = cuf.getLabel();
		this.value = customFieldValue.getValue();
		this.position = newBindingPosition;
		this.denormalizedFieldHolderId = denormalizedFieldHolderId;
		this.denormalizedFieldHolderType = denormalizedFieldHolderType;
	}

	public Long getId() {
		return id;
	}

	public CustomFieldValue getCustomFieldValue() {
		return customFieldValue;
	}

	public String getCode() {
		return code;
	}

	public Long getDenormalizedFieldHolderId() {
		return denormalizedFieldHolderId;
	}

	public DenormalizedFieldHolderType getDenormalizedFieldHolderType() {
		return denormalizedFieldHolderType;
	}

	public InputType getInputType() {
		return inputType;
	}

	public String getLabel() {
		return label;
	}

	public int getPosition() {
		return position;
	}

	public String getValue() {
		return value;
	}

	public Set<RenderingLocation> getRenderingLocations() {
		return renderingLocations;
	}

	public void setRenderingLocations(Set<RenderingLocation> renderingLocations) {
		this.renderingLocations = renderingLocations;
	}
	
	

}
