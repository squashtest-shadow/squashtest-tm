/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
package org.squashtest.tm.domain.dataset;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Store;
import org.squashtest.tm.domain.parameter.GlobalParameter;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.CollectionSizeBridge;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.tree.TreeEntity;

import javax.persistence.*;
import javax.validation.constraints.Size;

import java.util.*;

import static org.squashtest.tm.domain.dataset.GlobalDataset.DATASET_TYPE;

/**
 * @author aguilhem
 */
@Entity
@DiscriminatorValue(DATASET_TYPE)
public class GlobalDataset extends AbstractDataset implements TreeEntity<DatasetTreeEntityVisitor>, DatasetWorkspaceElement {

	static final String DATASET_TYPE = "GLOBAL";

	@Size(max = MAX_REF_SIZE)
	@Column
	private String reference;

	@Column
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String description;

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROJECT_ID")
	private Project project;

	@OneToMany(cascade = {CascadeType.ALL})
	@OrderColumn(name = "PARAM_ORDER")
	@JoinTable(name = "DATASET_PARAMETER", joinColumns = @JoinColumn(name = "DATASET_ID"), inverseJoinColumns = @JoinColumn(name = "PARAM_ID"))
	@Field(analyze = Analyze.NO, store = Store.YES)
	@FieldBridge(impl = CollectionSizeBridge.class)
	protected List<GlobalParameter> globalParameters = new ArrayList<>();

	@Override
	public void accept(DatasetTreeEntityVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public void setProject(Project project) {
		this.project = project;
	}

	@Override
	public String getReference() {
		return reference;
	}

	@Override
	public void setReference(String reference) {
		this.reference = reference;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public TreeEntity createCopy() {
		GlobalDataset copy = new GlobalDataset();
		copy.setProject(this.getProject());
		copy.setName(this.getName());
		copy.setDescription(this.getDescription());
		copy.setReference(this.getReference());
		return this.copyGlobalParametersAndParameterValues(copy);
	}

	//TODO keep GlobalParameters order when copying
	private GlobalDataset copyGlobalParametersAndParameterValues(GlobalDataset target) {
		List<GlobalParameter> unorderedParamList = new ArrayList<>();
		this.parameterValues.forEach(datasetParamValue -> {
			GlobalParameter globalParameterCopy = datasetParamValue.getParameter().createGlobalParameterTypeCopy();
			unorderedParamList.add(globalParameterCopy);

			DatasetParamValue paramValueCopy = datasetParamValue.createCopy();
			paramValueCopy.setParameter(globalParameterCopy);
			paramValueCopy.setDataset(target);
			target.addParameterValue(paramValueCopy);
		});
		target.setGlobalParameters(sortGlobalParameters(unorderedParamList));
		return target;
	}

	private List<GlobalParameter> sortGlobalParameters(List<GlobalParameter> unorderedParamList) {
		List<GlobalParameter> orderedParamList = new ArrayList<>();
		Iterator<GlobalParameter> iterator = this.globalParameters.iterator();
		GlobalParameter paramToRemove = new GlobalParameter();
		while (iterator.hasNext()){
			GlobalParameter param = iterator.next();
			for (GlobalParameter unorderedParam : unorderedParamList){
				if(param.getName().equals(unorderedParam.getName())){
					orderedParamList.add(unorderedParam);
					paramToRemove = unorderedParam;
				}
			}
			unorderedParamList.remove(paramToRemove);
		}
		return orderedParamList;
	}

	@Override
	public List<GlobalParameter> getGlobalParameters() {
		return globalParameters;
	}

	@Override
	public void setGlobalParameters(List<GlobalParameter> globalParameters) {
		this.globalParameters = globalParameters;
	}

	@Override
	public void addGlobalParameter(GlobalParameter globalParameter) {
		this.globalParameters.add(globalParameter);
	}

	@Override
	public void removeGlobalParameter(GlobalParameter globalParameter) {
		this.globalParameters.remove(globalParameter);
	}

	@Override
	public Long getId() {
		return id;
	}
}
