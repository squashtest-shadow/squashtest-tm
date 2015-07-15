/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.validation.constraint.HasDefaultItem;
import org.squashtest.tm.validation.constraint.UniqueItems;

@Entity
@Auditable
public class InfoList implements Comparable<InfoList> {

	@Id
	@Column(name = "INFO_LIST_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "info_list_info_list_id_seq")
	@SequenceGenerator(name = "info_list_info_list_id_seq", sequenceName = "info_list_info_list_id_seq")
	private Long id;

	@Size(max = 100)
	@NotBlank
	private String label = "";

	@Lob
	@Type(type = "org.hibernate.type.StringClobType")
	private String description;

	@Size(max = 30)
	@NotBlank
	private String code;

	@Valid @UniqueItems @HasDefaultItem("isDefault")
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@OrderColumn(name = "ITEM_INDEX")
	@JoinColumn(name = "LIST_ID")
	private List<InfoListItem> items = new ArrayList<>();

	public InfoList() {
		super();
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

	public List<InfoListItem> getItems() {
		return items;
	}

	public void addItem(InfoListItem item) {
		items.add(item);
	}

	public void removeItem(InfoListItem item) {
		this.removeItem(item.getCode());
	}

	public void removeItem(Long itemId) {
		Iterator<InfoListItem> iter = items.iterator();
		while (iter.hasNext()) {
			if (itemId.equals(iter.next().getId())) {
				iter.remove();
				return;
			}
		}
		// if not found, well, the list is already in the desired state.
	}

	public void removeItem(String code) {
		Iterator<InfoListItem> iter = items.iterator();
		while (iter.hasNext()) {
			if (code.equals(iter.next().getCode())) {
				iter.remove();
				return;
			}
		}
		// if not found, well, the list is already in the desired state.
	}

	public InfoListItem getDefaultItem() {
		for (InfoListItem it : items) {
			if (it.isDefault()) {
				return it;
			}
		}
		throw new IllegalStateException("No default item was defined for this list");
	}

	public boolean contains(InfoListItem item) {
		for (InfoListItem it : items) {
			if (it.references(item)) {
				return true;
			}
		}
		return false;
	}


	public void addItems(int newIndex, List<InfoListItem> addedItems) {
		items.addAll(newIndex, addedItems);
	}

	@Override
	public int compareTo(InfoList infoList) {

		if (this.label != null && infoList.label != null) {
			return this.label.compareToIgnoreCase(infoList.label);
		}
		return 0;
	}

	public int compare(final InfoList object1, final InfoList object2) {
		return object1.getLabel().compareTo(object2.getLabel());
	}

}