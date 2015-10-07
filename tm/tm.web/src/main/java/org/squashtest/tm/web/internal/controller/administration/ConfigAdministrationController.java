/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.administration;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.event.ConfigUpdateEvent;
import org.squashtest.tm.exception.client.ClientNameAlreadyExistsException;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.security.OAuth2ClientService;
import org.squashtest.tm.service.user.UserManagerService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.*;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

@Controller
@RequestMapping("administration/config")
public class ConfigAdministrationController implements ApplicationContextAware { //, BundleContextAware {

    private static final String WHITE_LIST = "uploadfilter.fileExtensions.whitelist";
    private static final String UPLOAD_SIZE_LIMIT = "uploadfilter.upload.sizeLimitInBytes";
    private static final String IMPORT_SIZE_LIMIT = "uploadfilter.upload.import.sizeLimitInBytes";
    @Inject
    private ConfigurationService configService;

    @Inject
    private OAuth2ClientService clientService;

    @Inject
    private InternationalizationHelper messageSource;

    @Inject
    private FeatureManager features;

    @Inject
    private UserManagerService userManager;

    /**
     * application context needed to create osgi event
     */
    private ApplicationContext applicationCtx;

    /**
     * publisher of the osgi event.
     */
//	@Inject
//	private OsgiBundleApplicationContextEventMulticaster publisher;
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public void changeConfig(@RequestParam(WHITE_LIST) String whiteList,
                             @RequestParam(UPLOAD_SIZE_LIMIT) String uploadSizeLimit,
                             @RequestParam(IMPORT_SIZE_LIMIT) String importSizeLimit) {

        configService.updateConfiguration(WHITE_LIST, whiteList);
        configService.updateConfiguration(UPLOAD_SIZE_LIMIT, uploadSizeLimit);
        configService.updateConfiguration(IMPORT_SIZE_LIMIT, importSizeLimit);
        sendUpdateEvent();
    }

//	@Override
//	public void setBundleContext(BundleContext bundleContext) {
//		bundleCtx = bundleContext;
//
//	}

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationCtx = applicationContext;

    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView administration() {
        ModelAndView mav = new ModelAndView("page/administration/config");

        mav.addObject("whiteList", configService.findConfiguration(WHITE_LIST));
        mav.addObject("uploadSizeLimit", configService.findConfiguration(UPLOAD_SIZE_LIMIT));
        mav.addObject("uploadImportSizeLimit", configService.findConfiguration(IMPORT_SIZE_LIMIT));

        mav.addObject("caseInsensitiveLogin", features.isEnabled(Feature.CASE_INSENSITIVE_LOGIN));
        mav.addObject("duplicateLogins", userManager.findAllDuplicateLogins());

        return mav;
    }

    @RequestMapping(method = RequestMethod.POST, params = {"id=whiteList", VALUE})
    @ResponseBody
    public String changeWhiteList(@RequestParam(VALUE) String newWhiteList) {
        configService.updateConfiguration(WHITE_LIST, newWhiteList);
        sendUpdateEvent();
        return newWhiteList;
    }

    @RequestMapping(method = RequestMethod.POST, params = {"id=uploadSizeLimit", VALUE})
    @ResponseBody
    public String changeUploadSizeLimit(@RequestParam(VALUE) String newUploadSizeLimit) {
        configService.updateConfiguration(UPLOAD_SIZE_LIMIT, newUploadSizeLimit);
        sendUpdateEvent();
        return newUploadSizeLimit;
    }

    @RequestMapping(method = RequestMethod.POST, params = {"id=uploadImportSizeLimit", VALUE})
    @ResponseBody
    public String changeUploadImportSizeLimit(@RequestParam(VALUE) String newUploadImportSizeLimit) {
        configService.updateConfiguration(IMPORT_SIZE_LIMIT, newUploadImportSizeLimit);
        sendUpdateEvent();
        return newUploadImportSizeLimit;
    }


    private void sendUpdateEvent() {
        ConfigUpdateEvent event = new ConfigUpdateEvent(applicationCtx);
//		publisher.multicastEvent(event);
    }

    @RequestMapping(value = "clients/{idList}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    void removeMilestones(@PathVariable("idList") List<String> idList) {
        clientService.removeClientDetails(idList);
    }

    @RequestMapping(value = "clients", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public
    @ResponseBody
    ClientDetails addClient(@Valid @ModelAttribute("add-client") ClientModel model) {
        BaseClientDetails clientDetails = convertClientModelToBaseClientDetails(model);
        try {
            clientService.addClientDetails(clientDetails);
        } catch (ClientAlreadyExistsException ex) {
            throw new ClientNameAlreadyExistsException(ex);
        }
        return clientDetails;
    }

    private BaseClientDetails convertClientModelToBaseClientDetails(ClientModel model) {
        BaseClientDetails clientDetails = new BaseClientDetails();
        clientDetails.setClientId(model.getClientId());
        clientDetails.setClientSecret(model.getClientSecret());
        Set<String> uris = new HashSet<String>();
        uris.add(model.getRegisteredRedirectUri());
        clientDetails.setRegisteredRedirectUri(uris);
        return clientDetails;
    }

    @RequestMapping(value = "clients/list")
    @ResponseBody
    public DataTableModel getClientsTableModel(final DataTableDrawParameters params, final Locale locale) {

        ClientDataTableModelHelper helper = new ClientDataTableModelHelper(messageSource);
        helper.setLocale(locale);
        Collection<Object> aaData = helper.buildRawModel(clientService.findClientDetailsList());
        DataTableModel model = new DataTableModel("");
        model.setAaData((List<Object>) aaData);
        return model;
    }
}
