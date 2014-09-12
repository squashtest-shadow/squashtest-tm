/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.fileupload;

import java.text.NumberFormat;

import org.apache.commons.fileupload.ProgressListener;


public class UploadProgressListener implements ProgressListener {

	private long bytesTransferred = 0;
	
	private long fileSize = -100;
	
	/** The total number of bytes, which have been read so far. */
	private long totalBytesRead = 0;
	
	private long fiveKBRead = -1;
	

	public UploadProgress getStatus() {
		
		UploadProgress report = new UploadProgress();
		
		report.setTotalSize(fileSize / 1024);
		report.setBytesRead(totalBytesRead / 1024);
		// per looks like 0% - 100%, remove % before submission
		String per = NumberFormat.getPercentInstance().format(
				(double) bytesTransferred / (double) fileSize);
		int iPer = Integer.parseInt(per.substring(0, per.length() - 1).trim());
		report.setPercentage(iPer);
		return report;
	}

	// Function called by multipartResolver to notify the change of the upload
	// status
	/* (non-Javadoc)
	 * @see org.apache.commons.fileupload.ProgressListener#update(long, long, int)
	 */
	@Override
	public void update(long bytesRead, long contentLength, int items) {
		// update bytesTransferred and fileSize (if required) every 5 KB is read
		long fiveKB = bytesRead / 5120;
		totalBytesRead = bytesRead;
		
		// the number of kilobytes read has not increased since the last time we checked
		if (fiveKBRead == fiveKB) {
			return;
		}
		
		fiveKBRead = fiveKB;
		bytesTransferred = bytesRead;
		if (fileSize != contentLength) {
			fileSize = contentLength;
		}
	}
}