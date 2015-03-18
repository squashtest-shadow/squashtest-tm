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
package org.squashtest.tm.service.internal.advancedsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.Query;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModelType;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchRangeFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchSingleFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTagsFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTagsFieldModel.Operation;
import org.squashtest.tm.domain.search.AdvancedSearchTextFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTimeIntervalFieldModel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.advancedsearch.AdvancedSearchService;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.project.ProjectManagerService;

public class AdvancedSearchServiceImpl implements AdvancedSearchService {
	
	private final static List<String> MILESTONE_SEARCH_FIELD = Arrays.asList("milestone.label", "milestone.status",
			"milestone.endDate", "searchByMilestone");
       
	@Inject 
    private FeatureManager featureManager;

	@Inject
	private SessionFactory sessionFactory;
	
	@Inject
	private CustomFieldBindingFinderService customFieldBindingFinderService;

	@Inject
	protected ProjectManagerService projectFinder;

	private final static Integer EXPECTED_LENGTH = 7;

	@Override
	public List<CustomField> findAllQueryableCustomFieldsByBoundEntityType(BindableEntity entity) {

		Set<CustomField> result = new LinkedHashSet<CustomField>();

		List<Project> readableProjects = projectFinder.findAllReadable();
		for (Project project : readableProjects) {
			result.addAll(customFieldBindingFinderService.findBoundCustomFields(project.getId(), entity));
		}

		return new ArrayList<CustomField>(result);
	}

	private String padRawValue(Integer rawValue) {
		return StringUtils.leftPad(rawValue.toString(), EXPECTED_LENGTH, '0');
	}

	private Query buildLuceneRangeQuery(QueryBuilder qb, String fieldName, Integer minValue, Integer maxValue) {

		Query query = null;

		if (minValue == null) {

			String paddedMaxValue = padRawValue(maxValue);

			query = qb.bool()
					.must(qb.range().onField(fieldName).ignoreFieldBridge().below(paddedMaxValue).createQuery())
					.createQuery();

		} else if (maxValue == null) {

			String paddedMinValue = padRawValue(minValue);

			query = qb.bool()
					.must(qb.range().onField(fieldName).ignoreFieldBridge().above(paddedMinValue).createQuery())
					.createQuery();

		} else {

			String paddedMaxValue = padRawValue(maxValue);
			String paddedMinValue = padRawValue(minValue);

			query = qb
					.bool()
					.must(qb.range().onField(fieldName).ignoreFieldBridge().from(paddedMinValue).to(paddedMaxValue)
							.createQuery()).createQuery();
		}

		return query;
	}

	private Query buildLuceneValueInListQuery(QueryBuilder qb, String fieldName, List<String> values) {

		Query mainQuery = null;

		for (String value : values) {

			if ("".equals(value.trim())) {
				value = "$NO_VALUE";
			}

			Query query = qb
					.bool()
					.should(qb.keyword().onField(fieldName).ignoreFieldBridge().ignoreAnalyzer().matching(value)
							.createQuery()).createQuery();

			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().should(mainQuery).should(query).createQuery();
			}
		}

		return qb.bool().must(mainQuery).createQuery();
	}

	private Query buildLuceneSingleValueQuery(QueryBuilder qb, String fieldName, List<String> values, Locale locale) {

		Query mainQuery = null;

		for (String value : values) {

			Query query;

			if (value.contains("*")) {
				query = qb
						.bool()
						.must(qb.keyword().wildcard().onField(fieldName).ignoreFieldBridge().matching(value)
								.createQuery()).createQuery();
			} else {

				query = qb.bool()
						.must(qb.phrase().onField(fieldName).ignoreFieldBridge().sentence(value).createQuery())
						.createQuery();
			}

			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().must(mainQuery).must(query).createQuery();
			}
		}

		return mainQuery;
	}

	private Query buildLuceneTextQuery(QueryBuilder qb, String fieldName, List<String> values) {

		Query mainQuery = null;

		for (String value : values) {

			Query query;

			query = qb.bool().must(qb.phrase().onField(fieldName).ignoreFieldBridge().sentence(value).createQuery())
					.createQuery();
			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().must(mainQuery).must(query).createQuery();
			}
		}
		return mainQuery;
	}

	private Query buildLuceneTimeIntervalQuery(QueryBuilder qb, String fieldName, Date startdate, Date enddate) {

		Query query = qb
				.bool()
				.must(qb.range().onField(fieldName).ignoreFieldBridge()
						.from(DateTools.dateToString(startdate, DateTools.Resolution.DAY))
						.to(DateTools.dateToString(enddate, DateTools.Resolution.DAY)).createQuery()).createQuery();

		return query;
	}

	private Query buildLuceneTimeIntervalWithoutStartQuery(QueryBuilder qb, String fieldName, Date enddate) {

		Query query = qb
				.bool()
				.must(qb.range().onField(fieldName).ignoreFieldBridge()
						.below(DateTools.dateToString(enddate, DateTools.Resolution.DAY)).createQuery()).createQuery();

		return query;
	}

	private Query buildLuceneTimeIntervalWithoutEndQuery(QueryBuilder qb, String fieldName, Date startdate) {

		Query query = qb
				.bool()
				.must(qb.range().onField(fieldName).ignoreFieldBridge()
						.above(DateTools.dateToString(startdate, DateTools.Resolution.DAY)).createQuery())
						.createQuery();

		return query;
	}

	private Query buildQueryForSingleCriterium(String fieldKey, AdvancedSearchFieldModel fieldModel, QueryBuilder qb,
			Locale locale) {

		AdvancedSearchSingleFieldModel singleModel = (AdvancedSearchSingleFieldModel) fieldModel;
		if (singleModel.getValue() != null && !"".equals(singleModel.getValue().trim())) {
			List<String> inputs = parseInput(singleModel.getValue());

			return buildLuceneSingleValueQuery(qb, fieldKey, inputs, locale);
		}

		return null;
	}

	private Query buildQueryForListCriterium(String fieldKey, AdvancedSearchFieldModel fieldModel, QueryBuilder qb) {

		AdvancedSearchListFieldModel listModel = (AdvancedSearchListFieldModel) fieldModel;
		if (listModel.getValues() != null) {
			return buildLuceneValueInListQuery(qb, fieldKey, listModel.getValues());
		}

		return null;
	}

	private void addToTokens(List<String> tokens, String token) {
		if (StringUtils.isNotBlank(token)) {
			tokens.add(token);
		}

	}

	private List<String> parseInput(String textInput) {

		List<String> tokens = new ArrayList<String>();
		boolean inDoubleQuoteContext = false;
		char[] input = textInput.toCharArray();

		int start = 0;
		// if we encounter a double quote at the very start
		if (input[0] == '"') {
			inDoubleQuoteContext = true;
			start = 1;
		}

		// if we encounter a simple quote at the very start
		if (input[0] == '\'') {
			start = 1;
		}

		for (int i = 1; i < input.length; i++) {
			char charAtPosition = input[i];
			char charBeforePosition = input[i - 1];

			// if we encounter a blank while NOT in the context of a double quote
			if (isNewBlankInDoubleQuoteContext(inDoubleQuoteContext, charAtPosition, charBeforePosition)) {
				addToTokens(tokens, textInput.substring(start, i).trim());
				start = i + 1;
			}

			// treat apostrophes as word separators
			else if (isSimpleQuote(charAtPosition, charBeforePosition)) {
				addToTokens(tokens, textInput.substring(start, i).trim());
				start = i + 1;
			}

			// if we encounter a double quote
			else if (isDoubleQuote(charAtPosition, charBeforePosition)) {
				inDoubleQuoteContext = !inDoubleQuoteContext;
				addToTokens(tokens, textInput.substring(start, i).trim());
				start = i + 1;
			}

		}

		if (input[input.length - 1] != '"' && input[input.length - 1] != ' ') {
			addToTokens(tokens, textInput.substring(start, input.length).trim());
		}

		return tokens;
	}

	private boolean isSimpleQuote(char charAtPosition, char charBeforePosition) {
		return charAtPosition == '\'' && charBeforePosition != '\\';
	}

	private boolean isDoubleQuote(char charAtPosition, char charBeforePosition) {
		return charAtPosition == '"' && charBeforePosition != '\\';
	}

	private boolean isNewBlankInDoubleQuoteContext(boolean inDoubleQuoteContext, char charAtPosition,
			char charBeforePosition) {
		return charAtPosition == ' ' && charBeforePosition != ' ' && !inDoubleQuoteContext;
	}

	private Query buildQueryForTextCriterium(String fieldKey, AdvancedSearchFieldModel fieldModel, QueryBuilder qb) {
		AdvancedSearchTextFieldModel textModel = (AdvancedSearchTextFieldModel) fieldModel;
		if (textModel.getValue() != null && !"".equals(textModel.getValue().trim())) {
			List<String> inputs = parseInput(textModel.getValue());
			return buildLuceneTextQuery(qb, fieldKey, inputs);
		}

		return null;
	}

	private Query buildQueryForRangeCriterium(String fieldKey, AdvancedSearchFieldModel fieldModel, QueryBuilder qb) {
		AdvancedSearchRangeFieldModel rangeModel = (AdvancedSearchRangeFieldModel) fieldModel;
		if (rangeModel.getMinValue() != null || rangeModel.getMaxValue() != null) {
			return buildLuceneRangeQuery(qb, fieldKey, rangeModel.getMinValue(), rangeModel.getMaxValue());
		}

		return null;
	}

	private Query buildQueryForTimeIntervalCriterium(String fieldKey, AdvancedSearchFieldModel fieldModel,
			QueryBuilder qb) {
		AdvancedSearchTimeIntervalFieldModel intervalModel = (AdvancedSearchTimeIntervalFieldModel) fieldModel;
		Date startDate = intervalModel.getStartDate();
		Date endDate = intervalModel.getEndDate();
		Query query = null;
		if (startDate != null) {
			if (endDate != null) {
				query = buildLuceneTimeIntervalQuery(qb, fieldKey, startDate, endDate);
			} else {
				query = buildLuceneTimeIntervalWithoutEndQuery(qb, fieldKey, startDate);
			}
		} else {
			if (endDate != null) {
				query = buildLuceneTimeIntervalWithoutStartQuery(qb, fieldKey, endDate);
			}
		}

		return query;
	}

	private Query buildQueryForTagsCriterium(String fieldKey,  AdvancedSearchFieldModel fieldModel,
			QueryBuilder qb) {

		AdvancedSearchTagsFieldModel model = (AdvancedSearchTagsFieldModel) fieldModel;
		Query query = null;

		if (model == null){
			return query;
		}

		List<String> tags = model.getTags();
		Operation operation = model.getOperation();

		query = buildLuceneTagsQuery(qb, fieldKey, tags, operation);

		return query;

	}


	protected Query buildLuceneQuery(QueryBuilder qb, List<TestCase> testcaseList, Locale locale) {

		Query mainQuery = null;
		Query query = null;

		for (TestCase testcase : testcaseList) {
			List<String> id = new ArrayList<String>();
			id.add(testcase.getId().toString());
			query = buildLuceneSingleValueQuery(qb, "id", id, locale);

			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().should(mainQuery).should(query).createQuery();
			}
		}
		return mainQuery;
	}

	protected Query buildLuceneQuery(QueryBuilder qb, AdvancedSearchModel model, Locale locale) {
		if (featureManager.isEnabled(Feature.MILESTONE)){
			addMilestoneFilter(model);
			}
		Query mainQuery = null;

		Set<String> fieldKeys = model.getFields().keySet();

		for (String fieldKey : fieldKeys) {

		
			AdvancedSearchFieldModel fieldModel = model.getFields().get(fieldKey);
			AdvancedSearchFieldModelType type = fieldModel.getType();

			Query query = buildQueryDependingOnType(qb, locale, fieldKey, fieldModel, type);

			if (query != null) {
				if (mainQuery == null) {
					mainQuery = query;
				} else {
					mainQuery = qb.bool().must(mainQuery).must(query).createQuery();
				}
			}
			
		}

		return mainQuery;
	}

	@SuppressWarnings("unchecked")
	private void  addMilestoneFilter (AdvancedSearchModel searchModel){
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(Milestone.class);

		Map<String, AdvancedSearchFieldModel> fields = searchModel.getFields();
		
		
		AdvancedSearchSingleFieldModel searchByMilestone = (AdvancedSearchSingleFieldModel) fields.get("searchByMilestone");
		
		if ("true".equals(searchByMilestone.getValue())) {
		
		for (Entry<String, AdvancedSearchFieldModel> entry : fields.entrySet()) {

			AdvancedSearchFieldModel model = entry.getValue();
			if (model != null) {

				switch (entry.getKey()) {

				case "milestone.label":
					
					List<String> labelValues = ((AdvancedSearchListFieldModel) model).getValues();
					if (labelValues != null){
					crit.add(Restrictions.in("label", labelValues));
					}
					break;
				case "milestone.status":
					List<String> statusValues = ((AdvancedSearchListFieldModel) model).getValues();
					if (statusValues != null){
					crit.add(Restrictions.in("status", convertStatus(statusValues)));
					}
					break;
				case "milestone.endDate":
					Date startDate = ((AdvancedSearchTimeIntervalFieldModel) model).getStartDate();
					Date endDate = ((AdvancedSearchTimeIntervalFieldModel) model).getEndDate();
					
					if (startDate != null && endDate != null){
						crit.add(Restrictions.between("endDate", startDate, endDate));
					} else if (startDate != null){
						crit.add(Restrictions.gt("endDate", startDate));
					} else if (endDate != null){
						crit.add(Restrictions.le("endDate", endDate));
						
					}

					break;
				default:
					// do nothing
				}
			}
		}

	
	
		List<String> milestoneIds = new ArrayList<String>();
		for (Milestone milestone : (List<Milestone>) crit.list()){
			milestoneIds.add(String.valueOf(milestone.getId()));		
		}
		
		AdvancedSearchListFieldModel milestonesModel = new AdvancedSearchListFieldModel();
		milestonesModel.setValues(milestoneIds);

		fields.put("milestones.id", milestonesModel);
		}
		
		
		for (String s : MILESTONE_SEARCH_FIELD){
			fields.remove(s);
		}
		
	}
	private List<MilestoneStatus> convertStatus(List<String> values) {
		List<MilestoneStatus> status = new ArrayList<MilestoneStatus>();
		for (String value : values) {
			int level = Integer.valueOf(value.substring(0, 1));
			status.add(MilestoneStatus.getByLevel(level));
		}
		return status;
	}
	
	
	protected Query buildLuceneTagsQuery(QueryBuilder qb, String fieldKey, List<String> tags, Operation operation ){

		Query main = null;


		List<String> lowerTags = (List<String>)CollectionUtils.collect(tags, new Transformer() {
			@Override
			public Object transform(Object input) {
				return ((String)input).toLowerCase();
			}
		});

		switch(operation){
		case AND :
			Query query = null;
			for (String tag : lowerTags){
				query = qb.bool().must(qb.keyword().onField(fieldKey).ignoreFieldBridge().ignoreAnalyzer().matching(tag).createQuery()).createQuery();

				if (query == null){
					break;
				}
				if (main == null){
					main = query;
				}else {
					main = qb.bool().must(main).must(query).createQuery();
				}
			}

			return qb.bool().must(main).createQuery();

		case OR :
			return buildLuceneValueInListQuery(qb, fieldKey, lowerTags);

		default :
			throw new IllegalArgumentException("search on tag '"+fieldKey+"' : operation unknown");

		}


	}



	private Query buildQueryDependingOnType(QueryBuilder qb, Locale locale, String fieldKey,
			AdvancedSearchFieldModel fieldModel, AdvancedSearchFieldModelType type) {
		Query query = null;
		switch (type) {
		case SINGLE:
			query = buildQueryForSingleCriterium(fieldKey, fieldModel, qb, locale);
			break;
		case LIST:
			query = buildQueryForListCriterium(fieldKey, fieldModel, qb);
			break;
		case TEXT:
			query = buildQueryForTextCriterium(fieldKey, fieldModel, qb);
			break;
		case RANGE:
			query = buildQueryForRangeCriterium(fieldKey, fieldModel, qb);
			break;
		case TIME_INTERVAL:
			query = buildQueryForTimeIntervalCriterium(fieldKey, fieldModel, qb);
			break;
		case TAGS :
			query = buildQueryForTagsCriterium(fieldKey, fieldModel, qb);
			break;
		default:
			break;
		}
		return query;
	}

}
