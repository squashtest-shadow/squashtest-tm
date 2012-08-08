package squashtm.testautomation.jenkins

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;

import spock.lang.Specification

class TestAutomationJenkinsConnectorTest extends Specification {
	
	private TestAutomationJenkinsConnector connector
	
	def setup(){
		connector = new TestAutomationJenkinsConnector()
	}
	
	def "should return a well formatted query"(){
		
		given :
			TestAutomationServer server = new TestAutomationServer(new URL("http://ci.jruby.org"), "", "")
			
		when :
			def method = connector.newGetJobsMethod(server)
			
		then :
			method.path == "http://ci.jruby.org/api/json"
			method.queryString == "tree=jobs%5Bname%5D"
		
		
	}
	
}
