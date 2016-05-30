/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.SingleSelectField;
public interface CustomFieldDao extends JpaRepository<CustomField, Long> {

    	
	@NativeMethodFromJpaRepository
	CustomField save(CustomField customField);

        
	@UsesTheSpringJpaDsl
	CustomField findById(long id);
        

	/**
         * This is a downcast-version of #findById
	 * will find the {@link SingleSelectField} of the given id
	 * @param customFieldId the id of the wanted {@link SingleSelectField}
	 * @return the {@link SingleSelectField} or <code>null</code>
	 */
        @Query("select ssf from SingleSelectField ssf where id = :customFieldId")
	SingleSelectField findSingleSelectFieldById(@Param("customFieldId") Long customFieldId);

	/**
	 * Will return the list of custom fields that can be bound to the given project and the given bindable entity (ie,
	 * those who aren't bound yet).
	 * 
	 * 
	 * @param projectId
	 * @param bindableEntity
	 * @return
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<CustomField> findAllBindableCustomFields(Long projectId, BindableEntity bindableEntity);
	
	
	/**
	 * returns the complementary of {@link #findAllBindableCustomFields(Long, BindableEntity)}
	 * 
	 * @param projectId
	 * @param bindableEntity
	 * @return
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<CustomField> findAllBoundCustomFields(Long projectId, BindableEntity bindableEntity);


	/**
	 * Returns the field matching the name if it exists.
	 * 
	 * @param name
	 * @return
	 */
	@UsesTheSpringJpaDsl
	CustomField findByName(@NotNull String name);

	/**
	 * Will find all custom fields and return them ordered by their name.
	 * 
	 * @return the list of all existing {@link CustomField} ordered by {@link CustomField#getName()}
	 */
	@UsesTheSpringJpaDsl
	// note : the extra 'By' in 'findAllByOrderBy' is necessary, see http://stackoverflow.com/questions/19733464/order-by-date-desc-with-spring-data
	List<CustomField> findAllByOrderByNameAsc();


        	
	/**
	 * Will find all existing custom fields ordered according to the given {@link Pageable} params.
	 * 
	 * @param filter the {@link Pageable} param that holds order and paging requirements.
	 * @return the list of all existing {@link CustomField} ordered according to the {@link Pageable} param
	 */
        @UsesTheSpringJpaDsl
	Page<CustomField> findAll(Pageable filter);
        
	/**
	 * Will count all existing custom fields
	 * 
	 * @return the number of custom fields
	 */
	@NativeMethodFromJpaRepository
	long count();

	/**
	 * Will find the CustomField having a code value matching the parameter.
	 * 
	 * @param code
	 * @return the {@link CustomField} matching the code param.
	 */
	@UsesTheSpringJpaDsl
	CustomField findByCode(@NotNull String code);
        
        
}
