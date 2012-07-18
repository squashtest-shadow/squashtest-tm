package org.squashtest.csp.h2.triggers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.tools.TriggerAdapter;

/**
 * 
 * Triggered after a RequirementLibraryNode was attached to another one (generally a RequirementFolder). It will
 * attach that node and its subtree to the ancestors of that node.
 * 
 * @author bsiri
 *
 */
public class RLNAfterAttach extends TriggerAdapter {

	private static final String SQL = 
		"insert into RLN_RELATIONSHIP_CLOSURE\n"+ 
			"select c1.ancestor_id, c2.descendant_id, c1.depth+c2.depth+1\n"+
				"from RLN_RELATIONSHIP_CLOSURE c1\n"+
			"cross join RLN_RELATIONSHIP_CLOSURE c2\n"+
				"where c1.descendant_id = ?\n"+
				"and c2.ancestor_id = ?;";
	
	
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
