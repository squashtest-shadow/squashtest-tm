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
package org.squashtest.tm.service.internal.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.orm.hibernate3.support.AbstractLobType;

public class BlobUserType extends AbstractLobType {

	@Override
	public int[] sqlTypes() {
		return new int[] {Types.BLOB};
	}

	@Override
	public Class<?> returnedClass() {
		return InputStream.class;
	}

	@Override
	protected Object nullSafeGetInternal(ResultSet rs, String[] names,
			Object owner, LobHandler lobHandler) throws SQLException,
			IOException, HibernateException {
		   return lobHandler.getBlobAsBinaryStream(rs, names[0]);

	}

	@Override
	protected void nullSafeSetInternal(PreparedStatement ps, int index,	Object value, LobCreator lobCreator) throws SQLException,
			IOException, HibernateException {
		

		  if (value != null) {
			  lobCreator.setBlobAsBinaryStream(ps, index, (InputStream) value, -1);

		 }
		  else {
			    lobCreator.setBlobAsBytes(ps, index, null);
		  }


	}

}
