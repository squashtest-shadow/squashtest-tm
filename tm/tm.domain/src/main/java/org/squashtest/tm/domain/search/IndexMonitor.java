/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.domain.search;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;

public class IndexMonitor {

	private static final BigInteger CENT = BigInteger.valueOf(100);

	public static Map<Class<?>, IndexMonitor> monitors = new HashMap<Class<?>, IndexMonitor>();
	public static IndexMonitor total = new IndexMonitor();

	static {
		monitors.put(TestCase.class, new IndexMonitor());
		monitors.put(RequirementVersion.class, new IndexMonitor());
		monitors.put(IterationTestPlanItem.class, new IndexMonitor());
	}

	private BigInteger totalCount = BigInteger.ZERO;
	private BigInteger documentsBuilt = BigInteger.ZERO;


	public void addToTotalCount(long count) {
		totalCount = totalCount.add(BigInteger.valueOf(count));
	}

	public void addToDocumentsBuilded(int doc) {
		documentsBuilt = documentsBuilt.add(BigInteger.valueOf(doc));
	}

	public void addToTotalCount(BigInteger count) {
		totalCount = totalCount.add(count);
	}

	public void addToDocumentsBuilded(BigInteger doc) {
		documentsBuilt = documentsBuilt.add(doc);
	}

	public BigInteger getTotalCount() {
		return totalCount;
	}

	public BigInteger getDocumentsBuilt() {
		return documentsBuilt;
	}

	public BigInteger getPercentComplete() {

		if (totalCount.equals(BigInteger.ZERO)) {
			return CENT;
		}
		return documentsBuilt.multiply(CENT).divide(totalCount);

	}

	public static void resetTotal() {
		total = new IndexMonitor();
	}

}