/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.batchexport;

import java.util.LinkedList;
import java.util.List;


public class TestCaseExportModel {
	
	
	
	
	
	
	
	
	
	
	public static final class TestCaseModel {
		
		private Long projectId;
		private String projectName;
		private String path;
		private Integer order;
		private Long id;
		private String reference;
		private String name;
		private boolean weightAuto;
		private String weight;
		private String nature;
		private String type;
		private String status;
		private String description;
		private String prerequisite;
		private int nbReq;
		private int nbCaller;
		private int nbAttachments;
		private String createdOn;
		private String createdBy;
		private String lastModifiedOn;
		private String lastModifiedBy;
		private List<CustomField> cufs = new LinkedList<CustomField>();
		
		public TestCaseModel(){
			super();
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
		
		public boolean isWeightAuto() {
			return weightAuto;
		}
		
		public void setWeightAuto(boolean weightAuto) {
			this.weightAuto = weightAuto;
		}
		
		public String getWeight() {
			return weight;
		}
		
		public void setWeight(String weight) {
			this.weight = weight;
		}
		
		public String getNature() {
			return nature;
		}
		
		public void setNature(String nature) {
			this.nature = nature;
		}
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public String getStatus() {
			return status;
		}
		
		public void setStatus(String status) {
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
		
		public int getNbReq() {
			return nbReq;
		}
		
		public void setNbReq(int nbReq) {
			this.nbReq = nbReq;
		}
		
		public int getNbCaller() {
			return nbCaller;
		}
		
		public void setNbCaller(int nbCaller) {
			this.nbCaller = nbCaller;
		}
		
		public int getNbAttachments() {
			return nbAttachments;
		}
		
		public void setNbAttachments(int nbAttachments) {
			this.nbAttachments = nbAttachments;
		}
		
		public String getCreatedOn() {
			return createdOn;
		}
		
		public void setCreatedOn(String createdOn) {
			this.createdOn = createdOn;
		}
		
		public String getCreatedBy() {
			return createdBy;
		}
		
		public void setCreatedBy(String createdBy) {
			this.createdBy = createdBy;
		}
		
		public String getLastModifiedOn() {
			return lastModifiedOn;
		}
		
		public void setLastModifiedOn(String lastModifiedOn) {
			this.lastModifiedOn = lastModifiedOn;
		}
		
		public String getLastModifiedBy() {
			return lastModifiedBy;
		}
		
		public void setLastModifiedBy(String lastModifiedBy) {
			this.lastModifiedBy = lastModifiedBy;
		}
		
		public void addCuf(CustomField cuf){
			cufs.add(cuf);
		}
		
		public List<CustomField> getCufs() {
			return cufs;
		}
		
	}
	
	
	public static final class TestStepModel{
		
		private String tcOwnerPath;
		private long tcOwnerId;
		private long id;
		private int order;
		private boolean isCallStep;
		private String action;
		private String result;
		private int nbReq;
		private int nbAttach;
		private List<CustomField> cufs = new LinkedList<CustomField>();
		
		
		public TestStepModel(){
			super();
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


		public boolean isCallStep() {
			return isCallStep;
		}


		public void setCallStep(boolean isCallStep) {
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


		public int getNbReq() {
			return nbReq;
		}


		public void setNbReq(int nbReq) {
			this.nbReq = nbReq;
		}


		public int getNbAttach() {
			return nbAttach;
		}


		public void setNbAttach(int nbAttach) {
			this.nbAttach = nbAttach;
		}

		public void addCuf(CustomField cuf){
			cufs.add(cuf);
		}

		public List<CustomField> getCufs() {
			return cufs;
		}
		
		
	}
	
	
	public static final class ParameterModel{
		
		private String tcOwnerPath;
		private long tcOwnerId;
		private long id;
		private String name;
		private String description;
		
		
		public ParameterModel(){
			super();
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
	
	
	public static final class DatasetModel{
		
		private String tcOwnerPath;
		private long ownerId;
		private long id;
		private String name;
		private String paramOwnerPath;
		private long paramOwnerId;
		private String paramName;
		private String paramValue;
		
		
		public DatasetModel(){
			super();
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
	
	
	public static final class CustomField{
		
		String code;
		String value;
		Datatype type;
		
		
		public CustomField(String code, String value) {
			super();
			this.code = code;
			this.value = value;
			this.type = Datatype.PLAIN_TEXT;
		}


		public CustomField(String code, String value, Datatype type) {
			super();
			this.code = code;
			this.value = value;
			this.type = type;
		}
		
		public CustomField(String code, String value, String type) {
			super();
			this.code = code;
			this.value = value;
			this.type = Datatype.valueOf(type);
		}
		
		
		public String getCode() {
			return code;
		}
		public String getValue() {
			return value;
		}
		public Datatype getType() {
			return type;
		}
		
		
		
	}
	
	public static enum Datatype{
		PLAIN_TEXT,
		HTML,
		DATE;
	}

}
