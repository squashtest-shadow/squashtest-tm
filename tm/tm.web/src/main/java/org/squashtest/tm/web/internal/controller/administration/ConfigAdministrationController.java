package org.squashtest.tm.web.internal.controller.administration;

import javax.inject.Inject;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.event.ConfigUpdateEvent;
import org.squashtest.tm.service.configuration.ConfigurationService;

@Controller
@RequestMapping("administration/config")
public class ConfigAdministrationController {

	private static final String WHITE_LIST = "uploadfilter.fileExtensions.whitelist";
	private static final String UPLOAD_SIZE_LIMIT = "uploadfilter.upload.sizeLimitInBytes";
	private static final String IMPORT_SIZE_LIMIT = "uploadfilter.upload.import.sizeLimitInBytes";
	@Inject
	private ConfigurationService configService;
	
	@Inject
    private ApplicationEventPublisher publisher;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public void changeConfig(@RequestParam(WHITE_LIST) String whiteList,
			@RequestParam(UPLOAD_SIZE_LIMIT) String uploadSizeLimit,
			@RequestParam(IMPORT_SIZE_LIMIT) String importSizeLimit){
		
		configService.updateConfiguration(WHITE_LIST, whiteList);
		configService.updateConfiguration(UPLOAD_SIZE_LIMIT, uploadSizeLimit);
		configService.updateConfiguration(IMPORT_SIZE_LIMIT, importSizeLimit);
		ConfigUpdateEvent event = new ConfigUpdateEvent(this);
		publisher.publishEvent(event);
	}

	
	
}
