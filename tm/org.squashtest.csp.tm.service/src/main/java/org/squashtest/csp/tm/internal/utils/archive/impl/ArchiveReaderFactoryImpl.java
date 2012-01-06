package org.squashtest.csp.tm.internal.utils.archive.impl;

import java.io.InputStream;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.internal.utils.archive.ArchiveReader;
import org.squashtest.csp.tm.internal.utils.archive.ArchiveReaderFactory;

@Component
public class ArchiveReaderFactoryImpl implements ArchiveReaderFactory {

	@Override
	public ArchiveReader createReader(InputStream stream) {
		return new ZipReader(stream);
	}

}
