package org.squashtest.tm.service.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.squashtest.tm.domain.synchronisation.RemoteSynchronisation;

import java.util.List;

public interface RemoteSynchronisationDao extends JpaRepository<RemoteSynchronisation,Long> {

	List<RemoteSynchronisation> findByKind(String kind);
}
