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
package org.squashtest.tm.service.internal.batchexport;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;

public class ExportModel {
	private List<TestCaseModel> testCases = new LinkedList<TestCaseModel>();

	private List<TestStepModel> testSteps = new LinkedList<TestStepModel>();

	private List<ParameterModel> parameters = new LinkedList<ParameterModel>();

	private List<DatasetModel> datasets = new LinkedList<DatasetModel>();

	public ExportModel() {
		super();
	}

	public void setTestCases(List<TestCaseModel> models) {
		this.testCases = models;
	}

	public void setTestSteps(List<TestStepModel> models) {
		this.testSteps = models;
	}

	public void setParameters(List<ParameterModel> models) {
		this.parameters = models;
	}

	public void setDatasets(List<DatasetModel> models) {
		this.datasets = models;
	}

	public void addTestCaseModel(TestCaseModel model) {
		testCases.add(model);
	}

	public void addTestStepModel(TestStepModel model) {
		testSteps.add(model);
	}

	public void addParameterModel(ParameterModel model) {
		parameters.add(model);
	}

	public void addDatasetModel(DatasetModel model) {
		datasets.add(model);
	}

	public List<TestCaseModel> getTestCases() {
		return testCases;
	}

	public List<TestStepModel> getTestSteps() {
		return testSteps;
	}

	public List<ParameterModel> getParameters() {
		return parameters;
	}

	public List<DatasetModel> getDatasets() {
		return datasets;
	}

	public static final class TestCaseModel {
		public static final Comparator<TestCaseModel> COMPARATOR = new Comparator<ExportModel.TestCaseModel>() {
			@Override
			public int compare(TestCaseModel o1, TestCaseModel o2) {
				return o1.getPath().compareTo(o2.getPath());

			}
		};

		private Long projectId;
		private String projectName;
		private String path;
		private Integer order;
		private Long id;
		private String reference;
		private String name;
		private int weightAuto;
		private TestCaseImportance weight;
		private TestCaseNature nature;
		private TestCaseType type;
		private TestCaseStatus status;
		private String description;
		private String prerequisite;
		private Long nbReq;
		private Long nbCaller;
		private Long nbAttachments;
		private Date createdOn;
		private String createdBy;
		private Date lastModifiedOn;
		private String lastModifiedBy;
		private List<CustomField> cufs = new LinkedList<CustomField>();

		// that monster constructor will be used by Hibernate in a hql query
		public TestCaseModel(Long projectId, String projectName, Integer order, Long id, String reference, String name,
				Boolean weightAuto, TestCaseImportance weight, TestCaseNature nature, TestCaseType type,
				TestCaseStatus status, String description, String prerequisite, Long nbReq, Long nbCaller,
				Long nbAttachments, Date createdOn, String createdBy, Date lastModifiedOn, String lastModifiedBy) {

			super();
			this.projectId = projectId;
			this.projectName = projectName;
			this.order = order;
			this.id = id;
			this.reference = reference;
			this.name = name;
			this.weightAuto = weightAuto ? 1 : 0;
			this.weight = weight;
			this.nature = nature;
			this.type = type;
			this.status = status;
			this.description = description;
			this.prerequisite = prerequisite;
			this.nbReq = nbReq;
			this.nbCaller = nbCaller;
			this.nbAttachments = nbAttachments;
			this.createdOn = createdOn;
			this.createdBy = createdBy;
			this.lastModifiedOn = lastModifiedOn;
			this.lastModifiedBy = lastModifiedBy;
		}

		public Long getProjectId() {
			return projectId;
		}

		public void setProjectId(Long projectId) {
			this.projectId = projectId;
		}

		public String getProjectName() {
			return projectName;
		}

		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public Integer getOrder() {
			return order;
		}

		public void setOrder(Integer order) {
			this.order = order;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getReference() {
			return reference;
		}

		public void setReference(String reference) {
			this.reference = reference;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getWeightAuto() {
			return weightAuto;
		}

		public void setWeightAuto(int weightAuto) {
			this.weightAuto = weightAuto;
		}

		public TestCaseImportance getWeight() {
			return weight;
		}

		public void setWeight(TestCaseImportance weight) {
			this.weight = weight;
		}

		public TestCaseNature getNature() {
			return nature;
		}

		public void setNature(TestCaseNature nature) {
			this.nature = nature;
		}

		public TestCaseType getType() {
			return type;
		}

		public void setType(TestCaseType type) {
			this.type = type;
		}

		public TestCaseStatus getStatus() {
			return status;
		}

		public void setStatus(TestCaseStatus status) {
			this.status = status;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getPrerequisite() {
			return prerequisite;
		}

		public void setPrerequisite(String prerequisite) {
			this.prerequisite = prerequisite;
		}

		public Long getNbReq() {
			return nbReq;
		}

		public void setNbReq(Long nbReq) {
			this.nbReq = nbReq;
		}

		public Long getNbCaller() {
			return nbCaller;
		}

		public void setNbCaller(Long nbCaller) {
			this.nbCaller = nbCaller;
		}

		public Long getNbAttachments() {
			return nbAttachments;
		}

		public void setNbAttachments(Long nbAttachments) {
			this.nbAttachments = nbAttachments;
		}

		public Date getCreatedOn() {
			return createdOn;
		}

		public void setCreatedOn(Date createdOn) {
			this.createdOn = createdOn;
		}

		public String getCreatedBy() {
			return createdBy;
		}

		public void setCreatedBy(String createdBy) {
			this.createdBy = createdBy;
		}

		public Date getLastModifiedOn() {
			return lastModifiedOn;
		}

		public void setLastModifiedOn(Date lastModifiedOn) {
			this.lastModifiedOn = lastModifiedOn;
		}

		public String getLastModifiedBy() {
			return lastModifiedBy;
		}

		public void setLastModifiedBy(String lastModifiedBy) {
			this.lastModifiedBy = lastModifiedBy;
		}

		public void addCuf(CustomField cuf) {
			cufs.add(cuf);
		}

		public List<CustomField> getCufs() {
			return cufs;
		}

	}

	public static final class TestStepModel {
		public static final Comparator<TestStepModel> COMPARATOR = new Comparator<ExportModel.TestStepModel>() {
			@Override
			public int compare(TestStepModel o1, TestStepModel o2) {
				int comp1 = o1.getTcOwnerPath().compareTo(o2.getTcOwnerPath());
				if (comp1 == 0) {
					return o1.getOrder() - o2.getOrder();
				} else {
					return comp1;
				}
			}
		};

		private String tcOwnerPath;
		private long tcOwnerId;
		private long id;
		private int order;
		private Integer isCallStep;
		private String action;
		private String result;
		private Long nbReq;
		private Long nbAttach;
		private String calledDsName;
		private boolean delegateParameters;
		private List<CustomField> cufs = new LinkedList<CustomField>();


		// about delegateParameters : when fetching an action step we want to select the literal 'false'. However Hibernate
		// can't do that, hence it is here shipped as int before casting to Boolean.
		public TestStepModel(long tcOwnerId, long id, int order, Integer isCallStep, String action, String result,
				Long nbReq, Long nbAttach, String calledDsName, Integer delegateParameters) {

			super();
			this.tcOwnerId = tcOwnerId;
			this.id = id;
			this.order = order;
			this.isCallStep = isCallStep;
			this.action = action;
			this.result = result;
			this.nbReq = nbReq;
			this.nbAttach = nbAttach;
			this.calledDsName = calledDsName;	// special call steps
			this.delegateParameters = (delegateParameters==1) ? true : false; // special call steps
		}

		public String getTcOwnerPath() {
			return tcOwnerPath;
		}

		public void setTcOwnerPath(String tcOwnerPath) {
			this.tcOwnerPath = tcOwnerPath;
		}

		public long getTcOwnerId() {
			return tcOwnerId;
		}

		public void setTcOwnerId(long tcOwnerId) {
			this.tcOwnerId = tcOwnerId;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public int getOrder() {
			return order;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		public Integer getIsCallStep() {
			return isCallStep;
		}

		public void setIsCallStep(Integer isCallStep) {
			this.isCallStep = isCallStep;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public String getResult() {
			return result;
		}

		public void setResult(String result) {
			this.result = result;
		}

		public Long getNbReq() {
			return nbReq;
		}

		public void setNbReq(Long nbReq) {
			this.nbReq = nbReq;
		}

		public Long getNbAttach() {
			return nbAttach;
		}

		public void setNbAttach(Long nbAttach) {
			this.nbAttach = nbAttach;
		}

		public void addCuf(CustomField cuf) {
			cufs.add(cuf);
		}

		public List<CustomField> getCufs() {
			return cufs;
		}

		public String getDsName(){
			String name;
			if (delegateParameters){
				name = "INHERIT";
			}
			else {
				//inexact (we should explicitly treat the no-dataset scenario,
				// but conveniently sufficient here
				name = calledDsName;
			}
			return name;
		}

	}

	public static final class ParameterModel {
		public static final Comparator<ParameterModel> COMPARATOR = new Comparator<ExportModel.ParameterModel>() {
			@Override
			public int compare(ParameterModel o1, ParameterModel o2) {
				int comp1 = o1.getTcOwnerPath().compareTo(o2.getTcOwnerPath());
				if (comp1 == 0) {
					return o1.getName().compareTo(o2.getName());
				} else {
					return comp1;
				}
			}
		};

		private String tcOwnerPath;
		private long tcOwnerId;
		private long id;
		private String name;
		private String description;

		public ParameterModel(long tcOwnerId, long id, String name, String description) {
			super();
			this.tcOwnerId = tcOwnerId;
			this.id = id;
			this.name = name;
			this.description = description;
		}

		public String getTcOwnerPath() {
			return tcOwnerPath;
		}

		public void setTcOwnerPath(String tcOwnerPath) {
			this.tcOwnerPath = tcOwnerPath;
		}

		public long getTcOwnerId() {
			return tcOwnerId;
		}

		public void setTcOwnerId(long tcOwnerId) {
			this.tcOwnerId = tcOwnerId;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}

	public static final class DatasetModel  {
		public static final Comparator<DatasetModel> COMPARATOR = new Comparator<ExportModel.DatasetModel>() {
			@Override
			public int compare(DatasetModel o1, DatasetModel o2) {
				int comp1 = o1.getTcOwnerPath().compareTo(o2.getTcOwnerPath());
				int comp2 = o1.getName().compareTo(o2.getName());
				int comp3 = o1.getParamName().compareTo(o2.getParamName());
				return (comp1 != 0) ? comp1 : (comp2 != 0) ? comp2 : comp3;
			}
		};

		private String tcOwnerPath;
		private long ownerId;
		private long id;
		private String name;
		private String paramOwnerPath;
		private long paramOwnerId;
		private String paramName;
		private String paramValue;

		public DatasetModel(long ownerId, long id, String name, long paramOwnerId, String paramName, String paramValue) {
			super();
			this.ownerId = ownerId;
			this.id = id;
			this.name = name;
			this.paramOwnerId = paramOwnerId;
			this.paramName = paramName;
			this.paramValue = paramValue;
		}

		public String getTcOwnerPath() {
			return tcOwnerPath;
		}

		public void setTcOwnerPath(String tcOwnerPath) {
			this.tcOwnerPath = tcOwnerPath;
		}

		public long getOwnerId() {
			return ownerId;
		}

		public void setOwnerId(long ownerId) {
			this.ownerId = ownerId;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getParamOwnerPath() {
			return paramOwnerPath;
		}

		public void setParamOwnerPath(String paramOwnerPath) {
			this.paramOwnerPath = paramOwnerPath;
		}

		public long getParamOwnerId() {
			return paramOwnerId;
		}

		public void setParamOwnerId(long paramOwnerId) {
			this.paramOwnerId = paramOwnerId;
		}

		public String getParamName() {
			return paramName;
		}

		public void setParamName(String paramName) {
			this.paramName = paramName;
		}

		public String getParamValue() {
			return paramValue;
		}

		public void setParamValue(String paramValue) {
			this.paramValue = paramValue;
		}

	}

	public static final class CustomField {
		private Long ownerId;
		private BindableEntity ownerType;
		private String code;
		private String value;
		private InputType type;

		public CustomField(Long ownerId, BindableEntity ownerType, String code, String value, String longValue, InputType type) {
			super();
			this.ownerId = ownerId;
			this.ownerType = ownerType;
			this.code = code;
			this.value = (! StringUtils.isBlank(longValue)) ? longValue : value;
			this.type = type;
		}

		public Long getOwnerId() {
			return this.ownerId;
		}

		public BindableEntity getOwnerType() {
			return ownerType;
		}

		public String getCode() {
			return code;
		}

		public String getValue() {
			return value;
		}

		public InputType getType() {
			return type;
		}

	}

}
