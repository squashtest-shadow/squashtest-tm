package org.squashtest.csp.tm.internal.utils.archive;

import java.io.InputStream;

public interface ArchiveReaderFactory {

	ArchiveReader createReader(InputStream stream);
	
}
