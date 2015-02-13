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
package org.squashtest.tm.service.internal.security;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.oauth2.Client;
import org.squashtest.tm.service.internal.repository.ClientDao;
import org.squashtest.tm.service.security.OAuth2ClientService;

@Service("squashtest.tm.service.OAuth2ClientService")
public class OAuth2ClientServiceImpl implements OAuth2ClientService{

	@Inject
	ClientDao clientDao;

	@Override
	public List<Client> findClientList() {

		return clientDao.findClientsOrderedByName();
	}

	@Override
	public void addClient(String name, String secret) {
		Client client = new Client(name, secret);
		clientDao.persist(client);

	}

	@Override
	public void removeClient(Long id) {
		Client client = clientDao.findById(id);
		clientDao.remove(client);
	}

	@Override
	public void changeClientSecret(String name, String newSecret) {
		Client client = clientDao.findClientByName(name);
		client.setClientSecret(newSecret);
	}

	@Override
	public void removeClients(List<Long> idList) {
		for(Long id : idList){
			removeClient(id);
		}
	}

	@Override
	public void addClient(Client client) {
		clientDao.persist(client);
	}

}
