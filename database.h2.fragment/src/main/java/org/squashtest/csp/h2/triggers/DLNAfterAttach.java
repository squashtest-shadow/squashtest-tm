/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
package org.squashtest.csp.h2.triggers;

import org.h2.tools.TriggerAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * Triggered after a DatasetLibraryNode was attached to another one. It will
 * attach that node and its subtree to the ancestors of that node.
 *
 * @author aguilhem
 *
 */
public class DLNAfterAttach extends TriggerAdapter {

	private static final String SQL =
		"insert into DLN_RELATIONSHIP_CLOSURE\n"+
			"select d1.ancestor_id, d2.descendant_id, d1.depth+d2.depth+1\n"+
				"from DLN_RELATIONSHIP_CLOSURE d1\n"+
			"cross join DLN_RELATIONSHIP_CLOSURE d2\n"+
				"where d1.descendant_id = ?\n"+
				"and d2.ancestor_id = ?;";


	@Override
	public void fire(Connection conn, ResultSet oldRow, ResultSet newRow)
			throws SQLException {

		PreparedStatement stmt = conn.prepareStatement(SQL);

		Long ancestorId = newRow.getLong(1);
		Long descendantId = newRow.getLong(2);
		stmt.setLong(1, ancestorId);
		stmt.setLong(2,descendantId);

		stmt.execute();

	}


}
