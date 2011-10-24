<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ tag body-content="empty" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- the following javascript could have been a .js but we need it to be processed by the jsp processor first
	hence the .tag file
 --%>

<%-------------------------- status------------------------------%>

<f:message var="statusBloqued" key="execution.execution-status.BLOCKED" />
<f:message var="statusFailure" key="execution.execution-status.FAILURE" />
<f:message var="statusSuccess" key="execution.execution-status.SUCCESS" />
<f:message var="statusRunning" key="execution.execution-status.RUNNING" />
<f:message var="statusReady" key="execution.execution-status.READY" />

<%-------------------------- /status-----------------------------%>


<script type="text/javascript">

	function ExecutionStatusFactory(){
		this.getExecutionStatus = getExecutionStatus;
		this.getDisplayableStatus = getDisplayableStatus;
		
		
		
		
		function getExecutionStatus(status){
			var execStatus;
			execStatus=getLocalizedExecutionStatus(status);
			
			if (execStatus==null)
				execStatus=getNonLocalizeExecutionStatus(status);
			
			return execStatus;
		}
		
		function getDisplayableStatus(status){
			var execStatus;
			execStatus=getLocalizedExecutionStatus(status);
			
			if (execStatus==null)
				execStatus=getNonLocalizeExecutionStatus(status);
			
			return makeDisplayableStatus(execStatus);			
		}
		

		
		function getLocalizedExecutionStatus(status){
			var status;
			var imagePath="${ pageContext.servletContext.contextPath }/images/";
			
			switch(status){
				case "${statusBloqued}" : 
					status=new ExecutionStatus("${statusBloqued}", imagePath+"Icon_Yellow.png") ;
					break;
				
				case "${statusFailure}" :
					status=new ExecutionStatus("${statusFailure}", imagePath+"Icon_Red.png") ;
					break;			
					
				case "${statusSuccess}" :
					status=new ExecutionStatus("${statusSuccess}", imagePath+"Icon_Green.png") ;
					break;			
					
				case "${statusRunning}" :
					status=new ExecutionStatus("${statusRunning}", imagePath+"Icon_Blue.png") ;
					break;			
					
				case "${statusReady}" :
					status=new ExecutionStatus("${statusReady}", imagePath+"Icon_Grey.png") ;
					break;	
					
				default : 
					status=null;
					break;
				
			}
			
			return status;		
		}
		
		
		function getNonLocalizeExecutionStatus(status){
			var status;
			var imagePath="${ pageContext.servletContext.contextPath }/images/";
			
			switch(status){
				case "BLOCKED" : 
					status=new ExecutionStatus("${statusBloqued}", imagePath+"Icon_Yellow.png") ;
					break;
				
				case "FAILURE" :
					status=new ExecutionStatus("${statusFailure}", imagePath+"Icon_Red.png") ;
					break;			
					
				case "SUCCESS" :
					status=new ExecutionStatus("${statusSuccess}", imagePath+"Icon_Green.png") ;
					break;			
					
				case "RUNNING" :
					status=new ExecutionStatus("${statusRunning}", imagePath+"Icon_Blue.png") ;
					break;			
					
				case "READY" :
					status=new ExecutionStatus("${statusReady}", imagePath+"Icon_Grey.png") ;
					break;		
				
			}
			
			return status;
			
		}
		
		

		function makeDisplayableStatus(execStatus){
			var content;
			var context = "${ pageContext.servletContext.contextPath }";
			var header="<div style=\"white-space:nowrap;\"><img src=\"";
			var label="\" /><span style=\"vertical-align:top;margin-left:10px;\">";
			var footer="</span> </div>";
 
			
			content=header+execStatus.icon+label+execStatus.text+footer;
		
			return content;		
		}
		
		
		
	}


	function ExecutionStatus(text, icon){
		this.text=text;
		this.icon=icon;
	}
	

</script>