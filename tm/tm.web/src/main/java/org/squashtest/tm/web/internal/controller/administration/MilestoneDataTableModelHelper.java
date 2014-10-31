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
		row.put("index", getCurrentIndex());
		row.put("label", item.getLabel());
		row.put("description", item.getDescription());
		row.put("range",i18nRange(item.getRange()));
		row.put("status", i18nStatus(item.getStatus()));
		row.put("endDate",  messageSource.localizeDate(item.getEndDate(), locale));
		row.put("created-on", messageSource.localizeDate(auditable.getCreatedOn(), locale));
		row.put("created-by", auditable.getCreatedBy());
		row.put("last-mod-on", messageSource.localizeDate(auditable.getLastModifiedOn(), locale));
		row.put("last-mod-by", auditable.getLastModifiedBy());
		row.put("delete", "");
		
		return row;
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
