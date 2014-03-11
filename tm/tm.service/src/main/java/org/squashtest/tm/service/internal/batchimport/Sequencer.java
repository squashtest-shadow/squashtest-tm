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

import java.io.File;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.stereotype.Component;
import org.squashtest.tm.service.importer.ImportLog;

@Component
public class Sequencer {

	
	@Inject
	private Provider<SimulationFacility> simulatorProvider;
	
	@Inject
	private Provider<FacilityImpl> facilityImplProvider;

	@Inject
	private Provider<Model> modelProvider;
	
	
	public ImportLog simulateImport(File excelFile){
		SimulationFacility simulator = simulatorProvider.get();
		Model model = modelProvider.get();
		simulator.setModel(model);
		
		// TODO : for each LogTrain, remember to use setForAll(...) to set them with 
		// the line number and import mode.
		
		return null;
	}
	
	public ImportLog performImport(File excelFile){
		SimulationFacility simulator = simulatorProvider.get();
		FacilityImpl impl = facilityImplProvider.get();
		
		Model model = modelProvider.get();
		simulator.setModel(model);
		impl.setModel(model);
		impl.setSimulator(simulator);
		
		// TODO : for each LogTrain, remember to use setForAll(...) to set them with 
		// the line number and import mode.
		
		
		return null;
	}
	
	
	
}
