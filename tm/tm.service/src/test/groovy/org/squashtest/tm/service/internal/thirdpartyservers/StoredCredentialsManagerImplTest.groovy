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
package org.squashtest.tm.service.internal.thirdpartyservers

import org.squashtest.tm.domain.thirdpartyservers.Credentials
import org.squashtest.tm.domain.thirdpartyservers.UsernamePasswordCredentials
import org.squashtest.tm.service.thirdpartyservers.EncryptionKeyChangedException
import spock.lang.Specification

class StoredCredentialsManagerImplTest extends Specification{

	StoredCredentialsManagerImpl manager

	def setup(){
		manager = new StoredCredentialsManagerImpl();
		manager.initialize()
	}

	// *************** test of the object mapper configuration ****************

	def "should serialize credentials"(){

		given :

			UsernamePasswordCredentials creds = new UsernamePasswordCredentials("bob", "you'll never find it" as char[])

		when :
			String res = manager.objectMapper.writeValueAsString(creds)

		then :
			res == '{"@class":"org.squashtest.tm.domain.thirdpartyservers.UsernamePasswordCredentials","username":"bob","password":"you\'ll never find it"}'

	}


	def "should deserialize credentials"(){

		given :
			def str = '{"@class":"org.squashtest.tm.domain.thirdpartyservers.UsernamePasswordCredentials","username":"bob","password":"you\'ll never find it"}'

		when :
			def res = manager.objectMapper.readValue(str, Credentials.class)


		then :
			res instanceof UsernamePasswordCredentials
			res.username == "bob"
			res.password.join() == "you'll never find it"

	}


	def "should find that an error at deserialization comes from unknown credential implementation"(){

		given :
			def str = '{"@class":"unknown.credentials.Implementation","username":"bob"}'

		when :
			def ex = manager.investigateDeserializationError(str)

		then :
			ex instanceof RuntimeException


	}


	def "should find that an error at deserialization comes from failed decryption"(){

		given :
		def str = '0165584eddf6zer54ggf68h4fr6ty48ret'

		when :
		def ex = manager.investigateDeserializationError(str)

		then :
		ex instanceof EncryptionKeyChangedException


	}

}
