package squashtm.testautomation.jenkins.internal

import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;

import spock.lang.Specification
import squashtm.testautomation.jenkins.internal.beans.JobList;

class JsonParserTest extends Specification {

	 JsonParser parser;
	 
	 def setup(){
		 parser = new JsonParser();
	 } 
	
	 
	 def "should return a collection of projects"(){
		 
		 given :
		 	def json ='{"jobs":[{"name":"bob"},{"name":"mike"},{"name":"robert"}]}'
		 
		 when : 
		 	def res = parser.readJobListFromJson(json)
		 
		 then :
		 	res.collect{it.name} == ["bob", "mike", "robert"]
	 }


}
