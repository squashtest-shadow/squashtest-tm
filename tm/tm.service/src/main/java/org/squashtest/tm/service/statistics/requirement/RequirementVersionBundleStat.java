/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.statistics.requirement;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class RequirementVersionBundleStat {

	Map<Long, SimpleRequirementVersionStats> reqVersionStats = new HashMap<>();

	public Map<Long, SimpleRequirementVersionStats> getReqVersionStats() {
		return reqVersionStats;
	}

	public void setReqVersionStats(Map<Long, SimpleRequirementVersionStats> reqVersionStats) {
		this.reqVersionStats = reqVersionStats;
	}

	public void computeRedactionRate(Long reqVersionId , Integer countAllTC, Integer countVerifiedTestTC) {
		SimpleRequirementVersionStats stats = getSimpleStats(reqVersionId);
		double rate = 0d;
		if(countAllTC != null && countAllTC != 0 && countVerifiedTestTC != null){
			rate = countVerifiedTestTC.doubleValue() / countAllTC.doubleValue();
		}
		rate = makeProperRoundedRate(rate);
		stats.redactionRate = rate;
	}

	private double makeProperRoundedRate(double rate) {
		rate = rate * 100;
		BigDecimal bigDecimal = BigDecimal.valueOf(rate).setScale(2, BigDecimal.ROUND_HALF_UP);
		rate = bigDecimal.doubleValue();
		return rate;
	}

	private SimpleRequirementVersionStats getSimpleStats(Long reqVersionId) {
		SimpleRequirementVersionStats rates;
		if(reqVersionStats.containsKey(reqVersionId)){
			rates = reqVersionStats.get(reqVersionId);
		} else {
			rates = new SimpleRequirementVersionStats();
			reqVersionStats.put(reqVersionId, rates);
		}
		return rates;
	}

//	public void setVerificationRate(Double verificationRate) {
//		this.verificationRate = verificationRate;
//	}
//
//	public void setValidationRate(Double validationRate) {
//		this.validationRate = validationRate;
//	}

	public static class SimpleRequirementVersionStats {
		private Long reqVersionId;
		/**
		 * Rate of coverage with test case with status UNDER_REVIEW, APPROVED. so it's not just the coverage as usual, but the coverage by validated test cases
		 */
		private Double redactionRate;
		private Double verificationRate;
		private Double validationRate;

		public Long getReqVersionId() {
			return reqVersionId;
		}

		public void setReqVersionId(Long reqVersionId) {
			this.reqVersionId = reqVersionId;
		}

		public Double getRedactionRate() {
			return redactionRate;
		}

		public void setRedactionRate(Double redactionRate) {
			this.redactionRate = redactionRate;
		}

		public Double getVerificationRate() {
			return verificationRate;
		}

		public void setVerificationRate(Double verificationRate) {
			this.verificationRate = verificationRate;
		}

		public Double getValidationRate() {
			return validationRate;
		}

		public void setValidationRate(Double validationRate) {
			this.validationRate = validationRate;
		}
	}
}
