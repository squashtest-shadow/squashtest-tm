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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;

@Entity
public class DenormalizedInfoList {

	@Id
	@Column(name = "INFO_LIST_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "denormalized_info_list_info_list_id_seq")
	@SequenceGenerator(name = "denormalized_info_list_info_list_id_seq", sequenceName = "denormalized_info_list_info_list_id_seq")
	private Long id;

	@Column
	private Long originalId;

	@Column
	private int originalVersion;

	@Column
	@Size(max=100)
	private String label = "";

	@Lob
	@Type(type = "org.hibernate.type.StringClobType")
	private String description;

	@Column
	@Size(max=30)
	private String code;


	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@OrderColumn(name = "ITEM_INDEX")
	@JoinColumn(name = "DENO_LIST_ID")
	private List<DenormalizedInfoListItem> items = new ArrayList<DenormalizedInfoListItem>();


	// to be used by Hibernate
	public DenormalizedInfoList(){
		super();
	}

	// preferred way to manually create such list when creating a new-to-be-persisted instance
	public DenormalizedInfoList(Long originalId, int originalVersion){
		super();
		this.originalId=originalId;
		this.originalVersion=originalVersion;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Long getId() {
		return id;
	}

	public Long getOriginalId() {
		return originalId;
	}

	public int getOriginalVersion() {
		return originalVersion;
	}



	public List<DenormalizedInfoListItem> getItems() {
		return items;
	}

	public void addItem(DenormalizedInfoListItem item){
		items.add(item);
	}


	public DenormalizedInfoListItem find(DenormalizedListItemReference reference){
		for (DenormalizedInfoListItem item : items){
			if (reference.references(item)){
				return item;
			}
		}
		throw new NoSuchElementException("denormalized info list item for list(id="+reference.getOriginalListId()+
				", version="+reference.getOriginalListVersion()+") and item code="+reference.getCode()+
				" was not found in denormalized list of id="+id);
	}



	public void removeItem(DenormalizedInfoListItem item){
		this.removeItem(item.getCode());
	}


	public void removeItem(Long itemId){
		Iterator<DenormalizedInfoListItem> iter = items.iterator();
		while (iter.hasNext()){
			if (itemId.equals(iter.next().getId())){
				iter.remove();
				return;
			}
		}
		// if not found, well, the list is already in the desired state.
	}

	public void removeItem(String code){
		Iterator<DenormalizedInfoListItem> iter = items.iterator();
		while (iter.hasNext()){
			if (code.equals(iter.next().getCode())){
				iter.remove();
				return;
			}
		}
		// if not found, well, the list is already in the desired state.
	}

	public boolean contains(DenormalizedInfoListItem item){
		for (DenormalizedInfoListItem it : items){
			if (it.references(item)){
				return true;
			}
		}
		return false;
	}

}
