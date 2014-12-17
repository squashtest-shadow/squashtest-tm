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
package org.squashtest.tm.web.internal.controller.infolist;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.SystemInfoListCode;
import org.squashtest.tm.domain.infolist.UserListItem;
import org.squashtest.tm.service.infolist.InfoListItemManagerService;
import org.squashtest.tm.service.infolist.InfoListManagerService;
import org.squashtest.tm.web.internal.helper.JEditablePostParams;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.util.InfoListItemList;

@Controller
@RequestMapping("/info-lists")
public class InfoListController {

	private static final Logger LOGGER = LoggerFactory.getLogger(InfoListController.class);

	@Inject
	private InfoListManagerService listManager;

	@Inject
	private InfoListItemManagerService listItemManager;

	@RequestMapping(value = "/{infoListId}", method = RequestMethod.GET)
	public String showInfoListModificationPage(@PathVariable Long infoListId, Model model) {
		InfoList list = listManager.findById(infoListId);
		SystemInfoListCode.verifyModificationPermission(list);
		model.addAttribute("infoList", list);
		model.addAttribute("itemListIcons", InfoListItemList.getInfoListItems());
		LOGGER.debug("id " + list.getId());
		LOGGER.debug("label " + list.getLabel());
		LOGGER.debug("code " + list.getCode());
		LOGGER.debug("description " + list.getDescription());
		return "info-list-modification.html";
	}

	@RequestMapping(value = "/{infoListId}", method = RequestMethod.POST, params = { "id=info-list-label",
			JEditablePostParams.VALUE })
	@ResponseBody
	public String changeLabel(@PathVariable Long infoListId, @RequestParam(JEditablePostParams.VALUE) String label) {
		listManager.changeLabel(infoListId, label);
		return HtmlUtils.htmlEscape(label);
	}

	@RequestMapping(value = "/{infoListId}", method = RequestMethod.POST, params = { "id=info-list-code",
			JEditablePostParams.VALUE })
	@ResponseBody
	public String changeCode(@PathVariable Long infoListId, @RequestParam(JEditablePostParams.VALUE) String code) {
		listManager.changeCode(infoListId, code);
		return HtmlUtils.htmlEscape(code);
	}

	@RequestMapping(value = "/{infoListId}", method = RequestMethod.POST, params = { "id=info-list-description",
			JEditablePostParams.VALUE })
	@ResponseBody
	public String changeDescription(@PathVariable Long infoListId,
			@RequestParam(JEditablePostParams.VALUE) String description) {
		listManager.changeDescription(infoListId, description);
		return description;
	}

	@RequestMapping(value = "/{infoListId}/items", method = RequestMethod.GET)
	@ResponseBody
	public DataTableModel getInfoListItems(@PathVariable Long infoListId) {
		InfoList list = listManager.findById(infoListId);

		return buildInfoListItemTableModel(list.getItems());
	}

	@RequestMapping(value = "/{infoListId}/items/positions", method = RequestMethod.POST, params = { "itemIds[]",
	"newIndex" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void changeOptionsPositions(@PathVariable long infoListId, @RequestParam int newIndex,
			@RequestParam("itemIds[]") List<Long> itemsIds) {
		listManager.changeItemsPositions(infoListId, newIndex, itemsIds);
	}

	@RequestMapping(value = "/{infoListId}/items", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody void addInfoListItem(@PathVariable long infoListId,
			@Valid @ModelAttribute("item") UserListItem item) {

		listItemManager.addInfoListItem(infoListId, item);
	}

	@RequestMapping(value = "/{infoListId}/isUsed", method = RequestMethod.GET)
	@ResponseBody
	public boolean isUsed(@PathVariable long infoListId) {
		return listManager.isUsedByOneOrMoreProject(infoListId);
	}

	@RequestMapping(value = "/{infoListId}", method = RequestMethod.DELETE)
	@ResponseBody
	public void delete(@PathVariable long infoListId) {
		listManager.deleteInfoList(infoListId);
	}

	@RequestMapping(value = "/{infoListId}/defaultItem", method = RequestMethod.GET)
	@ResponseBody
	public long getDefaultItemId(@PathVariable long infoListId) {
		InfoList infoList = listManager.findById(infoListId);
		return 	infoList.getDefaultItem().getId();
	}

	@RequestMapping(value = "/{infoListId}/{infoListItemId}", method = RequestMethod.DELETE)
	@ResponseBody
	public void delete(@PathVariable("infoListId") long infoListId, @PathVariable("infoListItemId") long infoListItemId) {
		listItemManager.removeInfoListItem(infoListItemId, infoListId);
	}


	private DataTableModel buildInfoListItemTableModel(Collection<InfoListItem> data) {
		InfoListItemDataTableModelHelper helper = new InfoListItemDataTableModelHelper();
		Collection<Object> aaData = helper.buildRawModel(data);
		DataTableModel model = new DataTableModel("");
		model.setAaData((List<Object>) aaData);
		return model;
	}

	private static final class InfoListItemDataTableModelHelper extends DataTableModelBuilder<InfoListItem> {

		private InfoListItemDataTableModelHelper() {
		}

		@Override
		public Object buildItemData(InfoListItem item) {
			Map<String, Object> data = new HashMap<String, Object>(7);
			data.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
			data.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex() + 1);
			data.put("default", item.isDefault());
			data.put("label", item.getLabel());
			data.put("code", item.getCode());
			data.put("iconName", item.getIconName());
			data.put(DataTableModelConstants.DEFAULT_EMPTY_ICON_HOLDER_KEY, "");
			data.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
			return data;
		}
	}

}
