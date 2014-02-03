/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.DateTools;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModelType;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchRangeFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchSingleFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTextFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTimeIntervalFieldModel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.advancedsearch.AdvancedSearchService;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.project.ProjectManagerService;

public  class AdvancedSearchServiceImpl implements AdvancedSearchService{

	
	@Inject
	private CustomFieldBindingFinderService customFieldBindingFinderService;
	

	@Inject
	protected ProjectManagerService projectFinder;

	private final static Integer EXPECTED_LENGTH = 7;
	
	
	public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
	

	@Override
	public List<CustomField> findAllQueryableCustomFieldsByBoundEntityType(
			BindableEntity entity) {

		Set<CustomField> result = new LinkedHashSet<CustomField>();

		List<Project> readableProjects = projectFinder.findAllReadable();
		for (Project project : readableProjects) {
			result.addAll(customFieldBindingFinderService
					.findBoundCustomFields(project.getId(), entity));
		}

		return new ArrayList<CustomField>(result);
	}
	

	private String padRawValue(Integer rawValue){
		StringBuilder builder = new StringBuilder();
		int length = Integer.toString(rawValue).length();
		int zeroesToAdd = EXPECTED_LENGTH - length;
		for(int i=0; i<zeroesToAdd; i++){
			builder.append("0");
		}
		builder.append(Integer.toString(rawValue));
		return builder.toString();
	}

	private org.apache.lucene.search.Query buildLuceneRangeQuery(
			QueryBuilder qb, String fieldName, Integer minValue,
			Integer maxValue) {

		org.apache.lucene.search.Query query = null;
		
		
		if (minValue == null) {

			String paddedMaxValue = padRawValue(maxValue);
					
			query = qb
					.bool()
					.must(qb.range().onField(fieldName).ignoreFieldBridge()
							.below(paddedMaxValue).createQuery()).createQuery();

		} else if (maxValue == null) {

			String paddedMinValue = padRawValue(minValue);
			
			query = qb
					.bool()
					.must(qb.range().onField(fieldName).ignoreFieldBridge()
							.above(paddedMinValue).createQuery()).createQuery();

		} else {

			String paddedMaxValue = padRawValue(maxValue);
			String paddedMinValue = padRawValue(minValue);
			
			query = qb
					.bool()
					.must(qb.range().onField(fieldName).ignoreFieldBridge()
							.from(paddedMinValue).to(paddedMaxValue).createQuery()).createQuery();
		}

		return query;
	}

	private org.apache.lucene.search.Query buildLuceneValueInListQuery(
			QueryBuilder qb, String fieldName, List<String> values) {

		org.apache.lucene.search.Query mainQuery = null;

		for (String value : values) {
			
			if("".equals(value.trim())){
				value = "$NO_VALUE";
			}
			
			org.apache.lucene.search.Query query = qb
					.bool()
					.should(qb.keyword().onField(fieldName).ignoreFieldBridge().ignoreAnalyzer()
							.matching(value).createQuery()).createQuery();

			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().should(mainQuery).should(query)
						.createQuery();
			}
		}

		return qb.bool().must(mainQuery).createQuery();
	}

	private org.apache.lucene.search.Query buildLuceneSingleValueQuery(
			QueryBuilder qb, String fieldName, List<String> values,
			boolean ignoreBridge, Locale locale) {


		org.apache.lucene.search.Query mainQuery = null;
		
		for(String value : values){
			
			org.apache.lucene.search.Query query;

			if (value.contains("*")){
				query = qb
						.bool()
						.must(qb.keyword().wildcard().onField(fieldName).ignoreFieldBridge()
								.matching(value.toLowerCase(locale)).createQuery()).createQuery();
			} else {
			
			/*if (ignoreBridge) {*/
				query = qb
						.bool()
						.must(qb.phrase().onField(fieldName).ignoreFieldBridge()
								.sentence(value).createQuery()).createQuery();
			/*} else {
				query = qb
						.bool()
						.must(qb.phrase().onField(fieldName).sentence(value)
								.createQuery()).createQuery();
			}*/}
			
			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().must(mainQuery).must(query).createQuery();
			}
		}


		return mainQuery;
	}

	private org.apache.lucene.search.Query buildLuceneTextQuery(
			QueryBuilder qb, String fieldName, List<String> values,
			boolean ignoreBridge) {

		org.apache.lucene.search.Query mainQuery = null;
		
		for(String value : values){
			
			org.apache.lucene.search.Query query;
	
			/*if (ignoreBridge) {*/
				query = qb
						.bool()
						.must(qb.phrase().onField(fieldName).ignoreFieldBridge().sentence(value).createQuery()).createQuery();
			/*} else {
				query = qb
						.bool()
						.must(qb.phrase().onField(fieldName).sentence(value).createQuery()).createQuery();
			}*/
			
			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().must(mainQuery).must(query).createQuery();
			}
		}
		return mainQuery;
	}

	private org.apache.lucene.search.Query buildLuceneTimeIntervalQuery(
			QueryBuilder qb, String fieldName, Date startdate, Date enddate) {

		org.apache.lucene.search.Query query = qb
				.bool()
				.must(qb.range()
						.onField(fieldName)
						.ignoreFieldBridge()
						.from(DateTools.dateToString(startdate,
								DateTools.Resolution.DAY))
						.to(DateTools.dateToString(enddate,
								DateTools.Resolution.DAY)).createQuery())
				.createQuery();

		return query;
	}

	private org.apache.lucene.search.Query buildLuceneTimeIntervalWithoutStartQuery(
			QueryBuilder qb, String fieldName, Date enddate) {

		org.apache.lucene.search.Query query = qb
				.bool()
				.must(qb.range()
						.onField(fieldName)
						.ignoreFieldBridge()
						.below(DateTools.dateToString(enddate,
								DateTools.Resolution.DAY)).createQuery())
				.createQuery();

		return query;
	}
	
	private org.apache.lucene.search.Query buildLuceneTimeIntervalWithoutEndQuery(
			QueryBuilder qb, String fieldName, Date startdate) {

		org.apache.lucene.search.Query query = qb
				.bool()
				.must(qb.range()
						.onField(fieldName)
						.ignoreFieldBridge()
						.above(DateTools.dateToString(startdate,
								DateTools.Resolution.DAY)).createQuery())
				.createQuery();

		return query;
	}
	
	private org.apache.lucene.search.Query buildQueryForSingleCriterium(
			String fieldKey, AdvancedSearchFieldModel fieldModel,
			QueryBuilder qb, boolean ignoreBridge, Locale locale) {

		AdvancedSearchSingleFieldModel singleModel = (AdvancedSearchSingleFieldModel) fieldModel;
		if (singleModel.getValue() != null
				&& !"".equals(singleModel.getValue().trim())) {
			List<String> inputs = parseInput(singleModel.getValue());
	
			return buildLuceneSingleValueQuery(qb, fieldKey,
					inputs, ignoreBridge, locale);
		}

		return null;
	}

	private org.apache.lucene.search.Query buildQueryForListCriterium(
			String fieldKey, AdvancedSearchFieldModel fieldModel,
			QueryBuilder qb) {

		AdvancedSearchListFieldModel listModel = (AdvancedSearchListFieldModel) fieldModel;
		if (listModel.getValues() != null) {
			return buildLuceneValueInListQuery(qb, fieldKey,
					listModel.getValues());
		}

		return null;
	}

	private void addToTokens(List<String> tokens, String token){
		if(StringUtils.isNotBlank(token)){
			tokens.add(token);
		}

	}
	
	private List<String> parseInput(String textInput){
		
		List<String> tokens = new ArrayList<String>();
		boolean inDoubleQuoteContext = false;
		char[] input = textInput.toCharArray();
		
		int start = 0;
		//if we encounter a double quote at the very start 
		if(input[0] == '"'){
			inDoubleQuoteContext = true;
			start = 1;
		}
		
		for(int i=1; i<input.length; i++){
			//if we encounter a blank while NOT in the context of a double quote
			if(input[i] == ' ' && input[i-1] != ' ' && !inDoubleQuoteContext){
				addToTokens(tokens, textInput.substring(start, i).trim());
				start = i+1;
			}
			
			//if we encounter a double quote while in the context of a double quote
			else if(input[i] == '"' && input[i-1] != '\\' && inDoubleQuoteContext){	
				addToTokens(tokens, textInput.substring(start, i).trim());
				inDoubleQuoteContext = false;
				start = i+1;
			}
			
			//if we encounter a double quote while NOT in the context of a double quote
			else if(input[i] == '"' && input[i-1] != '\\' && !inDoubleQuoteContext){
				addToTokens(tokens, textInput.substring(start, i).trim());
				inDoubleQuoteContext = true;
				start = i+1;
			}
			
		}
		
		if(input[input.length-1] != '"' && input[input.length-1] != ' '){
			addToTokens(tokens, textInput.substring(start, input.length).trim());
		}
		
		return tokens;
	}
	
	private org.apache.lucene.search.Query buildQueryForTextCriterium(
			String fieldKey, AdvancedSearchFieldModel fieldModel,
			QueryBuilder qb, boolean ignoreBridge) {
		AdvancedSearchTextFieldModel textModel = (AdvancedSearchTextFieldModel) fieldModel;
		if (textModel.getValue() != null && !"".equals(textModel.getValue().trim())) {
			List<String> inputs = parseInput(textModel.getValue());
			return buildLuceneTextQuery(qb, fieldKey, inputs, ignoreBridge);
		}

		return null;
	}

	private org.apache.lucene.search.Query buildQueryForRangeCriterium(
			String fieldKey, AdvancedSearchFieldModel fieldModel,
			QueryBuilder qb) {
		AdvancedSearchRangeFieldModel rangeModel = (AdvancedSearchRangeFieldModel) fieldModel;
		if (rangeModel.getMinValue() != null
				|| rangeModel.getMaxValue() != null) {
			return buildLuceneRangeQuery(qb, fieldKey,
					rangeModel.getMinValue(), rangeModel.getMaxValue());
		}

		return null;
	}

	private org.apache.lucene.search.Query buildQueryForTimeIntervalCriterium(
			String fieldKey, AdvancedSearchFieldModel fieldModel,
			QueryBuilder qb) {
		AdvancedSearchTimeIntervalFieldModel intervalModel = (AdvancedSearchTimeIntervalFieldModel) fieldModel;
		if (intervalModel.getStartDate() != null && intervalModel.getEndDate() != null) {
			return buildLuceneTimeIntervalQuery(qb, fieldKey, intervalModel.getStartDate(), intervalModel.getEndDate());
		} else if (intervalModel.getStartDate() == null && intervalModel.getEndDate() != null) {
			return buildLuceneTimeIntervalWithoutStartQuery(qb, fieldKey, intervalModel.getEndDate());
		} else if (intervalModel.getStartDate() != null && intervalModel.getEndDate() == null) {
			return buildLuceneTimeIntervalWithoutEndQuery(qb, fieldKey, intervalModel.getStartDate());
		}

		return null;
	}

	protected org.apache.lucene.search.Query buildLuceneQuery(QueryBuilder qb, List<TestCase> testcaseList, Locale locale) {
		
		org.apache.lucene.search.Query mainQuery = null;
		org.apache.lucene.search.Query query = null;
		
		for(TestCase testcase : testcaseList){
			List<String> id = new ArrayList<String>();
			id.add(testcase.getId().toString());
			query = buildLuceneSingleValueQuery(qb, "id", id, true, locale);
		
			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().should(mainQuery).should(query).createQuery();
			}
		}
		return mainQuery;
	}
	
	protected org.apache.lucene.search.Query buildLuceneQuery(QueryBuilder qb, AdvancedSearchModel model, Locale locale) {

		org.apache.lucene.search.Query mainQuery = null;
		org.apache.lucene.search.Query query = null;

		Set<String> fieldKeys = model.getFields().keySet();

		for (String fieldKey : fieldKeys) {

			AdvancedSearchFieldModel fieldModel = model.getFields().get(
					fieldKey);
			AdvancedSearchFieldModelType type = fieldModel.getType();
			boolean ignoreBridge = fieldModel.isIgnoreBridge();
			
			switch (type) {
				case SINGLE:
					query = buildQueryForSingleCriterium(fieldKey, fieldModel, qb, ignoreBridge, locale);
					break;
				case LIST:
					query = buildQueryForListCriterium(fieldKey, fieldModel, qb);
					break;
				case TEXT:
					query = buildQueryForTextCriterium(fieldKey, fieldModel, qb, ignoreBridge);
					break;
				case RANGE:
					query = buildQueryForRangeCriterium(fieldKey, fieldModel, qb);
					break;
				case TIME_INTERVAL:
					query = buildQueryForTimeIntervalCriterium(fieldKey, fieldModel, qb);
				default:
					break;
			}
			

			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().must(mainQuery).must(query).createQuery();
			}
		}

		return mainQuery;
	}
	

}
