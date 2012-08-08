package squashtm.testautomation.jenkins.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;

import squashtm.testautomation.jenkins.internal.beans.Job;
import squashtm.testautomation.jenkins.internal.beans.JobList;
import squashtm.testautomation.spi.exceptions.UnreadableResponseException;



@Component
public class JsonParser {

	private ObjectMapper objMapper = new ObjectMapper();
	
	
	
	public Collection<TestAutomationProject> readJobListFromJson(String json){
		
		try {
			JobList list = objMapper.readValue(json, JobList.class);
			
			return toProjectList(list);
		} 
		catch (JsonParseException e) {
			throw new UnreadableResponseException(e);
		} 
		catch (JsonMappingException e) {
			throw new UnreadableResponseException(e);
		} 
		catch (IOException e) {
			throw new UnreadableResponseException(e);
		} 
		
	}
	
	
	
	protected Collection<TestAutomationProject> toProjectList(JobList jobList){
		
		Collection<TestAutomationProject> projects = new ArrayList<TestAutomationProject>();
		
		for (Job job : jobList.getJobs()){
			projects.add(new TestAutomationProject(job.getName(), null));
		}
		
		return projects;
		
		
	}

	
}
