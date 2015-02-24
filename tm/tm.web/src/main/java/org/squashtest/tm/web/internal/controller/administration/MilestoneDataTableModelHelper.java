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
package org.squashtest.tm.web.internal.controller.administration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

public class MilestoneDataTableModelHelper  extends DataTableModelBuilder<Milestone> {
	
	private InternationalizationHelper messageSource;
	private Locale locale;
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public InternationalizationHelper getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(InternationalizationHelper messageSource) {
		this.messageSource = messageSource;
	}
	
	public MilestoneDataTableModelHelper(InternationalizationHelper messageSource) {
		this.messageSource = messageSource;
	}
	
	@Override
	protected Object buildItemData(Milestone item) {
		Map<String, Object> row = new HashMap<String, Object>(12);
		final AuditableMixin auditable = (AuditableMixin) item;
		row.put("entity-id", item.getId());
		row.put("index", getCurrentIndex() +1);
		row.put("label", item.getLabel());
		row.put("nbOfProjects", item.getNbOfBindedProject());
		row.put("description", item.getDescription());
		row.put("range",i18nRange(item.getRange()));
		row.put("owner", ownerToPrint(item));
		row.put("status", i18nStatus(item.getStatus()));
		row.put("binded-to-objects",messageSource.internationalizeYesNo(item.isBoundToObjects() ,locale));
		row.put("endDate",  messageSource.localizeDate(item.getEndDate(), locale));
		row.put("created-on", messageSource.localizeDate(auditable.getCreatedOn(), locale));
		row.put("created-by", auditable.getCreatedBy());
		row.put("last-mod-on", messageSource.localizeDate(auditable.getLastModifiedOn(), locale));
		row.put("last-mod-by", auditable.getLastModifiedBy());
		row.put("delete", "");
		row.put("checkbox", "");
		
		return row;
	}
	
	private Object ownerToPrint(Milestone item) {
		String owner = null;
		if (item.getRange().equals(MilestoneRange.GLOBAL)){
			owner = messageSource.internationalize("label.milestone.global.owner", locale);
		} else {
			owner = item.getOwner().getName();
		}
		return owner;
	}

	private String i18nRange(final MilestoneRange milestoneRange){
		final String i18nKey = milestoneRange.getI18nKey();
		return  messageSource.internationalize(i18nKey, locale);
	}
	
private String i18nStatus(final MilestoneStatus milestoneStatus){
	final String i18nKey = milestoneStatus.getI18nKey();
	return  messageSource.internationalize(i18nKey, locale);
	}

}
