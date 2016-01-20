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
package org.squashtest.tm.service.internal.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.CustomFieldVisitor;
import org.squashtest.tm.domain.customfield.MultiSelectField;
import org.squashtest.tm.domain.customfield.RichTextField;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedSingleSelectField;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.service.denormalizedfield.DenormalizedFieldValueManager;
import org.squashtest.tm.service.internal.denormalizedField.PrivateDenormalizedFieldValueService;
import org.squashtest.tm.service.internal.repository.AttachmentDao;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao;
import org.squashtest.tm.service.internal.repository.ExecutionStepDao;

@Component
public class ExecutionStepModificationHelper {

	@Inject
	private AttachmentDao attachmentDao;

	@Inject
	private DenormalizedFieldValueManager denormalizedFieldValueManager;

	@Inject
	private CustomFieldValueDao customFieldValueDao;

	@Inject
	private ExecutionStepDao executionStepDao;

	@Inject
	private PrivateDenormalizedFieldValueService privateDenormalizedFieldValueService;

	public long doUpdateStep(List<ExecutionStep> toBeUpdated, Execution execution) {

		long firstModifiedIndex = -1;

		for (ExecutionStep execStep : toBeUpdated) {
			ActionTestStep step = (ActionTestStep) execStep.getReferencedTestStep();

			if (step == null) {
				execution.removeStep(execStep.getId());
				continue;
			}

			firstModifiedIndex = firstModifiedIndex < 0 ? execution.getStepIndex(execStep.getId()) : firstModifiedIndex;

			execStep.setAction(step.getAction());
			execStep.setExpectedResult(step.getExpectedResult());
			execStep.setExecutionStatus(ExecutionStatus.READY);

			privateDenormalizedFieldValueService.deleteAllDenormalizedFieldValues(execStep);
			privateDenormalizedFieldValueService.createAllDenormalizedFieldValues(step, execStep);

			// We need to remove attachment first, then clear the list.
			// All attachment are removed then added, this may be suboptimal.
			// Maybe some optimization may be required later.
			attachmentDao.removeAll(new ArrayList<Attachment>(execStep.getAttachmentList().getAllAttachments()));
			execStep.getAttachmentList().getAllAttachments().clear();

			for (Attachment actionStepAttach : step.getAllAttachments()) {
				Attachment clone = actionStepAttach.hardCopy();
				execStep.getAttachmentList().addAttachment(clone);
			}
			executionStepDao.persist(execStep);
		}
		return firstModifiedIndex;

	}

	public List<ExecutionStep> findStepsToUpdate(Execution execution) {
		List<ExecutionStep> execSteps = execution.getSteps();
		List<ExecutionStep> toBeUpdated = new ArrayList<ExecutionStep>();

		for (ExecutionStep eStep : execSteps) {
			ActionTestStep aStep = (ActionTestStep) eStep.getReferencedTestStep();
			if (!isStepEqual(eStep, aStep)) {
				toBeUpdated.add(eStep);
			}
		}

		return toBeUpdated;
	}

	private boolean isStepEqual(ExecutionStep eStep, ActionTestStep aStep) {
		return actionStepExist(aStep) && sameAction(eStep, aStep) && sameResult(eStep, aStep)
				&& sameAttach(eStep, aStep) && sameCufs(eStep, aStep);
	}

	private boolean sameCufs(ExecutionStep eStep, ActionTestStep aStep) {

		List<DenormalizedFieldValue> denormalizedFieldValues = denormalizedFieldValueManager.findAllForEntity(eStep);

		List<CustomFieldValue> originalValues = customFieldValueDao.findAllCustomValues(aStep.getId(),
				BindableEntity.TEST_STEP);

		// different number of CUF
		if (originalValues.size() != denormalizedFieldValues.size()) {
			return false;
		}

		for (DenormalizedFieldValue denormVal : denormalizedFieldValues) {

			CustomFieldValue origVal = denormVal.getCustomFieldValue();
			if (origVal == null || hasChanged(denormVal, origVal)) {
				return false;
			}

		}

		return true;
	}


	private boolean hasChanged(final DenormalizedFieldValue denormVal, final CustomFieldValue origVal) {

		final boolean[] hasChanged = { false };

		origVal.getCustomField().accept(new CustomFieldVisitor() {

			private void testValChange() {
				if (valueHasChanged(denormVal, origVal)) {
					hasChanged[0] = true;
				}
			}

			@Override
			public void visit(MultiSelectField multiselect) {
				testValChange();
			}

			@Override
			public void visit(RichTextField richField) {
				testValChange();

			}

			@Override
			public void visit(CustomField standardValue) {
				testValChange();

			}

			@Override
			public void visit(SingleSelectField selectField) {
				testValChange();
				testOptionsChange(selectField);
			}

			private void testOptionsChange(SingleSelectField selectField) {

				DenormalizedSingleSelectField denormSSF = (DenormalizedSingleSelectField) denormVal;

				if (!CollectionUtils.isEqualCollection(denormSSF.getOptions(), selectField.getOptions())) {
					hasChanged[0] = true;
				}
			}
		});


		return hasChanged[0];
	}

	private boolean valueHasChanged(DenormalizedFieldValue denormVal, CustomFieldValue origVal) {
		return !denormVal.getValue().equals(origVal.getValue());
	}

	private boolean actionStepExist(ActionTestStep aStep) {
		return aStep != null;
	}

	private boolean sameAction(ExecutionStep eStep, ActionTestStep aStep) {
		return eStep.getAction().equals(aStep.getAction());
	}

	private boolean sameResult(ExecutionStep eStep, ActionTestStep aStep) {
		return eStep.getExpectedResult().equals(aStep.getExpectedResult());
	}

	private boolean sameAttach(ExecutionStep eStep, ActionTestStep aStep) {

		Set<Attachment> eStepAttach = eStep.getAttachmentList().getAllAttachments();
		Set<Attachment> aStepAttach = aStep.getAllAttachments();

		if (eStepAttach.size() != aStepAttach.size()) {
			return false;
		}

		for (final Attachment aAttach : aStepAttach) {

			boolean exist = CollectionUtils.exists(eStepAttach, new Predicate() {

				@Override
				public boolean evaluate(Object eAttach) {
					Attachment toCompare = (Attachment) eAttach;
					boolean sameName = toCompare.getName().equals(aAttach.getName());
					boolean sameSize = toCompare.getSize().equals(aAttach.getSize());
					return sameName && sameSize;
				}
			});

			if (!exist) {
				return false;
			}

		}

		return true;
	}

}
