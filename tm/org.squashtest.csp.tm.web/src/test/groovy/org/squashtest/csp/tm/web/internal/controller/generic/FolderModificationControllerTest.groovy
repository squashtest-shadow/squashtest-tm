/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.web.internal.controller.generic

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.NullArgumentException;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.library.Folder;
import org.squashtest.csp.tm.domain.library.LibraryNode;
import org.squashtest.csp.tm.service.FolderModificationService;

import spock.lang.Specification;

class FolderModificationControllerTest extends Specification {
	DummyFolderModificationController controller = new DummyFolderModificationController()
	FolderModificationService service = Mock()

	def setup() {
		controller.service = service
	}

	def "should return folder page fragment"() {
		given:
		HttpServletRequest req = Mock()
		req.getPathInfo() >> "/dummy-something/1"
		Folder f = Mock()
		service.findFolder(15) >> f

		when:
		ModelAndView res = controller.showFolder(15, req)

		then:
		res.viewName == "fragment/generics/edit-folder"
		res.modelMap['folder'] == f
	}
}


class DummyFolderModificationController extends FolderModificationController {
	FolderModificationService service

	@Override
	protected FolderModificationService getFolderModificationService() {
		return service;
	}
}