package squashtm.testautomation.jenkins.internal

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
