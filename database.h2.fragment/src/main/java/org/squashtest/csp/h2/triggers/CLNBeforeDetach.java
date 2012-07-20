/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.tools.TriggerAdapter;

/**
 * Triggered before a CampaignLibraryNode is detached from its parent. It will cut from the 
 * relationship closure table the subtree of the detached node.
 * 
 * 
 * @author bsiri
 *
 */
public class CLNBeforeDetach extends TriggerAdapter {

	private static final String SQL = 
		"delete clos1 from CLN_RELATIONSHIP_CLOSURE clos1\n"+ 
		"join CLN_RELATIONSHIP_CLOSURE clos2\n"+ 
			"on clos1.descendant_id = clos2.descendant_id\n"+
		"left join CLN_RELATIONSHIP_CLOSURE clos3\n"+ 
			"on clos3.ancestor_id = clos2.ancestor_id\n"+ 
			"and clos3.descendant_id = clos1.ancestor_id\n"+
		"where clos2.ancestor_id = ?\n"+ 
		"and clos3.ancestor_id is null;";		
	
	
	@Override
	public void fire(Connection conn, ResultSet oldRow, ResultSet newRow)
			throws SQLException {
		
		PreparedStatement stmt = conn.prepareStatement(SQL);
		
		Long detachedId = newRow.getLong(2);
		stmt.setLong(1, detachedId);		
		
		stmt.execute();

	}

}
