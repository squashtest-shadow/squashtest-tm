package org.squashtest.csp.h2.triggers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.tools.TriggerAdapter;

/**
 * Triggered before a RequirementLibraryNode is detached from its parent. It will cut from the 
 * relationship closure table the subtree of the detached node.
 * 
 * 
 * @author bsiri
 *
 */
public class RLNBeforeDetach extends TriggerAdapter {

	private static final String SQL = 
		"delete clos1 from RLN_RELATIONSHIP_CLOSURE clos1\n"+ 
		"join RLN_RELATIONSHIP_CLOSURE clos2\n"+ 
			"on clos1.descendant_id = clos2.descendant_id\n"+
		"left join RLN_RELATIONSHIP_CLOSURE clos3\n"+ 
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
