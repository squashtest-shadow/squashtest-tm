package org.squashtest.tm.service.internal.milestone;

import org.apache.commons.lang3.EnumUtils;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;
import org.squashtest.tm.service.milestone.MilestoneModelService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.squashtest.tm.jooq.domain.Tables.*;

/**
 *  As for Squash 1.17, all our DAO are JPA or Hibernate DAO.
 *  Decision was made to not create specific DAO for jOOQ and Models. So the requests are in service object.
 */
@Service
@Transactional(readOnly = true)
public class MilestoneModelServiceImpl implements MilestoneModelService {

	@Inject
	private DSLContext DSL;

	@Override
	public Map<Long, JsonMilestone> findUsedMilestones(List<Long> readableProjectIds) {
		List<Long> usedMilestonesIds = findUsedMilestoneIds(readableProjectIds);
		return findJsonMilestones(usedMilestonesIds);
	}

	@Override
	public JsonMilestone findMilestoneModel(Long milestoneId) {
		ArrayList<Long> milestoneIds = new ArrayList<>();
		milestoneIds.add(milestoneId);
		Map<Long, JsonMilestone> jsonMilestones = findJsonMilestones(milestoneIds);
		return jsonMilestones.get(milestoneId);
	}

	protected Map<Long, JsonMilestone> findJsonMilestones(List<Long> usedMilestonesIds) {
		return DSL.select(MILESTONE.MILESTONE_ID, MILESTONE.LABEL, MILESTONE.M_RANGE, MILESTONE.STATUS, MILESTONE.END_DATE
			, CORE_USER.LOGIN)
			.from(MILESTONE)
			.join(CORE_USER).on(MILESTONE.USER_ID.eq(CORE_USER.PARTY_ID))
			.where(MILESTONE.MILESTONE_ID.in(usedMilestonesIds))
			.fetch()
			.stream()
			.map(r -> {
				String mRangeKey = r.get(MILESTONE.M_RANGE);
				MilestoneRange milestoneRange = EnumUtils.getEnum(MilestoneRange.class, mRangeKey);

				String mStatusKey = r.get(MILESTONE.STATUS);
				MilestoneStatus milestoneStatus = EnumUtils.getEnum(MilestoneStatus.class, mStatusKey);

				return new JsonMilestone(r.get(MILESTONE.MILESTONE_ID), r.get(MILESTONE.LABEL), milestoneStatus, milestoneRange, r.get(MILESTONE.END_DATE), r.get(CORE_USER.LOGIN));
			})
			.collect(Collectors.toMap(JsonMilestone::getId, Function.identity()));
	}

	protected List<Long> findUsedMilestoneIds(List<Long> readableProjectIds) {
		return DSL.selectDistinct(MILESTONE_BINDING.MILESTONE_ID)
			.from(MILESTONE_BINDING)
			.where(MILESTONE_BINDING.PROJECT_ID.in(readableProjectIds))
			.fetch(MILESTONE_BINDING.MILESTONE_ID, Long.class);
	}
}
