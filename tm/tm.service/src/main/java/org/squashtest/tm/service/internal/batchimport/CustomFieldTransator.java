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
package org.squashtest.tm.service.internal.batchimport;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gregory Fouquet
 * @since 1.14.0  04/05/16
 */
@Component
@Scope("prototype")
class CustomFieldTransator {
	private final Map<String, CustomFieldInfos> cufInfosCache = new HashMap<>();
	@Inject
	private CustomFieldDao cufDao;
	@Inject
	private PrivateCustomFieldValueService cufvalueService;
	/**
	 * because the service identifies cufs by their id, not their code<br/>
	 * also populates the cache (cufIdByCode), and transform the input data in a
	 * single string or a collection of string depending on the type of the
	 * custom field (Tags on non-tags).
	 */
	protected final Map<Long, RawValue> toAcceptableCufs(Map<String, String> origCufs) {

		Map<Long, RawValue> result = new HashMap<>(origCufs.size());

		for (Map.Entry<String, String> origCuf : origCufs.entrySet()) {
			String cufCode = origCuf.getKey();

			if (!cufInfosCache.containsKey(cufCode)) {

				CustomField customField = cufDao.findByCode(cufCode);

				// that bit of code checks that if the custom field doesn't
				// exist, the hashmap entry contains
				// a dummy value for this code.
				CustomFieldInfos infos = null;
				if (customField != null) {
					Long id = customField.getId();
					InputType type = customField.getInputType();
					infos = new CustomFieldInfos(id, type);
				}

				cufInfosCache.put(cufCode, infos);
			}

			// now add to our map the id of the custom field, except if null :
			// the custom field
			// does not exist and therefore wont be included.
			CustomFieldInfos infos = cufInfosCache.get(cufCode);
			if (infos != null) {
				switch (infos.getType()) {
					case TAG:
						List<String> values = Arrays.asList(origCuf.getValue().split("\\|"));
						result.put(infos.getId(), new RawValue(values));
						break;
					default:
						result.put(infos.getId(), new RawValue(origCuf.getValue()));
						break;
				}
			}
		}

		return result;

	}
}
