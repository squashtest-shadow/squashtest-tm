package org.squashtest.tm.service.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.squashtest.tm.domain.chart.ColumnPrototype;

/**
 * Created by jthebault on 29/11/2016.
 */
public interface ColumnPrototypeDao extends JpaRepository<ColumnPrototype, Long> {
	ColumnPrototype findByLabel(String label);
}
