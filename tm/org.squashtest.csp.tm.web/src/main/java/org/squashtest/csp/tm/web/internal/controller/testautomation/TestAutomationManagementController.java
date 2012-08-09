package org.squashtest.csp.tm.web.internal.controller.testautomation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.service.TestAutomationManagementService;


@Controller
@RequestMapping("/test-automation")
public class TestAutomationManagementController {

	
	private TestAutomationManagementService testAutomationManagementService;

	
	@ServiceReference
	public void setTestAutomationManagementService(
			TestAutomationManagementService testAutomationManagementService) {
		this.testAutomationManagementService = testAutomationManagementService;
	}
	

	@RequestMapping(value = "/servers/projects-list", method = RequestMethod.GET, headers = "Accept=application/json", params = {"url", "login", "password"} )
	@ResponseBody
	public Collection<TestAutomationProject> listProjectsOnServer(@RequestParam("url") String strURL, 
																  @RequestParam("login") String login, 
																  @RequestParam("password") String password) throws MalformedURLException{
		
		return testAutomationManagementService.listProjectsOnServer(new URL(strURL), login, password);
		
	}
	
	
}
