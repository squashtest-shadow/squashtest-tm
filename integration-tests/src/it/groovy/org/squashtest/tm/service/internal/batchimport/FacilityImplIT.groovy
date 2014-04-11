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
package org.squashtest.tm.service.internal.batchimport;

import javax.inject.Inject
import javax.inject.Provider

import org.hibernate.SessionFactory
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.service.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport


@UnitilsSupport
@Transactional
@RunWith(Sputnik)
public class FacilityImplIT extends DbunitServiceSpecification {

	
	@Inject
	private SessionFactory sessionFactory
	
	@Inject
	Provider<SimulationFacility> simulatorProvider;
	
	@Inject
	Provider<FacilityImpl> implProvider;
	
	@Inject
	Provider<Model> modelProvider;	
		
	SimulationFacility simulator;
	
	FacilityImpl impl;
	
	Model model;
	
	def setup(){
		simulator = simulatorProvider.get();
		impl = implProvider.get();
		
		model = modelProvider.get();
		simulator.setModel(model);
		impl.setModel(model);
		impl.setSimulator(simulator);
	}
		
	
	
	@DataSet("batchimport.sandbox.xml")
	def "should return a failure because of basic checks"(){
		
		given :
			true
		
		when :
			true
		
		then :
			true
		
		
	}
		
}
