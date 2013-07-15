/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.campaign;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignExportCSVModel;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.campaign.CampaignExportCSVModel.Row;
import org.squashtest.tm.service.campaign.CampaignFinder;
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.library.LibraryNavigationService;
import org.squashtest.tm.web.internal.controller.campaign.CampaignFormModel.CampaignFormModelValidator;
import org.squashtest.tm.web.internal.controller.campaign.IterationFormModel.IterationFormModelValidator;
import org.squashtest.tm.web.internal.controller.generic.LibraryNavigationController;
import org.squashtest.tm.web.internal.model.builder.CampaignLibraryTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.IterationNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.builder.TestSuiteNodeBuilder;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

/**
 * Controller which processes requests related to navigation in a {@link CampaignLibrary}.
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping(value = "/campaign-browser")
public class CampaignLibraryNavigationController extends
		LibraryNavigationController<CampaignLibrary, CampaignFolder, CampaignLibraryNode> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CampaignLibraryNavigationController.class);
	private static final String NODE_IDS = "nodeIds[]";

	@Inject
	private Provider<DriveNodeBuilder> driveNodeBuilder;
	@Inject
	private Provider<IterationNodeBuilder> iterationNodeBuilder;
	@Inject
	private Provider<CampaignLibraryTreeNodeBuilder> campaignLibraryTreeNodeBuilder;
	@Inject
	private Provider<TestSuiteNodeBuilder> suiteNodeBuilder;

	private CampaignLibraryNavigationService campaignLibraryNavigationService;

	@Inject
	private CampaignFinder campaignFinder;



	@ServiceReference
	public void setCampaignLibraryNavigationService(CampaignLibraryNavigationService campaignLibraryNavigationService) {
		this.campaignLibraryNavigationService = campaignLibraryNavigationService;
	}

	private IterationModificationService iterationModificationService;

	@ServiceReference
	public void setIterationModificationService(IterationModificationService iterationModificationService) {
		this.iterationModificationService = iterationModificationService;
	}

	@InitBinder("add-campaign")
	public void addCampaignBinder(WebDataBinder binder) {
		CampaignFormModelValidator validator = new CampaignFormModelValidator();
		validator.setMessageSource(getMessageSource());
		binder.setValidator(validator);
	}

	@InitBinder("add-iteration")
	public void addIterationBinder(WebDataBinder binder) {
		IterationFormModelValidator validator = new IterationFormModelValidator();
		validator.setMessageSource(getMessageSource());
		binder.setValidator(validator);
	}

	@RequestMapping(value = "/drives/{libraryId}/content/new-campaign", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewCampaignToLibraryRootContent(@PathVariable Long libraryId,
			@Valid @ModelAttribute("add-campaign") CampaignFormModel campaignForm) {

		Campaign newCampaign = campaignForm.getCampaign();
		Map<Long, String> customFieldValues = campaignForm.getCustomFields();

		campaignLibraryNavigationService.addCampaignToCampaignLibrary(libraryId, newCampaign, customFieldValues);

		return createTreeNodeFromLibraryNode(newCampaign);

	}

	@RequestMapping(value = "/folders/{folderId}/content/new-campaign", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewCampaignToFolderContent(@PathVariable long folderId,
			@Valid @ModelAttribute("add-campaign") CampaignFormModel campaignForm) {

		Campaign newCampaign = campaignForm.getCampaign();
		Map<Long, String> customFieldValues = campaignForm.getCustomFields();

		campaignLibraryNavigationService.addCampaignToCampaignFolder(folderId, newCampaign, customFieldValues);

		return createTreeNodeFromLibraryNode(newCampaign);

	}

	@Override
	protected LibraryNavigationService<CampaignLibrary, CampaignFolder, CampaignLibraryNode> getLibraryNavigationService() {
		return campaignLibraryNavigationService;
	}

	@Override
	protected JsTreeNode createTreeNodeFromLibraryNode(CampaignLibraryNode model) {
		return campaignLibraryTreeNodeBuilder.get().setNode(model).build();
	}

	@Override
	protected String getShowLibraryViewName() {
		return "page/campaign-libraries/show-campaign-library";
	}

	@RequestMapping(value = "/files/{campaignId}/content/new-iteration", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewIterationToCampaign(@PathVariable long campaignId,
			@Valid @ModelAttribute("add-iteration") IterationFormModel iterationForm) {

		Iteration newIteration = iterationForm.getIteration();
		Map<Long, String> customFieldValues = iterationForm.getCustomFields();
		boolean copyTestPlan = iterationForm.isCopyTestPlan();

		int newIterationIndex = campaignLibraryNavigationService.addIterationToCampaign(newIteration, campaignId,
				copyTestPlan, customFieldValues);

		return createIterationTreeNode(newIteration, newIterationIndex);
	}

	private JsTreeNode createIterationTreeNode(Iteration iteration, int iterationIndex) {
		return iterationNodeBuilder.get().setModel(iteration).setIterationIndex(iterationIndex).build();
	}

	private JsTreeNode createTestSuiteTreeNode(TestSuite testSuite) {
		return suiteNodeBuilder.get().setModel(testSuite).build();
	}

	@RequestMapping(value = "/files/{campaignId}/content", method = RequestMethod.GET)
	public @ResponseBody
	List<JsTreeNode> getCampaignIterationsTreeModel(@PathVariable long campaignId) {
		List<Iteration> iterations = campaignLibraryNavigationService.findIterationsByCampaignId(campaignId);
		return createCampaignIterationsModel(iterations);
	}

	@RequestMapping(value = "/resources/{resourceId}/content", method = RequestMethod.GET)
	public @ResponseBody
	List<JsTreeNode> getIterationTestSuitesTreeModel(@PathVariable("resourceId") long iterationId) {

		List<TestSuite> testSuites = campaignLibraryNavigationService.findIterationContent(iterationId);

		return createIterationTestSuitesModel(testSuites);

	}

	private @ResponseBody
	List<JsTreeNode> createCampaignIterationsModel(List<Iteration> iterations) {
		List<JsTreeNode> res = new ArrayList<JsTreeNode>();

		for (int i = 0; i < iterations.size(); i++) {
			Iteration iteration = iterations.get(i);
			res.add(createIterationTreeNode(iteration, i));
		}

		return res;
	}

	private List<JsTreeNode> createIterationTestSuitesModel(List<TestSuite> suites) {
		TestSuiteNodeBuilder nodeBuilder = suiteNodeBuilder.get();
		JsTreeNodeListBuilder<TestSuite> listBuilder = new JsTreeNodeListBuilder<TestSuite>(nodeBuilder);

		return listBuilder.setModel(suites).build();

	}

	private @ResponseBody
	List<JsTreeNode> createCopiedIterationsModel(List<Iteration> newIterations, int nextIterationNumber) {
		int iterationIndex = nextIterationNumber;
		List<JsTreeNode> res = new ArrayList<JsTreeNode>();

		for (Iteration iteration : newIterations) {
			res.add(createIterationTreeNode(iteration, iterationIndex));
			iterationIndex++;
		}

		return res;
	}

	private @ResponseBody
	List<JsTreeNode> createCopiedTestSuitesModel(List<TestSuite> newTestSuites) {

		List<JsTreeNode> res = new ArrayList<JsTreeNode>();

		for (TestSuite testSuite : newTestSuites) {
			res.add(createTestSuiteTreeNode(testSuite));
		}

		return res;
	}

	@RequestMapping(value = "/drives", method = RequestMethod.GET, params = { "linkables" })
	public @ResponseBody
	List<JsTreeNode> getLinkablesRootModel() {
		List<CampaignLibrary> linkableLibraries = campaignLibraryNavigationService.findLinkableCampaignLibraries();
		return createLinkableLibrariesModel(linkableLibraries);
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<CampaignLibrary> linkableLibraries) {
		JsTreeNodeListBuilder<CampaignLibrary> listBuilder = new JsTreeNodeListBuilder<CampaignLibrary>(
				driveNodeBuilder.get());

		return listBuilder.setModel(linkableLibraries).build();
	}

	@RequestMapping(value = "/delete-iterations/simulate", method = RequestMethod.POST, params = { NODE_IDS })
	public @ResponseBody
	Message simulateIterationDeletion(@RequestParam(NODE_IDS) List<Long> nodeIds, Locale locale) {

		List<SuppressionPreviewReport> reportList = campaignLibraryNavigationService.simulateIterationDeletion(nodeIds);

		StringBuilder builder = new StringBuilder();

		for (SuppressionPreviewReport report : reportList) {
			builder.append(report.toString(getMessageSource(), locale));
			builder.append("<br/>");
		}

		return new Message(builder.toString());
	}

	public static class Message {
		private String message;

		public Message(String message) {
			this.message = message;
		}

		public String getMessage() {
			return this.message;
		}
	}

	@RequestMapping(value = "/delete-iterations/confirm", method = RequestMethod.DELETE, params = { NODE_IDS })
	public @ResponseBody
	List<Long> confirmIterationsDeletion(@RequestParam(NODE_IDS) List<Long> nodeIds) {

		return campaignLibraryNavigationService.deleteIterations(nodeIds);
	}

	@RequestMapping(value = "/delete-test-suites/simulate", method = RequestMethod.POST, params = { NODE_IDS })
	public @ResponseBody
	String simulateSuiteDeletion(@RequestParam(NODE_IDS) List<Long> nodeIds, Locale locale) {
		List<SuppressionPreviewReport> reportList = campaignLibraryNavigationService.simulateSuiteDeletion(nodeIds);

		StringBuilder builder = new StringBuilder();

		for (SuppressionPreviewReport report : reportList) {
			builder.append(report.toString(getMessageSource(), locale));
			builder.append("<br/>");
		}

		return builder.toString();

	}

	@RequestMapping(value = "/delete-test-suites/confirm", method = RequestMethod.DELETE, params = { NODE_IDS })
	public @ResponseBody
	List<Long> confirmSuitesDeletion(@RequestParam(NODE_IDS) List<Long> nodeIds) {

		return campaignLibraryNavigationService.deleteSuites(nodeIds);
	}

	@RequestMapping(value = "/copy-iterations", method = RequestMethod.POST)
	public @ResponseBody
	List<JsTreeNode> copyIterations(@RequestParam("object-ids[]") Long[] iterationsIds,
			@RequestParam("destination-id") long campaignId, @RequestParam("destination-type") String destType,
			@RequestParam("next-iteration-number") int nextIterationNumber) {

		return copyNodes(iterationsIds, campaignId, destType, nextIterationNumber);
	}

	@RequestMapping(value = "/copy-test-suites", method = RequestMethod.POST)
	public @ResponseBody
	List<JsTreeNode> copyTestSuites(@RequestParam("object-ids[]") Long[] testSuiteIds,
			@RequestParam("destination-id") long iterationId, @RequestParam("destination-type") String destType) {

		return copyNodes(testSuiteIds, iterationId, destType, 0);

	}

	@RequestMapping(value="/export-campaign", method = RequestMethod.GET, params = "export=csv")
	public @ResponseBody
	void exportCampaign(@PathVariable("campaignId") long campaignId, @RequestParam(value = "exportType") String exportType, HttpServletResponse response) {

		BufferedWriter writer = null;

		try {
			Campaign campaign = campaignFinder.findById(campaignId);
			CampaignExportCSVModel model = campaignLibraryNavigationService.exportCampaignToCSV(campaignId, exportType);

			// prepare the response
			writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));

			response.setContentType("application/octet-stream");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

			response.setHeader("Content-Disposition", "attachment; filename=" + "EXPORT_CPG_"+exportType+"_"+campaign.getName().replace(" ", "_")
					+"_"+sdf.format(new Date()) + ".csv");

			// print
			Row header = model.getHeader();
			writer.write(header.toString() + "\n");

			Iterator<Row> iterator = model.dataIterator();
			while (iterator.hasNext()) {
				Row datarow = iterator.next();
				String cleanRowValue = HTMLCleanupUtils.htmlToText(datarow.toString()).replaceAll("\\n", " ")
						.replaceAll("\\r", " ");
				writer.write(cleanRowValue + "\n");
			}

			// closes stream in the finally clause
		} catch (IOException ex) {
			LOGGER.error(ex.getMessage());
			throw new RuntimeException(ex);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ex) {
					LOGGER.warn(ex.getMessage());
				}
			}
		}

	}

	private List<JsTreeNode> copyNodes(Long[] itemIds, long destinationId, String destinationType,
			int nextNumberInDestination) {
		if (destinationType.equals("campaign")) {
			List<Iteration> iterationsList;
			iterationsList = campaignLibraryNavigationService.copyIterationsToCampaign(destinationId, itemIds);
			return createCopiedIterationsModel(iterationsList, nextNumberInDestination);
		} else if (destinationType.equals("iteration")) {
			List<TestSuite> testSuiteList;
			testSuiteList = iterationModificationService.copyPasteTestSuitesToIteration(itemIds, destinationId);
			return createCopiedTestSuitesModel(testSuiteList);
		} else {
			throw new IllegalArgumentException("copy nodes : cannot paste item to : " + destinationType);
		}
	}

}
