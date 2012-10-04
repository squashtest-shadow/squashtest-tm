/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.domain.customfield;

import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A CustomField which stores a single option selected from a list.
 * @author Gregory Fouquet
 */
@Entity
@DiscriminatorValue("SSF")
public class SingleSelectField extends CustomField {
    @Embedded
    private List<CustomFieldOption> options = new ArrayList<CustomFieldOption>();

    public void addOption(@NotBlank String label) {
       options.add(new CustomFieldOption(label));
    }
    public void removeOption(@NotBlank String label) {

    }

    public List<CustomFieldOption> getOptions() {
        return Collections.unmodifiableList(options);
    }
}
