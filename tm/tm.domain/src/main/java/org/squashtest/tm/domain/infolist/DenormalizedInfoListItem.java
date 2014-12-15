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
package org.squashtest.tm.domain.infolist;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Size;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ITEM_TYPE", discriminatorType = DiscriminatorType.STRING)
public abstract class DenormalizedInfoListItem {

	@Id
	@Column(name = "ITEM_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "denormalized_info_list_item_item_id_seq")
	@SequenceGenerator(name = "denormalized_info_list_item_item_id_seq", sequenceName = "denormalized_info_list_item_item_id_seq")
	private Long id;


	@ManyToOne
	@JoinColumn(name="DENO_LIST_ID", insertable=false, updatable=false)
	private DenormalizedInfoList infoList;

	@Column
	@Size(max=100)
	private String label = "";

	@Column
	@Size(max=30)
	private String code = "";

	@Column
	private Boolean isDefault = false;

	@Column
	@Size(max=100)
	private String iconName = "";

	public DenormalizedInfoList getInfoList() {
		return infoList;
	}

	public void setInfoList(DenormalizedInfoList infoList) {
		this.infoList = infoList;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

	public Long getId() {
		return id;
	}

	public Long getOriginalListId(){
		return infoList.getOriginalId();
	}

	public int getOriginalListVersion(){
		return infoList.getOriginalVersion();
	}

	public boolean references(Object other){
		if (other == null){
			return false;
		}

		if (! (DenormalizedInfoListItem.class.isAssignableFrom(other.getClass()))){
			return false;
		}

		if (DenormalizedListItemReference.class.isAssignableFrom(other.getClass())){
			DenormalizedListItemReference otherRef = (DenormalizedListItemReference)other;
			return (otherRef.getCode().equals(getCode()) &&
					otherRef.getOriginalListId().equals(getOriginalListId()) &&
					otherRef.getOriginalListVersion() == getOriginalListVersion()
					) ;
		}
		else{
			DenormalizedInfoListItem otherItem = (DenormalizedInfoListItem) other;
			return ( otherItem.getCode().equals(getCode()) &&
					otherItem.getInfoList().getOriginalId().equals(getOriginalListId()) &&
					otherItem.getInfoList().getOriginalVersion() == getOriginalListVersion()
					);
		}
	}


	// TODO : remove this method completely once we're sure the method "references" is
	// used where it should be and that method "equals" is used properly
	@Override
	public boolean equals(Object o){
		throw new RuntimeException("TODO : use method #references instead");
	}


}
