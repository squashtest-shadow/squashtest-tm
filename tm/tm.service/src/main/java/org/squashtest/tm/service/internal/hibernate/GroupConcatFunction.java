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
package org.squashtest.tm.service.internal.hibernate;

import java.util.List;

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;


/**
 * <p>This custom implementation of group_concat. Because it can contain an embedded expression, hibernate will try to parse it just as if
 * it was within the scope of the main query - thus causing parsing exception.</p>
 * 
 *  <p>To prevent this we had to make the awkward syntax as follow:</p>
 * 
 *  <ul>
 *  	<li>group_concat(<it>col identifier</it>) : will concatenate as expected over the column identifier.</li>
 *  	<li>group_concat(<it>col identifier 1</it>, 'order by', <it>col identifier 2</it>, ['asc|desc']) : will send this to the target db as 'group_concat(id1 order by id2 [asc|desc])'
 *  </ul>
 * 
 * 
 * 
 * 
 * @author bsiri
 *
 */
public class GroupConcatFunction extends StandardSQLFunction {

	public GroupConcatFunction(String name, Type registeredType) {
		super(name, registeredType);
	}

	public GroupConcatFunction(String name) {
		super(name);
	}

	@Override
	public final String render(Type firstArgumentType, List arguments, SessionFactoryImplementor sessionFactory) {

		if (arguments.size()==1){
			return super.render(firstArgumentType, arguments, sessionFactory);
		}

		else{
			try{
				// validation
				String direction = (arguments.size()>=4) ? ((String)arguments.get(3)).replaceAll("'", "") : "asc";
				String separator = (arguments.size()>=5) ? ((String)arguments.get(4)).replaceAll("'", "") : ",";
				if (! (direction.equalsIgnoreCase("asc") || direction.equalsIgnoreCase("desc") )){
					throw new IllegalArgumentException();
				}
				if (! ((String)arguments.get(1)).equalsIgnoreCase("'order by'")){
					throw new IllegalArgumentException();
				}

				// expression
				return createSqlQuery(arguments, direction, separator);
			}
			catch(IllegalArgumentException ex){
				throw new IllegalArgumentException("usage of custom hql group_concat : group_concat(col id, [ 'order by', col id2, ['asc|desc']]", ex);
			}
		}

	}

	protected String createSqlQuery(List<?> arguments, String direction, String separator) {
		return "group_concat("+arguments.get(0)+" order by "+arguments.get(2)+" "+direction+" separator '"+separator+"')";
	}

}
