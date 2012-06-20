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
package org.squashtest.csp.tm.web.internal.controller.generic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.library.Folder;
import org.squashtest.csp.tm.service.FolderModificationService;

public abstract class FolderModificationController<FOLDER extends Folder<?>> {
	@RequestMapping(method = RequestMethod.GET)
	public final ModelAndView showFolder(@PathVariable long folderId, HttpServletRequest request) {
		FOLDER folder = getFolderModificationService().findFolder(folderId);

		ModelAndView mav = new ModelAndView("fragment/generics/edit-folder");
		mav.addObject("folder", folder);
		mav.addObject("updateUrl", getUpdateUrl(request.getPathInfo()));

		return mav;
	}

	protected abstract FolderModificationService<FOLDER> getFolderModificationService();

	@RequestMapping(method = RequestMethod.DELETE)
	public @ResponseBody
	String removeFolder(@PathVariable long folderId) {

		getFolderModificationService().removeFolder(folderId);
		return "ok";

	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	public @ResponseBody
	Object renameFolder(HttpServletResponse response, @RequestParam("newName") String newName,
			@PathVariable long folderId) {

		getFolderModificationService().renameFolder(folderId, newName);
		final String reNewName = newName;
		return new Object(){ public String newName = reNewName ; };

	}

	@RequestMapping(method = RequestMethod.POST, params = { "id", "value" })
	public @ResponseBody
	String updateDescription(@PathVariable long folderId, @RequestParam("value") String newDescription) {
		getFolderModificationService().updateFolderDescription(folderId, newDescription);
		return newDescription;
	}

	/***
	 * This method clean the path info from useless characters and returns only the raw part, like
	 * requirement-folders...
	 *
	 * @param pathInfo
	 *            the original pathInfo from request
	 * @return the cleaned path (String)
	 */
	private String getUpdateUrl(String pathInfo) {
		// remove first slash
		String toReturn = pathInfo.substring(1);
		// detect the last one...
		int lastSlash = toReturn.lastIndexOf('/');
		return toReturn.substring(0, lastSlash);
	}

}
