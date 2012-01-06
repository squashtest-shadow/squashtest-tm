/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

package org.squashtest.csp.tm.domain.event;

import java.util.List;

import org.squashtest.csp.tm.internal.repository.EntityDao;

/**
 * @author Gregory Fouquet
 * 
 */
class StubEntityDao<ENTITY> implements EntityDao<ENTITY> {

	/**
	 * @see org.squashtest.csp.tm.internal.repository.EntityDao#findById(long)
	 */
	@Override
	public ENTITY findById(long id) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.EntityDao#findAllByIdList(java.util.List)
	 */
	@Override
	public List<ENTITY> findAllByIdList(List<Long> id) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.EntityDao#persist(java.lang.Object)
	 */
	@Override
	public void persist(ENTITY transientEntity) {
		// NOOP

	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.EntityDao#remove(java.lang.Object)
	 */
	@Override
	public void remove(ENTITY entity) {
		// NOOP
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.EntityDao#flush()
	 */
	@Override
	public void flush() {
		// NOOP

	}
	
	/**
	 * @see org.squashtest.csp.tm.internal.repository.EntityDao#persist(List)
	 */
	@Override
	public void persist(List<ENTITY> transientEntities) {
		// NOOP
		
	}

}
