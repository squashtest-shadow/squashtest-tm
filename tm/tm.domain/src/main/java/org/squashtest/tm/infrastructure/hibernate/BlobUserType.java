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
package org.squashtest.tm.infrastructure.hibernate;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.EnhancedUserType;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
public class BlobUserType implements EnhancedUserType {

	LobHandler lobHandler = new DefaultLobHandler();


	@Override
	public int[] sqlTypes() {
		return new int[] {Types.BLOB};
	}

	@Override
	public Class<?> returnedClass() {
		return InputStream.class;
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		
		return lobHandler.getBlobAsBinaryStream(rs, names[0]);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {

		  if (value != null) {
			  lobHandler.getLobCreator().setBlobAsBinaryStream(st, index, (InputStream) value, -1);

		 }
		  else {
			  lobHandler.getLobCreator().setBlobAsBytes(st, index, null);
		  }
		
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {// NOSONAR this method actually implements a method in hibernate interface UserType
		boolean isEqual = false;
        if (x == y) {
            isEqual = true;
        }
        if (null == x || null == y) {
            isEqual = false;
        } else {
            isEqual = x.equals(y);
        }
        return isEqual;
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
	       return (Serializable) value;
	}

	@Override
	public Object assemble(Serializable cached, Object owner) {
		return cached;
	}

	@Override
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	@Override
	public String objectToSQLString(Object value) {
		return null;
	}

	@Override
	@Deprecated
	public String toXMLString(Object value) {
		return null;
	}

	@Override
	@Deprecated
	public Object fromXMLString(String xmlValue) {
		return null;
	}

}
