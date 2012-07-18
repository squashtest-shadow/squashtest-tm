package org.squashtest.csp.h2.triggers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.tools.TriggerAdapter;

/**
 *  Triggered after a new RequirementLibraryNode had been inserted. It will insert into the relationship 
 *  closure table the self-reference (the distance of a node with itself is always 0).
 *  
 * 
 * @author bsiri
 *
 */
public class RLNAfterInsert extends TriggerAdapter {

	private static final String SQL = "insert into RLN_RELATIONSHIP_CLOSURE values (?, ?, 0)";
	
	
	@Override
	public void fire(Connection conn, ResultSet oldRow, ResultSet newRow)
			throws SQLException {
		
		PreparedStatement stmt = conn.prepareStatement(SQL);
		
		Long id = newRow.getLong(1);
		stmt.setLong(1, id);
		stmt.setLong(2,id);
		
		stmt.execute();

	}

}
