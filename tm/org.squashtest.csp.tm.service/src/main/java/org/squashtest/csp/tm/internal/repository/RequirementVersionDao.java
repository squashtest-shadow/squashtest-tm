package org.squashtest.csp.tm.internal.repository;

import java.util.Collection;
import java.util.List;

import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
/**
 * 
 * @author Gregory Fouquet
 *
 */
public interface RequirementVersionDao extends CustomRequirementVersionDao {
	List<RequirementVersion> findAllByIdList(Collection<Long> ids);

	RequirementVersion findById(long requirementId); 

}
