package org.squashtest.csp.tm.internal.utils.archive;

import java.io.InputStream;
import java.util.Iterator;

public interface ArchiveReader extends Iterator<Entry>{

	void setStream(InputStream archiveStream);
	void close();
	
}
