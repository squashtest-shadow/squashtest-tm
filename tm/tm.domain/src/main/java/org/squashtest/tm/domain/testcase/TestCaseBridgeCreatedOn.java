package org.squashtest.tm.domain.testcase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.squashtest.tm.domain.audit.AuditableMixin;

public class TestCaseBridgeCreatedOn implements FieldBridge{

	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	
	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		
		AuditableMixin audit = ((AuditableMixin) value);
		
		Field field = new Field(name, dateFormat.format(audit.getCreatedOn()), luceneOptions.getStore(),
	    luceneOptions.getIndex(), luceneOptions.getTermVector() );
	    field.setBoost( luceneOptions.getBoost());
	    document.add(field);
	}
}
