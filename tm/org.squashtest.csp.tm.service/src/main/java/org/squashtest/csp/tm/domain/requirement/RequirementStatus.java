package org.squashtest.csp.tm.domain.requirement;

import java.util.Set;
import java.util.TreeSet;

import org.squashtest.csp.tm.domain.Internationalizable;

public enum RequirementStatus implements Internationalizable{
	
	WORK_IN_PROGRESS(){
		@Override
		public Set<RequirementStatus> getAvailableNextStatus() {
			Set<RequirementStatus> next = defaultAvailableSet();
			next.add(UNDER_REVIEW);
			return next;
		}

		@Override
		public boolean allowsUpdate() {
			return true;
		}

		@Override
		public boolean allowsStatusUpdate() {
			return true;
		}
	},
	
	UNDER_REVIEW(){
		@Override
		public Set<RequirementStatus> getAvailableNextStatus() {
			Set<RequirementStatus> next = defaultAvailableSet();
			next.add(WORK_IN_PROGRESS);
			next.add(APPROVED);
			return next;
		}

		@Override
		public boolean allowsUpdate() {
			return true;
		}

		@Override
		public boolean allowsStatusUpdate() {
			return true;
		}
	},
	
	APPROVED(){
		@Override
		public Set<RequirementStatus> getAvailableNextStatus() {
			Set<RequirementStatus> next = defaultAvailableSet();
			next.add(UNDER_REVIEW);
			next.add(WORK_IN_PROGRESS);
			return next;
		}

		@Override
		public boolean allowsUpdate() {
			return false;
		}

		@Override
		public boolean allowsStatusUpdate() {
			return true;
		}
	},
	
	OBSOLETE(){
		@Override
		public Set<RequirementStatus> getAvailableNextStatus() {
			Set<RequirementStatus> next = defaultAvailableSet();
			return next;
		}

		@Override
		public boolean allowsUpdate() {
			return false;
		}

		@Override
		public boolean allowsStatusUpdate() {
			return false;
		}
	};
	
	private static final String I18N_KEY_ROOT = "requirement.status.";	

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}
	
	public abstract Set<RequirementStatus> getAvailableNextStatus();
	public abstract boolean allowsUpdate();
	public abstract boolean allowsStatusUpdate();
	
	
	private static Set<RequirementStatus> defaultAvailableSet(){
		Set<RequirementStatus> next = new TreeSet<RequirementStatus>();
		next.add(RequirementStatus.OBSOLETE);
		return next;
	}
	
}
