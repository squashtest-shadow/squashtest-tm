package org.squashtest.tm.domain.testcase;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.squashtest.tm.domain.audit.AuditableMixin;

public class TestCaseBridgeModifiedBy implements FieldBridge{
	
	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		
		AuditableMixin audit = ((AuditableMixin) value);
		
		Field field = new Field(name, audit.getLastModifiedBy(), luceneOptions.getStore(),
	    luceneOptions.getIndex(), luceneOptions.getTermVector() );
	    field.setBoost( luceneOptions.getBoost());
	    document.add(field);
	}
}
