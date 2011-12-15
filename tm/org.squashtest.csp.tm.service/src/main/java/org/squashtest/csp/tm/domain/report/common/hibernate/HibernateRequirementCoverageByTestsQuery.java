/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.domain.report.common.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.type.LongType;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.report.common.dto.ReqCoverageByTestProjectDto;
import org.squashtest.csp.tm.domain.report.common.dto.ReqCoverageByTestRequirementSingleDto;
import org.squashtest.csp.tm.domain.report.common.dto.ReqCoverageByTestStatType;
import org.squashtest.csp.tm.domain.report.query.hibernate.HibernateReportQuery;
import org.squashtest.csp.tm.domain.report.query.hibernate.ReportCriterion;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;

/**
 * 
 * Manage hibernate query to get the requirements covered by a given test-case
 * 
 * @author bsiri
 * @reviewed-on 2011-11-30
 */
public class HibernateRequirementCoverageByTestsQuery extends HibernateReportQuery {
	/***
	 * The rates default value
	 */
	private static double DEFAULT_RATE_VALUE = 100D;

	/***
	 * Name of the project created only to display totals in the report
	 */
	private static String TOTAL_PROJECT_NAME = "TOTAL";

	public HibernateRequirementCoverageByTestsQuery() {
		Map<String, ReportCriterion> criterions = getCriterions();
		ReportCriterion projectIds = new IsInSet<Long>("projectIds[]", "id", Project.class, "projects") {

			@Override
			public Long fromValueToTypedValue(Object o) {
				return Long.parseLong(o.toString());
			}
		};
		// note : the name here follows the naming convention of http requests for array parameters. It allows the
		// controller to directly map the http query string to that criterion.
		criterions.put("projectIds[]", projectIds);
	}

	@Override
	public DetachedCriteria createHibernateQuery() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<?> doInSession(Session session) {

		// Get projectIds
		List<Long> ids = new ArrayList<Long>();
		// Check if there's projects id
		boolean isProjectIds = false;
		if (this.getCriterions().get("projectIds[]").getParameters() != null) {
			isProjectIds = true;
			// Put ids in a list
			for (Object id : this.getCriterions().get("projectIds[]").getParameters()) {
				ids.add(Long.parseLong((String) id));
			}
		}

		// Forced to get data with two requests so :
		// get requirement and requirementLibraryNode
		String hqlQueryFirstStep = "select r, rld from Requirement r, RequirementLibraryNode rld where r.id = rld.id";
		// get the folders
		String hqlQuerySecondStep = "from RequirementFolder";

		// Add projects id parameter if defined
		if (isProjectIds) {
			hqlQueryFirstStep += " and r.project in (:projectIds)";
			hqlQuerySecondStep += " where project in (:projectIds)";
		}

		Query queryFirstStep = session.createQuery(hqlQueryFirstStep);
		Query querySecondStep = session.createQuery(hqlQuerySecondStep);

		// Set the parameter if defined
		if (isProjectIds) {
			queryFirstStep.setParameterList("projectIds", ids, LongType.INSTANCE);
			querySecondStep.setParameterList("projectIds", ids, LongType.INSTANCE);
		}
		// Get the folders
		List<RequirementFolder> folderList = querySecondStep.list();

		// Initiate result list
		List<Object[]> results = new ArrayList<Object[]>();
		// Browse the requirement and requirementLibraryNode to get the folder which contains the requirement and fill
		// the result list
		for (Object[] requirements : (List<Object[]>) queryFirstStep.list()) {
			Object[] resultArray = new Object[2];
			// First the requirement
			resultArray[0] = requirements[0];
			resultArray[1] = null;
			// check the folder
			for (RequirementFolder folder : folderList) {
				if (folder.getContent().contains(requirements[1])) {
					// Add the folder if it exists
					resultArray[1] = folder;
				}
			}
			// add to the result list
			results.add(resultArray);
		}

		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<?> convertToDto(List<?> rawData) {
		// a bit of cleaning first.
		List<Object[]> filteredData = filterUnwantedDataOut((List<Object[]>) rawData);
		// Browse data
		Map<Long, ReqCoverageByTestProjectDto> projectList = populateRequirementDtosAndUpdateProjectStatistics(filteredData);
		// Create projectTotals to calculate... Totals
		ReqCoverageByTestProjectDto projectTotals = createProjectDto(TOTAL_PROJECT_NAME);
		// Now that we have all requirement numbers, we can update projectDto rates
		calculateProjectsCoverageRates(projectList, projectTotals);
		// update projectTotals rates
		calculateProjectCoverageRates(projectTotals);
		// add it to the list. We only need the sorted Map values...
		List<ReqCoverageByTestProjectDto> toReturn = new ArrayList<ReqCoverageByTestProjectDto>(projectList.values());

		toReturn.add(projectTotals);

		return toReturn;
	}

	private void calculateProjectsCoverageRates(Map<Long, ReqCoverageByTestProjectDto> projectList,
			ReqCoverageByTestProjectDto projectTotals) {
		for (ReqCoverageByTestProjectDto project : projectList.values()) {
			// Update current project rate
			calculateProjectCoverageRates(project);
			// update projectTotals
			projectTotals.increaseTotals(project.getTotalRequirementNumber(),
					project.getTotalVerifiedRequirementNumber(), project.getCriticalRequirementNumber(),
					project.getCriticalVerifiedRequirementNumber(), project.getMajorRequirementNumber(),
					project.getMajorVerifiedRequirementNumber(), project.getMinorRequirementNumber(),
					project.getMinorVerifiedRequirementNumber(), project.getUndefinedRequirementNumber(),
					project.getUndefinedVerifiedRequirementNumber());
		}
	}

	private Map<Long, ReqCoverageByTestProjectDto> populateRequirementDtosAndUpdateProjectStatistics(
			List<Object[]> filteredData) {
		// First initiate the projectDTO map, project id is the key
		Map<Long, ReqCoverageByTestProjectDto> projectList = new HashMap<Long, ReqCoverageByTestProjectDto>();
		for (Object[] objects : filteredData) {
			// Current project
			ReqCoverageByTestProjectDto currentProject;
			// easier to identify the two objects this way
			Requirement requirement = ((Requirement) objects[0]);
			RequirementFolder folder = ((RequirementFolder) objects[1]);
			// Get current project Id from requirement
			Long projectId = requirement.getProject().getId();

			// Create the requirementSingle
			ReqCoverageByTestRequirementSingleDto requirementSingleDto = createRequirementSingleDto(requirement, folder);

			currentProject = findProjectDto(projectList, requirement, projectId);
			// add the requirement
			currentProject.addRequirement(requirementSingleDto);
			// update statistics and the project in the Map
			updateProjectStatistics(currentProject, requirementSingleDto);
		}
		return projectList;
	}

	/**
	 * check if the project is not here and create if necessary
	 * 
	 * @param projectList
	 * @param requirement
	 * @param projectId
	 * @return
	 */
	private ReqCoverageByTestProjectDto findProjectDto(Map<Long, ReqCoverageByTestProjectDto> projectList,
			Requirement requirement, Long projectId) {
		ReqCoverageByTestProjectDto currentProject;
		if (!projectList.containsKey(projectId)) {
			currentProject = createProjectDto(requirement);
			// add to the Map
			projectList.put(projectId, currentProject);
		} else {
			// ... or find it
			currentProject = projectList.get(projectId);
		}
		return currentProject;
	}

	private ReqCoverageByTestProjectDto createProjectDto(Requirement requirement) {
		String projectName = requirement.getProject().getName();
		return createProjectDto(projectName);
	}

	private ReqCoverageByTestProjectDto createProjectDto(String projectName) {
		ReqCoverageByTestProjectDto currentProject;
		// Create the projectDto...
		currentProject = new ReqCoverageByTestProjectDto();
		currentProject.setProjectName(projectName);
		return currentProject;
	}

	protected List<Object[]> filterUnwantedDataOut(List<Object[]> list) {
		List<Object[]> toReturn = new LinkedList<Object[]>();

		for (Object[] array : list) {
			Requirement requirement = (Requirement) array[0];
			if (getDataFilteringService().isFullyAllowed(requirement)) {
				toReturn.add(array);
			}
		}

		return toReturn;
	}

	/***
	 * This method create a new ReqCoverageByTestRequirementSingleDto with informations from requirement and the name of
	 * the requirement folder if it exists
	 * 
	 * @param requirement
	 *            the requirement
	 * @param folder
	 *            the requirementFolder
	 * @return a ReqCoverageByTestRequirementSingleDto
	 */
	private ReqCoverageByTestRequirementSingleDto createRequirementSingleDto(Requirement requirement,
			RequirementFolder folder) {
		ReqCoverageByTestRequirementSingleDto requirementSingleDto = new ReqCoverageByTestRequirementSingleDto();
		requirementSingleDto.setLabel(requirement.getName());
		requirementSingleDto.setReference(requirement.getReference());
		requirementSingleDto.setCriticality(requirement.getCriticality());
		// XXX RequirementVersion
//		requirementSingleDto.setAssociatedTestCaseNumber(requirement.getVerifyingTestCase().size());
		if (folder != null) {
			requirementSingleDto.setFolder(folder.getName());
		}
		return requirementSingleDto;
	}

	/***
	 * This method update the projectDto statistics
	 * 
	 * @param project
	 * @param requirementSingleDto
	 */
	private void updateProjectStatistics(ReqCoverageByTestProjectDto project,
			ReqCoverageByTestRequirementSingleDto requirementSingleDto) {
		project.incrementNumber(ReqCoverageByTestStatType.TOTAL);
		// if verified by testCase
		boolean isVerifiedByTestCase = false;
		if (requirementSingleDto.hasAssociatedTestCases()) {
			isVerifiedByTestCase = true;
			project.incrementNumber(ReqCoverageByTestStatType.TOTAL_VERIFIED);
		}
		// TODO replace swich by RCBST.convertVerified and convert
		switch (requirementSingleDto.getCriticality()) {
		case CRITICAL:
			project.incrementNumber(ReqCoverageByTestStatType.CRITICAL);
			if (isVerifiedByTestCase) {
				project.incrementNumber(ReqCoverageByTestStatType.CRITICAL_VERIFIED);
			}
			break;
		case MAJOR:
			project.incrementNumber(ReqCoverageByTestStatType.MAJOR);
			if (isVerifiedByTestCase) {
				project.incrementNumber(ReqCoverageByTestStatType.MAJOR_VERIFIED);
			}
			break;
		case MINOR:
			project.incrementNumber(ReqCoverageByTestStatType.MINOR);
			if (isVerifiedByTestCase) {
				project.incrementNumber(ReqCoverageByTestStatType.MINOR_VERIFIED);
			}
			break;
		case UNDEFINED:
			project.incrementNumber(ReqCoverageByTestStatType.UNDEFINED);
			if (isVerifiedByTestCase) {
				project.incrementNumber(ReqCoverageByTestStatType.UNDEFINED_VERIFIED);
			}
			break;

		default:
			// useless here
			break;
		}
	}

	/***
	 * Method which sets all project's rates
	 * 
	 * @param givenProject
	 *            the project to modify
	 */
	private void calculateProjectCoverageRates(ReqCoverageByTestProjectDto givenProject) {
		// Global rate
		givenProject.setGlobalRequirementCoverage(caluculateAndRoundRate(
				givenProject.getTotalVerifiedRequirementNumber(), givenProject.getTotalRequirementNumber()));
		// Critical rate
		givenProject.setCriticalRequirementCoverage(caluculateAndRoundRate(
				givenProject.getCriticalVerifiedRequirementNumber(), givenProject.getCriticalRequirementNumber()));
		// Major rate
		givenProject.setMajorRequirementCoverage(caluculateAndRoundRate(
				givenProject.getMajorVerifiedRequirementNumber(), givenProject.getMajorRequirementNumber()));
		// Minor rate
		givenProject.setMinorRequirementCoverage(caluculateAndRoundRate(
				givenProject.getMinorVerifiedRequirementNumber(), givenProject.getMinorRequirementNumber()));
		// Undefined rate
		givenProject.setUndefinedRequirementCoverage(caluculateAndRoundRate(
				givenProject.getUndefinedVerifiedRequirementNumber(), givenProject.getUndefinedRequirementNumber()));
	}

	/***
	 * This method returns the rate calculated from the given values
	 * 
	 * @param verifiedNumber
	 *            the number of verified requirements
	 * @param totalNumber
	 *            the total number of requirement
	 * @return the rate (byte)
	 */
	private byte caluculateAndRoundRate(Long verifiedNumber, Long totalNumber) {
		Double result = DEFAULT_RATE_VALUE;
		if (totalNumber > 0) {
			result = ((double) verifiedNumber * 100 / (double) totalNumber);
		}
		// round
		result = Math.floor(result + 0.5);
		return result.byteValue();
	}

}
