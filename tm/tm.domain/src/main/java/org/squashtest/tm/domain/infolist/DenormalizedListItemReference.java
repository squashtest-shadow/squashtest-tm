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

/**
 * Instances of such classes are not meant to be persisted.
 * They merely hold a code, that should be looked up. The real instance
 * should then be used instead of that reference
 * 
 * @author bsiri
 *
 */
public class DenormalizedListItemReference extends DenormalizedInfoListItem {

	public Long originalListId;
	public int originalListVersion;

	public DenormalizedListItemReference(){
		super();
	}

	public DenormalizedListItemReference(Long originalListId, int originalListVersion, String code){
		super();
		setCode(code);
		this.originalListId = originalListId;
		this.originalListVersion = originalListVersion;
	}



	public Long getOriginalListId() {
		return originalListId;
	}

	public int getOriginalListVersion() {
		return originalListVersion;
	}

	@Override
	public boolean equals(Object other){
		if (other == null){
			return false;
		}

		if (! (DenormalizedInfoListItem.class.isAssignableFrom(other.getClass()))){
			return false;
		}

		if (DenormalizedListItemReference.class.isAssignableFrom(other.getClass())){
			DenormalizedListItemReference otherRef = (DenormalizedListItemReference)other;
			return (otherRef.getCode().equals(getCode()) &&
					otherRef.getOriginalListId().equals(originalListId) &&
					otherRef.getOriginalListVersion() == originalListVersion
					) ;
		}
		else{
			DenormalizedInfoListItem otherItem = (DenormalizedInfoListItem) other;
			return ( otherItem.getCode().equals(getCode()) &&
					otherItem.getInfoList().getOriginalId().equals(originalListId) &&
					otherItem.getInfoList().getOriginalVersion() == originalListVersion
					);
		}
	}


}
