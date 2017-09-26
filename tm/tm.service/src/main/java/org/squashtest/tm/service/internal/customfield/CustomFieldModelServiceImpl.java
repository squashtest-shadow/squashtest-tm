package org.squashtest.tm.service.internal.customfield;

import org.apache.commons.lang3.EnumUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.MultiSelectField;
import org.squashtest.tm.service.customfield.CustomFieldModelService;
import org.squashtest.tm.service.internal.dto.CustomFieldModel;
import org.squashtest.tm.service.internal.dto.CustomFieldModelFactory;
import org.squashtest.tm.service.internal.dto.InputTypeModel;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.squashtest.tm.jooq.domain.Tables.CUSTOM_FIELD;
import static org.squashtest.tm.jooq.domain.Tables.CUSTOM_FIELD_BINDING;
import static org.squashtest.tm.jooq.domain.Tables.CUSTOM_FIELD_OPTION;

@Service
@Transactional
public class CustomFieldModelServiceImpl implements CustomFieldModelService {

	@Inject
	private DSLContext DSL;

	@Inject
	private MessageSource messageSource;

	@Override
	public Map<Long, CustomFieldModel> findUsedCustomFields(List<Long> projectIds) {
		List<Long> usedCufIds = findUsedCustomFieldIds(projectIds);
		return findCufMap(usedCufIds);
	}

	protected List<Long> findUsedCustomFieldIds(List<Long> readableProjectIds) {
		return DSL
			.selectDistinct(CUSTOM_FIELD_BINDING.CF_ID)
			.from(CUSTOM_FIELD_BINDING)
			.where(CUSTOM_FIELD_BINDING.BOUND_PROJECT_ID.in(readableProjectIds))
			.fetch(CUSTOM_FIELD_BINDING.CF_ID, Long.class);
	}

	protected Map<Long, CustomFieldModel> findCufMap(List<Long> usedCufIds) {
		Map<Long, CustomFieldModel> cufMap = new HashMap<>();

		DSL.select(CUSTOM_FIELD.CF_ID, CUSTOM_FIELD.INPUT_TYPE, CUSTOM_FIELD.NAME, CUSTOM_FIELD.LABEL, CUSTOM_FIELD.CODE, CUSTOM_FIELD.OPTIONAL, CUSTOM_FIELD.DEFAULT_VALUE, CUSTOM_FIELD.LARGE_DEFAULT_VALUE
			, CUSTOM_FIELD_OPTION.CODE, CUSTOM_FIELD_OPTION.LABEL, CUSTOM_FIELD_OPTION.POSITION)
			.from(CUSTOM_FIELD)
			.leftJoin(CUSTOM_FIELD_OPTION).using(CUSTOM_FIELD.CF_ID)
			.where(CUSTOM_FIELD.CF_ID.in(usedCufIds))
			.fetch()
			.forEach(r -> {
				Long cufId = r.get(CUSTOM_FIELD.CF_ID);
				String type = r.get(CUSTOM_FIELD.INPUT_TYPE);
				InputType inputType = EnumUtils.getEnum(InputType.class, type);
				switch (inputType) {
					case RICH_TEXT:
						CustomFieldModel richTextCustomFieldModel = getRichTextCustomFieldModel(r);
						cufMap.put(richTextCustomFieldModel.getId(), richTextCustomFieldModel);
						break;
					//here is the not fun case
					//as we have made a left join, we can have the first tuple witch need to be treated as a cuf AND an option
					//or subsequent tuple witch must be treated only as option...
					case DROPDOWN_LIST:
						if (cufMap.containsKey(cufId)) {
							CustomFieldModelFactory.SingleSelectFieldModel singleSelectFieldModel = (CustomFieldModelFactory.SingleSelectFieldModel) cufMap.get(cufId);
							singleSelectFieldModel.addOption(getCufValueOptionModel(r));
						} else {
							CustomFieldModelFactory.SingleSelectFieldModel singleSelectFieldModel = getSingleSelectFieldModel(r);
							singleSelectFieldModel.addOption(getCufValueOptionModel(r));
							cufMap.put(singleSelectFieldModel.getId(), singleSelectFieldModel);
						}
						break;

					case DATE_PICKER:
						CustomFieldModel datePickerCustomFieldModel = getDatePickerCustomFieldModel(r);
						cufMap.put(datePickerCustomFieldModel.getId(), datePickerCustomFieldModel);
						break;

					case TAG:
						if (cufMap.containsKey(cufId)) {
							CustomFieldModelFactory.MultiSelectFieldModel multiSelectFieldModel = (CustomFieldModelFactory.MultiSelectFieldModel) cufMap.get(cufId);
							multiSelectFieldModel.addOption(getCufValueOptionModel(r));
						} else {
							CustomFieldModelFactory.MultiSelectFieldModel multiSelectFieldModel = getMultiSelectFieldModel(r);
							multiSelectFieldModel.addOption(getCufValueOptionModel(r));
							cufMap.put(multiSelectFieldModel.getId(), multiSelectFieldModel);
						}
						break;

					default:
						CustomFieldModel cufModel = getSingleValueCustomFieldModel(r);
						cufMap.put(cufId, cufModel);
				}
			});

		return cufMap;
	}

	private CustomFieldModelFactory.MultiSelectFieldModel getMultiSelectFieldModel(Record r) {
		CustomFieldModelFactory.MultiSelectFieldModel multiSelectFieldModel = new CustomFieldModelFactory.MultiSelectFieldModel();
		initCufModel(r, multiSelectFieldModel);
		for (String value : r.get(CUSTOM_FIELD.DEFAULT_VALUE).split(MultiSelectField.SEPARATOR_EXPR)) {
			multiSelectFieldModel.addDefaultValue(value);
		}
		return multiSelectFieldModel;
	}

	private CustomFieldModelFactory.SingleSelectFieldModel getSingleSelectFieldModel(Record r) {
		CustomFieldModelFactory.SingleSelectFieldModel singleSelectFieldModel = new CustomFieldModelFactory.SingleSelectFieldModel();
		initCufModel(r, singleSelectFieldModel);
		singleSelectFieldModel.setDefaultValue(r.get(CUSTOM_FIELD.DEFAULT_VALUE));
		return singleSelectFieldModel;
	}

	private CustomFieldModelFactory.CustomFieldOptionModel getCufValueOptionModel(Record r) {
		CustomFieldModelFactory.CustomFieldOptionModel optionModel = new CustomFieldModelFactory.CustomFieldOptionModel();
		optionModel.setCode(r.get(CUSTOM_FIELD_OPTION.CODE));
		optionModel.setLabel(r.get(CUSTOM_FIELD_OPTION.LABEL));
		return optionModel;
	}

	private CustomFieldModel getDatePickerCustomFieldModel(Record r) {
		CustomFieldModelFactory.DatePickerFieldModel cufModel = new CustomFieldModelFactory.DatePickerFieldModel();
		initCufModel(r, cufModel);
		Locale locale = LocaleContextHolder.getLocale();
		cufModel.setFormat(getMessage("squashtm.dateformatShort.datepicker"));
		cufModel.setLocale(locale.toString());
		cufModel.setDefaultValue(r.get(CUSTOM_FIELD.DEFAULT_VALUE));
		return cufModel;
	}

	private CustomFieldModel getRichTextCustomFieldModel(Record r) {
		CustomFieldModelFactory.SingleValuedCustomFieldModel cufModel = new CustomFieldModelFactory.SingleValuedCustomFieldModel();
		initCufModel(r, cufModel);
		cufModel.setDefaultValue(r.get(CUSTOM_FIELD.LARGE_DEFAULT_VALUE));
		return cufModel;
	}

	//Take care if you change the JOOQ request, the result can become incompatible.
	private CustomFieldModelFactory.SingleValuedCustomFieldModel getSingleValueCustomFieldModel(Record r) {
		CustomFieldModelFactory.SingleValuedCustomFieldModel cufModel = new CustomFieldModelFactory.SingleValuedCustomFieldModel();
		initCufModel(r, cufModel);
		cufModel.setDefaultValue(r.get(CUSTOM_FIELD.DEFAULT_VALUE));
		return cufModel;
	}

	private void initCufModel(Record r, CustomFieldModel cufModel) {
		cufModel.setId(r.get(CUSTOM_FIELD.CF_ID));
		cufModel.setCode(r.get(CUSTOM_FIELD.CODE));
		cufModel.setName(r.get(CUSTOM_FIELD.NAME));
		cufModel.setLabel(r.get(CUSTOM_FIELD.LABEL));
		cufModel.setOptional(r.get(CUSTOM_FIELD.OPTIONAL));

		cufModel.setDenormalized(false);

		InputTypeModel inputTypeModel = new InputTypeModel();
		String inputTypeKey = r.get(CUSTOM_FIELD.INPUT_TYPE);
		InputType inputType = EnumUtils.getEnum(InputType.class, inputTypeKey);
		inputTypeModel.setEnumName(inputTypeKey);
		inputTypeModel.setFriendlyName(getMessage(inputType.getI18nKey()));

		cufModel.setInputType(inputTypeModel);
	}

	private String getMessage(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, locale);
	}


}
