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
package org.squashtest.tm.domain.denormalizedfield;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.lang.IsoDateUtils;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.validation.constraint.HasDefaultAsRequired;

@NamedQueries(value = {
		@NamedQuery(name = "DenormalizedFieldValue.deleteAllForEntity", query = "delete DenormalizedFieldValue dfv where dfv.denormalizedFieldHolderId = :entityId and dfv.denormalizedFieldHolderType = :entityType"),
		@NamedQuery(name = "DenormalizedFieldValue.findDFVForEntity", query = "from DenormalizedFieldValue dfv where dfv.denormalizedFieldHolderId = :entityId and dfv.denormalizedFieldHolderType = :entityType order by dfv.position"),
		@NamedQuery(name = "DenormalizedFieldValue.findDFVForEntityAndRenderingLocation", query = "select dfv from DenormalizedFieldValue dfv join dfv.renderingLocations rl where dfv.denormalizedFieldHolderId = :entityId and dfv.denormalizedFieldHolderType = :entityType and rl = :renderingLocation order by dfv.position"),
		@NamedQuery(name = "DenormalizedFieldValue.findDFVForEntities", query = "select dfv from DenormalizedFieldValue dfv where dfv.denormalizedFieldHolderId in (:entityIds) and dfv.denormalizedFieldHolderType = :entityType order by dfv.position"),
		@NamedQuery(name = "DenormalizedFieldValue.findDFVForEntitiesAndLocations", query = "select dfv from DenormalizedFieldValue dfv join dfv.renderingLocations rl where dfv.denormalizedFieldHolderId in (:entityIds) and dfv.denormalizedFieldHolderType = :entityType and rl in (:locations) order by dfv.position") 
		})
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "FIELD_TYPE", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("CF")
public class DenormalizedFieldValue {

	private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizedFieldValue.class);
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
	
	@Size(min = 0, max = 255)
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
	 * Will create a DenormalizedFieldValue with the value param. The position will be valorized with the given param.
	 * No rendering location is added.
	 * 
	 * @param customFieldValue
	 * @param newBindingPosition
	 * @param denormalizedFieldHolderId
	 * @param denormalizedFieldHolderType
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

	/**
	 * Return the value as a Date or <code>null</code> if the input type is not Date-picker and if the parsing can't be
	 * done.
	 * 
	 * @return a {@link Date} or <code>null</code> in case of ParseException and wrong input-type
	 */
	public Date getValueAsDate() {
		Date toReturn = null;
		if (this.inputType == InputType.DATE_PICKER) {
			try {
				toReturn = IsoDateUtils.parseIso8601Date(value);
			} catch (ParseException e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}
		return toReturn;
	}
	
	public Set<RenderingLocation> getRenderingLocations() {
		return renderingLocations;
	}

	public void setRenderingLocations(Set<RenderingLocation> renderingLocations) {
		this.renderingLocations = renderingLocations;
	}

	public void accept(DenormalizedFieldVisitor visitor) {
		visitor.visit(this);
	}
	

	public void setValue(String value) {
		this.value = value;
	}

}
