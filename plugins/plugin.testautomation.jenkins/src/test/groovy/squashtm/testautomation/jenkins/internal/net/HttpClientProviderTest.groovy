package squashtm.testautomation.jenkins.internal.net

import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;

import spock.lang.Specification
import squashtm.testautomation.jenkins.internal.net.HttpClientProvider.ServerKey;

class HttpClientProviderTest extends Specification {

	// ********************* tests for ServerKey : is it fit for being a key in a map ?*************** 
	
	
	def "keys generated from different instances of the 'same' TestAutomationServer should be equal"(){
		
		given :
			TestAutomationServer server1 = new TestAutomationServer(new URL("http://www.toto.com"), "toto", "toto")
			TestAutomationServer server2 = new TestAutomationServer(new URL("http://www.toto.com"), "toto", "toto")
		
			
		when :
			def key1 = new ServerKey(server1)
			def key2 = new ServerKey(server2)
			
		then :
			key1.equals(key2)
		
	}	
	
	def "keys generated from two different TestAutomationServer should not be equal"(){
		given :
			TestAutomationServer server1 = new TestAutomationServer(new URL("http://www.toto.com"), "toto", "toto")
			TestAutomationServer server2 = new TestAutomationServer(new URL("http://www.titi.com"), "titi", "titi")
		
			
		when :
			def key1 = new ServerKey(server1)
			def key2 = new ServerKey(server2)
			
		then :
			! key1.equals(key2)
	}
}
