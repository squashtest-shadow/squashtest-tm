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
package org.squashtest.tm.web.internal.controller.campaign;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignExportCSVModel;
import org.squashtest.tm.domain.campaign.CampaignExportCSVModel.Row;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.service.campaign.CampaignFinder;
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.library.LibraryNavigationService;
import org.squashtest.tm.web.internal.controller.campaign.CampaignFormModel.CampaignFormModelValidator;
import org.squashtest.tm.web.internal.controller.campaign.IterationFormModel.IterationFormModelValidator;
import org.squashtest.tm.web.internal.controller.generic.LibraryNavigationController;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseFormModel.TestCaseFormModelValidator;
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


	@Inject
	@Named("campaign.driveNodeBuilder")
	private Provider<DriveNodeBuilder<CampaignLibraryNode>> driveNodeBuilder;

	@Inject
	private Provider<IterationNodeBuilder> iterationNodeBuilder;
	@Inject
	private Provider<CampaignLibraryTreeNodeBuilder> campaignLibraryTreeNodeBuilder;
	@Inject
	private Provider<TestSuiteNodeBuilder> suiteNodeBuilder;
	@Inject
	private CampaignLibraryNavigationService campaignLibraryNavigationService;
	@Inject
	private CampaignFinder campaignFinder;
	@Inject
	private IterationModificationService iterationModificationService;



	@RequestMapping(value = "/drives/{libraryId}/content/new-campaign", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewCampaignToLibraryRootContent(@PathVariable Long libraryId,
			@RequestBody CampaignFormModel campaignForm) throws BindException{

		BindingResult validation = new BeanPropertyBindingResult(campaignForm, "add-campaign");
		CampaignFormModelValidator validator = new CampaignFormModelValidator(getMessageSource());
		validator.validate(campaignForm, validation);

		if (validation.hasErrors()){
			throw new BindException(validation);
		}


		Campaign newCampaign = campaignForm.getCampaign();
		Map<Long, RawValue> customFieldValues = campaignForm.getCufs();

		campaignLibraryNavigationService.addCampaignToCampaignLibrary(libraryId, newCampaign, customFieldValues);

		return createTreeNodeFromLibraryNode(newCampaign);

	}

	@RequestMapping(value = "/folders/{folderId}/content/new-campaign", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewCampaignToFolderContent(@PathVariable long folderId,
			@RequestBody CampaignFormModel campaignForm)  throws BindException {

		BindingResult validation = new BeanPropertyBindingResult(campaignForm, "add-campaign");
		CampaignFormModelValidator validator = new CampaignFormModelValidator(getMessageSource());
		validator.validate(campaignForm, validation);

		if (validation.hasErrors()){
			throw new BindException(validation);
		}


		Campaign newCampaign = campaignForm.getCampaign();
		Map<Long, RawValue> customFieldValues = campaignForm.getCufs();

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


	@RequestMapping(value = "/campaigns/{campaignId}/content/new-iteration", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewIterationToCampaign(@PathVariable long campaignId,
			@RequestBody IterationFormModel iterationForm) throws BindException {

		BindingResult validation = new BeanPropertyBindingResult(iterationForm, "add-iteration");
		IterationFormModelValidator validator = new IterationFormModelValidator(getMessageSource());
		validator.validate(iterationForm, validation);

		if (validation.hasErrors()){
			throw new BindException(validation);
		}



		Iteration newIteration = iterationForm.getIteration();
		Map<Long, RawValue> customFieldValues = iterationForm.getCufs();
		boolean copyTestPlan = iterationForm.isCopyTestPlan();

		int newIterationIndex = campaignLibraryNavigationService.addIterationToCampaign(newIteration, campaignId,
				copyTestPlan, customFieldValues);

		return createIterationTreeNode(newIteration, newIterationIndex);
	}

	private JsTreeNode createIterationTreeNode(Iteration iteration, int iterationIndex) {
		return iterationNodeBuilder.get().setModel(iteration).setIndex(iterationIndex).build();
	}

	private JsTreeNode createTestSuiteTreeNode(TestSuite testSuite) {
		return suiteNodeBuilder.get().setModel(testSuite).build();
	}

	@RequestMapping(value = "/campaigns/{campaignId}/content", method = RequestMethod.GET)
	public @ResponseBody
	List<JsTreeNode> getCampaignIterationsTreeModel(@PathVariable long campaignId) {
		List<Iteration> iterations = campaignLibraryNavigationService.findIterationsByCampaignId(campaignId);
		return createCampaignIterationsModel(iterations);
	}

	@RequestMapping(value = "/iterations/{resourceId}/content", method = RequestMethod.GET)
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

	@RequestMapping(value = "/iterations/{iterationIds}/deletion-simulation", method = RequestMethod.GET)
	public @ResponseBody
	Messages simulateIterationDeletion(@PathVariable("iterationIds") List<Long> iterationIds, Locale locale) {

		List<SuppressionPreviewReport> reportList = campaignLibraryNavigationService
				.simulateIterationDeletion(iterationIds);

		Messages messages = new Messages();
		for (SuppressionPreviewReport report : reportList) {
			messages.addMessage(report.toString(getMessageSource(), locale));
		}

		return messages;
	}

	@RequestMapping(value = "/iterations/{iterationIds}", method = RequestMethod.DELETE)
	public @ResponseBody
	OperationReport confirmIterationsDeletion(@PathVariable("iterationIds") List<Long> iterationIds) {

		return campaignLibraryNavigationService.deleteIterations(iterationIds);
	}

	@RequestMapping(value = "/test-suites/{suiteIds}/deletion-simulation", method = RequestMethod.GET)
	public @ResponseBody
	Messages simulateSuiteDeletion(@PathVariable("suiteIds") List<Long> suiteIds, Locale locale) {
		List<SuppressionPreviewReport> reportList = campaignLibraryNavigationService.simulateSuiteDeletion(suiteIds);

		Messages messages = new Messages();
		for (SuppressionPreviewReport report : reportList) {
			messages.addMessage(report.toString(getMessageSource(), locale));
		}

		return messages;

	}

	@RequestMapping(value = "/test-suites/{suiteIds}", method = RequestMethod.DELETE)
	public @ResponseBody
	OperationReport confirmSuitesDeletion(@PathVariable("suiteIds") List<Long> suiteIds) {

		return campaignLibraryNavigationService.deleteSuites(suiteIds);
	}

	@RequestMapping(value = "/campaigns/{campaignId}/iterations/new", method = RequestMethod.POST, params = { "nodeIds[]", "next-iteration-index" })
	public @ResponseBody
	List<JsTreeNode> copyIterations(@RequestParam("nodeIds[]") Long[] nodeIds,
			@PathVariable("campaignId") long campaignId, @RequestParam("next-iteration-index") int nextIterationIndex) {

		List<Iteration> iterationsList;
		iterationsList = campaignLibraryNavigationService.copyIterationsToCampaign(campaignId, nodeIds);
		return createCopiedIterationsModel(iterationsList, nextIterationIndex);
	}

	@RequestMapping(value = "/iterations/{iterationId}/test-suites/new", method = RequestMethod.POST, params = { "nodeIds[]" })
	public @ResponseBody
	List<JsTreeNode> copyTestSuites(@RequestParam("nodeIds[]") Long[] nodeIds,
			@PathVariable("iterationId") long iterationId) {

		List<TestSuite> testSuiteList;
		testSuiteList = iterationModificationService.copyPasteTestSuitesToIteration(nodeIds, iterationId);
		return createCopiedTestSuitesModel(testSuiteList);

	}


	@RequestMapping(value="/export-campaign/{campaignId}", method = RequestMethod.GET, params = "export=csv")

	public @ResponseBody
	FileSystemResource exportCampaign(@PathVariable("campaignId") long campaignId, @RequestParam(value = "exportType",defaultValue="S") String exportType, HttpServletResponse response) {

		Campaign campaign = campaignFinder.findById(campaignId);
		CampaignExportCSVModel model = campaignLibraryNavigationService.exportCampaignToCSV(campaignId, exportType);

		// prepare the response
		response.setContentType("application/octet-stream");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

		response.setHeader("Content-Disposition", "attachment; filename=" + "EXPORT_CPG_"+exportType+"_"+campaign.getName().replace(" ", "_")
				+"_"+sdf.format(new Date()) + ".csv");


		File exported = exportToFile(model);
		return new FileSystemResource(exported);
	}



	private File exportToFile(CampaignExportCSVModel model){

		File file;
		PrintWriter writer = null;
		try {
			file = File.createTempFile("export-requirement", "tmp");
			file.deleteOnExit();

			writer = new PrintWriter(file);

			// print header
			Row header = model.getHeader();
			writer.write(header.toString() + "\n");

			// print the rest
			Iterator<Row> iterator = model.dataIterator();
			while (iterator.hasNext()){
				Row datarow = iterator.next();
				String cleanRowValue = HTMLCleanupUtils.htmlToText(datarow.toString()).replaceAll("\\n", "").replaceAll("\\r", "");
				writer.write(cleanRowValue + "\n");
			}

			writer.close();

			return file;
		} catch (IOException e) {
			LOGGER.error("campaign export : I/O failure while creating the temporary file : "+e.getMessage());
			throw new RuntimeException(e);
		}
		finally{
			if (writer != null) {
				writer.close();
			}
		}


	}

}
