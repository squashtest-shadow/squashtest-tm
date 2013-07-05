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
package org.squashtest.tm.domain.attachment;

import java.util.Date;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class Attachment {
	private static final float MEGA_BYTE = 1048576.000f;

	@Id
	@GeneratedValue
	@Column(name = "ATTACHMENT_ID")
	private Long id;

	@NotEmpty
	private String name;

	private String type;

	private Long size = 0L;

	@OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
	@JoinColumn(name = "CONTENT_ID")
	private AttachmentContent content;

	public Attachment() {
		super();
	}

	public Attachment(String name) {
		setName(name);
	}

	public Long getId() {
		return id;
	}

	public AttachmentContent getContent() {
		return content;
	}

	public void setContent(AttachmentContent content) {
		this.content = content;
	}

	@Temporal(TemporalType.TIMESTAMP)
	private Date addedOn;

	/**
	 * @return the full name of the file (including extension)
	 * 
	 */

	public String getName() {
		return name;
	}

	/**
	 * sets the full name (including extensions). The file type will be set on the fly.
	 * 
	 * @param String
	 *            name
	 */
	public void setName(String name) {
		this.name = name;
		setType();
	}

	/**
	 * When dealing with name this is the one you want most of the time
	 * 
	 * @return the filename without extension
	 */
	@NotBlank
	public String getShortName() {
		int position = name.lastIndexOf('.');
		return name.substring(0, position);
	}

	/**
	 * When dealing with names this is the one you want most of the time
	 * 
	 * @param shortName
	 *            represents the filename without extension
	 */
	public void setShortName(String shortName) {
		name = shortName + '.' + type;
	}

	public final void setType(String strType) {
		this.type = strType;
	}

	private void setType() {
		if (name != null) {
			int position = name.lastIndexOf('.');
			type = name.substring(position + 1);
		}
	}

	public final String getType() {
		return type;
	}

	public final void setAddedOn(Date addedOn) {
		this.addedOn = addedOn;
	}

	public final Date getAddedOn() {
		return addedOn;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getFormattedSize() {
		return getFormattedSize(Locale.getDefault());
	}

	// TODO text formatting should not be the responsibility of domain object. computing size in megs is, though
	public String getFormattedSize(Locale locale) {
		Float megaSize = Float.valueOf(size / MEGA_BYTE);
		return String.format(locale, "%.2f", megaSize);
	}

	/**
	 * will perform a deep copy of this Attachment. All attributes will be duplicated including the content.
	 * 
	 * Note : the properties 'id' and 'addedOn' won't be duplicated and will be automatically set by the system.
	 * 
	 */
	public Attachment hardCopy() {
		Attachment clone = new Attachment();

		clone.setName(this.getName());
		clone.setSize(this.getSize());
		clone.setType(this.getType());
		clone.setAddedOn(new Date());
		if (this.getContent() != null) {
			clone.setContent(this.getContent().hardCopy());
		}

		return clone;
	}

}
