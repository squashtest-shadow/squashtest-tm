package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.domain.oauth2.Client;

public interface CustomClientDao {

	List<Client> findClientsOrderedByName();

	Client findClientByName(String name);

	void persist(Client client);

	void remove(Client client);
}
