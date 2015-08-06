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
package org.squashtest.tm.service.internal.charts

import spock.lang.Specification;
import org.squashtest.tm.service.charts.Column;
import org.squashtest.tm.service.charts.PerimeterQuery;
import static org.squashtest.tm.service.charts.Datatype.*;

class PerimeterUtilsTest extends Specification {

	def "should generate a correct HQL query"(){

		given :
		def fromClause = "from Person p inner join p.town town"

		and :
		def axes = [ new Column("town-name", STRING, "town name", "town.name")];
		def data = [ new Column("person-name", STRING, "person name", "p.name" )];

		and :
		def util = new PerimeterUtils()

		when :
		def hql = util.getHQL(new PerimeterQuery(axes : axes, data : data), fromClause)


		then :
		hql == """select town.name, count(distinct p.name) from Person p inner join p.town town group by town.name order by town.name asc"""


	}

}
