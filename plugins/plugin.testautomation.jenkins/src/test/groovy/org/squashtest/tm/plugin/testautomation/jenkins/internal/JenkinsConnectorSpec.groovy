package org.squashtest.tm.plugin.testautomation.jenkins.internal

import org.apache.http.StatusLine
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.CloseableHttpClient
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GetBuildID

import spock.lang.Specification

abstract class JenkinsConnectorSpec extends Specification {
	GetBuildID getID = new GetBuildID()
	CloseableHttpClient client = Mock()
	HttpUriRequest method = Mock()
	BuildAbsoluteId absoluteId;
	JsonParser parser= new JsonParser()
	CloseableHttpResponse resp = Mock()
	
	def setup(){
		StatusLine status = Mock()
		status.getStatusCode() >> 200
		resp.getStatusLine() >> status

		getID.client = client
		getID.method = method
		getID.parser = parser;
		getID.absoluteId = new BuildAbsoluteId("CorrectJob", "CorrectExternalID")

		client.execute(method) >> resp
		
		RequestExecutor.INSTANCE = Mock(RequestExecutor)
	}

}
